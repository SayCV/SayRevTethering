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

public class SgsRegistrationEventArgs extends SgsEventArgs {
	private final static String TAG = SgsRegistrationEventArgs.class.getCanonicalName();
	
	private long mSessionId;
	private SgsRegistrationEventTypes mType;
	private short mSipCode;
	private String mPhrase;
    
	public static final String ACTION_REGISTRATION_EVENT = TAG + ".ACTION_REGISTRATION_CHANGED";
	
	public static final String EXTRA_EMBEDDED = SgsEventArgs.EXTRA_EMBEDDED;
	
    public SgsRegistrationEventArgs(long sessionId, SgsRegistrationEventTypes type, short sipCode, String phrase){
    	super();
    	mSessionId = sessionId;
    	mType = type;
    	mSipCode = sipCode;
    	mPhrase = phrase;
    }
    
    public SgsRegistrationEventArgs(Parcel in){
    	super(in);
    }

    public static final Parcelable.Creator<SgsRegistrationEventArgs> CREATOR = new Parcelable.Creator<SgsRegistrationEventArgs>() {
        public SgsRegistrationEventArgs createFromParcel(Parcel in) {
            return new SgsRegistrationEventArgs(in);
        }

        public SgsRegistrationEventArgs[] newArray(int size) {
            return new SgsRegistrationEventArgs[size];
        }
    };

    public long getSessionId(){
    	return mSessionId;
    }
    
    public SgsRegistrationEventTypes getEventType(){
        return mType;
    }

    public short getSipCode(){
        return mSipCode;
    }

    public String getPhrase(){
        return mPhrase;
    }

	@Override
	protected void readFromParcel(Parcel in) {
		mSessionId = in.readLong();
		mType = Enum.valueOf(SgsRegistrationEventTypes.class, in.readString());
		mSipCode = (short)in.readInt();
		mPhrase = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mSessionId);
		dest.writeString(mType.toString());
		dest.writeInt(mSipCode);
		dest.writeString(mPhrase);
	}
}
