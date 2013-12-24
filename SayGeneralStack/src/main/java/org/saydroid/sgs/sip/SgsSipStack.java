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
package org.saydroid.sgs.sip;

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
public class SgsSipStack extends SipStack{

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
	public SgsSipStack(SipCallback callback, String realmUri, String impiUri, String impuUri){
		super(callback, realmUri, impiUri, impuUri);
		
		// Services
		mNetworkService = SgsEngine.getInstance().getNetworkService();
		
		// Set first and second DNS servers (used for DNS NAPTR+SRV discovery and ENUM)
		String dnsServer;
		if((dnsServer = mNetworkService.getDnsServer(DNS_TYPE.DNS_1)) != null && !dnsServer.equals("0.0.0.0")){
			this.addDnsServer(dnsServer);
			if((dnsServer = mNetworkService.getDnsServer(DNS_TYPE.DNS_2)) != null && !dnsServer.equals("0.0.0.0")){
				this.addDnsServer(dnsServer);
			}
		}
		else{
			// On the emulator FIXME
			this.addDnsServer("212.27.40.241");
		}
		
	     // Sip headers
        super.addHeader("Allow", "INVITE, ACK, CANCEL, BYE, MESSAGE, OPTIONS, NOTIFY, PRACK, UPDATE, REFER");
        super.addHeader("Privacy", "none");
        super.addHeader("P-Access-Network-Info", "ADSL;utran-cell-id-3gpp=00000000");
        super.addHeader("User-Agent", String.format("IM-client/OMA1.0 android-sgs-stack/v%s (doubango r%s - %s)", 
				SgsApplication.getVersionName(), 
				SgsApplication.getContext().getString(R.string.sgs_revision),
				Build.MODEL));
	}

	@Override
	public boolean start() {
		if(mNetworkService.acquire()){
			mState = STACK_STATE.STARTING;
			return super.start();
		}
		else{
			return false;
		}
	}

	@Override
	public boolean stop() {
		mNetworkService.release();
		mState = STACK_STATE.STOPPING;
		return super.stop();
	}
	
	public void setState(STACK_STATE state){
		mState = state;
	}
	
	public STACK_STATE getState(){
		return mState;
	}
	
	public String getSigCompId(){
		return mCompId;
	}
	
	public void setSigCompId(String compId){
		if(mCompId != null && mCompId != compId){
			super.removeSigCompCompartment(mCompId);
		}
		if((mCompId = compId) != null){
			super.addSigCompCompartment(mCompId);
		}
	}
}
