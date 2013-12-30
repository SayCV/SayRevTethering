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

import org.saydroid.sgs.model.SgsAccessPoint;
import org.saydroid.sgs.services.ISgsBaseService;
import org.saydroid.sgs.utils.SgsObservableList;
import org.saydroid.tether.usb.Services.Impl.TetheringNetworkService.DNS_TYPE;

public interface ITetheringNetworkService extends ISgsBaseService {
	String getDnsServer(DNS_TYPE type);
	String getLocalIP(boolean ipv6);
	boolean isScanning();
	boolean setNetworkEnabledAndRegister();
	boolean setNetworkEnabled(String SSID, boolean enabled, boolean force);
	boolean setNetworkEnabled(int networkId, boolean enabled, boolean force);
	boolean forceConnectToNetwork();
	SgsObservableList<SgsAccessPoint> getAccessPoints();
	int configure(SgsAccessPoint ap, String password, boolean bHex);
	boolean scan();
	boolean acquire();
	boolean release();
}
