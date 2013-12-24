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
package org.saydroid.sgs.events;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Generic event argument containing short string
 */
public class SgsStringEventArgs extends SgsEventArgs {
	private String mValue;
	
	public SgsStringEventArgs(String value){
		mValue = value;
	}
	
	public SgsStringEventArgs(Parcel in){
    	super(in);
    }

    public static final Parcelable.Creator<SgsStringEventArgs> CREATOR = new Parcelable.Creator<SgsStringEventArgs>() {
        public SgsStringEventArgs createFromParcel(Parcel in) {
            return new SgsStringEventArgs(in);
        }

        public SgsStringEventArgs[] newArray(int size) {
            return new SgsStringEventArgs[size];
        }
    };
    
	public String getValue(){
		return mValue;
	}
	
	
	@Override
	public String toString() {
		return mValue;
	}

	@Override
	protected void readFromParcel(Parcel in) {
		mValue = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mValue);
	}
}
