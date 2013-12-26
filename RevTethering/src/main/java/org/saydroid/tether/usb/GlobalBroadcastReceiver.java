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

package org.saydroid.tether.usb;

import org.saydroid.sgs.utils.SgsConfigurationEntry;
import org.saydroid.sgs.utils.SgsStringUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class GlobalBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			SharedPreferences settings = context.getSharedPreferences(SgsConfigurationEntry.SHARED_PREF_NAME, 0);
			if (settings != null && settings.getBoolean(SgsConfigurationEntry.GENERAL_AUTOSTART.toString(), SgsConfigurationEntry.DEFAULT_GENERAL_AUTOSTART)) {
				Intent i = new Intent(context, NativeService.class);
				i.putExtra("autostarted", true);
				context.startService(i);
			}
		} else if (Intent.ACTION_NEW_OUTGOING_CALL.equals(action) && Engine.getInstance().getSipService().isRegistered()) {
			final String number = getResultData();
			if (SgsStringUtils.isNullOrEmpty(number)) {
				return;
			}
			final boolean intercept = Engine.getInstance().getConfigurationService().getBoolean(SgsConfigurationEntry.GENERAL_INTERCEPT_OUTGOING_CALLS, SgsConfigurationEntry.DEFAULT_GENERAL_INTERCEPT_OUTGOING_CALLS);
			if (intercept) {
				//ServiceManager.getScreenService().bringToFront(Main.ACTION_INTERCEPT_OUTGOING_CALL, new String[] { "number", number });
				//setResultData(null);
				//return;
			}

			setResultData(number);
		}

	}
}