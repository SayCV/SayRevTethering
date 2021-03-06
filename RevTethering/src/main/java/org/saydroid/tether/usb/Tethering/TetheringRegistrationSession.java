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

package org.saydroid.tether.usb.Tethering;


import org.saydroid.logger.Log;
import org.saydroid.sgs.services.ISgsConfigurationService;
import org.saydroid.sgs.utils.SgsConfigurationEntry;
import org.saydroid.sgs.utils.SgsDateTimeUtils;
import org.saydroid.sgs.utils.SgsFileUtils;
import org.saydroid.tether.usb.Engine;
import org.saydroid.tether.usb.Events.TrafficCountEventArgs;
import org.saydroid.tether.usb.Events.TrafficCountEventArgs.DataCount;
import org.saydroid.tether.usb.Events.TrafficCountEventTypes;
import org.saydroid.tether.usb.Services.Impl.TetheringService;

import java.util.Date;

/**
 * Registration state
 */
public class TetheringRegistrationSession extends TetheringSession {
    private static final String TAG = TetheringRegistrationSession.class.getCanonicalName();

    //private final TetheringRegistrationSession mSession;
    private static boolean sTrafficCounterThreadEndWithOnce = false;
    private String mTetherNetworkDevice = "";
    private Thread mTrafficCounterThread = null;

    private final ISgsConfigurationService mConfigurationService;

    /**
     * Creates new registration session
     * @param tetheringStack the stack to use to create the session
     */
    public TetheringRegistrationSession(TetheringStack tetheringStack){
        super(tetheringStack);
        //mSession = new TetheringRegistrationSession(tetheringStack);

        this.mConfigurationService = Engine.getInstance().getConfigurationService();
        super.init();
    }

    /**
     * Sends SIP REGISTER request
     * @return true if succeed and false otherwise
     */
    public boolean register(){
        //return mSession.register_();
        setTrafficCounterThreadClassEnabled(true);
        return true;
    }

    /**
     * Unregisters (SIP REGISTER with expires=0)
     * @return true if succeed and false otherwise
     */
    public boolean unregister(){
        //return mSession.unRegister();
        setTrafficCounterThreadClassEnabled(false);
        return true;
    }

    // @Override
    // protected TetheringSession getSession() { return mSession; }

    public void setTetheringNetworkDevice(String tetherNetworkDevice) { mTetherNetworkDevice = tetherNetworkDevice; }

    protected long[] getDataTraffic(String device) {
        // Returns traffic usage for all interfaces starting with 'device'.
        long [] dataCount = new long[] {0, 0};
        if (device == "")
            return dataCount;
        for (String line : SgsFileUtils.readLinesFromFile("/proc/net/dev")) {
            if (line.startsWith(device) == false)
                continue;
            line = line.replace(':', ' ');
            String[] values = line.split(" +");
            dataCount[0] += Long.parseLong(values[1]);
            dataCount[1] += Long.parseLong(values[9]);
        }
        //logger.debug(MSG_TAG+"Data rx: " + dataCount[0] + ", tx: " + dataCount[1]);
       android.util.Log.d(TAG, "Data rx: " + dataCount[0] + ", tx: " + dataCount[1]);
        return dataCount;
    }

    public void setTrafficCounterThreadClassEnabled(boolean enabled) {
        if (enabled == true) {
            if (mTrafficCounterThread == null || mTrafficCounterThread.isAlive() == false) {
                mTrafficCounterThread = new Thread(new TrafficCounterThreadClass());
                mTrafficCounterThread.start();
            }
        } else {
            if (mTrafficCounterThread != null)
                mTrafficCounterThread.interrupt();
        }
    }


    class TrafficCounterThreadClass implements Runnable {
        private static final int INTERVAL = 2;  // Sample rate in seconds.

        private static final int NETWORK_TRAFFIC_COUNT_THREAD_STATE_NONE = 0;
        private static final int NETWORK_TRAFFIC_COUNT_THREAD_STATE_START = 1;

        long previousDownload;
        long previousUpload;
        String firstTimeChecked;
        String lastTimeChecked;
        //long [] trafficCount = new long[] {0, 0};

        //@Override
        public void run() {
            sTrafficCounterThreadEndWithOnce = false;
            this.previousDownload = this.previousUpload = 0;
            this.firstTimeChecked = SgsDateTimeUtils.now();// new Date().getTime();
            this.lastTimeChecked = SgsDateTimeUtils.now();
            if( NETWORK_TRAFFIC_COUNT_THREAD_STATE_NONE == mConfigurationService.getInt(
                    SgsConfigurationEntry.NETWORK_TRAFFIC_COUNT_THREAD_STATE,
                    SgsConfigurationEntry.DEFAULT_NETWORK_TRAFFIC_COUNT_THREAD_STATE) ){
                // DEFAULT_NETWORK_TRAFFIC_COUNT_AT_START
                mConfigurationService.putString(
                        SgsConfigurationEntry.NETWORK_TRAFFIC_COUNT_AT_START,
                        this.firstTimeChecked);
                mConfigurationService.putInt(
                        SgsConfigurationEntry.NETWORK_TRAFFIC_COUNT_THREAD_STATE,
                        NETWORK_TRAFFIC_COUNT_THREAD_STATE_START);
                mConfigurationService.commit();
            } else {
                this.firstTimeChecked = String.valueOf(mConfigurationService.getString(
                        SgsConfigurationEntry.NETWORK_TRAFFIC_COUNT_AT_START,
                        //SgsConfigurationEntry.DEFAULT_NETWORK_TRAFFIC_COUNT_AT_START));
                        SgsDateTimeUtils.now()));
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // nothing
            }

            //long [] trafficCountAtStart = getDataTraffic(mTetherNetworkDevice);
            long [] trafficCountAtStart = new long[] {0, 0};
            /*trafficCountAtStart[0] = (long)Engine.getInstance().getConfigurationService().getFloat(
                    SgsConfigurationEntry.NETWORK_TRAFFIC_COUNT_RX_AT_START, SgsConfigurationEntry.DEFAULT_NETWORK_TRAFFIC_RX_COUNT_AT_START);
            trafficCountAtStart[1] = (long)Engine.getInstance().getConfigurationService().getFloat(
                    SgsConfigurationEntry.NETWORK_TRAFFIC_COUNT_TX_AT_START, SgsConfigurationEntry.DEFAULT_NETWORK_TRAFFIC_TX_COUNT_AT_START);*/

            while (!Thread.currentThread().isInterrupted()) {
                // Check data count
                long [] trafficCount = getDataTraffic(mTetherNetworkDevice);
                //long currentTime = new Date().getTime();
                String currentTime = SgsDateTimeUtils.now();
                float elapsedTime = (float) ((SgsDateTimeUtils.parseDate(currentTime).getTime() - SgsDateTimeUtils.parseDate(this.lastTimeChecked).getTime()) / 1000);
                this.lastTimeChecked = currentTime;
                DataCount datacount = new DataCount();
                datacount.totalUpload = trafficCount[0]-trafficCountAtStart[0];
                datacount.totalDownload = trafficCount[1]-trafficCountAtStart[1];
                //datacount.uploadRate = (long) ((datacount.totalUpload - this.previousUpload)*8/elapsedTime);
                //datacount.downloadRate = (long) ((datacount.totalDownload - this.previousDownload)*8/elapsedTime);
                datacount.uploadRate = (long) ((datacount.totalUpload - this.previousUpload)/elapsedTime);
                datacount.downloadRate = (long) ((datacount.totalDownload - this.previousDownload)/elapsedTime);

                /*Message message = Message.obtain();
                message.what = MainActivity.MESSAGE_TRAFFIC_COUNT;
                message.obj = datacount;
                MainActivity.currentInstance.viewUpdateHandler.sendMessage(message);*/
                ((TetheringService)((Engine)Engine.getInstance()).getTetheringService()).broadcastTrafficCountEvent(
                        new TrafficCountEventArgs(TrafficCountEventTypes.COUNTING,
                                datacount.totalUpload, datacount.totalDownload, datacount.uploadRate, datacount.downloadRate),
                        this.lastTimeChecked//SgsDateTimeUtils.now()
                );

                this.previousUpload = datacount.totalUpload;
                this.previousDownload = datacount.totalDownload;
                /*Engine.getInstance().getConfigurationService().putFloat(
                        SgsConfigurationEntry.NETWORK_TRAFFIC_COUNT_RX_AT_START, this.previousDownload);
                Engine.getInstance().getConfigurationService().putFloat(
                        SgsConfigurationEntry.NETWORK_TRAFFIC_COUNT_TX_AT_START, this.previousUpload);
                Engine.getInstance().getConfigurationService().commit();*/
                try {
                    Thread.sleep(INTERVAL * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            /*Engine.getInstance().getConfigurationService().putFloat(
                    SgsConfigurationEntry.NETWORK_TRAFFIC_COUNT_RX_AT_START, 0l);
            Engine.getInstance().getConfigurationService().putFloat(
                    SgsConfigurationEntry.NETWORK_TRAFFIC_COUNT_TX_AT_START, 0l);
            Engine.getInstance().getConfigurationService().commit();*/
            /*Message message = Message.obtain();
            message.what = MainActivity.MESSAGE_TRAFFIC_END;
            MainActivity.currentInstance.viewUpdateHandler.sendMessage(message);*/
            //long [] trafficCount = getDataTraffic(mTetherNetworkDevice);
            //Log.d(TAG, "Traffic Count Thread previousUpload = " + this.previousUpload);
            //Log.d(TAG, "Traffic Count Thread previousDownload = " + this.previousDownload);
            //Log.d(TAG, "Traffic Count Tx End date = " + this.firstTimeChecked);
            if(sTrafficCounterThreadEndWithOnce == false) {
                sTrafficCounterThreadEndWithOnce = true;
                /*mConfigurationService.putString(
                        SgsConfigurationEntry.NETWORK_TRAFFIC_COUNT_AT_START,
                        this.firstTimeChecked);*/
                mConfigurationService.putInt(
                        SgsConfigurationEntry.NETWORK_TRAFFIC_COUNT_THREAD_STATE,
                        NETWORK_TRAFFIC_COUNT_THREAD_STATE_NONE);
                mConfigurationService.commit();

                ((TetheringService)((Engine)Engine.getInstance()).getTetheringService()).broadcastTrafficCountEvent(
                        new TrafficCountEventArgs(TrafficCountEventTypes.END,
                                this.previousUpload,
                                this.previousDownload,
                                0,
                                0),
                        this.firstTimeChecked//SgsDateTimeUtils.now()
                );
            }
        }
    }
}
