/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
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
package org.saydroid.sgs.sip;

import org.saydroid.sgs.utils.SgsContentType;
import org.saydroid.sgs.utils.SgsObservableHashMap;
import org.saydroid.tinyWRAP.SipSession;
import org.saydroid.tinyWRAP.SubscriptionSession;

public class SgsSubscriptionSession extends SgsSipSession{
	private final SubscriptionSession mSession;
	private final EventPackageType mPackage;
	
	public enum EventPackageType {
		None,
		
	    Conference, 
	    Dialog, 
	    MessageSummary, 
	    Presence, 
	    PresenceList, 
	    Reg, 
	    SipProfile, 
	    UAProfile, 
	    WInfo, 
	    XcapDiff
    }
	
	private final static SgsObservableHashMap<Long, SgsSubscriptionSession> sSessions = new SgsObservableHashMap<Long, SgsSubscriptionSession>(
			true);
	
	public static SgsSubscriptionSession createOutgoingSession(SgsSipStack sipStack, String toUri, EventPackageType eventPackage) {
		synchronized (sSessions) {
			final SgsSubscriptionSession subSession = new SgsSubscriptionSession(sipStack, toUri, eventPackage);
			sSessions.put(subSession.getId(), subSession);
			return subSession;
		}
	}

	public static void releaseSession(SgsSubscriptionSession session) {
		synchronized (sSessions) {
			if (session != null && sSessions.containsKey(session.getId())) {
				long id = session.getId();
				session.decRef();
				sSessions.remove(id);
			}
		}
	}

	public static void releaseSession(long id) {
		synchronized (sSessions) {
			SgsSubscriptionSession session = SgsSubscriptionSession.getSession(id);
			if (session != null) {
				session.decRef();
				sSessions.remove(id);
			}
		}
	}

	public static SgsSubscriptionSession getSession(long id) {
		synchronized (sSessions) {
			if (sSessions.containsKey(id))
				return sSessions.get(id);
			else
				return null;
		}
	}

	public static int getSize() {
		synchronized (sSessions) {
			return sSessions.size();
		}
	}

	public static boolean hasSession(long id) {
		synchronized (sSessions) {
			return sSessions.containsKey(id);
		}
	}
	
	protected SgsSubscriptionSession(SgsSipStack sipStack, String toUri, EventPackageType eventPackage){
		super(sipStack);
		mSession = new SubscriptionSession(sipStack);
		super.init();
		
		switch ((mPackage = eventPackage))
        {
            case Conference:
            	mSession.addHeader("Event", "conference");
            	mSession.addHeader("Accept", SgsContentType.CONFERENCE_INFO);
                break;
            case Dialog:
            	mSession.addHeader("Event", "dialog");
            	mSession.addHeader("Accept", SgsContentType.DIALOG_INFO);
                break;
            case MessageSummary:
            	mSession.addHeader("Event", "message-summary");
            	mSession.addHeader("Accept", SgsContentType.MESSAGE_SUMMARY);
                break;
            case Presence:
            case PresenceList:
            default:
            	mSession.addHeader("Event", "presence");
                if (eventPackage == EventPackageType.PresenceList){
                	mSession.addHeader("Supported", "eventlist");
                }
                mSession.addHeader("Accept",
                        String.format("%s, %s, %s, %s",
                        		SgsContentType.MULTIPART_RELATED,
                                SgsContentType.PIDF,
                                SgsContentType.RLMI,
                                SgsContentType.RPID
                                ));
                break;
            case Reg:
            	mSession.addHeader("Event", "reg");
            	mSession.addHeader("Accept", SgsContentType.REG_INFO);
                // 3GPP TS 24.229 5.1.1.6 User-initiated deregistration
            	mSession.setSilentHangup(true);
                break;
            case SipProfile:
            	mSession.addHeader("Event", "sip-profile");
            	mSession.addHeader("Accept", SgsContentType.OMA_DEFERRED_LIST);
                break;
            case UAProfile:
            	mSession.addHeader("Event", "ua-profile");
            	mSession.addHeader("Accept", SgsContentType.XCAP_DIFF);
                break;
            case WInfo:
            	mSession.addHeader("Event", "presence.winfo");
            	mSession.addHeader("Accept", SgsContentType.WATCHER_INFO);
                break;
            case XcapDiff:
            	mSession.addHeader("Event", "xcap-diff");
            	mSession.addHeader("Accept", SgsContentType.XCAP_DIFF);
                break;
        }
		
		super.setSigCompId(sipStack.getSigCompId());
		super.setToUri(toUri);
		super.setFromUri(toUri);
	}

	@Override
	protected SipSession getSession() {
		return mSession;
	}
	
	public boolean subscribe() {
		return mSession.subscribe();
	}

	public boolean unSubscribe() {
		return mSession.unSubscribe();
	}

	public EventPackageType getEventPackage() {
		return mPackage;
	}
}
