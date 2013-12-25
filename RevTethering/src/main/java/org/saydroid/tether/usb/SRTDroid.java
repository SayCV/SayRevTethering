/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
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
package org.saydroid.tether.usb;

import org.saydroid.logger.Log;
import org.saydroid.logger.LogConfiguration;
import org.saydroid.sgs.SgsApplication;

public class SRTDroid extends SgsApplication{
	private final static String TAG = SRTDroid.class.getCanonicalName();
	
	public SRTDroid() {

        // Start log to file from here
        LogConfiguration.getInstance().setLoggerName(SRTDroid.class.getCanonicalName());
        LogConfiguration.getInstance().setFileName(String.format("/data/data/%s/%s", MainActivity.class.getPackage().getName(),"SRTDroid.log"));
        LogConfiguration.getInstance().setInternalDebugging(true);
        //LogConfiguration.getInstance().setFilePattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        LogConfiguration.getInstance().setFilePattern("%msg%n");
        LogConfiguration.getInstance().configure();

    	Log.d(TAG, "IMSDroid()");
    }
}
