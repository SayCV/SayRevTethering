/*
 * Copyright (C) 2013, sayDroid.
 *
 * Copyright 2013 The sayDroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.saydroid.tether.usb.Screens;

import org.saydroid.tether.usb.CustomDialog;
import org.saydroid.tether.usb.Engine;
import org.saydroid.tether.usb.R;
import org.saydroid.tether.usb.Services.IScreenService;
import org.saydroid.sgs.utils.SgsStringUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

import org.saydroid.logger.Log;

public abstract class BaseScreen extends Activity implements IBaseScreen {
	private static final String TAG = BaseScreen.class.getCanonicalName();
	public static enum SCREEN_TYPE {
		// Well-Known
		ABOUT_T,
        MANUAL_T,
		AV_QUEUE_T,
		CHAT_T,
		CHAT_QUEUE_T,
		CODECS_T,
		CONTACTS_T,
		DIALER_T,
		FILETRANSFER_QUEUE_T,
		FILETRANSFER_VIEW_T,
		HOME_T,
		IDENTITY_T,
		INTERCEPT_CALL_T,
		GENERAL_T,
		MESSAGING_T,
		NATT_T,
		NETWORK_T,
		PRESENCE_T,
		QOS_T,
		SETTINGS_T,
		SECURITY_T,
		SPLASH_T,
		
		TAB_CONTACTS, 
		TAB_HISTORY_T, 
		TAB_INFO_T, 
		TAB_ONLINE,
		TAB_MESSAGES_T,
		
		
		// All others
		AV_T
	}
	
	protected String mId;
	protected final SCREEN_TYPE mType;
	protected boolean mComputeConfiguration;
	protected ProgressDialog mProgressDialog;
	protected Handler mHandler;
	
	protected final IScreenService mScreenService;

	protected BaseScreen(SCREEN_TYPE type, String id) {
		super();
		mType = type;
		mId = id;
		mScreenService = ((Engine)Engine.getInstance()).getScreenService();
	}

	protected Engine getEngine(){
		return (Engine)Engine.getInstance();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);
        mHandler = new Handler();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!processKeyDown(keyCode, event)) {
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}

	@Override
	public String getId() {
		return mId;
	}

	@Override
	public SCREEN_TYPE getType() {
		return mType;
	}

	@Override
	public boolean hasMenu() {
		return false;
	}

	@Override
	public boolean hasBack() {
		return false;
	}

	@Override
	public boolean back() {
		return mScreenService.back();
	}

	@Override
	public boolean createOptionsMenu(Menu menu) {
		return false;
	}

    // setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT)
    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        //newConfig.orientation获得当前屏幕状态是横向或者竖向
        //Configuration.ORIENTATION_PORTRAIT 表示竖向
        //Configuration.ORIENTATION_LANDSCAPE 表示横屏
        if(newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
            String message = "Now Screen Changes to Portrait";
            Log.d(TAG, message);
            // Sending message
            Message msg = new Message();
            msg.obj = message;
            ((Engine)Engine.getInstance()).displayMessageHandler.sendMessage(msg);
            return ;
        }
        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE) {
            String message = "Now Screen Changes to Landscape";
            Log.d(TAG, message);
            // Sending message
            Message msg = new Message();
            msg.obj = message;
            ((Engine)Engine.getInstance()).displayMessageHandler.sendMessage(msg);
            return ;
        }
        super.onConfigurationChanged(newConfig);
    }

	protected void addConfigurationListener(RadioButton radioButton) {
		radioButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				mComputeConfiguration = true;
			}
		});
	}

	protected void addConfigurationListener(EditText editText) {
		editText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mComputeConfiguration = true;
			}
		});
	}

	protected void addConfigurationListener(CheckBox checkBox) {
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				mComputeConfiguration = true;
			}
		});
	}

	protected void addConfigurationListener(Spinner spinner) {
		// setOnItemClickListener not supported by Spinners
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				mComputeConfiguration = true;
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	protected int getSpinnerIndex(String value, String[] values) {
		for (int i = 0; i < values.length; i++) {
			if (SgsStringUtils.equals(value, values[i], true)) {
				return i;
			}
		}
		return 0;
	}
	
	protected int getSpinnerIndex(int value, int[] values) {
		for (int i = 0; i < values.length; i++) {
			if (value == values[i]) {
				return i;
			}
		}
		return 0;
	}

	protected void showInProgress(String text, boolean bIndeterminate,
			boolean bCancelable) {
		synchronized (this) {
			if (mProgressDialog == null) {
				mProgressDialog = new ProgressDialog(this);
				mProgressDialog.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						mProgressDialog = null;
					}
				});
				mProgressDialog.setMessage(text);
				mProgressDialog.setIndeterminate(bIndeterminate);
				mProgressDialog.setCancelable(bCancelable);
				mProgressDialog.show();
			}
		}
	}

	protected void cancelInProgress() {
		synchronized (this) {
			if (mProgressDialog != null) {
				mProgressDialog.cancel();
				mProgressDialog = null;
			}
		}
	}

	protected void cancelInProgressOnUiThread() {
        mHandler.post(new Runnable() {
			@Override
			public void run() {
				cancelInProgress();
			}
		});
	}

	protected void showInProgressOnUiThread(final String text,
			final boolean bIndeterminate, final boolean bCancelable) {
        mHandler.post(new Runnable() {
			@Override
			public void run() {
				showInProgress(text, bIndeterminate, bCancelable);
			}
		});
	}

	protected void showMsgBox(String title, String message) {
		CustomDialog.show(this, R.drawable.ic_launcher, title, message, "OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}, null, null);
	}

	protected void showMsgBoxOnUiThread(final String title, final String message) {
        mHandler.post(new Runnable() {
			@Override
			public void run() {
				showMsgBox(title, message);
			}
		});
	}

    protected String getPath(Uri uri) {
    	try{
	        String[] projection = { MediaStore.Images.Media.DATA };
	        Cursor cursor = managedQuery(uri, projection, null, null, null);
	        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	        cursor.moveToFirst();
	        final String path = cursor.getString(column_index);
	        cursor.close();
	        return path;
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    }
    
	public static boolean processKeyDown(int keyCode, KeyEvent event) {
		final IScreenService screenService = ((Engine)Engine.getInstance()).getScreenService();
		final IBaseScreen currentScreen = screenService.getCurrentScreen();
		if (currentScreen != null) {
			if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0
					&& currentScreen.getType() != SCREEN_TYPE.HOME_T) {
				if (currentScreen.hasBack()) {
					if (!currentScreen.back()) {
						return false;
					}
				} else {
					screenService.back();
				}
				return true;
			}
			else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP){
				if(currentScreen.getType() == SCREEN_TYPE.AV_T){
					Log.d(TAG, "intercepting volume changed event");

				}
			}
			else if (keyCode == KeyEvent.KEYCODE_MENU
					&& event.getRepeatCount() == 0) {
				if (currentScreen instanceof Activity
						&& currentScreen.hasMenu()) {
					return false;
					// return ((Activity)currentScreen).onKeyDown(keyCode,
					// event);
				}
				/*
				 * if(!currentScreen.hasMenu()){
				 * screenService.show(ScreenHome.class); return true; } else
				 * if(currentScreen instanceof Activity){ return
				 * ((Activity)currentScreen).onKeyDown(keyCode, event); }
				 */
				return true;
			}
		}
		return false;
	}
}

