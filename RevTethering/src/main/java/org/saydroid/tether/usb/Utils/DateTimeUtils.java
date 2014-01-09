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

package org.saydroid.tether.usb.Utils;

import org.saydroid.tether.usb.R;
import org.saydroid.sgs.utils.SgsDateTimeUtils;
import org.saydroid.tether.usb.SRTDroid;

import java.text.DateFormat;
import java.util.Date;


public class DateTimeUtils extends SgsDateTimeUtils {

	static final DateFormat sDateFormat = DateFormat.getInstance();
	static final DateFormat sDateTimeFormat = DateFormat.getDateTimeInstance();
	static final DateFormat sTimeFormat = DateFormat.getTimeInstance();
	
	static String sTodayName;
	static String sYesterdayName;
	
	public static String getTodayName(){
		if(sTodayName == null){
			sTodayName = SRTDroid.getContext().getResources().getString(R.string.day_today);
		}
		return sTodayName;
	}
	
	public static String getYesterdayName(){
		if(sYesterdayName == null){
			sYesterdayName = SRTDroid.getContext().getResources().getString(R.string.day_yesterday);
		}
		return sYesterdayName;
	}
	
	public static String getFriendlyDateString(final Date date){
		final Date today = new Date();
        if (DateTimeUtils.isSameDay(date, today)){
            return String.format("%s %s", getTodayName(), sTimeFormat.format(date));
        }
        else if ((today.getDay() - date.getDay()) == 1){
            return String.format("%s %s", getYesterdayName(), sTimeFormat.format(date));
        }
        else{
            return sDateTimeFormat.format(date);
        }
	}
}
