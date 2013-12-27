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

import org.saydroid.tether.usb.Screens.IBaseScreen;
import org.saydroid.sgs.services.ISgsBaseService;

import android.app.Activity;

public interface IScreenService extends ISgsBaseService{
	boolean back();
	boolean bringToFront(int action, String[]... args);
	boolean bringToFront(String[]... args);
	boolean show(Class<? extends Activity> cls, String id);
	boolean show(Class<? extends Activity> cls);
	boolean show(String id);
	void runOnUiThread(Runnable r);
	boolean destroy(String id);
	void setProgressInfoText(String text);
	IBaseScreen getCurrentScreen();
	IBaseScreen getScreen(String id);
}
