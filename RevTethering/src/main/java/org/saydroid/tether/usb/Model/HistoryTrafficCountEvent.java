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

package org.saydroid.tether.usb.Model;

import org.saydroid.sgs.media.SgsMediaType;
import org.saydroid.sgs.model.SgsHistoryEvent;
import org.saydroid.sgs.utils.SgsPredicate;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Root
public class HistoryTrafficCountEvent extends SgsHistoryEvent {
	
	@Element(data=true, required=false)
	protected String mContent;
    @Element(name = "TotalUpload", required = true)
    protected String mTotalUpload;
    @Element(name = "TotalDownload", required = true)
    protected String mTotalDownload;
    //@Element(name = "RealFirstTimeChecked", required = true)
    //protected String mRealFirstTimeChecked;

    HistoryTrafficCountEvent(){
		this(null, StatusType.Failed);
	}
	
	public HistoryTrafficCountEvent(String remoteParty, StatusType status) {
		super(SgsMediaType.TrafficCount, remoteParty);
		super.setStatus(status);
	}
	
	public void setContent(String content){
		this.mContent = content;
	}
    public String getContent(){
        return this.mContent;
    }

    public void setTotalUpload(String content) { this.mTotalUpload = content; }
    public void setTotalDownload(String content) { this.mTotalDownload = content; }

    public String getTotalUpload() { return this.mTotalUpload; }
    public String getTotalDownload() { return this.mTotalDownload; }

    //public void setRealFirstTimeChecked(String content) { this.mRealFirstTimeChecked = content; }
    //public String getRealFirstTimeChecked() { return this.mRealFirstTimeChecked; }

	public static class HistoryEventTrafficCountIntelligentFilter implements SgsPredicate<SgsHistoryEvent> {
		private final List<String> mRemoteParties = new ArrayList<String>();
		
		protected void reset(){
			mRemoteParties.clear();
		}
		
		@Override
		public boolean apply(SgsHistoryEvent event) {
			if (event != null && (event.getMediaType() == SgsMediaType.TrafficCount)){
				if(!mRemoteParties.contains(event.getRemoteParty())){
					mRemoteParties.add(event.getRemoteParty());
					return true;
				}
			}
			return false;
		}
	}
	
	public static class HistoryEventTrafficCountFilter implements SgsPredicate<SgsHistoryEvent>{
		@Override
		public boolean apply(SgsHistoryEvent event) {
			return (event != null && (event.getMediaType() == SgsMediaType.TrafficCount));
		}
	}
}
