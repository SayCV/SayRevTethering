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

package org.saydroid.tether.usb.Events;

import android.os.Parcel;
import android.os.Parcelable;

import org.saydroid.sgs.events.SgsEventArgs;

public class TrafficCountEventArgs extends SgsEventArgs {
	private final static String TAG = TrafficCountEventArgs.class.getCanonicalName();
	
	//private long mSessionId;
    private TrafficCountEventTypes mEventType;
    //private String mPhrase;
    //private byte[] mPayload;
    private DataCount mContent;

    public static class DataCount {
        // Total data uploaded
        public long totalUpload;
        // Total data downloaded
        public long totalDownload;
        // Current upload rate
        public long uploadRate;
        // Current download rate
        public long downloadRate;
    }

    public static final String ACTION_TRAFFIC_COUNT_EVENT = TAG + ".ACTION_TRAFFIC_COUNT_EVENT";
    
    public static final String EXTRA_EMBEDDED = SgsEventArgs.EXTRA_EMBEDDED;
    public static final String EXTRA_SESSION = TAG + "session";
    public static final String EXTRA_CODE = TAG + "code";
    public static final String EXTRA_DATA_COUNT_TOTAL_UPLOAD = TAG + "totalUpload";
    public static final String EXTRA_DATA_COUNT_TOTAL_DOWNLOAD = TAG + "totalDownload";
    public static final String EXTRA_DATA_COUNT_UPLOAD_RATE = TAG + "uploadRate";
    public static final String EXTRA_DATA_COUNT_DOWNLOAD_RATE = TAG + "downloadRate";
    public static final String EXTRA_DATE = TAG + "date";

    public TrafficCountEventArgs(TrafficCountEventTypes type, DataCount content){
    	super();
        //mSessionId = sessionId;
        mEventType = type;
        //mPhrase = phrase;
        //mPayload = payload;
        mContent = content;
    }

    public TrafficCountEventArgs(Parcel in){
    	super(in);
    }

    public static final Parcelable.Creator<TrafficCountEventArgs> CREATOR = new Parcelable.Creator<TrafficCountEventArgs>() {
        public TrafficCountEventArgs createFromParcel(Parcel in) {
            return new TrafficCountEventArgs(in);
        }

        public TrafficCountEventArgs[] newArray(int size) {
            return new TrafficCountEventArgs[size];
        }
    };

    public TrafficCountEventTypes getEventType(){
        return mEventType;
    }
    
    public DataCount getContent() {
    	return mContent;
    }

	@Override
	protected void readFromParcel(Parcel in) {
		//mSessionId = in.readLong();
		mEventType = Enum.valueOf(TrafficCountEventTypes.class, in.readString());
		//mPhrase = in.readString();
		mContent.totalUpload = in.readLong();
        mContent.totalDownload = in.readLong();
        mContent.uploadRate = in.readLong();
        mContent.downloadRate = in.readLong();
        //mPayload = in.createByteArray();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		//dest.writeLong(mSessionId);
		dest.writeString(mEventType.toString());
		//dest.writeString(mPhrase);
        dest.writeLong(mContent.totalUpload);
        dest.writeLong(mContent.totalDownload);
        dest.writeLong(mContent.uploadRate);
        dest.writeLong(mContent.downloadRate);
        //dest.writeByteArray(mPayload);
	}
}
