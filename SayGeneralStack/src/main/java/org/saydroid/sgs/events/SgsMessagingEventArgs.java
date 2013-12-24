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

public class SgsMessagingEventArgs extends SgsEventArgs{
	private final static String TAG = SgsMessagingEventArgs.class.getCanonicalName();
	
	private long mSessionId;
    private SgsMessagingEventTypes mEventType;
    private String mPhrase;
    private byte[] mPayload;
    private String mContentType;
    
    public static final String ACTION_MESSAGING_EVENT = TAG + ".ACTION_MESSAGING_EVENT";
    
    public static final String EXTRA_EMBEDDED = SgsEventArgs.EXTRA_EMBEDDED;
    public static final String EXTRA_SESSION = TAG + "session";
    public static final String EXTRA_CODE = TAG + "code";
    public static final String EXTRA_REMOTE_PARTY = TAG + "from";
    public static final String EXTRA_DATE = TAG + "date";

    public SgsMessagingEventArgs(long sessionId, SgsMessagingEventTypes type, String phrase, byte[] payload, String contentType){
    	super();
        mSessionId = sessionId;
        mEventType = type;
        mPhrase = phrase;
        mPayload = payload;
        mContentType = contentType;
    }

    public SgsMessagingEventArgs(Parcel in){
    	super(in);
    }

    public static final Parcelable.Creator<SgsMessagingEventArgs> CREATOR = new Parcelable.Creator<SgsMessagingEventArgs>() {
        public SgsMessagingEventArgs createFromParcel(Parcel in) {
            return new SgsMessagingEventArgs(in);
        }

        public SgsMessagingEventArgs[] newArray(int size) {
            return new SgsMessagingEventArgs[size];
        }
    };
    
    public long getSessionId(){
        return mSessionId;
    }

    public SgsMessagingEventTypes getEventType(){
        return mEventType;
    }

    public String getPhrase(){
        return mPhrase;
    }

    public byte[] getPayload(){
        return mPayload;
    }
    
    public String getContentType() {
    	return mContentType;
    }

	@Override
	protected void readFromParcel(Parcel in) {
		mSessionId = in.readLong();
		mEventType = Enum.valueOf(SgsMessagingEventTypes.class, in.readString());
		mPhrase = in.readString();
		mContentType = in.readString();
		mPayload = in.createByteArray();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mSessionId);
		dest.writeString(mEventType.toString());
		dest.writeString(mPhrase);
		dest.writeString(mContentType);
		dest.writeByteArray(mPayload);
	}
}
