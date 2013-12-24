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
package org.saydroid.sgs.media;

import java.math.BigInteger;
import java.util.Hashtable;

import org.saydroid.tinyWRAP.MediaSessionMgr;
import org.saydroid.tinyWRAP.ProxyAudioConsumer;
import org.saydroid.tinyWRAP.ProxyAudioProducer;
import org.saydroid.tinyWRAP.ProxyPlugin;
import org.saydroid.tinyWRAP.ProxyPluginMgr;
import org.saydroid.tinyWRAP.ProxyPluginMgrCallback;
import org.saydroid.tinyWRAP.ProxyVideoConsumer;
import org.saydroid.tinyWRAP.ProxyVideoProducer;
import org.saydroid.tinyWRAP.tmedia_bandwidth_level_t;
import org.saydroid.tinyWRAP.tmedia_chroma_t;
import org.saydroid.tinyWRAP.twrap_proxy_plugin_type_t;

import android.util.Log;

/***
 * MyProxyPluginMgr
 */
public class SgsProxyPluginMgr {
	private static final String TAG = SgsProxyPluginMgr.class.getCanonicalName();
	private static final MyProxyPluginMgrCallback sMyProxyPluginMgrCallback  = new MyProxyPluginMgrCallback();
	private static final ProxyPluginMgr sPluginMgr = ProxyPluginMgr.createInstance(sMyProxyPluginMgrCallback);
	private static final Hashtable<BigInteger, SgsProxyPlugin>sPlugins = new Hashtable<BigInteger, SgsProxyPlugin>(); // HashTable is synchronized
	
	public static void Initialize() {
        ProxyVideoConsumer.setDefaultChroma(tmedia_chroma_t.tmedia_chroma_rgb565le);
        ProxyVideoConsumer.setDefaultAutoResizeDisplay(true);
        ProxyVideoProducer.setDefaultChroma(tmedia_chroma_t.tmedia_chroma_nv21);
        
        // these values will be updated by the engine using ones stored using the
        // configuration service
        MediaSessionMgr.defaultsSetAgcEnabled(false);
        MediaSessionMgr.defaultsSetBandwidthLevel(tmedia_bandwidth_level_t.tmedia_bl_unrestricted);
        MediaSessionMgr.defaultsSetEchoSuppEnabled(false);
        MediaSessionMgr.defaultsSetVadEnabled(false);

        MediaSessionMgr.defaultsSetNoiseSuppEnabled(false);
        MediaSessionMgr.defaultsSetEchoTail(0);
	}
	
	private SgsProxyPluginMgr(){
		
	}
	
	public static ProxyPlugin findNativePlugin(BigInteger id){
		return sPluginMgr.findPlugin(id);
	}
	
	public static SgsProxyPlugin findPlugin(BigInteger id){
		return sPlugins.get(id);
	}
	
	/**
	 * MyProxyPluginMgrCallback
	 */
	static class MyProxyPluginMgrCallback extends ProxyPluginMgrCallback
	{
		MyProxyPluginMgrCallback(){
			super();
		}
		
		@Override
		public int OnPluginCreated(BigInteger id, twrap_proxy_plugin_type_t type) {
			Log.d(TAG, "OnPluginCreated("+id+","+ type+")");
			switch(type){
				case twrap_proxy_plugin_audio_producer:
				{	
					synchronized(this){
						ProxyAudioProducer producer = sPluginMgr.findAudioProducer(id);
						if(producer != null){
							SgsProxyAudioProducer myProducer = new SgsProxyAudioProducer(id, producer);
							sPlugins.put(id, myProducer);
						}
					}
					break;
				}
				case twrap_proxy_plugin_video_producer:
				{
					synchronized(this){
						ProxyVideoProducer producer = sPluginMgr.findVideoProducer(id);
						if(producer != null){
							SgsProxyVideoProducer myProducer = new SgsProxyVideoProducer(id, producer);
							sPlugins.put(id, myProducer);
						}
					}
					break;
				}
				case twrap_proxy_plugin_audio_consumer:
				{
					synchronized(this){
						ProxyAudioConsumer consumer = sPluginMgr.findAudioConsumer(id);
						if(consumer != null){
							SgsProxyAudioConsumer myConsumer = new SgsProxyAudioConsumer(id, consumer);
							sPlugins.put(id, myConsumer);
						}
					}
					break;
				}
				case twrap_proxy_plugin_video_consumer:
				{
					synchronized(this){
						ProxyVideoConsumer consumer = sPluginMgr.findVideoConsumer(id);
						if(consumer != null){
							SgsProxyVideoConsumer myConsumer = new SgsProxyVideoConsumer(id, consumer);
							sPlugins.put(id, myConsumer);
						}
					}
					break;
				}
				default:
				{
					Log.e(TAG, "Invalid Plugin type");
					return -1;
				}
			}
			return 0;
		}

		@Override
		public int OnPluginDestroyed(BigInteger id, twrap_proxy_plugin_type_t type) {
			Log.d(TAG, "OnPluginDestroyed("+id+","+ type+")");
			switch(type){
				case twrap_proxy_plugin_audio_producer:
				case twrap_proxy_plugin_video_producer:
				case twrap_proxy_plugin_audio_consumer:
				case twrap_proxy_plugin_video_consumer:
				{
					synchronized(this){
						SgsProxyPlugin plugin = sPlugins.get(id);
						if(plugin != null){
							plugin.invalidate();
							sPlugins.remove(id);
							return 0;
						}
						else{
							Log.e(TAG, "Failed to find plugin");
							return -1;
						}
					}
				}
				default:
				{
					Log.e(TAG, "Invalid Plugin type");
					return -1;
				}
			}
		}
	}
}
