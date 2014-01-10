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
import android.net.ConnectivityManager;
import android.os.ConditionVariable;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import org.saydroid.logger.Log;
import org.saydroid.sgs.SgsApplication;
import org.saydroid.sgs.SgsEngine;
import org.saydroid.sgs.events.SgsInviteEventArgs;
import org.saydroid.sgs.events.SgsMessagingEventArgs;
import org.saydroid.sgs.events.SgsMessagingEventTypes;
import org.saydroid.sgs.events.SgsPublicationEventArgs;
import org.saydroid.sgs.events.SgsRegistrationEventArgs;
import org.saydroid.sgs.events.SgsRegistrationEventTypes;
import org.saydroid.sgs.events.SgsSubscriptionEventArgs;
import org.saydroid.sgs.services.ISgsConfigurationService;
import org.saydroid.sgs.services.ISgsNetworkService;
import org.saydroid.sgs.services.impl.SgsBaseService;
import org.saydroid.sgs.sip.SgsPresenceStatus;
import org.saydroid.sgs.sip.SgsRegistrationSession;
import org.saydroid.sgs.utils.SgsConfigurationEntry;
import org.saydroid.sgs.utils.SgsDateTimeUtils;
import org.saydroid.tether.usb.Engine;
import org.saydroid.tether.usb.Events.TetheringErrorsEventTypes;
import org.saydroid.tether.usb.Events.TrafficCountEventArgs;
import org.saydroid.tether.usb.MainActivity;
import org.saydroid.tether.usb.Services.ITetheringNetworkService;
import org.saydroid.tether.usb.Services.ITetheringService;
import org.saydroid.tether.usb.Tethering.TetheringPrefrences;
import org.saydroid.tether.usb.Tethering.TetheringRegistrationSession;
import org.saydroid.tether.usb.Tethering.TetheringSession;
import org.saydroid.tether.usb.Tethering.TetheringSession.ConnectionState;
import org.saydroid.tether.usb.Tethering.TetheringStack;
import org.saydroid.tether.usb.Tethering.TetheringStack.STACK_STATE;

public class TetheringService extends SgsBaseService
implements ITetheringService {
	private final static String TAG = TetheringService.class.getCanonicalName();
	
	private TetheringRegistrationSession mRegSession;
	private TetheringStack mTetheringStack;
	private final TetheringPrefrences mPreferences;
	
	private final ISgsConfigurationService mConfigurationService;
	private final ITetheringNetworkService mTetheringNetworkService;
	
	private ConditionVariable mCondHackAoR;

	public TetheringService() {
		super();
		
		//mSipCallback = new MySipCallback(this);
		mPreferences = new TetheringPrefrences();
		
		mConfigurationService = SgsEngine.getInstance().getConfigurationService();
        mTetheringNetworkService = ((Engine)Engine.getInstance()).getTetheringNetworkService();
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
			return mRegSession.getConnectionState();
		}
		return ConnectionState.NONE;
	}

    public void setRegistrationState(ConnectionState state){
        if (mRegSession != null) {
            mRegSession.setConnectionState(state);
        }
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
		if(mTetheringStack != null && (mTetheringStack.getState() == STACK_STATE.STARTING)){
            mTetheringStack.stop();
		}
        stopTether();
		return false;
	}

	@Override
	public boolean register(Context context) {
		Log.d(TAG,"register()");
		mPreferences.setMobileNetworkEnabled(mConfigurationService.getBoolean(SgsConfigurationEntry.NETWORK_USE_3G,
                SgsConfigurationEntry.DEFAULT_NETWORK_USE_3G));
		mPreferences.setMobileNetworkFaked(mConfigurationService.getBoolean(SgsConfigurationEntry.NETWORK_USE_FAKED_3G,
                SgsConfigurationEntry.DEFAULT_NETWORK_USE_FAKED_3G));
		mPreferences.setLocalIP(mConfigurationService.getString(SgsConfigurationEntry.NETWORK_LOCAL_IP,
                SgsConfigurationEntry.DEFAULT_NETWORK_LOCAL_IP_WINXP));
        mPreferences.setSubMask(mConfigurationService.getString(SgsConfigurationEntry.NETWORK_SUB_MASK,
                SgsConfigurationEntry.DEFAULT_NETWORK_SUB_MASK));
        mPreferences.setGateWay(mConfigurationService.getString(SgsConfigurationEntry.NETWORK_GATE_WAY,
                SgsConfigurationEntry.DEFAULT_NETWORK_GATE_WAY_WINXP));
        mPreferences.setPreferredDNS(mConfigurationService.getString(SgsConfigurationEntry.NETWORK_PREFERRED_DNS,
                SgsConfigurationEntry.DEFAULT_NETWORK_PREFERRED_DNS));
        mPreferences.setSecondaryDNS(mConfigurationService.getString(SgsConfigurationEntry.NETWORK_SECONDARY_DNS,
                SgsConfigurationEntry.DEFAULT_NETWORK_SECONDARY_DNS));



		Log.d(TAG, String.format(
				"3G='%s', Faked 3G='%s'" +
                        "ip='%s'" +
                        "sub mask='%s'" +
                        "gate way='%s'" +
                        "dns1='%s'" +
                        "dns2='%s'",
                mPreferences.isMobileNetworkEnabled()?"on":"off",
                mPreferences.isMobileNetworkFaked()?"on":"off",
                mPreferences.getLocalIP(),
                mPreferences.getSubMask(),
                mPreferences.getGateWay(),
                mPreferences.getPreferredDNS(),
                mPreferences.getSecondaryDNS()));
		
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
		// Create registration session
        // Create registration session
        if (mRegSession == null) {
            mRegSession = new TetheringRegistrationSession(mTetheringStack);
        }
		/* Before registering, check if AoR hacking id enabled */
		if (startTether() != 0) {
			Log.e(TAG, "Failed to startTether request");
			return false;
		}
        if (!mRegSession.register()) {
            Log.e(TAG, "Failed to send REGISTER request");
            return false;
        }

        // Start the Stack
        if (!mTetheringStack.start()) {
            if(context != null && Thread.currentThread() == Looper.getMainLooper().getThread()){
                Toast.makeText(context, "Failed to start the Tethering stack", Toast.LENGTH_LONG).show();
            }
            Log.e(TAG, "Failed to start the Tethering stack");
            return false;
        }

		return true;
	}

    public boolean reRegister(Context context) {
        if (isRegistered()) {
            return true;
        } else {
            if (mTetheringStack == null) {
                mTetheringStack = new TetheringStack();
                //mTetheringStack.setDebugCallback(new DDebugCallback());
            }

            if (mRegSession == null) {
                mRegSession = new TetheringRegistrationSession(mTetheringStack);
            }
            String usbIface = mTetheringStack.getTetheredIfaces();
            Log.d(TAG, "Found usbIface: " + (usbIface == null ? "null" : usbIface));
            if(usbIface == null){
                stopTether();
                mRegSession.unregister();
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        stop();
                        //mTetheringStack.stop();
                    }
                }).start();
                return false;
            }
            //((TetheringNetworkService) mTetheringNetworkService).setTetherableIfaces(usbIface);
            mRegSession.setTetheringNetworkDevice(usbIface);
            if (!mRegSession.register()) {
                Log.e(TAG, "Failed to send REPEAT REGISTER request");
                return false;
            }
            mRegSession.setConnectionState(ConnectionState.CONNECTED);
        }
        return true;
    }

	@Override
	public boolean unRegister() {
		if (isRegistered()) {
            mRegSession.unregister();
            stopTether();
			new Thread(new Runnable(){
				@Override
				public void run() {
                    stop();
                    //mTetheringStack.stop();
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

    public void broadcastTrafficCountEvent(TrafficCountEventArgs args, String date){
        final Intent intent = new Intent(TrafficCountEventArgs.ACTION_TRAFFIC_COUNT_EVENT);
        /*intent.putExtra(TrafficCountEventArgs.EXTRA_DATA_COUNT_TOTAL_UPLOAD, dataCount.totalUpload);
        intent.putExtra(TrafficCountEventArgs.EXTRA_DATA_COUNT_TOTAL_DOWNLOAD, dataCount.totalDownload);
        intent.putExtra(TrafficCountEventArgs.EXTRA_DATA_COUNT_UPLOAD_RATE, dataCount.uploadRate);
        intent.putExtra(TrafficCountEventArgs.EXTRA_DATA_COUNT_DOWNLOAD_RATE, dataCount.downloadRate);*/
        intent.putExtra(TrafficCountEventArgs.EXTRA_DATE, date);
        intent.putExtra(TrafficCountEventArgs.EXTRA_EMBEDDED, args);
        SgsApplication.getContext().sendBroadcast(intent);
    }

    public void broadcastRegistrationEvent(SgsRegistrationEventArgs args){
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

    public int startTether() {
        String message;
    	/*
    	 * ReturnCodes:
    	 *    0 = All OK, Service started
    	 *    1 = Mobile-Data-Connection not established (not used at the moment)
    	 *    2 = Fatal error
    	 */

        broadcastRegistrationEvent(new SgsRegistrationEventArgs(0, SgsRegistrationEventTypes.REGISTRATION_INPROGRESS, (short)0, null));
        // check if usb is plugged
        if (!((TetheringNetworkService)mTetheringNetworkService).isUsbConnected()) {
            /*Toast.makeText(MainActivity.currentInstance,
                    "usb is not pluged, retry",
                    Toast.LENGTH_LONG).show();*/
            message = "usb is not pluged, retry ";
            Log.d(TAG, message);
            // Sending message
            Message msg = new Message();
            msg.obj = message;
            ((Engine)Engine.getInstance()).displayMessageHandler.sendMessage(msg);
            broadcastRegistrationEvent(new SgsRegistrationEventArgs(0, SgsRegistrationEventTypes.REGISTRATION_NOK, (short)0, null));
            return 2; //MESSAGE_USB_ACTION_DETACH
        }

        // pre turn on Settings USB Tethering
        // error return TETHER_ERROR_SERVICE_UNAVAIL
        // if(((TetheringNetworkService)mTetheringNetworkService).setUsbTetheringEnabled(true)) {
        if(((TetheringNetworkService)mTetheringNetworkService).setSystemUsbTetherEnabled(true) == false ) {
            message = "Unable to set sys.usb.config: rndis,adb";
            Log.d(TAG, message);
            // Sending message
            Message msg = new Message();
            msg.obj = message;
            ((Engine)Engine.getInstance()).displayMessageHandler.sendMessage(msg);
            broadcastRegistrationEvent(new SgsRegistrationEventArgs(0, SgsRegistrationEventTypes.REGISTRATION_NOK, (short)0, null));
            return 2;
        }
        ((TetheringNetworkService)mTetheringNetworkService).waitForFinish(1000);
        String usbIface = mTetheringStack.getTetherableIfaces();
        Log.d(TAG, "Found usbIface: " + (usbIface == null ? "null" : usbIface));

        ((TetheringNetworkService) mTetheringNetworkService).setTetherableIfaces(usbIface);
        mRegSession.setTetheringNetworkDevice(usbIface);


       /* if (usbIface == null) {
            // updateState();
            //MainActivity.currentInstance.openNoUSBIfaceDialog(); cause bug, cannot change UI inside child thread
            Message msg = Message.obtain();
            msg.what = MainActivity.MESSAGE_NO_USB_INTERFACE;
            msg.obj = "No tetherable usb inteface found ...but not assume as rndis0";
            MainActivity.currentInstance.viewUpdateHandler.sendMessage(msg);
            //usbIface = new String("rndis0");
        }*/


        // Starting service

        //has to use Integer
        if (mTetheringStack.setTetherableIfacesEnabled(usbIface) == 0) {
            //clientConnectEnable is mainly a separate thread to handle the dhcp leasing
            //this is not required for reverse tethering jason-12Apri2012
            //this.clientConnectEnable(true);

            //this.trafficCounterEnable(true);

            // Update resolv.conf-file
            //Move to after starttether, because android internal tether will set its own DNS.
            String dns[] = ((TetheringNetworkService) mTetheringNetworkService).getSystemDnsServer();
            //((TetheringNetworkService) mTetheringNetworkService).setDnsUpdateThreadClassEnabled(dns, true);

            String network[] = new String[3];
            network[0] = mPreferences.getLocalIP();
            network[1] = mPreferences.getGateWay();
            network[2] = mPreferences.getSubMask();

            ((TetheringNetworkService) mTetheringNetworkService).setIpConfigureThreadClassEnabled(network, true);

            if(Engine.getInstance().getConfigurationService().getBoolean(
                    SgsConfigurationEntry.NETWORK_USE_3G, SgsConfigurationEntry.DEFAULT_NETWORK_USE_3G)) {
                ((TetheringNetworkService) mTetheringNetworkService).setMobileNetworkEnabled(true);
            }
            if(Engine.getInstance().getConfigurationService().getBoolean(
                    SgsConfigurationEntry.NETWORK_USE_FAKED_3G, SgsConfigurationEntry.DEFAULT_NETWORK_USE_FAKED_3G)) {
                ((TetheringNetworkService) mTetheringNetworkService).setMobileNetworkFakedEnabled(true);
            }

            //debug here
            message = "tethering started ...";
            Log.d(TAG, message);
            // Sending message
            Message msg = new Message();
            msg.obj = message;
            ((Engine)Engine.getInstance()).displayMessageHandler.sendMessage(msg);

            // Acquire Wakelock
            //SgsApplication.getInstance().acquirePowerLock();
            if(!Engine.getInstance().getConfigurationService().getBoolean(
                    SgsConfigurationEntry.GENERAL_DWL, SgsConfigurationEntry.DEFAULT_GENERAL_DWL)) {
                SgsApplication.getInstance().acquireWakeLock();
            }

            //indicate the tether_stop is not valid
            //this.tetherStopped = -1;
            mRegSession.setConnectionState(ConnectionState.CONNECTED);
            Engine.getInstance().getConfigurationService().putBoolean(SgsConfigurationEntry.NETWORK_CONNECTED, true);
            Engine.getInstance().getConfigurationService().commit();
            broadcastRegistrationEvent(new SgsRegistrationEventArgs(0, SgsRegistrationEventTypes.REGISTRATION_OK, (short)0, null));
            return 0;
        }
        mRegSession.setConnectionState(ConnectionState.NONE);
        broadcastRegistrationEvent(new SgsRegistrationEventArgs(0, SgsRegistrationEventTypes.REGISTRATION_NOK, (short)0, null));
        return 2;//Enum.valueOf(TetheringErrorsEventTypes.class, "TETHER_ERROR_SERVICE_UNAVAIL").ordinal();
    }

    public boolean stopTether() {
        String message;

        if(mRegSession.getConnectionState() == ConnectionState.TERMINATED) { return true; }
        broadcastRegistrationEvent(new SgsRegistrationEventArgs(0, SgsRegistrationEventTypes.UNREGISTRATION_INPROGRESS, (short)0, null));
        String usbIface = mTetheringStack.getTetherableIfaces();

        ((TetheringNetworkService) mTetheringNetworkService).setTetherableIfaces(usbIface);
        mTetheringStack.setTetherableIfacesDisabled(usbIface);

        if(((TetheringNetworkService)mTetheringNetworkService).setSystemUsbTetherEnabled(false) == false ) {
            message = "Unable to set sys.usb.config: mtp,adb";
            Log.d(TAG, message);
            // Sending message
            Message msg = new Message();
            msg.obj = message;
            ((Engine)Engine.getInstance()).displayMessageHandler.sendMessage(msg);
        }
        ((TetheringNetworkService)mTetheringNetworkService).waitForFinish(1000);
        ((TetheringNetworkService) mTetheringNetworkService).setMobileNetworkEnabled(false);

        //((TetheringNetworkService) mTetheringNetworkService).setTrafficCounterThreadClassEnabled(false);

        ((TetheringNetworkService) mTetheringNetworkService).setDnsUpdateThreadClassEnabled(false);
        ((TetheringNetworkService) mTetheringNetworkService).setIpConfigureThreadClassEnabled(false);

        message = "tethering stopped ...";
        Log.d(TAG, message);
        // Sending message
        Message msg = new Message();
        msg.obj = message;
        ((Engine)Engine.getInstance()).displayMessageHandler.sendMessage(msg);

        // Release Wakelock
        //SgsApplication.getInstance().releasePowerLock();
        SgsApplication.getInstance().releaseWakeLock();

        mRegSession.setConnectionState(ConnectionState.TERMINATED);
        Engine.getInstance().getConfigurationService().putBoolean(SgsConfigurationEntry.NETWORK_CONNECTED, false);
        Engine.getInstance().getConfigurationService().commit();
        broadcastRegistrationEvent(new SgsRegistrationEventArgs(0, SgsRegistrationEventTypes.UNREGISTRATION_OK, (short)0, null));
        return true;
    }
}
