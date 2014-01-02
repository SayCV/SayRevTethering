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
import org.saydroid.tether.usb.MainActivity;
import org.saydroid.tether.usb.Services.Impl.TetheringNetworkService;
import org.saydroid.tether.usb.Services.Impl.TetheringNetworkService.DNS_TYPE;
import org.saydroid.tether.usb.Engine;
import org.saydroid.tether.usb.Services.ITetheringNetworkService;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
    private final TetheringPrefrences mPreferences;

    private String mUsbInterface = null;
    private String[] mUsbRegexs = null;

	/**
	 * Creates new SIP/IMS Stack. You should use
	 *
	 */
	public TetheringStack(){
		//super(callback, realmUri, impiUri, impuUri);
        mPreferences = new TetheringPrefrences();

		// Services
        mTetheringNetworkService = ((Engine)Engine.getInstance()).getTetheringNetworkService();
		
		// Set first and second DNS servers (used for DNS NAPTR+SRV discovery and ENUM)

		
	     // Sip headers
        getTetherableUsbRegexs();

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

    public String getTetherableIfaces(){
        getTetherableUsbRegexs();
        if(mUsbRegexs == null) {
            Log.e(TAG, "mUsbRegexs is null!");
        }
        getTetherableIfaces(mUsbRegexs);
        return mUsbInterface;
    }

    private String findIface(String[] ifaces, String[] regexes) {
        for (String iface : ifaces) {
            for (String regex : regexes) {
                if (iface.matches(regex)) {
                    return iface;
                }
            }
        }
        return null;
    }

    public void getTetherableUsbRegexs(){
        //String[] usbRegexs = null;
        //ArrayList mUsbIfaces;
        ConnectivityManager cm = SgsApplication.getConnectivityManager();
        Method getTetherableUsbRegexsLocal = null;
        try {
            getTetherableUsbRegexsLocal = cm.getClass().getMethod("getTetherableUsbRegexs");
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "cannot get method of getTetherableUsbRegexs, security exception");
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "cannot get method of getTetherableUsbRegexs, no such method exception");
            e.printStackTrace();
        }
        try {
            mUsbRegexs = (String [])getTetherableUsbRegexsLocal.invoke(cm);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void getTetherableIfaces(String[] usbRegexs){
        String[] available = null;
        ConnectivityManager cm = SgsApplication.getConnectivityManager();
        Method getTetherableIfacesLocal = null;
        try {
            getTetherableIfacesLocal = cm.getClass().getMethod("getTetherableIfaces");
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d(TAG, "getTetherableIfaces got security exception ...");
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            available = (String [])getTetherableIfacesLocal.invoke(cm);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.d(TAG, "getTetherableIfaces got ..." + (available.length > 0 ? available[0].toString() : "NULL"));

        mUsbInterface = findIface(available, usbRegexs);
    }

    public int setTetherableIfacesEnabled(String usbIf){
        int tetherStarted = -1;
        ConnectivityManager cm = SgsApplication.getConnectivityManager();
        Method tetherLocal = null;
        try {
            tetherLocal = cm.getClass().getMethod("tether", String.class);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d(TAG, "tether method got security exception ...");
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            tetherStarted = (Integer)tetherLocal.invoke(cm, usbIf);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.d(TAG, "tetherlocal returned value is: " + tetherStarted);
        return tetherStarted;
    }

    public int setTetherableIfacesDisabled(String usbIf){
        int tetherStopped = -1;
        ConnectivityManager cm = SgsApplication.getConnectivityManager();
        Method getTetheredIfacesLocal = null;
        try {
            getTetheredIfacesLocal = cm.getClass().getMethod("getTetheredIfaces");
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String[] tethered = null;
        try {
            tethered = (String [])getTetheredIfacesLocal.invoke(cm);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.d(TAG, "stopTether() getTetheredIfaces got ..." + (tethered.length > 0 ? tethered[0].toString() : "NULL"));
        //tethered = new String[] {"rndis0"};
        usbIf = findIface(tethered, mUsbRegexs);
        if (usbIf == null) {
            //TO DO : return with a pop up message
            //MainActivity.currentInstance.openNoUSBIfaceDialog();
            /*Message msg = Message.obtain();
            msg.what = MainActivity.MESSAGE_NO_USB_INTERFACE;
            msg.obj = "No tetherable usb inteface found ...";
            MainActivity.currentInstance.viewUpdateHandler.sendMessage(msg);*/

        }
        Method untetherLocal = null;
        try {
            untetherLocal = cm.getClass().getMethod("untether", String.class);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            tetherStopped = (Integer)untetherLocal.invoke(cm, usbIf);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return tetherStopped;
    }
}
