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
package org.saydroid.tether.usb.Services.Impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

import org.saydroid.tether.usb.Engine;
import org.saydroid.tether.usb.SRTDroid;
import org.saydroid.tether.usb.MainActivity;
import org.saydroid.tether.usb.R;
import org.saydroid.tether.usb.Screens.IBaseScreen;
import org.saydroid.tether.usb.Screens.ScreenHome;
import org.saydroid.tether.usb.Services.IScreenService;
import org.saydroid.sgs.services.impl.SgsBaseService;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.saydroid.logger.Log;

public class ScreenService extends SgsBaseService implements IScreenService {
	private final static String TAG = ScreenService.class.getCanonicalName();
	
	private int mLastScreensIndex = -1; // ring cursor
	private final String[] mLastScreens =  new String[]{ // ring
    		null,
    		null,
    		null,
    		null
	};
	
	@Override
	public boolean start() {
		Log.d(TAG, "starting...");
		return true;
	}

	@Override
	public boolean stop() {
		Log.d(TAG, "stopping...");
		return true;
	}

	@Override
	public boolean back() {
		String screen;
		
		// no screen in the stack
		if(mLastScreensIndex < 0){
			return true;
		}
		
		// zero is special case
		if(mLastScreensIndex == 0){
			if((screen = mLastScreens[mLastScreens.length-1]) == null){
				// goto home
				return show(ScreenHome.class);
			}
			else{
				return this.show(screen);
			}
		}
		// all other cases
		screen = mLastScreens[mLastScreensIndex-1];
		mLastScreens[mLastScreensIndex-1] = null;
		mLastScreensIndex--;
		if(screen == null || !show(screen)){
			return show(ScreenHome.class);
		}
		
		return true;
	}

	@Override
	public boolean bringToFront(int action, String[]... args) {
		Intent intent = new Intent(SRTDroid.getContext(), MainActivity.class);
		try{
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP  | Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("action", action);
			for(String[] arg : args){
				if(arg.length != 2){
					continue;
				}
				intent.putExtra(arg[0], arg[1]);
			}
            SRTDroid.getContext().startActivity(intent);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean bringToFront(String[]... args) {
		return this.bringToFront(MainActivity.ACTION_NONE);
	}

	@Override
	public boolean show(Class<? extends Activity> cls, String id) {
		final MainActivity mainActivity = (MainActivity)Engine.getInstance().getMainActivity();
		
		String screen_id = (id == null) ? cls.getCanonicalName() : id;
		Intent intent = new Intent(mainActivity, cls);
		intent.putExtra("id", screen_id);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		final Window window = mainActivity.getLocalActivityManager().startActivity(screen_id, intent);
		if(window != null){
			View view = mainActivity.getLocalActivityManager().startActivity(screen_id, intent).getDecorView();

            FrameLayout layout = (FrameLayout) mainActivity.findViewById(R.id.main_linearLayout_principal);
			layout.removeAllViews();
			layout.addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			
			// add to stack
			this.mLastScreens[(++this.mLastScreensIndex % this.mLastScreens.length)] = screen_id;
			this.mLastScreensIndex %= this.mLastScreens.length;
			return true;
		}
		return false;
	}

	@Override
	public boolean show(Class<? extends Activity> cls) {
		return this.show(cls, null);
	}

	@Override
	public boolean show(String id) {
		final  Activity screen = (Activity)((MainActivity)Engine.getInstance().getMainActivity()).getLocalActivityManager().getActivity(id);
		if (screen == null) {
			Log.e(TAG, String.format(
					"Failed to retrieve the Screen with id=%s", id));
			return false;
		} else {
			return this.show(screen.getClass(), id);
		}
	}

	@Override
	public void runOnUiThread(Runnable r) {
		if(Engine.getInstance().getMainActivity() != null){
			Engine.getInstance().getMainActivity().runOnUiThread(r);
		}
		else{
			Log.e(this.getClass().getCanonicalName(), "No Main activity");
		}
	}

	@Override
	public boolean destroy(String id) {
		final LocalActivityManager activityManager = (((MainActivity)Engine.getInstance().getMainActivity())).getLocalActivityManager();
		if(activityManager != null){
			activityManager.destroyActivity(id, true);
			
			// http://code.google.com/p/android/issues/detail?id=12359
			// http://www.netmite.com/android/mydroid/frameworks/base/core/java/android/app/LocalActivityManager.java
			try {
				final Field mActivitiesField = LocalActivityManager.class.getDeclaredField("mActivities");
				if(mActivitiesField != null){
					mActivitiesField.setAccessible(true);
					@SuppressWarnings("unchecked")
					final Map<String, Object> mActivities = (Map<String, Object>)mActivitiesField.get(activityManager);
					if(mActivities != null){
						mActivities.remove(id);
					}
					final Field mActivityArrayField = LocalActivityManager.class.getDeclaredField("mActivityArray");
					if(mActivityArrayField != null){
						mActivityArrayField.setAccessible(true);
						@SuppressWarnings("unchecked")
						final ArrayList<Object> mActivityArray = (ArrayList<Object>)mActivityArrayField.get(activityManager);
						if(mActivityArray != null){
							for(Object record : mActivityArray){
								final Field idField = record.getClass().getDeclaredField("id");
								if(idField != null){
									idField.setAccessible(true);
									final String _id = (String)idField.get(record);
									if(id.equals(_id)){
										mActivityArray.remove(record);
										break;
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	@Override
	public void setProgressInfoText(String text) {
	}

	@Override
	public IBaseScreen getCurrentScreen() {
		return (IBaseScreen)((MainActivity)Engine.getInstance().getMainActivity()).getLocalActivityManager().getCurrentActivity();
	}

	@Override
	public IBaseScreen getScreen(String id) {
		return (IBaseScreen)((MainActivity)Engine.getInstance().getMainActivity()).getLocalActivityManager().getActivity(id);
	}
}
