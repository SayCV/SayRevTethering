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

package org.saydroid.tether.usb;

import org.saydroid.tether.usb.Services.IScreenService;
import org.saydroid.tether.usb.Services.ITetheringService;
import org.saydroid.tether.usb.Services.Impl.ScreenService;
import org.saydroid.sgs.SgsApplication;
import org.saydroid.sgs.SgsEngine;
import org.saydroid.sgs.SgsNativeService;
import org.saydroid.sgs.media.SgsMediaType;
import org.saydroid.sgs.sip.SgsAVSession;
import org.saydroid.sgs.sip.SgsMsrpSession;
import org.saydroid.sgs.utils.SgsPredicate;
import org.saydroid.tether.usb.Services.Impl.TetheringService;
import org.saydroid.utils.AndroidUtils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

import org.saydroid.logger.Log;

public class Engine extends SgsEngine{
	private final static String TAG = Engine.class.getCanonicalName();
	
	private static final String CONTENT_TITLE = "SRTDroid";
	
	private static final int NOTIF_AVCALL_ID = 19833892;
	private static final int NOTIF_SMS_ID = 19833893;
	private static final int NOTIF_APP_ID = 19833894;
	private static final int NOTIF_CONTSHARE_ID = 19833895;
	private static final int NOTIF_CHAT_ID = 19833896;
	private static final String DATA_FOLDER = String.format("/data/data/%s", MainActivity.class.getPackage().getName());
	private static final String LIBS_FOLDER = String.format("%s/lib", Engine.DATA_FOLDER);
	
	private IScreenService mScreenService;
    private ITetheringService mTetheringService;

	static {
		// See 'http://code.google.com/p/imsdroid/issues/detail?id=197' for more information
		// Load Android utils library (required to detect CPU features)
		System.load(String.format("%s/%s", Engine.LIBS_FOLDER, "libutils_armv5te.so"));
		Log.d(TAG,"CPU_Feature="+AndroidUtils.getCpuFeatures());
		/*if(SgsApplication.isCpuNeon()){
			Log.d(TAG,"isCpuNeon()=YES");
			System.load(String.format("%s/%s", Engine.LIBS_FOLDER, "libtinyWRAP_armv7-a.so"));
		}
		else{
			Log.d(TAG,"isCpuNeon()=NO");
			System.load(String.format("%s/%s", Engine.LIBS_FOLDER, "libtinyWRAP_armv5te.so"));
		}*/
		// Initialize the engine
		SgsEngine.initialize();
	}
	
	public static SgsEngine getInstance(){
		if(sInstance == null){
			sInstance = new Engine();
		}
		return sInstance;
	}
	
	public Engine(){
		super();
	}
	
	@Override
	public boolean start() {
		return super.start();
	}
	
	@Override
	public boolean stop() {
		return super.stop();
	}
	
	private void showNotification(int notifId, int drawableId, String tickerText) {
		if(!mStarted){
			return;
		}
        //final Notification.Builder notificationBuilder = new Notification.Builder(SRTDroid.getContext());
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(SRTDroid.getContext())
                .setWhen(System.currentTimeMillis());
        notificationBuilder.setSmallIcon(drawableId);
        // Set the icon, scrolling text and timestamp
        //final Notification notification = new Notification(drawableId, "", System.currentTimeMillis());
        
        Intent intent = new Intent(SRTDroid.getContext(), MainActivity.class);
    	intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP  | Intent.FLAG_ACTIVITY_NEW_TASK);
        
        switch(notifId){
        	case NOTIF_APP_ID:
        		//notification.flags |= Notification.FLAG_ONGOING_EVENT;
                notificationBuilder.setOngoing(true);
        		intent.putExtra("notif-type", "reg");
        		break;
        		
        	case NOTIF_CONTSHARE_ID:
                intent.putExtra("action", MainActivity.ACTION_SHOW_CONTSHARE_SCREEN);
                //notification.defaults |= Notification.DEFAULT_SOUND;
                notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                break;
                
        	case NOTIF_SMS_ID:
                //notification.flags |= Notification.FLAG_AUTO_CANCEL;
                //notification.defaults |= Notification.DEFAULT_SOUND;
                //notification.tickerText = tickerText;
                notificationBuilder.setAutoCancel(true);
                notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                intent.putExtra("action", MainActivity.ACTION_SHOW_SMS);
                break;
                
        	case NOTIF_AVCALL_ID:
        		tickerText = String.format("%s (%d)", tickerText, SgsAVSession.getSize());
        		intent.putExtra("action", MainActivity.ACTION_SHOW_AVSCREEN);
        		break;
        		
        	case NOTIF_CHAT_ID:
        		//notification.defaults |= Notification.DEFAULT_SOUND;
                notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        		tickerText = String.format("%s (%d)", tickerText, SgsMsrpSession.getSize(new SgsPredicate<SgsMsrpSession>() {
					@Override
					public boolean apply(SgsMsrpSession session) {
						return session != null && SgsMediaType.isChat(session.getMediaType());
					}
				}));
        		intent.putExtra("action", MainActivity.ACTION_SHOW_CHAT_SCREEN);
        		break;
        		
       		default:
       			
       			break;
        }
        
        PendingIntent contentIntent = PendingIntent.getActivity(SRTDroid.getContext(), notifId/*requestCode*/, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set the info for the views that show in the notification panel.
        //notification.setLatestEventInfo(SRTDroid.getContext(), CONTENT_TITLE, tickerText, contentIntent);
        notificationBuilder.setContentTitle(CONTENT_TITLE);
        notificationBuilder.setTicker(tickerText);
        notificationBuilder.setContentIntent(contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNotifManager.notify(notifId, notificationBuilder.build());
    }
	
	public void showAppNotif(int drawableId, String tickerText){
    	Log.d(TAG, "showAppNotif");
    	showNotification(NOTIF_APP_ID, drawableId, tickerText);
    }
	
	public void showAVCallNotif(int drawableId, String tickerText){
    	showNotification(NOTIF_AVCALL_ID, drawableId, tickerText);
    }
	
	public void cancelAVCallNotif(){
    	if(!SgsAVSession.hasActiveSession()){
    		mNotifManager.cancel(NOTIF_AVCALL_ID);
    	}
    }
	
	public void refreshAVCallNotif(int drawableId){
		if(!SgsAVSession.hasActiveSession()){
    		mNotifManager.cancel(NOTIF_AVCALL_ID);
    	}
    	else{
    		showNotification(NOTIF_AVCALL_ID, drawableId, "In Call");
    	}
    }
	
	public void showContentShareNotif(int drawableId, String tickerText){
    	showNotification(NOTIF_CONTSHARE_ID, drawableId, tickerText);
    }
	
	public void cancelContentShareNotif(){
    	if(!SgsMsrpSession.hasActiveSession(new SgsPredicate<SgsMsrpSession>() {
			@Override
			public boolean apply(SgsMsrpSession session) {
				return session != null && SgsMediaType.isFileTransfer(session.getMediaType());
			}}))
    	{
    		mNotifManager.cancel(NOTIF_CONTSHARE_ID);
    	}
    }
    
	public void refreshContentShareNotif(int drawableId){
		if(!SgsMsrpSession.hasActiveSession(new SgsPredicate<SgsMsrpSession>() {
			@Override
			public boolean apply(SgsMsrpSession session) {
				return session != null && SgsMediaType.isFileTransfer(session.getMediaType());
			}}))
    	{
    		mNotifManager.cancel(NOTIF_CONTSHARE_ID);
    	}
    	else{
    		showNotification(NOTIF_CONTSHARE_ID, drawableId, "Content sharing");
    	}
    }
	
	public void showContentChatNotif(int drawableId, String tickerText){
    	showNotification(NOTIF_CHAT_ID, drawableId, tickerText);
    }
	
	public void cancelChatNotif(){
    	if(!SgsMsrpSession.hasActiveSession(new SgsPredicate<SgsMsrpSession>() {
			@Override
			public boolean apply(SgsMsrpSession session) {
				return session != null && SgsMediaType.isChat(session.getMediaType());
			}}))
    	{
    		mNotifManager.cancel(NOTIF_CHAT_ID);
    	}
    }
    
	public void refreshChatNotif(int drawableId){
		if(!SgsMsrpSession.hasActiveSession(new SgsPredicate<SgsMsrpSession>() {
			@Override
			public boolean apply(SgsMsrpSession session) {
				return session != null && SgsMediaType.isChat(session.getMediaType());
			}}))
    	{
    		mNotifManager.cancel(NOTIF_CHAT_ID);
    	}
    	else{
    		showNotification(NOTIF_CHAT_ID, drawableId, "Chat");
    	}
    }
	
	public void showSMSNotif(int drawableId, String tickerText){
    	showNotification(NOTIF_SMS_ID, drawableId, tickerText);
    }
	
	public IScreenService getScreenService(){
		if(mScreenService == null){
			mScreenService = new ScreenService();
		}
		return mScreenService;
	}

    public ITetheringService getTetheringService(){
        if(mTetheringService == null){
            mTetheringService = new TetheringService();
        }
        return mTetheringService;
    }

	@Override
	public Class<? extends SgsNativeService> getNativeServiceClass(){
		return NativeService.class;
	}
}
