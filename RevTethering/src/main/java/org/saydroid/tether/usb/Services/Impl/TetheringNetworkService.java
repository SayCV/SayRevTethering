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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
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
import org.saydroid.sgs.utils.SgsObservableList;
import org.saydroid.sgs.utils.SgsStringUtils;
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
	private final SgsObservableList<SgsAccessPoint> mAccessPoints;
	private BroadcastReceiver mNetworkWatcher;
	
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
		
		mAccessPoints = new SgsObservableList<SgsAccessPoint>(true);
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
	public String getDnsServer(DNS_TYPE type) {
		String dns = null;
		switch (type) {
			case DNS_1: default: dns = "dns1"; break;
			case DNS_2: dns = "dns2"; break;
			case DNS_3: dns = "dns3"; break;
			case DNS_4: dns = "dns4"; break;
		}

		if (mWifiManager != null) {
			String[] dhcpInfos = mWifiManager.getDhcpInfo().toString().split(" ");
			int i = 0;

			while (i++ < dhcpInfos.length) {
				if (dhcpInfos[i - 1].equals(dns)) {
					return dhcpInfos[i];
				}
			}
		}
		return null;
	}

	@Override
	public String getLocalIP(boolean ipv6) {
		final HashMap<String, String> addressMap = new HashMap<String, String>();
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					Log.d(TetheringNetworkService.TAG, inetAddress.getHostAddress().toString());
					if (!inetAddress.isLoopbackAddress()) {
						if (((inetAddress instanceof Inet4Address) && !ipv6) || ((inetAddress instanceof Inet6Address) && ipv6)) {
							addressMap.put(intf.getName(), inetAddress.getHostAddress().toString());
						}
					}
				}
			}
			if(addressMap.size() > 0){
				final String openvpn = addressMap.get(USB_INTERFACE_NAME);
				if(!SgsStringUtils.isNullOrEmpty(openvpn)){
					return openvpn;
				}
				return addressMap.values().iterator().next();
			}
		} catch (SocketException ex) {
			Log.e(TetheringNetworkService.TAG, ex.toString());
		}

		// Hack
		try {
			java.net.Socket socket = new java.net.Socket(ipv6 ? "ipv6.google.com" : "google.com", 80);
			Log.d(TetheringNetworkService.TAG, socket.getLocalAddress().getHostAddress());
			return socket.getLocalAddress().getHostAddress();
		} catch (UnknownHostException e) {
			Log.e(TetheringNetworkService.TAG, e.toString());
		} catch (IOException e) {
			Log.e(TetheringNetworkService.TAG, e.toString());
		}

		return null;
	}

	@Override
	public boolean isScanning(){
		return mScanning;
	}
	
	@Override
	public boolean setNetworkEnabledAndRegister() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setNetworkEnabled(String SSID, boolean enabled, boolean force) {
		return setNetworkEnabled(getNetworkIdBySSID(SSID), enabled, force);
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
	public SgsObservableList<SgsAccessPoint> getAccessPoints(){
		return mAccessPoints;
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
		if(mWifiManager == null){
			Log.e(TAG,"WiFi manager is Null");
			return false;
		}
		
		Toast.makeText(SgsApplication.getContext(), "Network Scanning...", Toast.LENGTH_SHORT).show();
		
		if(mNetworkWatcher == null){
			IntentFilter intentNetWatcher = new IntentFilter();
			intentNetWatcher.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
			intentNetWatcher.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
			intentNetWatcher.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
			intentNetWatcher.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			intentNetWatcher.addAction(WifiManager.RSSI_CHANGED_ACTION);
			mNetworkWatcher = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					handleNetworkEvent(context, intent);
				}
			};
			SgsApplication.getContext().registerReceiver(mNetworkWatcher, intentNetWatcher);
		}
		
		mScanning = true;
		if(mWifiManager.setWifiEnabled(true)){
			return mWifiManager.reassociate();
		}
		return false;
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
		if (mWifiLock != null) {
			if(mWifiLock.isHeld()){
				Log.d(TAG, "releaseNetworkLock()");
				mWifiLock.release();
			}	
			mWifiLock = null;
		}

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

	private int getNetworkIdBySSID(String SSID) {
		synchronized(mAccessPoints){
			final SgsAccessPoint ap = getAccessPointBySSID(SSID);
			if(ap != null){
				return ap.getNetworkId();
			}
			return -1;
		}
	}

	@SuppressWarnings("unused")
	private WifiConfiguration getWifiConfBySSID(String SSID) {
		synchronized(mAccessPoints){
			final SgsAccessPoint ap = getAccessPointBySSID(SSID);
			if(ap != null){
				return ap.getConf();
			}
			return null;
		}
	}
	
	private SgsAccessPoint getAccessPointBySSID(String SSID) {
		final List<SgsAccessPoint> accessPoints = mAccessPoints.getList();
		for (SgsAccessPoint ap : accessPoints) {
			String SSID1 = SgsStringUtils.unquote(ap.getSSID(), "\"");
			String SSID2 = SgsStringUtils.unquote(SSID, "\"");
			if (SSID1.equalsIgnoreCase(SSID2)) {
				return ap;
			}
		}
		return null;
	}
	
	private void loadConfiguredNetworks(){
		synchronized(mAccessPoints){
			mAccessPoints.clear();
			final List<WifiConfiguration> confNetworks = mWifiManager.getConfiguredNetworks();
			for (WifiConfiguration wifiConf : confNetworks) {
				SgsAccessPoint ap = new SgsAccessPoint(wifiConf);
				ap.setConnected(SgsStringUtils.equals(mConnetedSSID, ap.getSSID(), false));
				mAccessPoints.add(ap);
			}
		}
	}
	
	private void handleNetworkEvent(Context context, Intent intent){
		final String action = intent.getAction();
		Log.d(TAG, "NetworkService::BroadcastReceiver(" + action + ")");
		
		if(mWifiManager == null){
			Log.e(TAG, "Invalid state");
			return;
		}
		
		if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
			mScanning = true;
			// load() configured networks
			loadConfiguredNetworks();
			// load() network results
			synchronized(mAccessPoints){
				List<ScanResult> scanResults = mWifiManager.getScanResults();
				for(ScanResult sr : scanResults){
					SgsAccessPoint ap = getAccessPointBySSID(sr.SSID);
					if(ap == null){
						ap = new SgsAccessPoint(sr);
						mAccessPoints.add(ap);
					}
				}
			}
			
			updateConnectionState();
			mScanning = false;
		}
		else if(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION.equals(action)){
			final boolean connected = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
			Log.d(TAG, "SUPPLICANT_CONNECTION_CHANGE_ACTION.CONNECTED="+connected);
			if(connected){
				final WifiInfo wInfo = mWifiManager.getConnectionInfo();
				if(wInfo != null){
					if(!SgsStringUtils.equals(mConnetedSSID, wInfo.getSSID(), false)){
						triggerSipRegistration();
					}
					mConnetedSSID = wInfo.getSSID();
				}
			}
			updateConnectionState();
//			synchronized(mAccessPoints){
//				final List<AccessPoint> aps = mAccessPoints.getList();
//				for(AccessPoint ap : aps){
//					ap.setConnected(connected && StringUtils.equals(mConnetedSSID, ap.getSSID(), false));
//				}
//			}
		}
		else if(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)){
			updateConnectionState();
//			final SupplicantState newState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
//			if(newState != null){
//				synchronized(mAccessPoints){
//					final List<AccessPoint> aps = mAccessPoints.getList();
//					final WifiInfo wInfo = mWifiManager.getConnectionInfo();
//					if(wInfo != null){
//						for(AccessPoint ap : aps){
//							ap.setConnected((newState == SupplicantState.ASSOCIATED) && StringUtils.equals(wInfo.getSSID(), ap.getSSID(), false));
//						}
//					}
//				}
//			}
		}
		else if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)){
			updateConnectionState();
//			final boolean connected = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
//				== WifiManager.WIFI_STATE_ENABLED;
//			synchronized(mAccessPoints){
//				final List<AccessPoint> aps = mAccessPoints.getList();
//				final WifiInfo wInfo = mWifiManager.getConnectionInfo();
//				if(wInfo != null){
//					for(AccessPoint ap : aps){
//						ap.setConnected(connected && StringUtils.equals(wInfo.getSSID(), ap.getSSID(), false));
//					}
//				}
//			}
		}
		else if(WifiManager.RSSI_CHANGED_ACTION.equals(action)){
			final WifiInfo wInfo = mWifiManager.getConnectionInfo();
			if(wInfo != null){
				final SgsAccessPoint ap = getAccessPointBySSID(wInfo.getSSID());
				if(ap != null){
					final int newRssi = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -200);
					ap.setLevel(WifiManager.calculateSignalLevel(newRssi,
							sWifiSignalValues.length));
				}
			}
		}
	}
	
	private void updateConnectionState(){
		final WifiInfo wInfo = mWifiManager.getConnectionInfo();
		boolean bAtLeastOneConnected = false;
		if(wInfo != null){
			final DetailedState detailedState = WifiInfo
				.getDetailedStateOf(wInfo.getSupplicantState());
			boolean isConnecting = detailedState == DetailedState.CONNECTED
			|| detailedState == DetailedState.CONNECTING
			|| detailedState == DetailedState.OBTAINING_IPADDR;
			synchronized(mAccessPoints){
				final List<SgsAccessPoint> aps = mAccessPoints.getList();
				if(wInfo != null){
					for(SgsAccessPoint ap : aps){
						final boolean connected = isConnecting && SgsStringUtils.equals(wInfo.getSSID(), ap.getSSID(), false);
						ap.setConnected(connected);
						bAtLeastOneConnected |= connected;
					}
				}
			}
		}
		
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
        command = "setprop net.dns1 " + dns1;
        if(RootCommands.run(command)==false){
            Log.e(TAG, "Unable to set net.dns1 as dns of " + dns1);
            return null;
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

    public boolean setSystemMobileDataEnable(boolean enable) {
        // TODO Auto-generated method stub
        String command;
        String rmnetIface = "rmnet0";
        String rmnetIpAddr = "0.0.0.0";

        if(enable) {
            command = this.DATA_FOLDER + "/bin/ifconfig "+ rmnetIface + " " + rmnetIpAddr;//"up" ;
        } else {
            command = this.DATA_FOLDER + "/bin/ifconfig "+ rmnetIface + " " + "down" ;
        }
        Log.d(TAG, "command to " + (enable ? "up " : "down ") + rmnetIface + " the is :" + command);
        if(RootCommands.run(command)==false){
            Log.e(TAG, "Unable to " + (enable ? "up " : "down ") + rmnetIface);

            return false;
        }
        Log.d(TAG, "----" + (enable ? "up " : "down ") + rmnetIface + " success---");
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


    public boolean ifConfigSetInterface(String usbIf, String [] network ) {
        // TODO Auto-generated method stub
        String command;
        command = this.DATA_FOLDER + "/bin/ifconfig " + usbIf + " " + network[0] + " netmask " +
                network[1];
        Log.d(TAG, "command to setup the " + usbIf + " is :" + command);
        if(RootCommands.run(command)==false){
            Log.d(TAG, "Unable to setup usbIface with ip address of " + network[0]);
            Log.d(TAG, "Unable to setup usbIface with sub mask of " + network[1]);
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

    class DnsUpdate implements Runnable {
        String[] dns;

        public DnsUpdate(String[] dns) {
            this.dns = dns;
        }
        //@Override
        public void run() {

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
