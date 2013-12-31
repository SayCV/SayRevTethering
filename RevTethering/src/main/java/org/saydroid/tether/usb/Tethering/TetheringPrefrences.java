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

public class TetheringPrefrences {
	private boolean mPresence;
    private boolean mMobileNetwork;
    private boolean mMobileNetworkFaked;
    private String mIPVersion;
    private String mLocalIP;
    private String mSubMask;
    private String mGateWay;
    private String mPreferredDNS;
    private String mSecondaryDNS;
    
    public TetheringPrefrences(){
    	
    }
    
    public void setPresenceEnabled(boolean enabled) {
		this.mPresence = enabled;
	}
	public boolean isPresenceEnabled() { return mPresence; }

    public void setIPVersion(String IPVersion) { this.mIPVersion = IPVersion; }
    public String getIPVersion() { return mIPVersion; }

    public void setMobileNetworkEnabled(boolean enabled) { this.mMobileNetwork = enabled; }
    public boolean isMobileNetworkEnabled() { return mMobileNetwork; }

    public void setMobileNetworkFaked(boolean enabled) { this.mMobileNetworkFaked = enabled; }
    public boolean isMobileNetworkFaked() { return mMobileNetworkFaked; }

	public void setLocalIP(String localIP) {
		this.mLocalIP = localIP;
	}
	public String getLocalIP() { return mLocalIP; }

    public void setSubMask(String subMask) {
        this.mSubMask = subMask;
    }
    public String getSubMask() {
        return mSubMask;
    }

    public void setGateWay(String gateWay) {
        this.mGateWay = gateWay;
    }
    public String getGateWay() {
        return mGateWay;
    }

    public void setPreferredDNS(String preferredDNS) {
        this.mPreferredDNS = preferredDNS;
    }
    public String getPreferredDNS() {
        return mPreferredDNS;
    }

    public void setSecondaryDNS(String secondaryDNS) {
        this.mSecondaryDNS = secondaryDNS;
    }
    public String getSecondaryDNS() {
        return mSecondaryDNS;
    }
}
