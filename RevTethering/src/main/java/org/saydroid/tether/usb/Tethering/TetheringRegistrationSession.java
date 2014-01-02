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
import org.saydroid.sgs.utils.SgsFileUtils;

import java.util.Date;

/**
 * Registration state
 */
public class TetheringRegistrationSession extends TetheringSession {
    private static final String TAG = TetheringRegistrationSession.class.getCanonicalName();

    //private final TetheringRegistrationSession mSession;

    private String mTetherNetworkDevice = "";
    private Thread mTrafficCounterThread = null;
    public class DataCount {
        // Total data uploaded
        public long totalUpload;
        // Total data downloaded
        public long totalDownload;
        // Current upload rate
        public long uploadRate;
        // Current download rate
        public long downloadRate;
    }

    /**
     * Creates new registration session
     * @param tetheringStack the stack to use to create the session
     */
    public TetheringRegistrationSession(TetheringStack tetheringStack){
        super(tetheringStack);
        //mSession = new TetheringRegistrationSession(tetheringStack);

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
        Log.d(TAG, "Data rx: " + dataCount[0] + ", tx: " + dataCount[1]);
        return dataCount;
    }

    public void setTrafficCounterThreadClassEnabled(boolean enabled) {
        if (enabled == true) {
            if (this.mTrafficCounterThread == null || this.mTrafficCounterThread.isAlive() == false) {
                this.mTrafficCounterThread = new Thread(new TrafficCounterThreadClass());
                this.mTrafficCounterThread.start();
            }
        } else {
            if (this.mTrafficCounterThread != null)
                this.mTrafficCounterThread.interrupt();
        }
    }


    class TrafficCounterThreadClass implements Runnable {
        private static final int INTERVAL = 2;  // Sample rate in seconds.
        long previousDownload;
        long previousUpload;
        long lastTimeChecked;

        //@Override
        public void run() {
            this.previousDownload = this.previousUpload = 0;
            this.lastTimeChecked = new Date().getTime();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // nothing
            }

            long [] trafficCountAtStart = getDataTraffic(mTetherNetworkDevice);

            while (!Thread.currentThread().isInterrupted()) {
                // Check data count
                long [] trafficCount = getDataTraffic(mTetherNetworkDevice);
                long currentTime = new Date().getTime();
                float elapsedTime = (float) ((currentTime - this.lastTimeChecked) / 1000);
                this.lastTimeChecked = currentTime;
                DataCount datacount = new DataCount();
                datacount.totalUpload = trafficCount[0]-trafficCountAtStart[0];
                datacount.totalDownload = trafficCount[1]-trafficCountAtStart[1];
                datacount.uploadRate = (long) ((datacount.totalUpload - this.previousUpload)*8/elapsedTime);
                datacount.downloadRate = (long) ((datacount.totalDownload - this.previousDownload)*8/elapsedTime);
                /*Message message = Message.obtain();
                message.what = MainActivity.MESSAGE_TRAFFIC_COUNT;
                message.obj = datacount;
                MainActivity.currentInstance.viewUpdateHandler.sendMessage(message);*/
                this.previousUpload = datacount.totalUpload;
                this.previousDownload = datacount.totalDownload;
                try {
                    Thread.sleep(INTERVAL * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            /*Message message = Message.obtain();
            message.what = MainActivity.MESSAGE_TRAFFIC_END;
            MainActivity.currentInstance.viewUpdateHandler.sendMessage(message);*/

        }
    }
}