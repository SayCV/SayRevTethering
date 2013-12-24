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

import org.saydroid.sgs.media.SgsMediaType;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Event argument for SIP INVITE sessions
 */
public class SgsInviteEventArgs extends SgsEventArgs{
	private final static String TAG = SgsInviteEventArgs.class.getCanonicalName();
	
	private long mSessionId;
    private SgsInviteEventTypes mEventType;
    private SgsMediaType mMediaType;
    private String mPhrase;
    
    public static final String ACTION_INVITE_EVENT = TAG + ".ACTION_INVITE_EVENT";
    
    public static final String EXTRA_EMBEDDED = SgsEventArgs.EXTRA_EMBEDDED;
    public static final String EXTRA_SESSION = "session";
    public static final String EXTRA_SIPCODE = "sipCode";

    public SgsInviteEventArgs(long sessionId, SgsInviteEventTypes eventType, SgsMediaType mediaType, String phrase){
    	super();
    	mSessionId = sessionId;
    	mEventType = eventType;
    	mMediaType = mediaType;
    	mPhrase = phrase;
    }

    public SgsInviteEventArgs(Parcel in){
    	super(in);
    }

    public static final Parcelable.Creator<SgsInviteEventArgs> CREATOR = new Parcelable.Creator<SgsInviteEventArgs>() {
        public SgsInviteEventArgs createFromParcel(Parcel in) {
            return new SgsInviteEventArgs(in);
        }

        public SgsInviteEventArgs[] newArray(int size) {
            return new SgsInviteEventArgs[size];
        }
    };
    
    public long getSessionId(){
        return mSessionId;
    }

    public SgsInviteEventTypes getEventType(){
        return mEventType;
    }
    
    public SgsMediaType getMediaType(){
        return mMediaType;
    }

    public String getPhrase(){
        return mPhrase;
    }

    @Override
	protected void readFromParcel(Parcel in) {
    	mSessionId = in.readLong();
		mEventType = Enum.valueOf(SgsInviteEventTypes.class, in.readString());
		mMediaType = Enum.valueOf(SgsMediaType.class, in.readString());
		mPhrase = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mSessionId);
		dest.writeString(mEventType.toString());
		dest.writeString(mMediaType.toString());
		dest.writeString(mPhrase);
	}
}
