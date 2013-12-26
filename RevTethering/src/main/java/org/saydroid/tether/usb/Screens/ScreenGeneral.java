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


import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import org.saydroid.logger.Log;
import org.saydroid.sgs.services.ISgsConfigurationService;
import org.saydroid.sgs.utils.SgsConfigurationEntry;
import org.saydroid.tether.usb.R;

public class ScreenGeneral  extends BaseScreen {
	private final static String TAG = ScreenGeneral.class.getCanonicalName();
	
	private Spinner mSpNotificationRingtone;
	private CheckBox mCbVibrateOnConnect;
	private CheckBox mCbDisableUpdateCheck;
	private CheckBox mCbDisableWakeLock;
	
	private final ISgsConfigurationService mConfigurationService;
	
	private final static AudioPlayBackLevel[] sAudioPlaybackLevels =  new AudioPlayBackLevel[]{
					new AudioPlayBackLevel(0.25f, "Low"),
					new AudioPlayBackLevel(0.50f, "Medium"),
					new AudioPlayBackLevel(0.75f, "High"),
					new AudioPlayBackLevel(1.0f, "Maximum"),
			};
	private final static Profile[] sProfiles =  new Profile[]{
		new Profile(0, "Default (User Defined)"),
        new Profile(1, "SRT (Override)")
	};
	
	public ScreenGeneral() {
		super(SCREEN_TYPE.GENERAL_T, TAG);
		
		mConfigurationService = getEngine().getConfigurationService();
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_general);

        mSpNotificationRingtone = (Spinner)findViewById(R.id.screen_general_spinner_NotificationRingtone);
        mCbVibrateOnConnect = (CheckBox)this.findViewById(R.id.screen_general_checkBox_VibrateOnConnected);
        mCbDisableUpdateCheck = (CheckBox)this.findViewById(R.id.screen_general_checkBox_DisableUpdateCheck);
        mCbDisableWakeLock = (CheckBox)this.findViewById(R.id.screen_general_checkBox_DisableWakeLock);

        mCbVibrateOnConnect.setChecked(mConfigurationService.getBoolean(SgsConfigurationEntry.GENERAL_VOC,SgsConfigurationEntry.DEFAULT_GENERAL_VOC));
        mCbDisableUpdateCheck.setChecked(mConfigurationService.getBoolean(SgsConfigurationEntry.GENERAL_DUC,SgsConfigurationEntry.DEFAULT_GENERAL_DUC));
        mCbDisableWakeLock.setChecked(mConfigurationService.getBoolean(SgsConfigurationEntry.GENERAL_DWL,SgsConfigurationEntry.DEFAULT_GENERAL_DWL));

        //mSpNotificationRingtone.setSelection(mConfigurationService.getBoolean(
        //        SgsConfigurationEntry.NETWORK_CONNECTED,
        //        SgsConfigurationEntry.DEFAULT_NETWORK_CONNECTED));

        super.addConfigurationListener(mSpNotificationRingtone);
        super.addConfigurationListener(mCbVibrateOnConnect);
        super.addConfigurationListener(mCbDisableUpdateCheck);
        super.addConfigurationListener(mCbDisableWakeLock);
	}
	
	protected void onPause() {
		if(super.mComputeConfiguration){

			mConfigurationService.putBoolean(SgsConfigurationEntry.GENERAL_VOC, mCbVibrateOnConnect.isChecked());
			mConfigurationService.putBoolean(SgsConfigurationEntry.GENERAL_DUC, mCbDisableUpdateCheck.isChecked());
			mConfigurationService.putBoolean(SgsConfigurationEntry.GENERAL_DWL, mCbDisableWakeLock.isChecked());
			// profile should be moved to another screen (e.g. Media)
			//mConfigurationService.putString(SgsConfigurationEntry.MEDIA_PROFILE, sProfiles[mSpProfile.getSelectedItemPosition()].mValue.toString());
			
			// Compute
			if(!mConfigurationService.commit()){
				Log.e(TAG, "Failed to commit() configuration");
			}
			else
			{
				// codecs, AEC, NoiseSuppression, Echo cancellation, ....
				boolean aec        = mCbVibrateOnConnect.isChecked() ;
				boolean vad        = mCbDisableUpdateCheck.isChecked();
				boolean nr          = mCbDisableWakeLock.isChecked() ;
				int         echo_tail = mConfigurationService.getInt(SgsConfigurationEntry.GENERAL_ECHO_TAIL,SgsConfigurationEntry.DEFAULT_GENERAL_ECHO_TAIL);
				Log.d(TAG,"Configure AEC["+aec+"/"+echo_tail+"] NoiseSuppression["+nr+"], Voice activity detection["+vad+"]");

			}
			
			super.mComputeConfiguration = false;
		}
		super.onPause();
	}
	
	static class Profile{
		int mValue;
		String mDescription;
		
		Profile(int value, String description){
			mValue = value;
			mDescription = description;
		}

		@Override
		public String toString() {
			return mDescription;
		}
		
		static int getSpinnerIndex(int value){
			for(int i = 0; i< sProfiles.length; i++){
				if(sProfiles[i].mValue == value){
					return i;
				}
			}
			return 0;
		}
	}
	
	static class AudioPlayBackLevel{
		float mValue;
		String mDescription;
		
		AudioPlayBackLevel(float value, String description){
			mValue = value;
			mDescription = description;
		}

		@Override
		public String toString() {
			return mDescription;
		}
		
		static int getSpinnerIndex(float value){
			for(int i = 0; i< sAudioPlaybackLevels.length; i++){
				if(sAudioPlaybackLevels[i].mValue == value){
					return i;
				}
			}
			return 0;
		}
	}
}
