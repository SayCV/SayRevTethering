/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)saydroid(dot)org>
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
package org.saydroid.tether.usb.Screens;

import org.saydroid.tether.usb.R;
import org.saydroid.sgs.services.ISgsConfigurationService;
import org.saydroid.sgs.utils.SgsConfigurationEntry;
import org.saydroid.sgs.utils.SgsStringUtils;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
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
        
        // spinners


        mCbFaked3G.setChecked(mConfigurationService.getBoolean(SgsConfigurationEntry.NETWORK_USE_WIFI, SgsConfigurationEntry.DEFAULT_NETWORK_USE_WIFI));
        mCb3G.setChecked(mConfigurationService.getBoolean(SgsConfigurationEntry.NETWORK_USE_3G, SgsConfigurationEntry.DEFAULT_NETWORK_USE_3G));
        mRbIPv4.setChecked(mConfigurationService.getString(SgsConfigurationEntry.NETWORK_IP_VERSION,
        		SgsConfigurationEntry.DEFAULT_NETWORK_IP_VERSION).equalsIgnoreCase("ipv4"));
        mRbIPv6.setChecked(!mRbIPv4.isChecked());
        
        // add listeners (for the configuration)
        super.addConfigurationListener(mCbFaked3G);
        super.addConfigurationListener(mCb3G);
        super.addConfigurationListener(mRbIPv4);
        super.addConfigurationListener(mRbIPv6);
	}
	
	protected void onPause() {
		if(super.mComputeConfiguration){


			mConfigurationService.putBoolean(SgsConfigurationEntry.NETWORK_USE_FAKED_3G,
                    mCbFaked3G.isChecked());
			mConfigurationService.putBoolean(SgsConfigurationEntry.NETWORK_USE_3G, 
					mCb3G.isChecked());
			mConfigurationService.putString(SgsConfigurationEntry.NETWORK_IP_VERSION, 
					mRbIPv4.isChecked() ? "ipv4" : "ipv6");
			
			// Compute
			if(!mConfigurationService.commit()){
				Log.e(TAG, "Failed to commit() configuration");
			}
			
			super.mComputeConfiguration = false;
		}
		super.onPause();
	}
}
