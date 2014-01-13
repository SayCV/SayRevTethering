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

import java.util.Arrays;
import java.util.Collection;

import org.saydroid.logger.Log;
import org.saydroid.tether.usb.Utils.NetFlags;
import org.saydroid.tether.usb.Utils.NetInterfaceConfig;
import org.saydroid.tether.usb.Utils.NetInterfaceStat;
import org.saydroid.tether.usb.Utils.Sigar;
import org.saydroid.tether.usb.Utils.SigarException;
import org.sufficientlysecure.rootcommands.command.Command;

/**
 * Display network interface configuration and metrics.
 */
public class Ifconfig extends Command {
    private final static String TAG = Ifconfig.class.getCanonicalName();

    private StringBuilder sb = new StringBuilder();
    private int mExitCode;
    private Sigar mSigar;

    protected static Ifconfig sInstance;

    private void Ifconfig() {
    }

    public static Ifconfig getInstance(){
        if(sInstance == null){
            sInstance = new Ifconfig();
        }
        return sInstance;
    }

    public static Sigar getSigar(){
        return Sigar.getInstance();
    }

    public Ifconfig(String... command) {
        super(command);
        Ifconfig();
    }

    public Ifconfig(int timeout, String... command) {
        super(timeout, command);
        Ifconfig();
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

    protected boolean validateArgs(String[] args) {
        return args.length <= 1;
    }

    public String getSyntaxArgs() {
        return "[interface]";
    }

    public String getUsageShort() {
        return "Network interface information";
    }

    public Collection getCompletions() {
        String[] ifNames;

        try {
            ifNames = getSigar().getNetInterfaceList();
        } catch (SigarException e) {
            return null;//getSigar().getCompletions();
        }

        return Arrays.asList(ifNames);
    }

    public void output(String[] args) throws SigarException {
        String[] ifNames;

        if (args.length == 1) {
            ifNames = args;
        }
        else {
            ifNames = getSigar().getNetInterfaceList();
        }

        for (int i=0; i<ifNames.length; i++) {
            try {
                output(ifNames[i]);
            } catch (SigarException e) {
                Log.d(TAG, ifNames[i] + "\t" + e.getMessage());
            }
        }
    }

    public void output(String name) throws SigarException {
        NetInterfaceConfig ifconfig =
                getSigar().getNetInterfaceConfig(name);
        long flags = ifconfig.getFlags();

        String hwaddr = "";
        if (!NetFlags.NULL_HWADDR.equals(ifconfig.getHwaddr())) {
            hwaddr = " HWaddr " + ifconfig.getHwaddr();
        }

        if (!ifconfig.getName().equals(ifconfig.getDescription())) {
            Log.d(TAG, ifconfig.getDescription());
        }

        Log.d(TAG, ifconfig.getName() + "\t" +
                "Link encap:" + ifconfig.getType() +
                hwaddr);

        String ptp = "";
        if ((flags & NetFlags.IFF_POINTOPOINT) > 0) {
            ptp = "  P-t-P:" + ifconfig.getDestination();
        }

        String bcast = "";
        if ((flags & NetFlags.IFF_BROADCAST) > 0) {
            bcast = "  Bcast:" + ifconfig.getBroadcast();
        }

        Log.d(TAG, "\t" +
                "inet addr:" + ifconfig.getAddress() + 
                ptp + //unlikely
                bcast +
                "  Mask:" + ifconfig.getNetmask());

        if (ifconfig.getPrefix6Length() != 0) {
            Log.d(TAG, "\t" +
                    "inet6 addr: " + ifconfig.getAddress6() + "/" +
                    ifconfig.getPrefix6Length() +
                    " Scope:" + NetFlags.getScopeString(ifconfig.getScope6()));
        }

        Log.d(TAG, "\t" +
                NetFlags.getIfFlagsString(flags) +
                " MTU:" + ifconfig.getMtu() +
                "  Metric:" + ifconfig.getMetric());
        try {
            NetInterfaceStat ifstat =
                getSigar().getNetInterfaceStat(name);

            Log.d(TAG, "\t" +
                    "RX packets:" + ifstat.getRxPackets() +
                    " errors:" + ifstat.getRxErrors() +
                    " dropped:" + ifstat.getRxDropped() +
                    " overruns:" + ifstat.getRxOverruns() +
                    " frame:" + ifstat.getRxFrame());

            Log.d(TAG, "\t" +
                    "TX packets:" + ifstat.getTxPackets() +
                    " errors:" + ifstat.getTxErrors() +
                    " dropped:" + ifstat.getTxDropped() +
                    " overruns:" + ifstat.getTxOverruns() +
                    " carrier:" + ifstat.getTxCarrier());
            Log.d(TAG, "\t" + "collisions:" +
                    ifstat.getTxCollisions());

            long rxBytes = ifstat.getRxBytes();
            long txBytes = ifstat.getTxBytes();

            Log.d(TAG, "\t" +
                    "RX bytes:" + rxBytes +
                    " (" + Sigar.formatSize(rxBytes) + ")" +
                    "  " +
                    "TX bytes:" + txBytes + 
                    " (" + Sigar.formatSize(txBytes) + ")");
        } catch (SigarException e) {
        }

        Log.d(TAG, "");
    }

    public static void main(String[] args) throws Exception {
        new Ifconfig(args);
    }
}
