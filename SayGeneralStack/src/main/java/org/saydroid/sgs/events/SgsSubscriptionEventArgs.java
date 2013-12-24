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

import org.saydroid.sgs.sip.SgsSubscriptionSession.EventPackageType;

import android.os.Parcel;
import android.os.Parcelable;

public class SgsSubscriptionEventArgs extends SgsEventArgs{
	private final static String TAG = SgsSubscriptionEventArgs.class.getCanonicalName();
	
	public static final String ACTION_SUBSCRIBTION_EVENT = TAG + ".ACTION_SUBSCRIBTION_EVENT";
	
	private long mSessionId;
	private SgsSubscriptionEventTypes mType;
    private short mSipCode;
    private String mPhrase;
    private byte[] mContent;
    private String mContentType;
    private EventPackageType mEventPackage;

    public static final String EXTRA_EMBEDDED = SgsEventArgs.EXTRA_EMBEDDED;
    public final String EXTRA_CONTENTYPE_TYPE = "ContentTypeType";
    public final String EXTRA_CONTENTYPE_START = "ContentTypeStart";
    public final String EXTRA_CONTENTYPE_BOUNDARY = "ContentTypeBoundary";
	
	public SgsSubscriptionEventArgs(long sessionId, SgsSubscriptionEventTypes type, short sipCode, String phrase, 
			byte[] content, String contentType, EventPackageType eventPackage){
		super();
		mSessionId = sessionId;
		mType = type;
		mSipCode = sipCode;
		mPhrase = phrase;
		mContent = content;
		mContentType = contentType;
		mEventPackage = eventPackage;
	}

	public SgsSubscriptionEventArgs(Parcel in){
    	super(in);
    }

    public static final Parcelable.Creator<SgsSubscriptionEventArgs> CREATOR = new Parcelable.Creator<SgsSubscriptionEventArgs>() {
        public SgsSubscriptionEventArgs createFromParcel(Parcel in) {
            return new SgsSubscriptionEventArgs(in);
        }

        public SgsSubscriptionEventArgs[] newArray(int size) {
            return new SgsSubscriptionEventArgs[size];
        }
    };
    
    public long getSessionId(){
        return mSessionId;
    }

    public SgsSubscriptionEventTypes getEventType(){
        return mType;
    }

    public String getPhrase(){
        return mPhrase;
    }

    public byte[] getContent(){
        return mContent;
    }
    
    public String getContentType(){
        return mContentType;
    }
    
    public EventPackageType getEventPackage(){
        return mEventPackage;
    }
    
	@Override
	protected void readFromParcel(Parcel in) {
		mSessionId = in.readLong();
		mType = Enum.valueOf(SgsSubscriptionEventTypes.class, in.readString());
		mSipCode = (short)in.readInt();
		mPhrase = in.readString();
		mContent = in.createByteArray();
		mContentType = in.readString();
		mEventPackage = Enum.valueOf(EventPackageType.class, in.readString());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mSessionId);
		dest.writeString(mType.toString());
		dest.writeInt(mSipCode);
		dest.writeString(mPhrase);
		dest.writeByteArray(mContent);
		dest.writeString(mContentType);
		dest.writeString(mEventPackage.toString());
	}
}
