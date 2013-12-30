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

package org.saydroid.tether.usb.Tethering;

import org.saydroid.logger.Log;
import org.saydroid.rootcommands.RootCommands;
import org.saydroid.sgs.SgsApplication;
import org.saydroid.sgs.SgsEngine;
import org.saydroid.sgs.R;
import org.saydroid.sgs.services.ISgsNetworkService;
import org.saydroid.tether.usb.Services.Impl.TetheringNetworkService.DNS_TYPE;
import org.saydroid.tether.usb.Engine;
import org.saydroid.tether.usb.Services.ITetheringNetworkService;
import org.saydroid.tinyWRAP.SipCallback;
import org.saydroid.tinyWRAP.SipStack;

import android.os.Build;

/**
 * SIP/IMS Stack
 */
public class TetheringStack {
    private static final String TAG = TetheringStack.class.getCanonicalName();

	public enum STACK_STATE {
	     NONE, STARTING, STARTED, STOPPING, STOPPED
	}
	
	private STACK_STATE mState = STACK_STATE.NONE;
	private String mCompId;
    private Engine mEngine;
	private final ITetheringNetworkService mTetheringNetworkService;

    private String mSettingDataBasePath;
	
	/**
	 * Creates new SIP/IMS Stack. You should use
	 *
	 */
	public TetheringStack(){
		//super(callback, realmUri, impiUri, impuUri);

		// Services
        mTetheringNetworkService = ((Engine)Engine.getInstance()).getTetheringNetworkService();
		
		// Set first and second DNS servers (used for DNS NAPTR+SRV discovery and ENUM)
		String dnsServer;
		if((dnsServer = mTetheringNetworkService.getDnsServer(DNS_TYPE.DNS_1)) != null && !dnsServer.equals("0.0.0.0")){
			//this.addDnsServer(dnsServer);
			if((dnsServer = mTetheringNetworkService.getDnsServer(DNS_TYPE.DNS_2)) != null && !dnsServer.equals("0.0.0.0")){
				//this.addDnsServer(dnsServer);
			}
		}
		else{
			// On the emulator FIXME
			//this.addDnsServer("212.27.40.241");
		}
		
	     // Sip headers

	}

	public boolean start() {
		if(mTetheringNetworkService.acquire()){
			mState = STACK_STATE.STARTING;
			return true;
		} else {
			return false;
		}
	}

	public boolean stop() {
        mTetheringNetworkService.release();
		mState = STACK_STATE.STOPPING;
		return true;
	}
	
	public void setState(STACK_STATE state){
		mState = state;
	}
	
	public STACK_STATE getState(){
		return mState;
	}

    public boolean isValid(){
        return true;
    }
	
	public String getSigCompId(){
		return mCompId;
	}
	
	public void setSigCompId(String compId){
		if(mCompId != null && mCompId != compId){
			//super.removeSigCompCompartment(mCompId);
		}
		if((mCompId = compId) != null){
			//super.addSigCompCompartment(mCompId);
		}
	}




}
