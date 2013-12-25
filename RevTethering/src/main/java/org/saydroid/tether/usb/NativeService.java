/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
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
package org.saydroid.tether.usb;


import org.saydroid.sgs.SgsApplication;
import org.saydroid.sgs.SgsNativeService;
import org.saydroid.sgs.events.SgsEventArgs;
import org.saydroid.sgs.events.SgsInviteEventArgs;
import org.saydroid.sgs.events.SgsMessagingEventArgs;
import org.saydroid.sgs.events.SgsMsrpEventArgs;
import org.saydroid.sgs.events.SgsRegistrationEventArgs;
import org.saydroid.sgs.events.SgsRegistrationEventTypes;
import org.saydroid.sgs.media.SgsMediaType;
import org.saydroid.sgs.model.SgsHistorySMSEvent;
import org.saydroid.sgs.model.SgsHistoryEvent.StatusType;
import org.saydroid.sgs.sip.SgsAVSession;
import org.saydroid.sgs.sip.SgsMsrpSession;
import org.saydroid.sgs.utils.SgsDateTimeUtils;
import org.saydroid.sgs.utils.SgsStringUtils;
import org.saydroid.sgs.utils.SgsUriUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Window;
import android.view.WindowManager;

import org.saydroid.logger.Log;

public class NativeService extends SgsNativeService {
	private final static String TAG = NativeService.class.getCanonicalName();
	public static final String ACTION_STATE_EVENT = TAG + ".ACTION_STATE_EVENT";
	
	private PowerManager.WakeLock mWakeLock;
	private BroadcastReceiver mBroadcastReceiver;
	private Engine mEngine;
	
	public NativeService(){
		super();
		mEngine = (Engine)Engine.getInstance();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate()");
		
		final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if(powerManager != null && mWakeLock == null){
			mWakeLock = powerManager.newWakeLock(PowerManager.ON_AFTER_RELEASE 
					| PowerManager.SCREEN_BRIGHT_WAKE_LOCK
					| PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
		}
        /*final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if(mEngine != null){
            mEngine.getInstance().getMainActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }*/
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "onStart()");
		
		// register()
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				
				// Registration Events
				//if(SgsRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)){ }
				
				// PagerMode Messaging Events
				//if(SgsMessagingEventArgs.ACTION_MESSAGING_EVENT.equals(action)){ }
				
				// MSRP chat Events
				// For performance reasons, file transfer events will be handled by the owner of the context
				if(SgsMsrpEventArgs.ACTION_MSRP_EVENT.equals(action)){ }
				
				// Invite Events
				else if(SgsInviteEventArgs.ACTION_INVITE_EVENT.equals(action)){ }
			}
		};
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(SgsRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
		intentFilter.addAction(SgsInviteEventArgs.ACTION_INVITE_EVENT);
		intentFilter.addAction(SgsMessagingEventArgs.ACTION_MESSAGING_EVENT);
		intentFilter.addAction(SgsMsrpEventArgs.ACTION_MSRP_EVENT);
		registerReceiver(mBroadcastReceiver, intentFilter);
		
		if(intent != null){
			Bundle bundle = intent.getExtras();
			if (bundle != null && bundle.getBoolean("autostarted")) {
				if (mEngine.start()) {
					mEngine.getSipService().register(null);
				}
			}
		}
		
		// alert()
		final Intent i = new Intent(ACTION_STATE_EVENT);
		i.putExtra("started", true);
		sendBroadcast(i);
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		if(mBroadcastReceiver != null){
			unregisterReceiver(mBroadcastReceiver);
			mBroadcastReceiver = null;
		}
		if(mWakeLock != null){
			if(mWakeLock.isHeld()){
				mWakeLock.release();
				mWakeLock = null;
			}
		}
		super.onDestroy();
	}
}
