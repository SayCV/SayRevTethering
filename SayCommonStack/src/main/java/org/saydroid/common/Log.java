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
package org.saydroid.common;

import android.app.Activity;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;

public final class Log {
	// private final static String TAG = Log.class.getCanonicalName();
    private final static String TAG_TOKEN = ":";

    public static org.slf4j.Logger sLogger = LogConfiguration.getInstance().getLogger();

    protected static boolean checkDebuggingEnabled(){
        if(sLogger!=null && LogConfiguration.getInstance().isInternalDebugging())
            return true;

        return false;
    }

    public static void v(String tag, String msg) {
        if (checkDebuggingEnabled()) {
            sLogger.debug(tag + TAG_TOKEN +msg);
        }
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (checkDebuggingEnabled()) {
            sLogger.debug(tag + TAG_TOKEN +msg, tr);
        }
    }

    public static void d(String tag, String msg) {
        if (checkDebuggingEnabled()) {
            sLogger.debug(tag + TAG_TOKEN +msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (checkDebuggingEnabled()) {
            sLogger.debug(tag + TAG_TOKEN +msg, tr);
        }
    }

    public static void i(String tag, String msg) {
        if (checkDebuggingEnabled()) {
            sLogger.info(tag + TAG_TOKEN +msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (checkDebuggingEnabled()) {
            sLogger.info(tag + TAG_TOKEN +msg, tr);
        }
    }

    public static void w(String tag, String msg) {
        sLogger.warn(tag + TAG_TOKEN +msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        sLogger.warn(tag + TAG_TOKEN +msg, tr);
    }

    public static void w(String tag, Throwable tr) {
        sLogger.warn(tag, tr);
    }

    public static void e(String tag, String msg) {
        sLogger.error(tag + TAG_TOKEN +msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        sLogger.error(tag + TAG_TOKEN +msg, tr);
    }
}
