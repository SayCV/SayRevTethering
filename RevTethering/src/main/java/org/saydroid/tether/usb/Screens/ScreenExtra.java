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

package org.saydroid.tether.usb.Screens;

import org.saydroid.logger.Log;
import org.saydroid.sgs.events.SgsEventArgs;
import org.saydroid.sgs.events.SgsRegistrationEventArgs;
import org.saydroid.sgs.services.ISgsConfigurationService;
import org.saydroid.sgs.utils.SgsConfigurationEntry;
import org.saydroid.sgs.utils.SgsStringUtils;
import org.saydroid.tether.usb.CustomExtends.NetworkLinkStatus;
import org.saydroid.tether.usb.Events.TrafficCountEventArgs;
import org.saydroid.tether.usb.RootCommands.NetInfo;
import org.saydroid.tether.usb.SRTDroid;
import org.saydroid.tether.usb.R;
import org.saydroid.tether.usb.Services.ITetheringNetworkService;
import org.saydroid.tether.usb.Services.ITetheringService;
import org.saydroid.tether.usb.Services.Impl.TetheringNetworkService;
import org.saydroid.tether.usb.Services.Impl.TetheringService;
import org.sufficientlysecure.rootcommands.Shell;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ScreenExtra extends BaseScreen {
    private static final String TAG = ScreenAbout.class.getCanonicalName();

    private Thread mLinkUpdateThread = null;
    
    private CheckBox mCbEnableUsbTetherConnect;
    private CheckBox mCbEnableMobileDataAndFaked;
    private CheckBox mCbEnableAutoConfigUsbTetherIP;

    private ListView mListView;
    private int mCanSpellableFilesCount;

    private final ISgsConfigurationService mConfigurationService;

    private BroadcastReceiver mLinkUpdateBroadCastRecv;

    public ScreenExtra() {
        super(SCREEN_TYPE.Extra_T, TAG);

        mConfigurationService = getEngine().getConfigurationService();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_manual);

        mListView = (ListView) findViewById(R.id.screen_extra_listView);
        mListView.setAdapter(new ScreenExtraFileAdapter(this));

        mCbEnableUsbTetherConnect = (CheckBox)findViewById(R.id.screen_manual_checkBox_enable_usbTetherConnect);
        mCbEnableMobileDataAndFaked = (CheckBox)findViewById(R.id.screen_manual_checkBox_enable_mobileDataAndFaked);
        mCbEnableAutoConfigUsbTetherIP = (CheckBox)findViewById(R.id.screen_manual_checkBox_enable_autoConfigUsbTetherIP);

        mCbEnableUsbTetherConnect.setChecked(
                mConfigurationService.getBoolean(
                        SgsConfigurationEntry.MANUAL_ENABLE_USB_TETHER_CONNECT,
                        SgsConfigurationEntry.DEFAULT_MANUAL_ENABLE_USB_TETHER_CONNECT));
        mCbEnableMobileDataAndFaked.setChecked(
                mConfigurationService.getBoolean(
                        SgsConfigurationEntry.MANUAL_ENABLE_MOBILE_DATA_AND_FAKED,
                        SgsConfigurationEntry.DEFAULT_MANUAL_ENABLE_MOBILE_DATA_AND_FAKED));
        mCbEnableAutoConfigUsbTetherIP.setChecked(
                mConfigurationService.getBoolean(
                        SgsConfigurationEntry.MANUAL_ENABLE_AUTO_CONFIG_USB_TETHER_IP,
                        SgsConfigurationEntry.DEFAULT_MANUAL_ENABLE_AUTO_CONFIG_USB_TETHER_IP));

        // add listeners (for the configuration)
        super.addConfigurationListener(mCbEnableUsbTetherConnect);
        super.addConfigurationListener(mCbEnableMobileDataAndFaked);
        super.addConfigurationListener(mCbEnableAutoConfigUsbTetherIP);

        mCbEnableUsbTetherConnect.setOnCheckedChangeListener(rbLocal_OnCheckedChangeListener);

        mLinkUpdateBroadCastRecv = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                // Registration Event
                if(SgsRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)){
                    SgsRegistrationEventArgs args = intent.getParcelableExtra(SgsEventArgs.EXTRA_EMBEDDED);
                    if(args == null){
                        Log.e(TAG, "Invalid event args");
                        return;
                    }
                    switch(args.getEventType()){
                        default:
                            //((ScreenNetworkLinkAdapter)mListView.getAdapter()).refresh();
                            break;
                    }
                }
            }
        };
        final IntentFilter intentFilter = new IntentFilter();
        //intentFilter.addAction(SgsRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
        //intentFilter.addAction(TrafficCountEventArgs.ACTION_TRAFFIC_COUNT_EVENT);
        registerReceiver(mLinkUpdateBroadCastRecv, intentFilter);
        //setLinkUpdateThreadClassEnabled(true);

        /*try {
            NetInfo binaryCommand = new NetInfo(null);

            // start root shell
            //Shell shell = Shell.startRootShell();

            //shell.add(binaryCommand);
            shell.add(binaryCommand).waitForFinish();

            Log.d(TAG, "Output of command: " + binaryCommand.getOutput());
            returnCode = binaryCommand.getExitCode();
            sb.append(binaryCommand.getOutput());

            // close root shell
            shell.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception!", e);
        }*/
    }

    private CompoundButton.OnCheckedChangeListener rbLocal_OnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener(){
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(mCbEnableUsbTetherConnect.isChecked()) {
                //
            }
        }
    };

    @Override
    protected void onDestroy() {
        setLinkUpdateThreadClassEnabled(false);
        if(mLinkUpdateBroadCastRecv != null){
            unregisterReceiver(mLinkUpdateBroadCastRecv);
            mLinkUpdateBroadCastRecv = null;
        }

        super.onDestroy();
    }

    protected void onPause() {
        if(super.mComputeConfiguration){


            mConfigurationService.putBoolean(SgsConfigurationEntry.MANUAL_ENABLE_USB_TETHER_CONNECT,
                    mCbEnableUsbTetherConnect.isChecked());
            mConfigurationService.putBoolean(SgsConfigurationEntry.MANUAL_ENABLE_MOBILE_DATA_AND_FAKED,
                    mCbEnableMobileDataAndFaked.isChecked());
            mConfigurationService.putBoolean(SgsConfigurationEntry.MANUAL_ENABLE_AUTO_CONFIG_USB_TETHER_IP,
                    mCbEnableAutoConfigUsbTetherIP.isChecked());

            // Compute
            if(!mConfigurationService.commit()){
                Log.e(TAG, "Failed to commit() configuration");
            }

            super.mComputeConfiguration = false;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void setLinkUpdateThreadClassEnabled(boolean enabled) {
        this.setLinkUpdateThreadClassEnabled(null, enabled);
    }

    public void setLinkUpdateThreadClassEnabled(String[] dns, boolean enabled) {
        if (enabled == true) {
            if (this.mLinkUpdateThread == null || this.mLinkUpdateThread.isAlive() == false) {
                this.mLinkUpdateThread = new Thread(new LinkUpdateThreadClass(dns));
                this.mLinkUpdateThread.start();
            }
        } else {
            if (this.mLinkUpdateThread != null)
                this.mLinkUpdateThread.interrupt();
        }
    }

    // todo
    class LinkUpdateThreadClass implements Runnable {
        String[] dns;

        public LinkUpdateThreadClass(String[] dns) {
            this.dns = dns;
        }
        //@Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {

                // Taking a nap
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    static class ScreenExtraFileAdapter extends BaseAdapter {

        private final LayoutInflater mInflater;
        private final ScreenExtra mBaseScreen;

        ScreenExtraFileAdapter(ScreenExtra baseScreen){
            mInflater = LayoutInflater.from(baseScreen);
            mBaseScreen = baseScreen;
        }

        void refresh(){
            notifyDataSetChanged();
        }

        public int getCount() {
            return mBaseScreen.mCanSpellableFilesCount;
        }

        @Override
        public Object getItem(int position) {
            return sListViewStatusItems[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            final NetworkLinkStatusItem item = (NetworkLinkStatusItem)getItem(position);

            if (view == null) {
                view = mInflater.inflate(R.layout.screen_manual_item_link, null);
            }

            if (item == null) {
                return view;
            }

            ((TextView) convertView.findViewById(android.R.id.text1))
                    .setText(getItem(position));
            return convertView;
        }
    }
}
