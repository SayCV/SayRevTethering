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
package org.saydroid.sgs.services.impl;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.saydroid.sgs.SgsApplication;
import org.saydroid.sgs.SgsEngine;
import org.saydroid.sgs.events.SgsInviteEventArgs;
import org.saydroid.sgs.events.SgsInviteEventTypes;
import org.saydroid.sgs.events.SgsMessagingEventArgs;
import org.saydroid.sgs.events.SgsMessagingEventTypes;
import org.saydroid.sgs.events.SgsPublicationEventArgs;
import org.saydroid.sgs.events.SgsPublicationEventTypes;
import org.saydroid.sgs.events.SgsRegistrationEventArgs;
import org.saydroid.sgs.events.SgsRegistrationEventTypes;
import org.saydroid.sgs.events.SgsSubscriptionEventArgs;
import org.saydroid.sgs.events.SgsSubscriptionEventTypes;
import org.saydroid.sgs.model.SgsDeviceInfo.Orientation;
import org.saydroid.sgs.services.ISgsConfigurationService;
import org.saydroid.sgs.services.ISgsNetworkService;
import org.saydroid.sgs.services.ISgsSipService;
import org.saydroid.sgs.sip.SgsAVSession;
import org.saydroid.sgs.sip.SgsInviteSession;
import org.saydroid.sgs.sip.SgsMessagingSession;
import org.saydroid.sgs.sip.SgsMsrpSession;
import org.saydroid.sgs.sip.SgsPresenceStatus;
import org.saydroid.sgs.sip.SgsPublicationSession;
import org.saydroid.sgs.sip.SgsRegistrationSession;
import org.saydroid.sgs.sip.SgsSipPrefrences;
import org.saydroid.sgs.sip.SgsSipSession;
import org.saydroid.sgs.sip.SgsSipStack;
import org.saydroid.sgs.sip.SgsSubscriptionSession;
import org.saydroid.sgs.sip.SgsInviteSession.InviteState;
import org.saydroid.sgs.sip.SgsSipSession.ConnectionState;
import org.saydroid.sgs.sip.SgsSipStack.STACK_STATE;
import org.saydroid.sgs.sip.SgsSubscriptionSession.EventPackageType;
import org.saydroid.sgs.utils.SgsConfigurationEntry;
import org.saydroid.sgs.utils.SgsContentType;
import org.saydroid.sgs.utils.SgsDateTimeUtils;
import org.saydroid.sgs.utils.SgsStringUtils;
import org.saydroid.sgs.utils.SgsUriUtils;
import org.saydroid.tinyWRAP.CallSession;
import org.saydroid.tinyWRAP.DDebugCallback;
import org.saydroid.tinyWRAP.DialogEvent;
import org.saydroid.tinyWRAP.InviteEvent;
import org.saydroid.tinyWRAP.InviteSession;
import org.saydroid.tinyWRAP.MessagingEvent;
import org.saydroid.tinyWRAP.MessagingSession;
import org.saydroid.tinyWRAP.MsrpSession;
import org.saydroid.tinyWRAP.OptionsEvent;
import org.saydroid.tinyWRAP.OptionsSession;
import org.saydroid.tinyWRAP.RPMessage;
import org.saydroid.tinyWRAP.SMSData;
import org.saydroid.tinyWRAP.SMSEncoder;
import org.saydroid.tinyWRAP.SipCallback;
import org.saydroid.tinyWRAP.SipMessage;
import org.saydroid.tinyWRAP.SipSession;
import org.saydroid.tinyWRAP.SipStack;
import org.saydroid.tinyWRAP.StackEvent;
import org.saydroid.tinyWRAP.SubscriptionEvent;
import org.saydroid.tinyWRAP.SubscriptionSession;
import org.saydroid.tinyWRAP.tinyWRAPConstants;
import org.saydroid.tinyWRAP.tsip_invite_event_type_t;
import org.saydroid.tinyWRAP.tsip_message_event_type_t;
import org.saydroid.tinyWRAP.tsip_options_event_type_t;
import org.saydroid.tinyWRAP.tsip_request_type_t;
import org.saydroid.tinyWRAP.tsip_subscribe_event_type_t;
import org.saydroid.tinyWRAP.twrap_media_type_t;
import org.saydroid.tinyWRAP.twrap_sms_type_t;

import android.content.Context;
import android.content.Intent;
import android.os.ConditionVariable;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;


public class SgsSipService extends SgsBaseService 
implements ISgsSipService, tinyWRAPConstants {
	private final static String TAG = SgsSipService.class.getCanonicalName();
	
	private SgsRegistrationSession mRegSession;
	private SgsSipStack mSipStack;
	private final MySipCallback mSipCallback;
	private final SgsSipPrefrences mPreferences;
	
	private final ISgsConfigurationService mConfigurationService;
	private final ISgsNetworkService mNetworkService;
	
	private ConditionVariable mCondHackAoR;
	
	public SgsSipService() {
		super();
		
		mSipCallback = new MySipCallback(this);
		mPreferences = new SgsSipPrefrences();
		
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
		if(mSipStack != null && mSipStack.getState() == STACK_STATE.STARTED){
			return mSipStack.stop();
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
	public SgsSipStack getSipStack() {
		return mSipStack;
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
			return mRegSession.getConnectionState();
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
		if(mSipStack != null){
			mSipStack.stop();
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
		
		if (mSipStack == null) {
			mSipStack = new SgsSipStack(mSipCallback, mPreferences.getRealm(), mPreferences.getIMPI(), mPreferences.getIMPU());	
			mSipStack.setDebugCallback(new DDebugCallback());
			SipStack.setCodecs_2(mConfigurationService.getInt(SgsConfigurationEntry.MEDIA_CODECS, 
					SgsConfigurationEntry.DEFAULT_MEDIA_CODECS));
		} else {
			if (!mSipStack.setRealm(mPreferences.getRealm())) {
				Log.e(TAG, "Failed to set realm");
				return false;
			}
			if (!mSipStack.setIMPI(mPreferences.getIMPI())) {
				Log.e(TAG, "Failed to set IMPI");
				return false;
			}
			if (!mSipStack.setIMPU(mPreferences.getIMPU())) {
				Log.e(TAG, "Failed to set IMPU");
				return false;
			}
		}
		
		// set the Password
		mSipStack.setPassword(mConfigurationService.getString(
				SgsConfigurationEntry.IDENTITY_PASSWORD, SgsConfigurationEntry.DEFAULT_IDENTITY_PASSWORD));
		// Set AMF
		mSipStack.setAMF(mConfigurationService.getString(
				SgsConfigurationEntry.SECURITY_IMSAKA_AMF, SgsConfigurationEntry.DEFAULT_SECURITY_IMSAKA_AMF));
		// Set Operator Id
		mSipStack.setOperatorId(mConfigurationService.getString(
				SgsConfigurationEntry.SECURITY_IMSAKA_OPID, SgsConfigurationEntry.DEFAULT_SECURITY_IMSAKA_OPID));
		
		// Check stack validity
		if (!mSipStack.isValid()) {
			Log.e(TAG, "Trying to use invalid stack");
			return false;
		}
		
		// Set STUN information
		if(mConfigurationService.getBoolean(SgsConfigurationEntry.NATT_USE_STUN, SgsConfigurationEntry.DEFAULT_NATT_USE_STUN)){			
			Log.d(TAG, "STUN=yes");
			if(mConfigurationService.getBoolean(SgsConfigurationEntry.NATT_STUN_DISCO, SgsConfigurationEntry.DEFAULT_NATT_STUN_DISCO)){
				final String realm = mPreferences.getRealm();
				String domain = realm.substring(realm.indexOf(':')+1);
				int []port = new int[1];
				String server = mSipStack.dnsSrv(String.format("_stun._udp.%s", domain), port);
				if(server == null){
					Log.e(TAG, "STUN discovery has failed");
				}
				Log.d(TAG, String.format("STUN1 - server=%s and port=%d", server, port[0]));
				mSipStack.setSTUNServer(server, port[0]);// Needed event if null
			}
			else{
				String server = mConfigurationService.getString(SgsConfigurationEntry.NATT_STUN_SERVER, 
						SgsConfigurationEntry.DEFAULT_NATT_STUN_SERVER);
				int port = mConfigurationService.getInt(SgsConfigurationEntry.NATT_STUN_PORT, 
						SgsConfigurationEntry.DEFAULT_NATT_STUN_PORT);
				Log.d(SgsSipService.TAG, String.format("STUN2 - server=%s and port=%d", server, port));
				mSipStack.setSTUNServer(server, port);
			}
		}
		else{
			Log.d(TAG, "STUN=no");
			mSipStack.setSTUNServer(null, 0);
		}
		
		// Set Proxy-CSCF
		mPreferences.setPcscfHost(mConfigurationService.getString(SgsConfigurationEntry.NETWORK_PCSCF_HOST,
				null)); // null will trigger DNS NAPTR+SRV
		mPreferences.setPcscfPort(mConfigurationService.getInt(SgsConfigurationEntry.NETWORK_PCSCF_PORT,
				SgsConfigurationEntry.DEFAULT_NETWORK_PCSCF_PORT));
		mPreferences.setTransport(mConfigurationService.getString(SgsConfigurationEntry.NETWORK_TRANSPORT,
				SgsConfigurationEntry.DEFAULT_NETWORK_TRANSPORT));
		mPreferences.setIPVersion(mConfigurationService.getString(SgsConfigurationEntry.NETWORK_IP_VERSION,
				SgsConfigurationEntry.DEFAULT_NETWORK_IP_VERSION));
		
		Log.d(TAG, String.format(
				"pcscf-host='%s', pcscf-port='%d', transport='%s', ipversion='%s'",
				mPreferences.getPcscfHost(), 
				mPreferences.getPcscfPort(),
				mPreferences.getTransport(),
				mPreferences.getIPVersion()));

		if (!mSipStack.setProxyCSCF(mPreferences.getPcscfHost(), mPreferences.getPcscfPort(), mPreferences.getTransport(),
				mPreferences.getIPVersion())) {
			Log.e(SgsSipService.TAG, "Failed to set Proxy-CSCF parameters");
			return false;
		}
		
		// Set local IP (If your reusing this code on non-Android platforms (iOS, Symbian, WinPhone, ...),
		// let Doubango retrieve the best IP address)
		boolean ipv6 = SgsStringUtils.equals(mPreferences.getIPVersion(), "ipv6", true);
		mPreferences.setLocalIP(mNetworkService.getLocalIP(ipv6));
		if(mPreferences.getLocalIP() == null){
//			if(fromNetworkService){
//				this.preferences.localIP = ipv6 ? "::" : "10.0.2.15"; /* Probably on the emulator */
//			}
//			else{
//				Log.e(TAG, "IP address is Null. Trying to start network");
//				this.networkService.setNetworkEnabledAndRegister();
//				return false;
//			}
		}
		if (!mSipStack.setLocalIP(mPreferences.getLocalIP())) {
			Log.e(TAG, "Failed to set the local IP");
			return false;
		}
		Log.d(TAG, String.format("Local IP='%s'", mPreferences.getLocalIP()));
		
		// Whether to use DNS NAPTR+SRV for the Proxy-CSCF discovery (even if the DNS requests are sent only when the stack starts,
		// should be done after setProxyCSCF())
		String discoverType = mConfigurationService.getString(SgsConfigurationEntry.NETWORK_PCSCF_DISCOVERY, SgsConfigurationEntry.DEFAULT_NETWORK_PCSCF_DISCOVERY);
		mSipStack.setDnsDiscovery(SgsStringUtils.equals(discoverType, SgsConfigurationEntry.PCSCF_DISCOVERY_DNS_SRV, true));		
		
		// enable/disable 3GPP early IMS
		mSipStack.setEarlyIMS(mConfigurationService.getBoolean(SgsConfigurationEntry.NETWORK_USE_EARLY_IMS,
				SgsConfigurationEntry.DEFAULT_NETWORK_USE_EARLY_IMS));
		
		// SigComp (only update compartment Id if changed)
		if(mConfigurationService.getBoolean(SgsConfigurationEntry.NETWORK_USE_SIGCOMP, SgsConfigurationEntry.DEFAULT_NETWORK_USE_SIGCOMP)){
			String compId = String.format("urn:uuid:%s", UUID.randomUUID().toString());
			mSipStack.setSigCompId(compId);
		}
		else{
			mSipStack.setSigCompId(null);
		}
		
		// Start the Stack
		if (!mSipStack.start()) {
			if(context != null && Thread.currentThread() == Looper.getMainLooper().getThread()){
				Toast.makeText(context, "Failed to start the SIP stack", Toast.LENGTH_LONG).show();
			}
			Log.e(TAG, "Failed to start the SIP stack");
			return false;
		}
		
		// Preference values
		mPreferences.setXcapEnabled(mConfigurationService.getBoolean(SgsConfigurationEntry.XCAP_ENABLED,
				SgsConfigurationEntry.DEFAULT_XCAP_ENABLED));
		mPreferences.setPresenceEnabled(mConfigurationService.getBoolean(SgsConfigurationEntry.RCS_USE_PRESENCE,
				SgsConfigurationEntry.DEFAULT_RCS_USE_PRESENCE));
		mPreferences.setMWI(mConfigurationService.getBoolean(SgsConfigurationEntry.RCS_USE_MWI,
				SgsConfigurationEntry.DEFAULT_RCS_USE_MWI));
		
		// Create registration session
		if (mRegSession == null) {
			mRegSession = new SgsRegistrationSession(mSipStack);
		}
		else{
			mRegSession.setSigCompId(mSipStack.getSigCompId());
		}
		
		// Set/update From URI. For Registration ToUri should be equals to realm
		// (done by the stack)
		mRegSession.setFromUri(mPreferences.getIMPU());
		
		/* Before registering, check if AoR hacking id enabled */
		mPreferences.setHackAoR(mConfigurationService.getBoolean(SgsConfigurationEntry.NATT_HACK_AOR, 
				SgsConfigurationEntry.DEFAULT_NATT_HACK_AOR));
		if (mPreferences.isHackAoR()) {
			if (mCondHackAoR == null) {
				mCondHackAoR = new ConditionVariable();
			}
			final OptionsSession optSession = new OptionsSession(mSipStack);
			// optSession.setToUri(String.format("sip:%s@%s", "hacking_the_aor", this.preferences.realm));
			optSession.send();
			try {
				synchronized (mCondHackAoR) {
					mCondHackAoR.wait(mConfigurationService.getInt(SgsConfigurationEntry.NATT_HACK_AOR_TIMEOUT,
							SgsConfigurationEntry.DEFAULT_NATT_HACK_AOR_TIMEOUT));
				}
			} catch (InterruptedException e) {
				Log.e(TAG, e.getMessage());
			}
			mCondHackAoR = null;
			optSession.delete();
		}

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
					mSipStack.stop();
				}
			}).start();
		}
		return true;
	}

	@Override
	public boolean PresencePublish() {
		return false;
	}

	@Override
	public boolean PresencePublish(SgsPresenceStatus status) {
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
	
	/**
	 * MySipCallback
	 */
	static class MySipCallback extends SipCallback{
		private final SgsSipService mSipService;

		private MySipCallback(SgsSipService sipService) {
			super();

			mSipService = sipService;
		}
		
		@Override
		public int OnDialogEvent(DialogEvent e){
			final String phrase = e.getPhrase();
			final short eventCode = e.getCode();
			final short sipCode;
			final SipSession session = e.getBaseSession();
			
			if(session == null){
				return 0;
			}
			
			final long sessionId = session.getId();
			final SipMessage message = e.getSipMessage();
			SgsSipSession mySession = null;
			
			sipCode = (message != null && message.isResponse()) ? message.getResponseCode() : eventCode;
			
			Log.d(TAG, String.format("OnDialogEvent (%s,%d)", phrase,sessionId));
			
			switch (eventCode){
				//== Connecting ==
				case tinyWRAPConstants.tsip_event_code_dialog_connecting:
				{
					// Registration
                    if (mSipService.mRegSession != null && mSipService.mRegSession.getId() == sessionId){
                    	mSipService.mRegSession.setConnectionState(ConnectionState.CONNECTING);
                    	mSipService.broadcastRegistrationEvent(new SgsRegistrationEventArgs(sessionId, SgsRegistrationEventTypes.REGISTRATION_INPROGRESS, 
                    			eventCode, phrase));
                    }
                    // Audio/Video/MSRP(Chat, FileTransfer)
                    else if (((mySession = SgsAVSession.getSession(sessionId)) != null) || ((mySession = SgsMsrpSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.CONNECTING);
                        ((SgsInviteSession)mySession).setState(InviteState.INPROGRESS);
                        mSipService.broadcastInviteEvent(new SgsInviteEventArgs(sessionId, SgsInviteEventTypes.INPROGRESS, ((SgsInviteSession)mySession).getMediaType(), phrase));
                    } 
                    // Publication
                    else if(((mySession = SgsPublicationSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.CONNECTING);
                    	mSipService.broadcastPublicationEvent(new SgsPublicationEventArgs(sessionId, SgsPublicationEventTypes.PUBLICATION_INPROGRESS, 
                    			eventCode, phrase));
                    }
                    // Subscription
                    else if(((mySession = SgsSubscriptionSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.CONNECTING);
                    	mSipService.broadcastSubscriptionEvent(new SgsSubscriptionEventArgs(sessionId, SgsSubscriptionEventTypes.SUBSCRIPTION_INPROGRESS, 
                    			eventCode, phrase, null, null, ((SgsSubscriptionSession)mySession).getEventPackage()));
                    }

					break;
				}
				
				//== Connected == //
				case tinyWRAPConstants.tsip_event_code_dialog_connected:
				{
					// Registration
                    if (mSipService.mRegSession != null && mSipService.mRegSession.getId() == sessionId){
                    	mSipService.mRegSession.setConnectionState(ConnectionState.CONNECTED);
                        // Update default identity (vs barred)
                        String _defaultIdentity = mSipService.mSipStack.getPreferredIdentity();
                        if (!SgsStringUtils.isNullOrEmpty(_defaultIdentity)){
                        	mSipService.setDefaultIdentity(_defaultIdentity);
                        }
                        mSipService.broadcastRegistrationEvent(new SgsRegistrationEventArgs(sessionId, SgsRegistrationEventTypes.REGISTRATION_OK, 
                        		sipCode, phrase));
                    }
                    // Audio/Video/MSRP(Chat, FileTransfer)
                    else if (((mySession = SgsAVSession.getSession(sessionId)) != null) || ((mySession = SgsMsrpSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.CONNECTED);
                    	((SgsInviteSession)mySession).setState(InviteState.INCALL);
                        mSipService.broadcastInviteEvent(new SgsInviteEventArgs(sessionId, SgsInviteEventTypes.CONNECTED, ((SgsInviteSession)mySession).getMediaType(), phrase));
                    }
                    // Publication
                    else if(((mySession = SgsPublicationSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.CONNECTED);
                    	mSipService.broadcastPublicationEvent(new SgsPublicationEventArgs(sessionId, SgsPublicationEventTypes.PUBLICATION_OK, 
                    			sipCode, phrase));
                    }
                    // Subscription
                    else if(((mySession = SgsSubscriptionSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.CONNECTED);
                    	mSipService.broadcastSubscriptionEvent(new SgsSubscriptionEventArgs(sessionId, SgsSubscriptionEventTypes.SUBSCRIPTION_OK, 
                    			sipCode, phrase, null, null, ((SgsSubscriptionSession)mySession).getEventPackage()));
                    }

					break;
				}
				
				//== Terminating == //
				case tinyWRAPConstants.tsip_event_code_dialog_terminating:
				{
					// Registration
					if (mSipService.mRegSession != null && mSipService.mRegSession.getId() == sessionId){
						mSipService.mRegSession.setConnectionState(ConnectionState.TERMINATING);
						mSipService.broadcastRegistrationEvent(new SgsRegistrationEventArgs(sessionId, SgsRegistrationEventTypes.UNREGISTRATION_INPROGRESS, 
								eventCode, phrase));
					}
					// Audio/Video/MSRP(Chat, FileTransfer)
                    else if (((mySession = SgsAVSession.getSession(sessionId)) != null) || ((mySession = SgsMsrpSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.TERMINATING);
                    	((SgsInviteSession)mySession).setState(InviteState.TERMINATING);
                    	mSipService.broadcastInviteEvent(new SgsInviteEventArgs(sessionId, SgsInviteEventTypes.TERMWAIT, ((SgsInviteSession)mySession).getMediaType(), phrase));
                    }
					// Publication
                    else if(((mySession = SgsPublicationSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.TERMINATING);
                    	mSipService.broadcastPublicationEvent(new SgsPublicationEventArgs(sessionId, SgsPublicationEventTypes.UNPUBLICATION_INPROGRESS, 
                    			eventCode, phrase));
                    }
                    // Subscription
                    else if(((mySession = SgsSubscriptionSession.getSession(sessionId)) != null)){
                    	mySession.setConnectionState(ConnectionState.TERMINATING);
                    	mSipService.broadcastSubscriptionEvent(new SgsSubscriptionEventArgs(sessionId, SgsSubscriptionEventTypes.UNSUBSCRIPTION_INPROGRESS, 
                    			eventCode, phrase, null, null, ((SgsSubscriptionSession)mySession).getEventPackage()));
                    }

					break;
				}
				
				//== Terminated == //
				case tinyWRAPConstants.tsip_event_code_dialog_terminated:
				{
					// Registration
					if (mSipService.mRegSession != null && mSipService.mRegSession.getId() == sessionId){
						mSipService.mRegSession.setConnectionState(ConnectionState.TERMINATED);
						mSipService.broadcastRegistrationEvent(new SgsRegistrationEventArgs(sessionId, SgsRegistrationEventTypes.UNREGISTRATION_OK, sipCode, phrase));
						/* Stop the stack (as we are already in the stack-thread, then do it in a new thread) */
						new Thread(new Runnable(){
							public void run() {	
								if(mSipService.mSipStack.getState() == STACK_STATE.STARTING || mSipService.mSipStack.getState() == STACK_STATE.STARTED){
									mSipService.mSipStack.stop();
								}
							}
						}).start();
					}
					// PagerMode IM
					else if(SgsMessagingSession.hasSession(sessionId)){
						SgsMessagingSession.releaseSession(sessionId);
					}
					// Audio/Video/MSRP(Chat, FileTransfer)
                    else if (((mySession = SgsAVSession.getSession(sessionId)) != null) || ((mySession = SgsMsrpSession.getSession(sessionId)) != null)){
                        mySession.setConnectionState(ConnectionState.TERMINATED);
                        ((SgsInviteSession)mySession).setState(InviteState.TERMINATED);
                        mSipService.broadcastInviteEvent(new SgsInviteEventArgs(sessionId, SgsInviteEventTypes.TERMINATED, ((SgsInviteSession)mySession).getMediaType(), phrase));
                        if(mySession instanceof SgsAVSession){
                        	SgsAVSession.releaseSession((SgsAVSession)mySession);
                        }
                        else if(mySession instanceof SgsMsrpSession){
                        	SgsMsrpSession.releaseSession((SgsMsrpSession)mySession);
                        }
                    }
					// Publication
                    else if(((mySession = SgsPublicationSession.getSession(sessionId)) != null)){
                    	ConnectionState previousConnState = mySession.getConnectionState();
                    	mySession.setConnectionState(ConnectionState.TERMINATED);
                    	mSipService.broadcastPublicationEvent(new SgsPublicationEventArgs(sessionId, 
                    			(previousConnState == ConnectionState.TERMINATING) ? SgsPublicationEventTypes.UNPUBLICATION_OK : SgsPublicationEventTypes.PUBLICATION_NOK, 
                    			sipCode, phrase));
                    }
                    // Subscription
                    else if(((mySession = SgsSubscriptionSession.getSession(sessionId)) != null)){
                    	ConnectionState previousConnState = mySession.getConnectionState();
                    	
                    	mySession.setConnectionState(ConnectionState.TERMINATED);
                    	mSipService.broadcastSubscriptionEvent(new SgsSubscriptionEventArgs(sessionId, 
                    			(previousConnState == ConnectionState.TERMINATING) ? SgsSubscriptionEventTypes.UNSUBSCRIPTION_OK : SgsSubscriptionEventTypes.SUBSCRIPTION_NOK, 
                    			sipCode, phrase, null, null, ((SgsSubscriptionSession)mySession).getEventPackage()));
                    }
					break;
				}
			}
			
			return 0;
		}
		
		@Override
		public int OnInviteEvent(InviteEvent e) {
			 final tsip_invite_event_type_t type = e.getType();
			 final short code = e.getCode();
			 final String phrase = e.getPhrase();
			 InviteSession session = e.getSession();
			 SgsSipSession mySession = null;
			
			switch (type){
                case tsip_i_newcall:
                    if (session != null) /* As we are not the owner, then the session MUST be null */{
                        Log.e(TAG, "Invalid incoming session");
                        session.hangup(); // To avoid another callback event
                        return -1;
                    }

                    SipMessage message = e.getSipMessage();
                    if (message == null){
                        Log.e(TAG,"Invalid message");
                        return -1;
                    }
                    twrap_media_type_t sessionType = e.getMediaType();

                    switch (sessionType){
                        case twrap_media_msrp:
                            {
                            	if ((session = e.takeMsrpSessionOwnership()) == null){
                                    Log.e(TAG,"Failed to take MSRP session ownership");
                                    return -1;
                                }

                                SgsMsrpSession msrpSession = SgsMsrpSession.takeIncomingSession(mSipService.getSipStack(), 
                                		(MsrpSession)session, message);
                                if (msrpSession == null){
                                	Log.e(TAG,"Failed to create new session");
                                    session.hangup();
                                    session.delete();
                                    return 0;
                                }
                                mSipService.broadcastInviteEvent(new SgsInviteEventArgs(msrpSession.getId(), SgsInviteEventTypes.INCOMING, msrpSession.getMediaType(), phrase));
                                break;
                            }

                        case twrap_media_audio:
                        case twrap_media_audiovideo:
                        case twrap_media_video:
                            {
                                if ((session = e.takeCallSessionOwnership()) == null){
                                    Log.e(TAG,"Failed to take audio/video session ownership");
                                    return -1;
                                }
                                final SgsAVSession avSession = SgsAVSession.takeIncomingSession(mSipService.getSipStack(), (CallSession)session, sessionType, message); 
                                mSipService.broadcastInviteEvent(new SgsInviteEventArgs(avSession.getId(), SgsInviteEventTypes.INCOMING, avSession.getMediaType(), phrase));
                                break;
                            }

                        default:
                            Log.e(TAG,"Invalid media type");
                            return 0;
                        
                    }
                    break;

                case tsip_ao_request:
                	// For backward compatibility keep both "RINGING" and "SIP_RESPONSE"
                    if (code == 180 && session != null){
                    	if (((mySession = SgsAVSession.getSession(session.getId())) != null) || ((mySession = SgsMsrpSession.getSession(session.getId())) != null)){
                    		mSipService.broadcastInviteEvent(new SgsInviteEventArgs(mySession.getId(), SgsInviteEventTypes.RINGING, ((SgsInviteSession)mySession).getMediaType(), phrase));
                    	}
                    }
                    if(session != null){
                    	if (((mySession = SgsAVSession.getSession(session.getId())) != null) || ((mySession = SgsMsrpSession.getSession(session.getId())) != null)){
                    		mSipService.broadcastInviteEvent(new SgsInviteEventArgs(mySession.getId(), SgsInviteEventTypes.SIP_RESPONSE, ((SgsInviteSession)mySession).getMediaType(), phrase), code);
                    	}
                    }
                    break;

                case tsip_i_request:
                    {
                    	final SipMessage sipMessage = e.getSipMessage();
                    	if(sipMessage != null && ((mySession = SgsAVSession.getSession(session.getId())) != null)){
                    		if(sipMessage.getRequestType() == tsip_request_type_t.tsip_INFO){
                    			final String contentType = sipMessage.getSipHeaderValue("c");
                    			if(SgsStringUtils.equals(contentType, SgsContentType.DOUBANGO_DEVICE_INFO, true)){
                    				final byte content[] = sipMessage.getSipContent();
                    				if(content != null){
                    					final String values[] = new String(content).split("\r\n");
                    					for(String value : values){
                    						if(value == null) continue;
                    						final String kvp[] = value.split(":");
                    						if(kvp.length == 2){
                    							if(SgsStringUtils.equals(kvp[0], "orientation", true)){
                    								if(SgsStringUtils.equals(kvp[1], "landscape", true)){
                    									((SgsInviteSession)mySession).getRemoteDeviceInfo().setOrientation(Orientation.LANDSCAPE);
                    								}
                    								else if(SgsStringUtils.equals(kvp[1], "portrait", true)){
                    									((SgsInviteSession)mySession).getRemoteDeviceInfo().setOrientation(Orientation.PORTRAIT);
                    								}
                    							}
                    							else if(SgsStringUtils.equals(kvp[0], "lang", true)){
                    								((SgsInviteSession)mySession).getRemoteDeviceInfo().setLang(kvp[1]);
                    							}
                    						}
                    					}
                    					mSipService.broadcastInviteEvent(new SgsInviteEventArgs(session.getId(), SgsInviteEventTypes.REMOTE_DEVICE_INFO_CHANGED, ((SgsInviteSession)mySession).getMediaType(), phrase));
                    				}
                    			}
                    		}
                    	}
                        break;
                    }
                case tsip_m_early_media:
                    {
                    	if (((mySession = SgsAVSession.getSession(session.getId())) != null) || ((mySession = SgsMsrpSession.getSession(session.getId())) != null)){
                    		((SgsInviteSession)mySession).setState(InviteState.EARLY_MEDIA);
                    		mSipService.broadcastInviteEvent(new SgsInviteEventArgs(session.getId(), SgsInviteEventTypes.EARLY_MEDIA, ((SgsInviteSession)mySession).getMediaType(), phrase));
                    	}
                        break;
                    }
                case tsip_m_local_hold_ok:
                    {
                    	if (((mySession = SgsAVSession.getSession(session.getId())) != null) || ((mySession = SgsMsrpSession.getSession(session.getId())) != null)){
                    		((SgsInviteSession)mySession).setLocalHold(true);
                    		mSipService.broadcastInviteEvent(new SgsInviteEventArgs(session.getId(), SgsInviteEventTypes.LOCAL_HOLD_OK, ((SgsInviteSession)mySession).getMediaType(), phrase));
                    	}
                        break;
                    }
                case tsip_m_updating:
	                {
	                	if (((mySession = SgsAVSession.getSession(session.getId())) != null) || ((mySession = SgsMsrpSession.getSession(session.getId())) != null)){
                    		mSipService.broadcastInviteEvent(new SgsInviteEventArgs(session.getId(), SgsInviteEventTypes.MEDIA_UPDATING, ((SgsInviteSession)mySession).getMediaType(), phrase));
                    	}
	                	break;
	                }
                case tsip_m_updated:
	                {
	                	if (((mySession = SgsAVSession.getSession(session.getId())) != null) || ((mySession = SgsMsrpSession.getSession(session.getId())) != null)){
	                		if(mySession instanceof SgsAVSession){
	                			SgsAVSession.handleMediaUpdate(mySession.getId(), e.getMediaType());
	                		}
	                		mSipService.broadcastInviteEvent(new SgsInviteEventArgs(session.getId(), SgsInviteEventTypes.MEDIA_UPDATED, ((SgsInviteSession)mySession).getMediaType(), phrase));
	                	}
	                	break;
	                }
                case tsip_m_local_hold_nok:
                    {
                    	if (((mySession = SgsAVSession.getSession(session.getId())) != null) || ((mySession = SgsMsrpSession.getSession(session.getId())) != null)){
                    		mSipService.broadcastInviteEvent(new SgsInviteEventArgs(session.getId(), SgsInviteEventTypes.LOCAL_HOLD_NOK, ((SgsInviteSession)mySession).getMediaType(), phrase));
                    	}
                        break;
                    }
                case tsip_m_local_resume_ok:
                    {
                    	if (((mySession = SgsAVSession.getSession(session.getId())) != null) || ((mySession = SgsMsrpSession.getSession(session.getId())) != null)){
                    		((SgsInviteSession)mySession).setLocalHold(false);
                    		mSipService.broadcastInviteEvent(new SgsInviteEventArgs(session.getId(), SgsInviteEventTypes.LOCAL_RESUME_OK, ((SgsInviteSession)mySession).getMediaType(), phrase));
                    	}
                        break;
                    }
                case tsip_m_local_resume_nok:
                    {
                    	if (((mySession = SgsAVSession.getSession(session.getId())) != null) || ((mySession = SgsMsrpSession.getSession(session.getId())) != null)){
                    		mSipService.broadcastInviteEvent(new SgsInviteEventArgs(session.getId(), SgsInviteEventTypes.LOCAL_RESUME_NOK, ((SgsInviteSession)mySession).getMediaType(), phrase));
                    	}
                        break;
                    }
                case tsip_m_remote_hold:
                    {
                    	if (((mySession = SgsAVSession.getSession(session.getId())) != null) || ((mySession = SgsMsrpSession.getSession(session.getId())) != null)){
                    		((SgsInviteSession)mySession).setRemoteHold(true);
                    		mSipService.broadcastInviteEvent(new SgsInviteEventArgs(session.getId(), SgsInviteEventTypes.REMOTE_HOLD, ((SgsInviteSession)mySession).getMediaType(), phrase));
                    	}
                        break;
                    }
                case tsip_m_remote_resume:
                    {
                    	if (((mySession = SgsAVSession.getSession(session.getId())) != null) || ((mySession = SgsMsrpSession.getSession(session.getId())) != null)){
                    		((SgsInviteSession)mySession).setRemoteHold(false);
                    		mSipService.broadcastInviteEvent(new SgsInviteEventArgs(session.getId(), SgsInviteEventTypes.REMOTE_RESUME, ((SgsInviteSession)mySession).getMediaType(), phrase));
                    	}
                        break;
                    }
            }
			
			return 0;
		}
		
		@Override
		public int OnMessagingEvent(MessagingEvent e) {
			final tsip_message_event_type_t type = e.getType();
			MessagingSession _session;
			final SipMessage message;
			
			switch(type){
				case tsip_ao_message:
					_session = e.getSession();
					message = e.getSipMessage();
					short code = e.getCode();
					if(_session != null && code>=200 && message != null){
						mSipService.broadcastMessagingEvent(new SgsMessagingEventArgs(_session.getId(), 
								(code >=200 && code<=299) ? SgsMessagingEventTypes.SUCCESS : SgsMessagingEventTypes.FAILURE, 
								e.getPhrase(), new byte[0], null), message.getSipHeaderValue("f"), SgsDateTimeUtils.now());
					}
					break;
				case tsip_i_message:
					message = e.getSipMessage();
					_session = e.getSession();
					SgsMessagingSession imSession;
					if (_session == null){
		             /* "Server-side-session" e.g. Initial MESSAGE sent by the remote party */
						_session = e.takeSessionOwnership();
					}
					
					if(_session == null){
						Log.e(SgsSipService.TAG, "Failed to take session ownership");
						return -1;
					}
					imSession = SgsMessagingSession.takeIncomingSession(mSipService.mSipStack, _session, message);
					if(message == null){
						imSession.reject();
						imSession.decRef();
						return 0;
					}
					
					
					String from = message.getSipHeaderValue("f");
					final String contentType = message.getSipHeaderValue("c");
					final byte[] bytes = message.getSipContent();
					byte[] content = null;
					
					if(bytes == null || bytes.length ==0){
						Log.e(SgsSipService.TAG, "Invalid MESSAGE");
						imSession.reject();
						imSession.decRef();
						return 0;
					}
					
					imSession.accept();
					
					if(SgsStringUtils.equals(contentType, SgsContentType.SMS_3GPP, true)){
						/* ==== 3GPP SMSIP  === */
						ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
						buffer.put(bytes);
						SMSData smsData = SMSEncoder.decode(buffer, buffer.capacity(), false);
                        if (smsData != null){
                            twrap_sms_type_t smsType = smsData.getType();
                            if (smsType == twrap_sms_type_t.twrap_sms_type_rpdata){
                            	/* === We have received a RP-DATA message === */
                                long payLength = smsData.getPayloadLength();
                                String SMSC = message.getSipHeaderValue("P-Asserted-Identity");
                                String SMSCPhoneNumber;
                                String origPhoneNumber = smsData.getOA();
                                
                                /* Destination address */
                                if(origPhoneNumber != null){
                                	from = SgsUriUtils.makeValidSipUri(origPhoneNumber);
                                }
                                else if((origPhoneNumber = SgsUriUtils.getValidPhoneNumber(from)) == null){
                                	Log.e(SgsSipService.TAG, "Invalid destination address");
                                	return 0;
                                }
                                
                                /* SMS Center 
                                 * 3GPP TS 24.341 - 5.3.2.4	Sending a delivery report
                                 * The address of the IP-SM-GW is received in the P-Asserted-Identity header in the SIP MESSAGE 
                                 * request including the delivered short message.
                                 * */
                                if((SMSCPhoneNumber = SgsUriUtils.getValidPhoneNumber(SMSC)) == null){
                                	SMSC = SgsEngine.getInstance().getConfigurationService().getString(SgsConfigurationEntry.RCS_SMSC, SgsConfigurationEntry.DEFAULT_RCS_SMSC);
                                	if((SMSCPhoneNumber = SgsUriUtils.getValidPhoneNumber(SMSC)) == null){
                                		Log.e(SgsSipService.TAG, "Invalid IP-SM-GW address");
                                		return 0;
                                	}
                                }
                                
                                if (payLength > 0) {
                                    /* Send RP-ACK */
                                    RPMessage rpACK = SMSEncoder.encodeACK(smsData.getMR(), SMSCPhoneNumber, origPhoneNumber, false);
                                    if (rpACK != null){
                                        long ack_len = rpACK.getPayloadLength();
                                        if (ack_len > 0){
                                        	buffer = ByteBuffer.allocateDirect((int)ack_len);
                                            long len = rpACK.getPayload(buffer, buffer.capacity());
                                            MessagingSession m = new MessagingSession(mSipService.getSipStack());
                                            m.setToUri(SMSC);
                                            m.addHeader("Content-Type", SgsContentType.SMS_3GPP);
                                            m.addHeader("Content-Transfer-Encoding", "binary");
                                            m.addCaps("+g.3gpp.smsip");
                                            m.send(buffer, len);
                                            m.delete();
                                        }
                                        rpACK.delete();
                                    }

                                    /* Get ascii content */
                                    buffer = ByteBuffer.allocateDirect((int)payLength);
                                    content = new byte[(int)payLength];
                                    smsData.getPayload(buffer, buffer.capacity());
                                    buffer.get(content);
                                }
                                else{
                                    /* Send RP-ERROR */
                                    RPMessage rpError = SMSEncoder.encodeError(smsData.getMR(), SMSCPhoneNumber, origPhoneNumber, false);
                                    if (rpError != null){
                                        long err_len = rpError.getPayloadLength();
                                        if (err_len > 0){
                                        	buffer = ByteBuffer.allocateDirect((int)err_len);
                                            long len = rpError.getPayload(buffer, buffer.capacity());

                                            MessagingSession m = new MessagingSession(mSipService.getSipStack());
                                            m.setToUri(SMSC);
                                            m.addHeader("Content-Type", SgsContentType.SMS_3GPP);
                                            m.addHeader("Transfer-Encoding", "binary");
                                            m.addCaps("+g.3gpp.smsip");
                                            m.send(buffer, len);
                                            m.delete();
                                        }
                                        rpError.delete();
                                    }
                                }
                            }
                            else{
                            	/* === We have received any non-RP-DATA message === */
                            	if(smsType == twrap_sms_type_t.twrap_sms_type_ack){
                            		/* Find message from the history (by MR) an update it's status */
                            		Log.d(SgsSipService.TAG, "RP-ACK");
                            	}
                            	else if(smsType == twrap_sms_type_t.twrap_sms_type_error){
                            		/* Find message from the history (by MR) an update it's status */
                            		Log.d(SgsSipService.TAG, "RP-ERROR");
                            	}
                            }
                        }
					}
					else{
						/* ==== text/plain or any other  === */
						content = bytes;
					}
					
					/* Alert the user and add the message to the history */
					if(content != null){
						mSipService.broadcastMessagingEvent(new SgsMessagingEventArgs(_session.getId(), SgsMessagingEventTypes.INCOMING, 
								e.getPhrase(), content, contentType), from, SgsDateTimeUtils.now());
					}
					
					break;
			}
			
			return 0;
		}

		@Override
		public int OnStackEvent(StackEvent e) {
			//final String phrase = e.getPhrase();
			final short code = e.getCode();
			switch(code){
				case tinyWRAPConstants.tsip_event_code_stack_started:
					mSipService.mSipStack.setState(STACK_STATE.STARTED);
					Log.d(SgsSipService.TAG, "Stack started");
					break;
				case tinyWRAPConstants.tsip_event_code_stack_failed_to_start:
					final String phrase = e.getPhrase();
					Log.e(TAG,String.format("Failed to start the stack. \nAdditional info:\n%s", phrase));
					break;
				case tinyWRAPConstants.tsip_event_code_stack_failed_to_stop:
					Log.e(TAG, "Failed to stop the stack");
					break;
				case tinyWRAPConstants.tsip_event_code_stack_stopped:
					mSipService.mSipStack.setState(STACK_STATE.STOPPED);
					Log.d(TAG, "Stack stopped");
					break;
			}
			return 0;
		}

		@Override
		public int OnSubscriptionEvent(SubscriptionEvent e) {
			final tsip_subscribe_event_type_t type = e.getType();
			SubscriptionSession _session = e.getSession();
			
			switch(type)
			{
				case tsip_i_notify:
				{
					 final short code = e.getCode();
                     final String phrase = e.getPhrase();
                     final SipMessage message = e.getSipMessage();
                     if(message == null || _session == null){
                         return 0;
                     }
                     final String contentType = message.getSipHeaderValue("c");
                     final byte[] content = message.getSipContent();
                     
                     if(SgsStringUtils.equals(contentType, SgsContentType.REG_INFO, true)){
                          //mReginfo = content;
                     }
                     else if(SgsStringUtils.equals(contentType, SgsContentType.WATCHER_INFO, true)){
                          // mWInfo = content;
                     }
                     
                     SgsSubscriptionSession sgsSession = SgsSubscriptionSession.getSession(_session.getId());
                     SgsSubscriptionEventArgs eargs = new SgsSubscriptionEventArgs(_session.getId(), 
                    		 SgsSubscriptionEventTypes.INCOMING_NOTIFY, 
                    		 code, 
                    		 phrase, 
                    		 content, 
                    		 contentType, 
                    		 sgsSession==null ? EventPackageType.None : sgsSession.getEventPackage());
                     mSipService.broadcastSubscriptionEvent(eargs);
                     
                     break;
				}
				
				case tsip_ao_notify:
				case tsip_i_subscribe:
				case tsip_ao_subscribe:
				case tsip_i_unsubscribe:
				case tsip_ao_unsubscribe:
				default:
				{
					break;
				}
			}
			
			return 0;
		}
		
		@Override
		public int OnOptionsEvent(OptionsEvent e) {
			final tsip_options_event_type_t type = e.getType();
			OptionsSession ptSession = e.getSession();

            switch (type){
                case tsip_i_options:
                    if (ptSession == null){ // New session
                        if ((ptSession = e.takeSessionOwnership()) != null){
                            ptSession.accept();
                            ptSession.delete();
                        }
                    }
                    break;
                default:
                    break;
            }
			return 0;
		}
		
	}
}
