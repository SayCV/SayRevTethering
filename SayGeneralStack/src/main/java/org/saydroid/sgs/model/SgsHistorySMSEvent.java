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
package org.saydroid.sgs.model;

import java.util.ArrayList;
import java.util.List;

import org.saydroid.sgs.media.SgsMediaType;
import org.saydroid.sgs.utils.SgsPredicate;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class SgsHistorySMSEvent extends SgsHistoryEvent{
	
	@Element(data=true, required=false)
	protected String mContent;
	
	SgsHistorySMSEvent(){
		this(null, StatusType.Failed);
	}
	
	public SgsHistorySMSEvent(String remoteParty, StatusType status) {
		super(SgsMediaType.SMS, remoteParty);
		super.setStatus(status);
	}
	
	public void setContent(String content){
		this.mContent = content;
	}
	
	public String getContent(){
		return this.mContent;
	}
	
	public static class HistoryEventSMSIntelligentFilter implements SgsPredicate<SgsHistoryEvent>{
		private final List<String> mRemoteParties = new ArrayList<String>();
		
		protected void reset(){
			mRemoteParties.clear();
		}
		
		@Override
		public boolean apply(SgsHistoryEvent event) {
			if (event != null && (event.getMediaType() == SgsMediaType.SMS || event.getMediaType() == SgsMediaType.Chat)){
				if(!mRemoteParties.contains(event.getRemoteParty())){
					mRemoteParties.add(event.getRemoteParty());
					return true;
				}
			}
			return false;
		}
	}
	
	public static class HistoryEventSMSFilter implements SgsPredicate<SgsHistoryEvent>{
		@Override
		public boolean apply(SgsHistoryEvent event) {
			return (event != null && (event.getMediaType() == SgsMediaType.SMS || event.getMediaType() == SgsMediaType.Chat));
		}
	}
}
