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

import org.saydroid.sgs.SgsApplication;
import org.saydroid.sgs.SgsEngine;
import org.saydroid.sgs.R;
import org.saydroid.sgs.services.ISgsNetworkService;
import org.saydroid.sgs.services.impl.SgsNetworkService.DNS_TYPE;
import org.saydroid.tinyWRAP.SipCallback;
import org.saydroid.tinyWRAP.SipStack;

import android.os.Build;

/**
 * SIP/IMS Stack
 */
public class TetheringStack {

	public enum STACK_STATE {
	     NONE, STARTING, STARTED, STOPPING, STOPPED
	}
	
	private STACK_STATE mState = STACK_STATE.NONE;
	private String mCompId;
	private final ISgsNetworkService mNetworkService;
	
	/**
	 * Creates new SIP/IMS Stack. You should use
	 * @param callback
	 * @param realmUri
	 * @param impiUri
	 * @param impuUri
	 */
	public TetheringStack(SipCallback callback, String realmUri, String impiUri, String impuUri){
		//super(callback, realmUri, impiUri, impuUri);
		
		// Services
		mNetworkService = SgsEngine.getInstance().getNetworkService();
		
		// Set first and second DNS servers (used for DNS NAPTR+SRV discovery and ENUM)
		String dnsServer;
		if((dnsServer = mNetworkService.getDnsServer(DNS_TYPE.DNS_1)) != null && !dnsServer.equals("0.0.0.0")){
			//this.addDnsServer(dnsServer);
			if((dnsServer = mNetworkService.getDnsServer(DNS_TYPE.DNS_2)) != null && !dnsServer.equals("0.0.0.0")){
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
		if(mNetworkService.acquire()){
			mState = STACK_STATE.STARTING;
			return true;
		}
		else{
			return false;
		}
	}

	public boolean stop() {
		mNetworkService.release();
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
