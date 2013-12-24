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

import java.nio.ByteBuffer;

import org.saydroid.sgs.utils.SgsContentType;
import org.saydroid.sgs.utils.SgsObservableHashMap;
import org.saydroid.sgs.utils.SgsStringUtils;
import org.saydroid.sgs.utils.SgsUriUtils;
import org.saydroid.tinyWRAP.MessagingSession;
import org.saydroid.tinyWRAP.RPMessage;
import org.saydroid.tinyWRAP.SMSEncoder;
import org.saydroid.tinyWRAP.SipMessage;
import org.saydroid.tinyWRAP.SipSession;

import android.util.Log;

/**
 * Messaging session used to send Pager Mode IM (SIP MESSAGE)
 */
public class SgsMessagingSession extends SgsSipSession {
	private static String TAG = SgsMessagingSession.class.getCanonicalName();
	
	private final MessagingSession mSession;
	private static int SMS_MR = 0;
	
	private final static SgsObservableHashMap<Long, SgsMessagingSession> sSessions = new SgsObservableHashMap<Long, SgsMessagingSession>(true);
	
	public static SgsMessagingSession takeIncomingSession(SgsSipStack sipStack, MessagingSession session, SipMessage sipMessage){
		final String toUri = sipMessage==null ? null: sipMessage.getSipHeaderValue("f");
		SgsMessagingSession imSession = new SgsMessagingSession(sipStack, session, toUri);
		sSessions.put(imSession.getId(), imSession);
        return imSession;
    }

    public static SgsMessagingSession createOutgoingSession(SgsSipStack sipStack, String toUri){
        synchronized (sSessions){
            final SgsMessagingSession imSession = new SgsMessagingSession(sipStack, null, toUri);
            sSessions.put(imSession.getId(), imSession);
            return imSession;
        }
    }
    
    public static void releaseSession(SgsMessagingSession session){
		synchronized (sSessions){
            if (session != null && sSessions.containsKey(session.getId())){
                long id = session.getId();
                session.decRef();
                sSessions.remove(id);
            }
        }
    }

    public static void releaseSession(long id){
		synchronized (sSessions){
			SgsMessagingSession session = SgsMessagingSession.getSession(id);
            if (session != null){
                session.decRef();
                sSessions.remove(id);
            }
        }
    }
    
	public static SgsMessagingSession getSession(long id) {
		synchronized (sSessions) {
			if (sSessions.containsKey(id))
				return sSessions.get(id);
			else
				return null;
		}
	}

	public static int getSize(){
        synchronized (sSessions){
            return sSessions.size();
        }
    }
	
    public static boolean hasSession(long id){
        synchronized (sSessions){
            return sSessions.containsKey(id);
        }
    }
    
	protected SgsMessagingSession(SgsSipStack sipStack, MessagingSession session, String toUri) {
		super(sipStack);
        mSession = session == null ? new MessagingSession(sipStack) : session;

        super.init();
        super.setSigCompId(sipStack.getSigCompId());
        super.setToUri(toUri);
	}

	@Override
	protected SipSession getSession() {
		return mSession;
	}
	
	/**
	 * Sends binary SMS (3gpp) using SIP MESSAGE request
	 * @param text the text (utf-8) to send.
	 * @param SMSC the address (PSI) of the SMS center
	 * @return true if succeed and false otherwise
	 * @sa @ref sendTextMessage()
	 */
	public boolean SendBinaryMessage(String text, String SMSC){
        String SMSCPhoneNumber;
        String dstPhoneNumber;
        String dstSipUri = super.getToUri();

        if ((SMSCPhoneNumber = SgsUriUtils.getValidPhoneNumber(SMSC)) != null && (dstPhoneNumber = SgsUriUtils.getValidPhoneNumber(dstSipUri)) != null){
            super.setToUri(SMSC);
            super.addHeader("Content-Type", SgsContentType.SMS_3GPP);
            super.addHeader("Content-Transfer-Encoding", "binary");
            super.addCaps("+g.3gpp.smsip");       
                    
            RPMessage rpMessage;
            //if(ServiceManager.getConfigurationService().getBoolean(CONFIGURATION_SECTION.RCS, CONFIGURATION_ENTRY.HACK_SMS, false)){
                //    rpMessage = SMSEncoder.encodeDeliver(++ScreenSMSCompose.SMS_MR, SMSCPhoneNumber, dstPhoneNumber, new String(content));
                //    session.addHeader("P-Asserted-Identity", SMSC);
            //}
            //else{
                    rpMessage = SMSEncoder.encodeSubmit(++SgsMessagingSession.SMS_MR, SMSCPhoneNumber, dstPhoneNumber, text);
            //}
        
            long rpMessageLen = rpMessage.getPayloadLength();
            ByteBuffer payload = ByteBuffer.allocateDirect((int)rpMessageLen);
            long payloadLength = rpMessage.getPayload(payload, (long)payload.capacity());
            boolean ret = mSession.send(payload, payloadLength);
            rpMessage.delete();
            if(SgsMessagingSession.SMS_MR >= 255){
                 SgsMessagingSession.SMS_MR = 0;
            }

            return ret;
        }
        else{
            Log.e(TAG, String.format("SMSC=%s or RemoteUri=%s is invalid", SMSC, dstSipUri));
            return sendTextMessage(text);
        }
    }

	/**
	 * Send plain text message using SIP MESSAGE request
	 * @param text
	 * @return true if succeed and false otherwise
	 * @sa @ref SendBinaryMessage()
	 */
    public boolean sendTextMessage(String text, String contentType){
    	if (!SgsStringUtils.isNullOrEmpty(contentType)) {
            super.addHeader("Content-Type", contentType);
    	}
    	else {
    		super.addHeader("Content-Type", SgsContentType.TEXT_PLAIN);
    	}
        byte[] bytes = text.getBytes();
        ByteBuffer payload = ByteBuffer.allocateDirect(bytes.length);
        payload.put(bytes);
        return mSession.send(payload, payload.capacity());
    }

    public boolean sendTextMessage(String text){
    	return sendTextMessage(text, null);    	
    }

    /**
     * Accepts the message (sends 200 OK).
     * @return true if succeed and false otherwise
     */
    public boolean accept() {
        return mSession.accept();
      }

    /**
     * Reject the message (sends 603 Decline)
     * @return true if succeed and false otherwise
     */
      public boolean reject() {
        return mSession.reject();
      }
}
