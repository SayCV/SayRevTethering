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
                        /*if(getEngine().getConfigurationService().getBoolean(SgsConfigurationEntry.NETWORK_CONNECTED,
                                SgsConfigurationEntry.DEFAULT_NETWORK_CONNECTED)) {
                            ((TetheringService)getEngine().getTetheringService()).setRegistrationState(TetheringSession.ConnectionState.CONNECTED);
                            SgsApplication.acquireWakeLock();
                        }*/
						mScreenService.show(ScreenHome.class);
						getEngine().getConfigurationService().putBoolean(SgsConfigurationEntry.GENERAL_AUTOSTART, true);
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
            //SgsApplication.releaseWakeLock();
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
                    if(getEngine().getConfigurationService().getBoolean(SgsConfigurationEntry.NETWORK_CONNECTED,
                            SgsConfigurationEntry.DEFAULT_NETWORK_CONNECTED)) {
                        //((TetheringService)getEngine().getTetheringService()).setRegistrationState(TetheringSession.ConnectionState.CONNECTED);
                        SgsApplication.acquireWakeLock();
                        ((TetheringService)getEngine().getTetheringService()).reRegister(null);
                    }
				}
			}
		});
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}
}