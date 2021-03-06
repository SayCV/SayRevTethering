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
package org.saydroid.sgs.services;

import org.saydroid.sgs.model.SgsAccessPoint;
import org.saydroid.sgs.services.impl.SgsNetworkService.DNS_TYPE;
import org.saydroid.sgs.utils.SgsObservableList;

public interface ISgsNetworkService extends ISgsBaseService{
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
