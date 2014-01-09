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

import org.saydroid.tether.usb.R;
import org.saydroid.sgs.services.ISgsConfigurationService;
import org.saydroid.sgs.utils.SgsConfigurationEntry;
import org.saydroid.sgs.utils.SgsStringUtils;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import org.saydroid.logger.Log;

public class ScreenNetwork extends BaseScreen {
	private final static String TAG = ScreenNetwork.class.getCanonicalName();
	
	private final ISgsConfigurationService mConfigurationService;

    private CheckBox mCbFaked3G;
	private CheckBox mCb3G;
	private RadioButton mRbIPv4;
	private RadioButton mRbIPv6;
    private RadioButton mRbStyleWINXP;
    private RadioButton mRbStyleWIN7;
    private EditText mEtLocalIP;
    private EditText mEtSubMask;
    private EditText mEtGateWay;
    private EditText mEtPreferredDNS;
    private EditText mEtSecondaryDNS;

	public ScreenNetwork() {
		super(SCREEN_TYPE.NETWORK_T, TAG);
		
		this.mConfigurationService = getEngine().getConfigurationService();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_network);

        mCbFaked3G = (CheckBox)findViewById(R.id.screen_network_checkBox_faked_3g_ip);
        mCb3G = (CheckBox)findViewById(R.id.screen_network_checkBox_3g);
        mRbIPv4 = (RadioButton)findViewById(R.id.screen_network_radioButton_ipv4);
        mRbIPv6 = (RadioButton)findViewById(R.id.screen_network_radioButton_ipv6);
        mRbStyleWINXP = (RadioButton)findViewById(R.id.screen_network_radioButton_ip_style_winxp);
        mRbStyleWIN7 = (RadioButton)findViewById(R.id.screen_network_radioButton_ip_style_win7);
        mEtLocalIP = (EditText)findViewById(R.id.screen_network_textView_local_ip);
        mEtSubMask = (EditText)findViewById(R.id.screen_network_editText_sub_mask);
        mEtGateWay = (EditText)findViewById(R.id.screen_network_editText_gateway);
        mEtPreferredDNS = (EditText)findViewById(R.id.screen_network_editText_preferred_dns);
        mEtSecondaryDNS = (EditText)findViewById(R.id.screen_network_editText_secondary_dns);


        mCbFaked3G.setChecked(mConfigurationService.getBoolean(SgsConfigurationEntry.NETWORK_USE_FAKED_3G, SgsConfigurationEntry.DEFAULT_NETWORK_USE_FAKED_3G));
        mCb3G.setChecked(mConfigurationService.getBoolean(SgsConfigurationEntry.NETWORK_USE_3G, SgsConfigurationEntry.DEFAULT_NETWORK_USE_3G));
        mRbIPv4.setChecked(mConfigurationService.getString(SgsConfigurationEntry.NETWORK_IP_VERSION,
        		SgsConfigurationEntry.DEFAULT_NETWORK_IP_VERSION).equalsIgnoreCase("ipv4"));
        mRbIPv6.setChecked(!mRbIPv4.isChecked());
        mRbStyleWINXP.setChecked(mConfigurationService.getString(SgsConfigurationEntry.NETWORK_IP_STYLE,
                SgsConfigurationEntry.DEFAULT_NETWORK_IP_STYLE).equalsIgnoreCase("ip_style_winxp"));
        mRbStyleWIN7.setChecked(!mRbStyleWINXP.isChecked());
        if(mRbStyleWINXP.isChecked()) {
            mEtLocalIP.setText(mConfigurationService.getString(SgsConfigurationEntry.NETWORK_LOCAL_IP, SgsConfigurationEntry.DEFAULT_NETWORK_LOCAL_IP_WINXP));
            mEtGateWay.setText(mConfigurationService.getString(SgsConfigurationEntry.NETWORK_GATE_WAY, SgsConfigurationEntry.DEFAULT_NETWORK_GATE_WAY_WINXP));
        } else {
            mEtLocalIP.setText(mConfigurationService.getString(SgsConfigurationEntry.NETWORK_LOCAL_IP, SgsConfigurationEntry.DEFAULT_NETWORK_LOCAL_IP_WIN7));
            mEtGateWay.setText(mConfigurationService.getString(SgsConfigurationEntry.NETWORK_GATE_WAY, SgsConfigurationEntry.DEFAULT_NETWORK_GATE_WAY_WIN7));
        }
        mEtSubMask.setText(mConfigurationService.getString(SgsConfigurationEntry.NETWORK_SUB_MASK, SgsConfigurationEntry.DEFAULT_NETWORK_SUB_MASK));
        mEtPreferredDNS.setText(mConfigurationService.getString(SgsConfigurationEntry.NETWORK_PREFERRED_DNS, SgsConfigurationEntry.DEFAULT_NETWORK_PREFERRED_DNS));
        mEtSecondaryDNS.setText(mConfigurationService.getString(SgsConfigurationEntry.NETWORK_SECONDARY_DNS, SgsConfigurationEntry.DEFAULT_NETWORK_SECONDARY_DNS));
        
        // add listeners (for the configuration)
        super.addConfigurationListener(mCbFaked3G);
        super.addConfigurationListener(mCb3G);
        super.addConfigurationListener(mRbIPv4);
        super.addConfigurationListener(mRbIPv6);
        super.addConfigurationListener(mRbStyleWINXP);
        super.addConfigurationListener(mRbStyleWIN7);
        super.addConfigurationListener(mEtLocalIP);
        super.addConfigurationListener(mEtSubMask);
        super.addConfigurationListener(mEtGateWay);
        super.addConfigurationListener(mEtPreferredDNS);
        super.addConfigurationListener(mEtSecondaryDNS);

        mRbStyleWINXP.setOnCheckedChangeListener(rbLocal_OnCheckedChangeListener);
        mRbStyleWIN7.setOnCheckedChangeListener(rbLocal_OnCheckedChangeListener);
    }

    private CompoundButton.OnCheckedChangeListener rbLocal_OnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener(){
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(mRbStyleWINXP.isChecked()) {
                mEtLocalIP.setText(SgsConfigurationEntry.DEFAULT_NETWORK_LOCAL_IP_WINXP);
                mEtGateWay.setText(SgsConfigurationEntry.DEFAULT_NETWORK_GATE_WAY_WINXP);
            } else {
                mEtLocalIP.setText(SgsConfigurationEntry.DEFAULT_NETWORK_LOCAL_IP_WIN7);
                mEtGateWay.setText(SgsConfigurationEntry.DEFAULT_NETWORK_GATE_WAY_WIN7);
            }
        }
    };
	
	protected void onPause() {
		if(super.mComputeConfiguration){


			mConfigurationService.putBoolean(SgsConfigurationEntry.NETWORK_USE_FAKED_3G,
                    mCbFaked3G.isChecked());
			mConfigurationService.putBoolean(SgsConfigurationEntry.NETWORK_USE_3G, 
					mCb3G.isChecked());
			mConfigurationService.putString(SgsConfigurationEntry.NETWORK_IP_VERSION, 
					mRbIPv4.isChecked() ? "ipv4" : "ipv6");
            mConfigurationService.putString(SgsConfigurationEntry.NETWORK_IP_STYLE,
                    mRbStyleWINXP.isChecked() ? "ip_style_winxp" : "ip_style_win7");
            mConfigurationService.putString(SgsConfigurationEntry.NETWORK_LOCAL_IP,
                    mEtLocalIP.getText().toString().trim());
            mConfigurationService.putString(SgsConfigurationEntry.NETWORK_SUB_MASK,
                    mEtSubMask.getText().toString().trim());
            mConfigurationService.putString(SgsConfigurationEntry.NETWORK_GATE_WAY,
                    mEtGateWay.getText().toString().trim());
            mConfigurationService.putString(SgsConfigurationEntry.NETWORK_PREFERRED_DNS,
                    mEtPreferredDNS.getText().toString().trim());
            mConfigurationService.putString(SgsConfigurationEntry.NETWORK_SECONDARY_DNS,
                    mEtSecondaryDNS.getText().toString().trim());

			// Compute
			if(!mConfigurationService.commit()){
				Log.e(TAG, "Failed to commit() configuration");
			}
			
			super.mComputeConfiguration = false;
		}
		super.onPause();
	}
}
