/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)saydroid(dot)org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package org.saydroid.tether.usb.Screens;

import org.saydroid.tether.usb.CustomDialog;
import org.saydroid.tether.usb.MainActivity;
import org.saydroid.tether.usb.R;
import org.saydroid.sgs.events.SgsEventArgs;
import org.saydroid.sgs.events.SgsRegistrationEventArgs;
import org.saydroid.sgs.services.ISgsSipService;
import org.saydroid.sgs.sip.SgsSipSession.ConnectionState;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import org.saydroid.logger.Log;

public class ScreenHome extends BaseScreen {
	private static String TAG = ScreenHome.class.getCanonicalName();
	
	private static final int MENU_EXIT = 0;
	private static final int MENU_SETTINGS = 1;
	
	private GridView mGridView;
	
	//private final ISgsSipService mSipService;
	
	private BroadcastReceiver mSipBroadCastRecv;
	
	public ScreenHome() {
		super(SCREEN_TYPE.HOME_T, TAG);
		
		//mSipService = getEngine().getSipService();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_home);
		
		mGridView = (GridView) findViewById(R.id.screen_home_gridview);
		mGridView.setAdapter(new ScreenHomeAdapter(this));
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final ScreenHomeItem item = (ScreenHomeItem)parent.getItemAtPosition(position);
				if (item != null) {
					if(position == ScreenHomeItem.ITEM_SIGNIN_SIGNOUT_POS){

					}
					else if(position == ScreenHomeItem.ITEM_EXIT_POS){
						CustomDialog.show(
								ScreenHome.this,
								R.drawable.exit_48,
								null,
								"Are you sure you want to exit?",
								"Yes",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										((MainActivity)(getEngine().getMainActivity())).exit();
									}
								}, "No",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.cancel();
									}
								});
					}
					else{					
						mScreenService.show(item.mClass, item.mClass.getCanonicalName());
					}
				}
			}
		});
		
		mSipBroadCastRecv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				
				// Registration Event
				if(SgsRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)){
					SgsRegistrationEventArgs args = intent.getParcelableExtra(SgsEventArgs.EXTRA_EMBEDDED);
					if(args == null){
						Log.e(TAG, "Invalid event args");
						return;
					}
					switch(args.getEventType()){
						case REGISTRATION_NOK:
						case UNREGISTRATION_OK:
						case REGISTRATION_OK:
						case REGISTRATION_INPROGRESS:
						case UNREGISTRATION_INPROGRESS:
						case UNREGISTRATION_NOK:
						default:
							((ScreenHomeAdapter)mGridView.getAdapter()).refresh();
							break;
					}
				}
			}
		};
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(SgsRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
	    registerReceiver(mSipBroadCastRecv, intentFilter);
	}

	@Override
	protected void onDestroy() {
       if(mSipBroadCastRecv != null){
    	   unregisterReceiver(mSipBroadCastRecv);
    	   mSipBroadCastRecv = null;
       }
        
       super.onDestroy();
	}
	
	@Override
	public boolean hasMenu() {
		return true;
	}
	
	@Override
	public boolean createOptionsMenu(Menu menu) {
		menu.add(0, ScreenHome.MENU_SETTINGS, 0, "Settings");
		/*MenuItem itemExit =*/ menu.add(0, ScreenHome.MENU_EXIT, 0, "Exit");
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case ScreenHome.MENU_EXIT:
				((MainActivity)getEngine().getMainActivity()).exit();
				break;
			case ScreenHome.MENU_SETTINGS:
				mScreenService.show(ScreenSettings.class);
				break;
		}
		return true;
	}
	
	
	/**
	 * ScreenHomeItem
	 */
	static class ScreenHomeItem {
		static final int ITEM_SIGNIN_SIGNOUT_POS = 0;
		static final int ITEM_EXIT_POS = 3;
		final int mIconResId;
		final String mText;
		final Class<? extends Activity> mClass;

		private ScreenHomeItem(int iconResId, String text, Class<? extends Activity> _class) {
			mIconResId = iconResId;
			mText = text;
			mClass = _class;
		}
	}
	
	/**
	 * ScreenHomeAdapter
	 */
	static class ScreenHomeAdapter extends BaseAdapter{
		static final int ALWAYS_VISIBLE_ITEMS_COUNT = 5;
		static final ScreenHomeItem[] sItems =  new ScreenHomeItem[]{
			// always visible
    		new ScreenHomeItem(R.drawable.start, "Start Tethering", null),
    		new ScreenHomeItem(R.drawable.options_48, "Options", ScreenSettings.class),
    		new ScreenHomeItem(R.drawable.about_48, "About", ScreenAbout.class),
            new ScreenHomeItem(R.drawable.exit_48, "Exit/Quit", null),
    		// visible only if connected
    		new ScreenHomeItem(R.drawable.stop, "Stop Tethering", null),
    		//new ScreenHomeItem(R.drawable.dialer_48, "Dialer", ScreenTabDialer.class),
    		//new ScreenHomeItem(R.drawable.eab2_48, "Address Book", ScreenTabContacts.class),
    		//new ScreenHomeItem(R.drawable.history_48, "History", ScreenTabHistory.class),
    		//new ScreenHomeItem(R.drawable.chat_48, "Messages", ScreenTabMessages.class),
		};
		
		private final LayoutInflater mInflater;
		private final ScreenHome mBaseScreen;
		
		ScreenHomeAdapter(ScreenHome baseScreen){
			mInflater = LayoutInflater.from(baseScreen);
			mBaseScreen = baseScreen;
		}
		
		void refresh(){
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return ALWAYS_VISIBLE_ITEMS_COUNT;
		}

		@Override
		public Object getItem(int position) {
			return sItems[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ScreenHomeItem item = (ScreenHomeItem)getItem(position);
			
			if(item == null){
				return null;
			}

			if (view == null) {
				view = mInflater.inflate(R.layout.screen_home_item, null);
			}
			
			if(position == ScreenHomeItem.ITEM_SIGNIN_SIGNOUT_POS){

			}
			else{				
				((TextView) view.findViewById(R.id.screen_home_item_text)).setText(item.mText);
				((ImageView) view .findViewById(R.id.screen_home_item_icon)).setImageResource(item.mIconResId);
			}
			
			return view;
		}
		
	}
}
