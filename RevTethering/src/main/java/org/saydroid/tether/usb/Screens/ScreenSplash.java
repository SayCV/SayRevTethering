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

import org.saydroid.sgs.SgsApplication;
import org.saydroid.tether.usb.Engine;
import org.saydroid.tether.usb.NativeService;
import org.saydroid.tether.usb.R;
import org.saydroid.sgs.utils.SgsConfigurationEntry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import org.saydroid.logger.Log;
import org.saydroid.tether.usb.Services.Impl.TetheringService;
import org.saydroid.tether.usb.Tethering.TetheringSession;

public class ScreenSplash extends BaseScreen {
	private static String TAG = ScreenSplash.class.getCanonicalName();
	
	private BroadcastReceiver mBroadCastRecv;
	
	public ScreenSplash() {
		super(SCREEN_TYPE.SPLASH_T, TAG);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.screen_splash);
		
		mBroadCastRecv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				Log.d(TAG, "onReceive()");
				
				if(NativeService.ACTION_STATE_EVENT.equals(action)){
					if(intent.getBooleanExtra("started", false)){
						mScreenService.show(ScreenHome.class);
						getEngine().getConfigurationService().putBoolean(SgsConfigurationEntry.GENERAL_AUTOSTART, true);
                        if(getEngine().getConfigurationService().getBoolean(SgsConfigurationEntry.NETWORK_CONNECTED,
                                SgsConfigurationEntry.DEFAULT_NETWORK_CONNECTED)) {
                            ((TetheringService)getEngine().getTetheringService()).setRegistrationState(TetheringSession.ConnectionState.CONNECTED);
                            SgsApplication.acquireWakeLock();
                        }
						finish();
					}
				}
			}
		};
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NativeService.ACTION_STATE_EVENT);
	    registerReceiver(mBroadCastRecv, intentFilter);
	}
	
	@Override
	protected void onDestroy() {
		if(mBroadCastRecv != null){
			unregisterReceiver(mBroadCastRecv);
            SgsApplication.releaseWakeLock();
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		final Engine engine = getEngine();
			
		final Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				if(!engine.isStarted()){
					Log.d(TAG, "Starts the engine from the splash screen");
					engine.start();
				}
			}
		});
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}
}