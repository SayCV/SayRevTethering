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

import java.io.Serializable;

public class TrafficCountEventArgs extends SgsEventArgs {
	private final static String TAG = TrafficCountEventArgs.class.getCanonicalName();
	
	//private long mSessionId;
    private TrafficCountEventTypes mEventType;
    //private String mPhrase;
    private long[] mPayload;
   // private DataCount mContent;

    // Total data uploaded
    public long mTotalUpload;
    // Total data downloaded
    public long mTotalDownload;
    // Current upload rate
    public long mUploadRate;
    // Current download rate
    public long mDownloadRate;

    public static final String ACTION_TRAFFIC_COUNT_EVENT = TAG + ".ACTION_TRAFFIC_COUNT_EVENT";

    public static final String EXTRA_EMBEDDED = SgsEventArgs.EXTRA_EMBEDDED;
    public static final String EXTRA_SESSION = TAG + "session";
    public static final String EXTRA_CODE = TAG + "code";
    public static final String EXTRA_REMOTE_PARTY = TAG + "from";
    public static final String EXTRA_DATE = TAG + "date";
    public static final String EXTRA_DATA_COUNT_TOTAL_UPLOAD = TAG + "totalUpload";
    public static final String EXTRA_DATA_COUNT_TOTAL_DOWNLOAD = TAG + "totalDownload";
    public static final String EXTRA_DATA_COUNT_UPLOAD_RATE = TAG + "uploadRate";
    public static final String EXTRA_DATA_COUNT_DOWNLOAD_RATE = TAG + "downloadRate";

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

    public TrafficCountEventArgs(TrafficCountEventTypes type, long totalUpload, long totalDownload, long uploadRate,long downloadRate){
    	super();
        //mSessionId = sessionId;
        mEventType = type;
        //mPhrase = phrase;
        //mPayload = payload;
        //mContent = content;
        mTotalUpload = totalUpload;
        mTotalDownload = totalDownload;
        mUploadRate = uploadRate;
        mDownloadRate = downloadRate;
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
    
    //public DataCount getContent() { return mContent; }

    public long getTotalUpload() { return mTotalUpload; }
    public long getTotalDownload() { return mTotalDownload; }
    public long getUploadRate() { return mUploadRate; }
    public long getDownloadRate() { return mDownloadRate; }

	@Override
	protected void readFromParcel(Parcel in) {
		//mSessionId = in.readLong();
		mEventType = Enum.valueOf(TrafficCountEventTypes.class, in.readString());
		//mPhrase = in.readString();
		mTotalUpload = in.readLong();
        mTotalDownload = in.readLong();
        mUploadRate = in.readLong();
        mDownloadRate = in.readLong();
        //mPayload = in.createByteArray();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		//dest.writeLong(mSessionId);
		dest.writeString(mEventType.toString());
		//dest.writeString(mPhrase);
        dest.writeLong(mTotalUpload);
        dest.writeLong(mTotalDownload);
        dest.writeLong(mUploadRate);
        dest.writeLong(mDownloadRate);
        //dest.writeByteArray(mPayload);
	}
}
