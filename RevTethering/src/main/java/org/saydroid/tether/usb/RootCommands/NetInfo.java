/*
 * Copyright (c) 2006 Hyperic, Inc.
 * Copyright (c) 2009 SpringSource, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

package org.saydroid.tether.usb.RootCommands;

import org.saydroid.logger.Log;
import org.saydroid.tether.usb.Utils.NetInterfaceConfig;
import org.saydroid.tether.usb.Utils.Sigar;
import org.saydroid.tether.usb.Utils.SigarException;
import org.sufficientlysecure.rootcommands.command.Command;

/**
 * Display network info.
 */
public class NetInfo extends NetCommand {
    private final static String TAG = Ifconfig.class.getCanonicalName();

    private StringBuilder sb = new StringBuilder();
    private int mExitCode;
    private Sigar mSigar;

    protected static NetInfo sInstance;

    public NetInfo(String... command) {
        super(command);
    }

    public NetInfo(int timeout, String... command) {
        super(timeout, command);
    }

    @Override
    public void output(int id, String line) {
        sb.append(line).append('\n');
    }

    @Override
    public void afterExecution(int id, int exitCode) {
        mExitCode = exitCode;
    }

    public String getOutput() {
        return sb.toString();
    }

    public int getExitCode() {
        return mExitCode;
    }

    public String getUsageShort() {
        return "Display network info";
    }

    public void output(String[] args) throws SigarException {
        NetInterfaceConfig config = getSigar().getNetInterfaceConfig(null);
        Log.d(TAG, "primary interface....." +
                config.getName());

        Log.d(TAG, "primary ip address...." +
                config.getAddress());

        Log.d(TAG, "primary mac address..." +
                config.getHwaddr());

        Log.d(TAG, "primary netmask......." +
                config.getNetmask());

        org.saydroid.tether.usb.Utils.NetInfo info =
                getSigar().getNetInfo();

        Log.d(TAG, "host name............." +
                info.getHostName());

        Log.d(TAG, "domain name..........." +
                info.getDomainName());

        Log.d(TAG, "default gateway......." +
                info.getDefaultGateway() +
                " (" + info.getDefaultGatewayInterface() + ")");

        Log.d(TAG, "primary dns..........." +
                info.getPrimaryDns());

        Log.d(TAG, "secondary dns........." +
                info.getSecondaryDns());
    }

    public static void main(String[] args) throws Exception {
        new NetInfo(args);
    }
}
