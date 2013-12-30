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

import org.saydroid.rootcommands.RootCommands;
import org.saydroid.tether.usb.Services.IScreenService;
import org.saydroid.tether.usb.Services.ITetheringNetworkService;
import org.saydroid.tether.usb.Services.ITetheringService;
import org.saydroid.tether.usb.Services.Impl.ScreenService;
import org.saydroid.sgs.SgsApplication;
import org.saydroid.sgs.SgsEngine;
import org.saydroid.sgs.SgsNativeService;
import org.saydroid.sgs.media.SgsMediaType;
import org.saydroid.sgs.sip.SgsAVSession;
import org.saydroid.sgs.sip.SgsMsrpSession;
import org.saydroid.sgs.utils.SgsPredicate;
import org.saydroid.tether.usb.Services.Impl.TetheringNetworkService;
import org.saydroid.tether.usb.Services.Impl.TetheringService;
import org.saydroid.utils.AndroidUtils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

import org.saydroid.logger.Log;

import java.io.File;

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

    private static final String SETTING_DB_PATH = "/data/data/com.android.providers.settings/databases/";
    private  static final String mGlobalSetting_tether_supported = "tether_supported"; //valid setting is 1
    private  static final String mGlobalSetting_tether_dun_required = "tether_dun_required"; //valid setting is 0

	
	private IScreenService mScreenService;
    protected ITetheringNetworkService mTetheringNetworkService;
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
        Engine.initialize();
	}

	public static SgsEngine getInstance(){
		if(sInstance == null){
			sInstance = new Engine();
		}
		return sInstance;
	}
	
	public Engine(){
		super();

        boolean mSupportedKernel = false;

        if (!this.hasRootPermission()){
            mSupportedKernel = false;
        }

        if (this.binariesExists() == false) {
            if (this.hasRootPermission()) {
                this.installFiles();
            }
        }
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

    public ITetheringNetworkService getTetheringNetworkService(){
        if(mTetheringNetworkService == null){
            mTetheringNetworkService = new TetheringNetworkService();
        }
        return mTetheringNetworkService;
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

    public boolean hasRootPermission() {
        return RootCommands.hasRootPermission();
    }

    public boolean binariesExists() {
        File file_ifconfig = new File(this.DATA_FOLDER + "/bin/ifconfig");
        File file_route = new File(this.DATA_FOLDER + "/bin/route");
        return (file_ifconfig.exists() && file_route.exists());
    }

    public void installFiles() {
        new Thread(new Runnable(){
            public void run(){
                String message = null;

                // tether
                if (message == null) {
                    message = TetherApplication.this.copyBinary(TetherApplication.this.coretask.DATA_FILE_PATH+"/bin/tether", R.raw.tether);
                }
                // iptables
                if (message == null) {
                    message = TetherApplication.this.copyBinary(TetherApplication.this.coretask.DATA_FILE_PATH+"/bin/iptables", R.raw.iptables);
                }
                // dnsmasq
                if (message == null) {
                    message = TetherApplication.this.copyBinary(TetherApplication.this.coretask.DATA_FILE_PATH+"/bin/dnsmasq", R.raw.dnsmasq);
                }
                // ifconfig
                if (message == null) {
                    message = TetherApplication.this.copyBinary(TetherApplication.this.coretask.DATA_FILE_PATH+"/bin/ifconfig", R.raw.ifconfig);
                }
                // sqlite3
                if (message == null) {
                    message = TetherApplication.this.copyBinary(TetherApplication.this.coretask.DATA_FILE_PATH+"/bin/sqlite3", R.raw.sqlite3);
                }
                //add route command from busybox to serve the setup gw purpose.
                if (message == null) {
                    message = TetherApplication.this.copyBinary(TetherApplication.this.coretask.DATA_FILE_PATH+"/bin/route", R.raw.route);
                }
                try {
                    TetherApplication.this.coretask.chmodBin();
                } catch (Exception e) {
                    message = "Unable to change permission on binary files!";
                }
                // version
                if (message == null) {
                    TetherApplication.this.copyBinary(TetherApplication.this.coretask.DATA_FILE_PATH+"/conf/version", R.raw.version);
                }
                // text
                if (message == null) {
                    TetherApplication.this.copyBinary(TetherApplication.this.coretask.DATA_FILE_PATH+"/setting.txt", R.raw.setting);
                    TetherApplication.this.copyBinary(TetherApplication.this.coretask.DATA_FILE_PATH+"/maxid.txt", R.raw.maxid);
                    TetherApplication.this.copyBinary(TetherApplication.this.coretask.DATA_FILE_PATH+"/maxid_exit.txt", R.raw.maxid_exit);
                    TetherApplication.this.copyBinary(TetherApplication.this.coretask.DATA_FILE_PATH+"/conf/route.conf", R.raw.route_conf);
                }
                if (message == null) {
                    message = "Binaries and config-files installed!";
                }

                // Removing ols lan-config-file
                File lanConfFile = new File(TetherApplication.this.coretask.DATA_FILE_PATH+"/conf/lan_network.conf");
                if (lanConfFile.exists()) {
                    lanConfFile.delete();
                }

                // Sending message
                Message msg = new Message();
                msg.obj = message;
                TetherApplication.this.displayMessageHandler.sendMessage(msg);
            }
        }).start();
    }

    /*
         * this function dumps the securesetting to a txt file. If success, return true, otherwise false
         * do not use new thread to run it now.
         */
    private boolean dumpGlobalSettings(){
        String dumpGlobalSettings = "echo \'.dump global\' | sqlite3 " +
                this.SETTING_DB_PATH + "settings.db  > " +
                this.DATA_FOLDER + "/setting.txt";
        Log.d(TAG, "command for dumping the GlobalSettings is: " + dumpGlobalSettings);
        if(RootCommands.run(dumpGlobalSettings)==false){
            Log.e(TAG, "Unable to dump the GlobalSettings to " + this.DATA_FOLDER + "/settings.txt");
            return false;
        }
        return true;
    }

    private boolean dumpGlobalSettingsMaxID(){
        String dumpGlobalSettingsMaxId = "sqlite3 " + this.SETTING_DB_PATH +
                "settings.db \'select max(_id) from global\'"+  " > " +
                this.DATA_FOLDER + "/maxid_exit.txt";
        Log.d(TAG, "command for dumping the max id is: " + dumpGlobalSettingsMaxId);
        if(RootCommands.run(dumpGlobalSettingsMaxId)==false){
            Log.e(TAG, "Unable to dump the global setting maxid to" + this.DATA_FOLDER + "maxid_exit.txt");
            return false;
        }
        return true;
    }
}
