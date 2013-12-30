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

package org.saydroid.tether.usb.Services.Impl;


import android.content.Context;
import android.content.Intent;
import android.os.ConditionVariable;

import org.saydroid.logger.Log;
import org.saydroid.sgs.SgsApplication;
import org.saydroid.sgs.SgsEngine;
import org.saydroid.sgs.events.SgsInviteEventArgs;
import org.saydroid.sgs.events.SgsMessagingEventArgs;
import org.saydroid.sgs.events.SgsMessagingEventTypes;
import org.saydroid.sgs.events.SgsPublicationEventArgs;
import org.saydroid.sgs.events.SgsRegistrationEventArgs;
import org.saydroid.sgs.events.SgsSubscriptionEventArgs;
import org.saydroid.sgs.services.ISgsConfigurationService;
import org.saydroid.sgs.services.ISgsNetworkService;
import org.saydroid.sgs.services.impl.SgsBaseService;
import org.saydroid.sgs.sip.SgsPresenceStatus;
import org.saydroid.sgs.sip.SgsRegistrationSession;
import org.saydroid.sgs.utils.SgsConfigurationEntry;
import org.saydroid.sgs.utils.SgsDateTimeUtils;
import org.saydroid.tether.usb.Services.ITetheringService;
import org.saydroid.tether.usb.Tethering.TetheringPrefrences;
import org.saydroid.tether.usb.Tethering.TetheringSession.ConnectionState;
import org.saydroid.tether.usb.Tethering.TetheringStack;
import org.saydroid.tether.usb.Tethering.TetheringStack.STACK_STATE;

public class TetheringService extends SgsBaseService
implements ITetheringService {
	private final static String TAG = TetheringService.class.getCanonicalName();
	
	private SgsRegistrationSession mRegSession;
	private TetheringStack mTetheringStack;
	private final TetheringPrefrences mPreferences;
	
	private final ISgsConfigurationService mConfigurationService;
	private final ISgsNetworkService mNetworkService;
	
	private ConditionVariable mCondHackAoR;
	
	public TetheringService() {
		super();
		
		//mSipCallback = new MySipCallback(this);
		mPreferences = new TetheringPrefrences();
		
		mConfigurationService = SgsEngine.getInstance().getConfigurationService();
		mNetworkService = SgsEngine.getInstance().getNetworkService();
	}
	
	@Override
	public boolean start() {
		Log.d(TAG, "starting...");
		return true;
	}

	@Override
	public boolean stop() {
		Log.d(TAG, "stopping...");
		if(mTetheringStack != null && mTetheringStack.getState() == STACK_STATE.STARTED){
			return mTetheringStack.stop();
		}
		return true;
	}

	@Override
	public String getDefaultIdentity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDefaultIdentity(String identity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TetheringStack getTetheringStack() {
		return mTetheringStack;
	}

	@Override
	public boolean isRegistered() {
		if (mRegSession != null) {
			return mRegSession.isConnected();
		}
		return false;
	}
	
	@Override
	public ConnectionState getRegistrationState(){
		if (mRegSession != null) {
			//return mRegSession.getConnectionState();
            return ConnectionState.NONE;
		}
		return ConnectionState.NONE;
	}

	@Override
	public boolean isXcapEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPublicationEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSubscriptionEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSubscriptionToRLSEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getCodecs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setCodecs(int coddecs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] getSubRLSContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getSubRegContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getSubMwiContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getSubWinfoContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean stopStack() {
		if(mTetheringStack != null){
            mTetheringStack.stop();
		}
		return false;
	}

	@Override
	public boolean register(Context context) {
		Log.d(TAG,"register()");
		mPreferences.setRealm(mConfigurationService.getString(SgsConfigurationEntry.NETWORK_REALM, 
				SgsConfigurationEntry.DEFAULT_NETWORK_REALM));
		mPreferences.setIMPI(mConfigurationService.getString(SgsConfigurationEntry.IDENTITY_IMPI, 
				SgsConfigurationEntry.DEFAULT_IDENTITY_IMPI));
		mPreferences.setIMPU(mConfigurationService.getString(SgsConfigurationEntry.IDENTITY_IMPU, 
				SgsConfigurationEntry.DEFAULT_IDENTITY_IMPU));
		
		Log.d(TAG, String.format(
				"realm='%s', impu='%s', impi='%s'", mPreferences.getRealm(), mPreferences.getIMPU(), mPreferences.getIMPI()));
		
		if (mTetheringStack == null) {
            mTetheringStack = new TetheringStack();
            //mTetheringStack.setDebugCallback(new DDebugCallback());
		} else {

		}
		
		// set the Password
		// Set AMF
		// Set Operator Id

		
		// Check stack validity
		if (!mTetheringStack.isValid()) {
			Log.e(TAG, "Trying to use invalid stack");
			return false;
		}
		
		// Set STUN information

		
		// Set Proxy-CSCF

		
		// Set local IP (If your reusing this code on non-Android platforms (iOS, Symbian, WinPhone, ...),
		// let Doubango retrieve the best IP address)

		
		// Whether to use DNS NAPTR+SRV for the Proxy-CSCF discovery (even if the DNS requests are sent only when the stack starts,
		// should be done after setProxyCSCF())

		
		// SigComp (only update compartment Id if changed)

		
		// Start the Stack

		
		// Preference values
		mPreferences.setXcapEnabled(mConfigurationService.getBoolean(SgsConfigurationEntry.XCAP_ENABLED,
				SgsConfigurationEntry.DEFAULT_XCAP_ENABLED));
		mPreferences.setPresenceEnabled(mConfigurationService.getBoolean(SgsConfigurationEntry.RCS_USE_PRESENCE,
				SgsConfigurationEntry.DEFAULT_RCS_USE_PRESENCE));
		mPreferences.setMWI(mConfigurationService.getBoolean(SgsConfigurationEntry.RCS_USE_MWI,
				SgsConfigurationEntry.DEFAULT_RCS_USE_MWI));
		
		// Create registration session

		
		// Set/update From URI. For Registration ToUri should be equals to realm
		// (done by the stack)
		mRegSession.setFromUri(mPreferences.getIMPU());
		
		/* Before registering, check if AoR hacking id enabled */


		if (!mRegSession.register()) {
			Log.e(TAG, "Failed to send REGISTER request");
			return false;
		}
		
		return true;
	}

	@Override
	public boolean unRegister() {
		if (isRegistered()) {
			new Thread(new Runnable(){
				@Override
				public void run() {
                    mTetheringStack.stop();
				}
			}).start();
		}
		return true;
	}

	@Override
	public boolean PresencePublish() {
		return false;
	}

	public boolean PresencePublish(TetheringPrefrences status) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void broadcastRegistrationEvent(SgsRegistrationEventArgs args){
		final Intent intent = new Intent(SgsRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
		intent.putExtra(SgsRegistrationEventArgs.EXTRA_EMBEDDED, args);
		SgsApplication.getContext().sendBroadcast(intent);
	}
	
	private void broadcastInviteEvent(SgsInviteEventArgs args, short sipCode){
		final Intent intent = new Intent(SgsInviteEventArgs.ACTION_INVITE_EVENT);
		intent.putExtra(SgsInviteEventArgs.EXTRA_EMBEDDED, args);
		intent.putExtra(SgsInviteEventArgs.EXTRA_SIPCODE, sipCode);
		SgsApplication.getContext().sendBroadcast(intent);
	}
	
	private void broadcastInviteEvent(SgsInviteEventArgs args){
		final Intent intent = new Intent(SgsInviteEventArgs.ACTION_INVITE_EVENT);
		intent.putExtra(SgsInviteEventArgs.EXTRA_EMBEDDED, args);
		intent.putExtra(SgsInviteEventArgs.EXTRA_SIPCODE, 0);
		SgsApplication.getContext().sendBroadcast(intent);
	}
	
	private void broadcastMessagingEvent(SgsMessagingEventArgs args, String remoteParty, String date){
		final Intent intent = new Intent(SgsMessagingEventArgs.ACTION_MESSAGING_EVENT);
		intent.putExtra(SgsMessagingEventArgs.EXTRA_REMOTE_PARTY, remoteParty);
		intent.putExtra(SgsMessagingEventArgs.EXTRA_DATE, date);
		intent.putExtra(SgsMessagingEventArgs.EXTRA_EMBEDDED, args);
		SgsApplication.getContext().sendBroadcast(intent);
	}
	
	private void broadcastPublicationEvent(SgsPublicationEventArgs args){
		final Intent intent = new Intent(SgsPublicationEventArgs.ACTION_PUBLICATION_EVENT);
		intent.putExtra(SgsPublicationEventArgs.EXTRA_EMBEDDED, args);
		SgsApplication.getContext().sendBroadcast(intent);
	}
	
	private void broadcastSubscriptionEvent(SgsSubscriptionEventArgs args){
		final Intent intent = new Intent(SgsSubscriptionEventArgs.ACTION_SUBSCRIBTION_EVENT);
		intent.putExtra(SgsSubscriptionEventArgs.EXTRA_EMBEDDED, args);
		SgsApplication.getContext().sendBroadcast(intent);
	}
	

		

}
