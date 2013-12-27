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

package org.saydroid.tether.usb.Services;

import android.content.Context;

import org.saydroid.sgs.services.ISgsBaseService;
import org.saydroid.tether.usb.Tethering.TetheringPrefrences;
import org.saydroid.tether.usb.Tethering.TetheringSession.ConnectionState;
import org.saydroid.tether.usb.Tethering.TetheringStack;


public interface ITetheringService extends ISgsBaseService {
	String getDefaultIdentity();
	void setDefaultIdentity(String identity);
	/**
	 * Gets the underlaying SIP/IMS stack managed by this service. This function should only be called after
	 * successful registration.
	 * @return a valid SIP/IMS stack if succeed and null otherwise
	 * @sa @ref register()
	 */
    TetheringStack getTetheringStack();
    /**
     * Checks whether we are already registered or not.
     * @return
     */
    boolean isRegistered();
    /**
     * Gets the registration state
     * @return the registration state
     */
    ConnectionState getRegistrationState();
    boolean isXcapEnabled();
    boolean isPublicationEnabled();
    boolean isSubscriptionEnabled();
    boolean isSubscriptionToRLSEnabled();
    /**
     * Gets the list of all active codecs
     * @return the list of all active codecs
     */
    int getCodecs();
    /**
     * Sets the list of all active codecs
     * @param coddecs the new codecs to activate
     */
    void setCodecs(int coddecs);

    byte[] getSubRLSContent();
    byte[] getSubRegContent();
    byte[] getSubMwiContent();
    byte[] getSubWinfoContent();

    /**
     * Stops the SIP/IMS stack. Before stopping the stack we the engine will hangup all calls and 
     * shutdown all active sip sessions.
     * @return true if succeed and false otherwise
     */
    boolean stopStack();
    /**
     * Sends a Sip REGISTER request to the Proxy-CSCF
     * @param context the context associated to this request. Could be null.
     * @return true if succeed and false otherwise
     * @sa @ref unRegister()
     */
    boolean register(Context context);
    /**
     * Deregisters the user by sending a Sip REGISTER request with an expires value equal to zero
     * @return true if succeed and false otherwise
     * @sa register
     */
    boolean unRegister();

    boolean PresencePublish();
    boolean PresencePublish(TetheringPrefrences status);
}
