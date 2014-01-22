/**
 *               DO WHAT YOU WANT TO PUBLIC LICENSE
 *                    Version 2, December 2004
 *
 * Copyright (C) 2004 Sam Hocevar <sam@hocevar.net>
 *
 * Everyone is permitted to copy and distribute verbatim or modified
 * copies of this license document, and changing it is allowed as long
 * as the name is changed.
 *
 *            DO WHAT YOU WANT TO PUBLIC LICENSE
 *   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 *  0. You just DO WHAT YOU WANT TO.
 */

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

package org.saydroid.tether.usb.EmbeddedFileExplorer;

import android.content.Context;
import android.content.SharedPreferences;

public class VersusPreferences {
	
	public static final String VERSUS_PREFERENCES = "versus_preferences";
	
	private SharedPreferences getVersusPreferences(Context c) {
		return c.getSharedPreferences(VERSUS_PREFERENCES, Context.MODE_PRIVATE);
	}	
	
	// *** GETter and SETter for various data types *** //
	
	public void removePreferenceValue(Context c, String... keys) {
		SharedPreferences preferences = getVersusPreferences(c);
		SharedPreferences.Editor editor = preferences.edit();
		boolean atLeastOnePreferenceHasBeenRemoved = false;
		for (String key : keys) {
			if (preferences.contains(key)) {
				editor.remove(key);
				atLeastOnePreferenceHasBeenRemoved = true;
			}
		}
		if (atLeastOnePreferenceHasBeenRemoved) {
			editor.commit();
		}
	}
	
	public void setPreferenceValue(Context c, boolean value, String... keys) {
		SharedPreferences preferences = getVersusPreferences(c);
		SharedPreferences.Editor editor = preferences.edit();
		for (String helpIndicator : keys) {
			editor.putBoolean(helpIndicator, value);
		}
		editor.commit();
	}
	
	public boolean getPreferenceValue(Context c, String helpIndicator, boolean defaultValue) {
		SharedPreferences preferences = getVersusPreferences(c);
		return preferences.getBoolean(helpIndicator, defaultValue);
	}

	public void setPreferenceValue(Context c, Integer value, String key) {
		SharedPreferences preferences = getVersusPreferences(c);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	
	public int getPreferenceValue(Context c, String key, int defaultValue) {
		SharedPreferences preferences = getVersusPreferences(c);
		return preferences.getInt(key, defaultValue);
	}
	
	public void setPreferenceValue(Context c, String value, String... keys) {
		SharedPreferences preferences = getVersusPreferences(c);
		SharedPreferences.Editor editor = preferences.edit();
		for (String key : keys) {
			editor.putString(key, value);
		}
		editor.commit();
	}
	
	public String getPreferenceValue(Context c, String key, String defaultValue) {
		SharedPreferences preferences = getVersusPreferences(c);
		return preferences.getString(key, defaultValue);
	}

}
