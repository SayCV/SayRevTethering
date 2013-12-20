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
package org.saydroid.ngn.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class NgnDateTimeUtils {
	static final DateFormat sDefaultDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static String now(String dateFormat) {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
	    return sdf.format(cal.getTime());
	}
	
	public static String now() {
	    Calendar cal = Calendar.getInstance();
	    return sDefaultDateFormat.format(cal.getTime());
	}
	
	public static Date parseDate(String date, DateFormat format){
		if(!NgnStringUtils.isNullOrEmpty(date)){
			try {
				return format == null ? sDefaultDateFormat.parse(date) : format.parse(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return new Date();
	}
	
	public static Date parseDate(String date){
		return parseDate(date, null);
	}
	
	public static boolean isSameDay(Date d1, Date d2){
		return d1.getTime() == d2.getTime();
	}

    public static String getDate(String dateFormat) {
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat(dateFormat, Locale.getDefault())
                .format(calendar.getTime());
    }

    public static String getDate(String dateFormat, long currentTimeMillis) {
        return new SimpleDateFormat(dateFormat, Locale.getDefault())
                .format(currentTimeMillis);
    }

    public static long getBuildDate(Context context) {

        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();

            return time;

        } catch (Exception e) {
        }

        return 0l;
    }

    public static long getInstallDate(Context context) {

        try {
            PackageInfo pi = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);

            long time = pi.lastUpdateTime;

            return time;

        } catch (Exception e) {
        }

        return 0l;
    }
}
