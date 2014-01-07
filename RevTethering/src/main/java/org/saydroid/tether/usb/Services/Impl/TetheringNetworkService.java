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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import org.saydroid.logger.Log;
import org.saydroid.rootcommands.RootCommands;
import org.saydroid.sgs.SgsApplication;
import org.saydroid.sgs.SgsEngine;
import org.saydroid.sgs.model.SgsAccessPoint;
import org.saydroid.sgs.services.ISgsNetworkService;
import org.saydroid.sgs.services.impl.SgsBaseService;
import org.saydroid.sgs.utils.SgsConfigurationEntry;
import org.saydroid.sgs.utils.SgsFileUtils;
import org.saydroid.sgs.utils.SgsObservableList;
import org.saydroid.sgs.utils.SgsStringUtils;
import org.saydroid.tether.usb.Engine;
import org.saydroid.tether.usb.MainActivity;
import org.saydroid.tether.usb.Services.ITetheringNetworkService;
import org.saydroid.tether.usb.Services.Impl.TetheringNetworkService.DNS_TYPE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.GroupCipher;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager.WifiLock;
import android.os.BatteryManager;
import android.os.Message;
import android.support.v7.appcompat.R;
import android.telephony.TelephonyManager;
import android.widget.Toast;

/**@page SgsNetworkService_page Network Service
 * The network service is used to manage both WiFi and 3g/4g network connections.
 */

/**
 * Network service.
 */
public class TetheringNetworkService  extends SgsBaseService implements ITetheringNetworkService {
	private static final String TAG = TetheringNetworkService.class.getCanonicalName();

    private static final String DATA_FOLDER = String.format("/data/data/%s", MainActivity.class.getPackage().getName());
	private static final String USB_INTERFACE_NAME = "rndis0";
	
	private WifiManager mWifiManager;
	private WifiLock mWifiLock;
    private ConnectivityManager mConnectivityManager;

	private String mConnetedSSID;
	private boolean mAcquired;
	private boolean mStarted;
	private boolean mScanning;
	private BroadcastReceiver mNetworkWatcher;

    private Thread mTrafficCounterThread = null;
    private Thread mDnsUpdateThread = null;
    private Thread mIpConfigureThread = null;

    private String mUsbInterface = null;
	
	public static final int[] sWifiSignalValues = new int[] {
        0,
        1,
        2,
        3,
        4
    };
	
	// Will be added in froyo SDK
	private static int ConnectivityManager_TYPE_WIMAX = 6;
	
	public static enum DNS_TYPE {
		DNS_1, DNS_2, DNS_3, DNS_4
	}
	
	public TetheringNetworkService() {
		super();
	}
	
	@Override
	public boolean start() {
		Log.d(TAG, "Starting...");
        mConnectivityManager = (ConnectivityManager) SgsApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if(mConnectivityManager == null){
			Log.e(TAG, "Connectivity manager is Null");
			return false;
		}

		mStarted = true;
		return true;
	}

	@Override
	public boolean stop() {
		Log.d(TAG, "Stopping...");
		if(!mStarted){
			Log.w(TAG, "Not started...");
			return false;
		}
		
		if(mNetworkWatcher != null){
			SgsApplication.getContext().unregisterReceiver(mNetworkWatcher);
			mNetworkWatcher = null;
		}
		
		release();
		mStarted = false;
		return true;
	}
	
	@Override
	public boolean setNetworkEnabledAndRegister() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setNetworkEnabled(String SSID, boolean enabled, boolean force) {
		return true;//setNetworkEnabled(getNetworkIdBySSID(SSID), enabled, force);
	}
	
	@Override
	public boolean setNetworkEnabled(int networkId, boolean enabled, boolean force){
		Log.d(TAG, "setNetworkEnabled(" + enabled + ")");
		
		if(mWifiManager == null){
			Log.e(TAG, "WiFi manager is Null");
			return false;
		}
		
		final boolean useWifi = SgsEngine.getInstance().getConfigurationService().getBoolean(
				SgsConfigurationEntry.NETWORK_USE_WIFI, SgsConfigurationEntry.DEFAULT_NETWORK_USE_WIFI);

		if (useWifi) {
			boolean ret = false;
			if ((force || !mWifiManager.isWifiEnabled()) && enabled) {
				Toast.makeText(SgsApplication.getContext(), "Trying to start WiFi",
						Toast.LENGTH_SHORT).show();
				ret = mWifiManager.setWifiEnabled(true);
				if (ret && networkId>=0) {
					ret = mWifiManager.enableNetwork(networkId, true);
				}
			} else if ((force || mWifiManager.isWifiEnabled()) && !enabled) {
				Toast.makeText(SgsApplication.getContext(), "Trying to stop WiFi",
						Toast.LENGTH_SHORT).show();
				ret = mWifiManager.setWifiEnabled(false);
				if (ret && networkId>=0) {
					ret = mWifiManager.disableNetwork(networkId);
				}
			}
			return ret;
		}
		else{
			Log.w(TAG, "setNetworkEnabled() is called but WiFi not enabled");
		}
		return false;
	}

	@Override
	public boolean forceConnectToNetwork() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public int configure(SgsAccessPoint ap, String password, boolean bHex){
		if(ap == null){
			Log.e(TAG, "Null AccessPoint");
			return -1;
		}
		else if(ap.isConfigured()){
			Log.w(TAG, "AccessPoint already configured");
			return -1;
		}
		else if(ap.getSR() == null){
			Log.e(TAG, "Null SR");
		}
		else if(mWifiManager == null){
			Log.e(TAG, "Null WifiManager");
			return -1;
		}
		
		final ScanResult sr = ap.getSR();
		WifiConfiguration wConf = new WifiConfiguration();
		//http://developer.android.com/reference/android/net/wifi/WifiConfiguration.html#SSID
		wConf.SSID = "\"" + sr.SSID + "\"";
		wConf.BSSID = sr.BSSID;
		wConf.priority = 40;
		String security = SgsAccessPoint.getScanResultSecurity(sr);
		if(security == SgsAccessPoint.AP_WEP){
			wConf.wepKeys[0] = bHex ? password : SgsStringUtils.quote(password, "\"");//hex not quoted
            
			wConf.wepTxKeyIndex = 0;
            
            wConf.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
            wConf.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);

            wConf.allowedKeyManagement.set(KeyMgmt.NONE);
            
            wConf.allowedGroupCiphers.set(GroupCipher.WEP40);
            wConf.allowedGroupCiphers.set(GroupCipher.WEP104);
		}
		else if(security == SgsAccessPoint.AP_WPA || security == SgsAccessPoint.AP_WPA2){
			wConf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);  
			wConf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);  
			wConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);  
			wConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);  
			wConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);  
			wConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);  
			wConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);  
			wConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);  
			wConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);  
			wConf.preSharedKey = "\"".concat("mamadoudiop").concat("\""); 
		}
		else if(security == SgsAccessPoint.AP_OPEN){
			wConf.allowedKeyManagement.set(KeyMgmt.NONE);
		}
		return mWifiManager.addNetwork(wConf);
	}
	
	@Override
	public boolean scan(){
		
		Toast.makeText(SgsApplication.getContext(), "Network Scanning...", Toast.LENGTH_SHORT).show();
		
		if(mNetworkWatcher == null){
			IntentFilter intentNetWatcher = new IntentFilter();
            // intentNetWatcher.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            // intentNetWatcher.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
            // intentNetWatcher.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
            // intentNetWatcher.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			// intentNetWatcher.addAction(WifiManager.RSSI_CHANGED_ACTION);
            intentNetWatcher.addAction(Intent.ACTION_BATTERY_CHANGED);
			mNetworkWatcher = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					handleNetworkEvent(context, intent);
				}
			};
			SgsApplication.getContext().registerReceiver(mNetworkWatcher, intentNetWatcher);
		}
		
		mScanning = true;
		/*if(mWifiManager.setWifiEnabled(true)){
			return mWifiManager.reassociate();
		}*/
		return true;
	}
	
	@Override
	public boolean acquire() {
		if (mAcquired) {
			return true;
		}
		
		Log.d(TAG, "acquireNetworkLock()");

		boolean connected = false;
		NetworkInfo networkInfo = SgsApplication.getConnectivityManager().getActiveNetworkInfo();
		if (networkInfo == null) {
			Log.e(TetheringNetworkService.TAG, "Failed to get Network information");
			return false;
		}

		int netType = networkInfo.getType();
		int netSubType = networkInfo.getSubtype();

		Log.d(TetheringNetworkService.TAG, String.format("netType=%d and netSubType=%d",
				netType, netSubType));

		boolean useWifi = SgsEngine.getInstance().getConfigurationService().getBoolean(SgsConfigurationEntry.NETWORK_USE_WIFI, 
				SgsConfigurationEntry.DEFAULT_NETWORK_USE_WIFI);
		boolean use3G = SgsEngine.getInstance().getConfigurationService().getBoolean(SgsConfigurationEntry.NETWORK_USE_3G,
				SgsConfigurationEntry.DEFAULT_NETWORK_USE_3G);

		if (useWifi && (netType == ConnectivityManager.TYPE_WIFI)) {
			if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
				mWifiLock = mWifiManager.createWifiLock(
						WifiManager.WIFI_MODE_FULL, TetheringNetworkService.TAG);
				final WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
				if (wifiInfo != null && mWifiLock != null) {
					final DetailedState detailedState = WifiInfo
							.getDetailedStateOf(wifiInfo.getSupplicantState());
					if (detailedState == DetailedState.CONNECTED
							|| detailedState == DetailedState.CONNECTING
							|| detailedState == DetailedState.OBTAINING_IPADDR) {
						mWifiLock.acquire();
						mConnetedSSID = wifiInfo.getSSID();
						connected = true;
					}
				}
			} else {
				Log.d(TetheringNetworkService.TAG, "WiFi not enabled");
			}
		} else if (use3G
				&& (netType == ConnectivityManager.TYPE_MOBILE || netType == ConnectivityManager_TYPE_WIMAX)) {
			if ((netSubType >= TelephonyManager.NETWORK_TYPE_UMTS)
					|| // HACK
					(netSubType == TelephonyManager.NETWORK_TYPE_GPRS)
					|| (netSubType == TelephonyManager.NETWORK_TYPE_EDGE)) {
				//Toast.makeText(WiPhone.getContext(),
				//		"Using 2.5G (or later) network", Toast.LENGTH_SHORT)
				//		.show();
				connected = true;
			}
		}

		if (!connected) {
			Log.d(TetheringNetworkService.TAG, "No active network");
			return false;
		}

		mAcquired = true;
		return true;
	}

	@Override
	public boolean release() {

		mAcquired = false;
		return true;
	}

    public boolean isUsbConnected(){
        return isUsbPlugged();
    }

    private boolean isUsbPlugged() {
        Intent intent = SgsApplication.getContext().registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        if (plugged == BatteryManager.BATTERY_PLUGGED_USB) {
            return true;
        } else {
            return false;
        }
    }
	
	private void loadConfiguredNetworks(){
	}
	
	private void handleNetworkEvent(Context context, Intent intent){
		final String action = intent.getAction();
		Log.d(TAG, "NetworkService::BroadcastReceiver(" + action + ")");
		
		/*if(mWifiManager == null){
			Log.e(TAG, "Invalid state");
			return;
		}*/
		
		if(Intent.ACTION_BATTERY_CHANGED.equals(action)){
            final int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            Log.d(TAG, "Intent.ACTION_BATTERY_CHANGED="+plugged);
            if (plugged == BatteryManager.BATTERY_PLUGGED_USB) {

            } else {

            }
            updateConnectionState();
        }
	}
	
	private void updateConnectionState(){
		final WifiInfo wInfo = mWifiManager.getConnectionInfo();
		boolean bAtLeastOneConnected = false;

		
		if(bAtLeastOneConnected || !SgsEngine.getInstance().getSipService().isRegistered()){
			triggerSipRegistration();
		}
		
	}
	
	private void triggerSipRegistration(){
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				Log.d(TAG, "Network connection chaged: restart the stack");
//				final ISipService sipService = ServiceManager.getSipService();
//				final ConnectionState registrationState = sipService.getRegistrationState();
//				switch(registrationState){
//					case NONE:
//					case TERMINATED:
//						sipService.register(null);
//						break;
//					case CONNECTING:
//					case TERMINATING:
//					case CONNECTED:
//						sipService.unRegister();
//						sipService.register(null);
//						break;
//				}
//			}
//		}).start();
	}

    public boolean setTetherableIfaces(String usbInterface) {
        mUsbInterface = usbInterface;
        return true;
    }

    public boolean setSystemUsbTetherEnabled(boolean enabled) {
        //
        String command;
        String usbTetherOn = "rndis,adb";
        String usbTetherOff = "mtp,adb";
        if(enabled) {
            command = "setprop sys.usb.config "+usbTetherOn;
        } else {
            command = "setprop sys.usb.config "+usbTetherOff;
        }
        if(RootCommands.run(command)==false){
            Log.e(TAG, "Unable to set sys.usb.config: " + enabled);
            return false;
        }
        return true;
    }

    public synchronized String[] getSystemDnsServer() {
        StringBuilder sb = new StringBuilder();
        String dns[] = new String[2];
        String command;
        command = "getprop net.dns1";
        if(RootCommands.run(command, sb)==false){
            Log.e(TAG, "Unable to get net.dns1");
            return null;
        }
        dns[0] = sb.toString();
        command = "getprop net.dns2";
        if(RootCommands.run(command, sb)==false){
            Log.e(TAG, "Unable to get net.dns2");
            return null;
        }
        dns[1] = sb.toString();
        if (dns[0] == null || dns[0].length() <= 0 || dns[0].equals("undefined")) {
            dns[0] = SgsConfigurationEntry.DEFAULT_NETWORK_PREFERRED_DNS;
        }
        if (dns[1] == null || dns[1].length() <= 0 || dns[1].equals("undefined")) {
            dns[1] = SgsConfigurationEntry.DEFAULT_NETWORK_SECONDARY_DNS;
        }
        return dns;
    }

    public synchronized String[] setSystemDnsServer(String dns1, String dns2) {
        String dns[] = new String[2];
        String command;
        if(dns1 == null) {
            dns1 = SgsConfigurationEntry.DEFAULT_NETWORK_PREFERRED_DNS;
        }
        command = "setprop net.dns1 " + dns1;
        if(RootCommands.run(command)==false){
            Log.e(TAG, "Unable to set net.dns1 as dns of " + dns1);
            return null;
        }

        if(dns2 == null) {
            dns2 = SgsConfigurationEntry.DEFAULT_NETWORK_SECONDARY_DNS;
        }
        command = "setprop net.dns2 " + dns2;
        if(RootCommands.run(command)==false){
            Log.e(TAG, "Unable to set net.dns2 as dns of " + dns2);
            return null;
        }
        dns[0] = dns1;
        dns[1] = dns2;
        return dns;
    }

    public void setMobileNetworkEnabled(boolean enabled){
        String[] available = null;
        ConnectivityManager cm = SgsApplication.getConnectivityManager();
        Method setMobileDataEnabledLocal = null;
        try {
            setMobileDataEnabledLocal = cm.getClass().getMethod("setMobileDataEnabled", boolean.class);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d(TAG, "setMobileDataEnabled method got security exception ...");
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            setMobileDataEnabledLocal.invoke(cm, enabled);
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
        Log.d(TAG, "setMobileDataEnabledLocal returned value is: " + (enabled ? "Enable" : "Disable"));

        if(enabled) {
            NetworkInfo wifiNetworkInfo, mobileNetworkInfo;
            boolean finished = false;
            synchronized (this) {
                while (!finished) {
                    try {
                        this.wait(1000);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "InterruptedException in v()", e);
                    }
                    mobileNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                    if(mobileNetworkInfo.isConnected()) {
                        finished = true;
                        //terminate("Timeout");
                        Log.d(TAG, "setMobileDataEnabled() Timeout has occurred.");
                    }
                }
            }
        }
    }

    public boolean setMobileNetworkFakedEnabled(boolean enabled) {
        // TODO Auto-generated method stub
        String command;
        String rmnetIface = "rmnet0";
        String rmnetIpAddr = "0.0.0.0";

        if(enabled) {
            command = this.DATA_FOLDER + "/bin/ifconfig "+ rmnetIface + " " + rmnetIpAddr;//"up" ;
        } else {
            command = this.DATA_FOLDER + "/bin/ifconfig "+ rmnetIface + " " + "down" ;
        }
        Log.d(TAG, "command to " + (enabled ? "up " : "down ") + rmnetIface + " the is :" + command);
        if(RootCommands.run(command)==false){
            Log.e(TAG, "Unable to " + (enabled ? "up " : "down ") + rmnetIface);

            return false;
        }
        Log.d(TAG, "----" + (enabled ? "up " : "down ") + rmnetIface + " success---");
        return true;
    }

    public boolean ifConfigUpInterface(String usbIf) {
        // TODO Auto-generated method stub
        String command;
        command = this.DATA_FOLDER + "/bin/ifconfig "+ usbIf + " up" ;
        Log.d(TAG, "command to up the " + usbIf + " is :" + command);
        if(RootCommands.run(command)==false){
            Log.d(TAG, "Unable to up " + usbIf);

            return false;
        }
        Log.d(TAG, "----up " + usbIf + " success---");
        return true;
    }


    public boolean ifConfigSetInterface(String usbIf, String ip, String subMask ) {
        // TODO Auto-generated method stub
        String command;
        command = this.DATA_FOLDER + "/bin/ifconfig " + usbIf + " " + ip + " netmask " + subMask;
        Log.d(TAG, "command to setup the " + usbIf + " is :" + command);
        if(RootCommands.run(command)==false){
            Log.d(TAG, "Unable to setup usbIface with ip address of " + ip);
            Log.d(TAG, "Unable to setup usbIface with sub mask of " + subMask);
            return false;
        }
        return true;
    }


    public boolean ifConfigSetGW(String usbIf, String network) {
        // TODO Auto-generated method stub
        String command;
        command = this.DATA_FOLDER + "/bin/route add default gw " + network + " " + usbIf;
        Log.d(TAG, "command to setup the gateway of " + network + " is :" + command);
        if(RootCommands.run(command)==false){
            Log.e(TAG, "Unable to setup gateway with ip address of " + network);
            return false;
        }
        return true;
    }

    public boolean dumpDefaultGW(){
        String dumpDefaultGW = this.DATA_FOLDER + "/bin/route  > " + this.DATA_FOLDER + "/conf/route.conf";
        Log.d(TAG, "command for dumping the default gateway is: " + dumpDefaultGW);
        if(RootCommands.run(dumpDefaultGW)==false){
            Log.e(TAG, "Unable to dump the route output to " + this.DATA_FOLDER + "/var/route.out");
            return false;
        }
        return true;
    }

    public String[] getCurrentGW( ){
        String currentGWandIface[] = new String[2];

        String filename = this.DATA_FOLDER + "/conf/route.conf";
        File inFile = new File(filename);
        if (inFile.exists() == true) {
            ArrayList<String> inputLines = SgsFileUtils.readLinesFromFile(filename);
            for (String line : inputLines) {
                if (line.startsWith("default")) {
                    String[] routeOutPuts = line.split(" ");
                    currentGWandIface[0] = routeOutPuts[1];  //ip of gateway
                    currentGWandIface[1] = routeOutPuts[routeOutPuts.length-1]; //interface of gateway
                    break;
                }

            }
        }

        if (currentGWandIface[0] == null || currentGWandIface[0].length() <= 0) {
            currentGWandIface[0] = "undefined";
        }
        if (currentGWandIface[1] == null || currentGWandIface[1].length() <= 0) {
            currentGWandIface[1] = "undefined";
        }
        return currentGWandIface;
    }

    public void setDnsUpdateThreadClassEnabled(boolean enabled) {
        this.setDnsUpdateThreadClassEnabled(null, enabled);
    }

    public void setDnsUpdateThreadClassEnabled(String[] dns, boolean enabled) {
        if (enabled == true) {
            if (this.mDnsUpdateThread == null || this.mDnsUpdateThread.isAlive() == false) {
                this.mDnsUpdateThread = new Thread(new DnsUpdateThreadClass(dns));
                this.mDnsUpdateThread.start();
            }
        } else {
            if (this.mDnsUpdateThread != null)
                this.mDnsUpdateThread.interrupt();
        }
    }

    // Move to after starttether, because android internal tether will set its own DNS.
    class DnsUpdateThreadClass implements Runnable {
        String[] dns;

        public DnsUpdateThreadClass(String[] dns) {
            this.dns = dns;
        }
        //@Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                String[] currentDns = getSystemDnsServer();
                if (this.dns == null || currentDns == null || this.dns[0].equals(currentDns[0]) == false || this.dns[1].equals(currentDns[1]) == false) {
                    //this.dns = updateResolvConf();
                    this.dns = setSystemDnsServer(this.dns[0], this.dns[1]);
                    //Log.d(TAG, "set dns1: " + this.dns[0]);
                    //Log.d(TAG, "set dns2: " + this.dns[1]);
                }
                // Taking a nap
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void setIpConfigureThreadClassEnabled(boolean enabled) {
        this.setIpConfigureThreadClassEnabled(null, enabled);
    }


    public void setIpConfigureThreadClassEnabled(String[] network, boolean enable) {
        if (enable == true) {
            if (this.mIpConfigureThread == null || this.mIpConfigureThread.isAlive() == false) {
                this.mIpConfigureThread = new Thread(new IpConfigureThreadClass(network));
                this.mIpConfigureThread.start();
            }
        } else {
            if (this.mIpConfigureThread != null)
                this.mIpConfigureThread.interrupt();
        }
    }

    class IpConfigureThreadClass implements Runnable {

        String[] network;
        String message = null;
        boolean again = true;

        public IpConfigureThreadClass(String[] network) {
            this.network = network;
        }

        //@Override
    	/*
    	 * cannot simply copy over the updateDNS function, it is ok to keep updating dns,
    	 * but the ifconfig only need setup once.
    	 */
        public void run() {
            while( again ) {
                //while (!Thread.currentThread().isInterrupted()) {
                //String[] currentDns = TetherApplication.this.coretask.getCurrentDns();//current means current system setting, not setting inside file.
                //if (this.dns == null || this.dns[0].equals(currentDns[0]) == false || this.dns[1].equals(currentDns[1]) == false) {
                if(ifConfigUpInterface(mUsbInterface)){ //ifconfig usb up command execution
                    if(ifConfigSetInterface(mUsbInterface, network[0], network[2])){
                        if(dumpDefaultGW()) {
                            String[] currentGW = getCurrentGW();
                            if ((currentGW[0].equals(network[1])==false)  || (currentGW[1].equals(mUsbInterface)==false)){
                                if(ifConfigSetGW(mUsbInterface, network[1])) {
                                    message = "ifconfig setup success";
                                    again = false;
                                } else {
                                    message = "cannot set default gate way";
                                }
                            } else {
                                message = "existing gateway is already correct";
                            }
                        } else {
                            message = "cannot dump system gate way";
                        }
                    } else { //ifConfigSetInterface(mUsbInterface, network)){
                        message = "cannot set ifconfig inteface";
                    }
                } else {
                    message = "cannot up usb interface";
                }

                Log.d(TAG, message);
                // Sending message
                Message msg = new Message();
                msg.obj = message + (again == false ? "" : ", will try again!!1");
                ((Engine)Engine.getInstance()).displayMessageHandler.sendMessage(msg);
                //((Engine)Engine.getInstance()).showAppMessage("Found error when ifconfig " + mUsbInterface);
                // Taking a nap
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }// while( again )
        }
    }

    public void waitForFinish(int timeout) {
        synchronized (this) {
            while (true) {
                try {
                    this.wait(timeout);
                } catch (InterruptedException e) {
                    Log.e(TAG, "InterruptedException in waitForFinish()", e);
                }
                return;
            }
        }
    }

}
