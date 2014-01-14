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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.saydroid.logger.Log;
import org.saydroid.tether.usb.Utils.NetConnection;
import org.saydroid.tether.usb.Utils.NetFlags;
import org.saydroid.tether.usb.Utils.Sigar;
import org.saydroid.tether.usb.Utils.SigarException;
import org.saydroid.tether.usb.Utils.Tcp;
import org.sufficientlysecure.rootcommands.command.Command;

/**
 * Display network connections.
 */
public class Netstat extends NetCommand {
    private final static String TAG = Ifconfig.class.getCanonicalName();

    protected static Netstat sInstance;
    
    private static final int LADDR_LEN = 20;
    private static final int RADDR_LEN = 35;

    private static final String[] HEADER = new String[] {
        "Proto",
        "Local Address",
        "Foreign Address",
        "State",
        ""
    };

    private static boolean isNumeric, wantPid, isStat;

    private StringBuilder sb = new StringBuilder();
    private int mExitCode;

    public Netstat(String... command) {
        super(command);
    }

    public Netstat(int timeout, String... command) {
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

    protected boolean validateArgs(String[] args) {
        return true;
    }

    public String getUsageShort() {
        return "Display network connections";
    }

    //poor mans getopt.
    public static int getFlags(String[] args, int flags) {
        int proto_flags = 0;
        isNumeric = false;
        wantPid = false;
        isStat = false;

        for (int i=0; i<args.length; i++) {
            String arg = args[i];
            int j = 0;

            while (j<arg.length()) {
                switch (arg.charAt(j++)) {
                  case '-':
                    continue;
                  case 'l':
                    flags &= ~NetFlags.CONN_CLIENT;
                    flags |= NetFlags.CONN_SERVER;
                    break;
                  case 'a':
                    flags |= NetFlags.CONN_SERVER | NetFlags.CONN_CLIENT;
                    break;
                  case 'n':
                    isNumeric = true;
                    break;
                  case 'p':
                    wantPid = true;
                    break;
                  case 's':
                    isStat = true;
                    break;
                  case 't':
                    proto_flags |= NetFlags.CONN_TCP;
                    break;
                  case 'u':
                    proto_flags |= NetFlags.CONN_UDP;
                    break;
                  case 'w':
                    proto_flags |= NetFlags.CONN_RAW;
                    break;
                  case 'x':
                    proto_flags |= NetFlags.CONN_UNIX;
                    break;
                  default:
                    Log.e(TAG, "unknown option");
                }
            }
        }

        if (proto_flags != 0) {
            flags &= ~NetFlags.CONN_PROTOCOLS;
            flags |= proto_flags;
        }

        return flags;
    }

    private String formatPort(int proto, long port) {
        if (port == 0) {
            return "*";
        }
        if (!isNumeric) {
            String service = getSigar().getNetServicesName(proto, port);
            if (service != null) {
                return service;
            }
        }
        return String.valueOf(port);
    }

    private String formatAddress(int proto, String ip,
                                 long portnum, int max) {
        
        String port = formatPort(proto, portnum);
        String address;

        if (NetFlags.isAnyAddress(ip)) {
            address = "*";
        }
        else if (isNumeric) {
            address = ip;
        }
        else {
            try {
                address = InetAddress.getByName(ip).getHostName();
            } catch (UnknownHostException e) {
                address = ip;
            }
        }

        max -= port.length() + 1;
        if (address.length() > max) {
            address = address.substring(0, max);
        }

        return address + ":" + port; 
    }

    private void outputTcpStats() throws SigarException {
        Tcp stat = super.getSigar().getTcp();
        final String dnt = "    ";
        Log.d(TAG, dnt + stat.getActiveOpens() + " active connections openings");
        Log.d(TAG, dnt + stat.getPassiveOpens() + " passive connection openings");
        Log.d(TAG, dnt + stat.getAttemptFails() + " failed connection attempts");
        Log.d(TAG, dnt + stat.getEstabResets() + " connection resets received");
        Log.d(TAG, dnt + stat.getCurrEstab() + " connections established");
        Log.d(TAG, dnt + stat.getInSegs() + " segments received");
        Log.d(TAG, dnt + stat.getOutSegs() + " segments send out");
        Log.d(TAG, dnt + stat.getRetransSegs() + " segments retransmited");
        Log.d(TAG, dnt + stat.getInErrs() + " bad segments received.");
        Log.d(TAG, dnt + stat.getOutRsts() + " resets sent");
    }

    private void outputStats(int flags) throws SigarException {
        if ((flags & NetFlags.CONN_TCP) != 0) {
            Log.d(TAG, "Tcp:");
            try {
                outputTcpStats();
            } catch (SigarException e) {
                Log.d(TAG, "    " + e);
            }
        }
    }

    //XXX currently weak sauce.  should end up like netstat command.
    public void output(String[] args) throws SigarException {
        //default
        int flags = NetFlags.CONN_CLIENT | NetFlags.CONN_PROTOCOLS;

        if (args.length > 0) {
            flags = getFlags(args, flags);
            if (isStat) {
                outputStats(flags);
                return;
            }
        }

        NetConnection[] connections = getSigar().getNetConnectionList(flags);
        Log.d(TAG, HEADER.toString());

        for (int i=0; i<connections.length; i++) {
            NetConnection conn = connections[i];
            String proto = conn.getTypeString();
            String state;

            if (conn.getType() == NetFlags.CONN_UDP) {
                state = "";
            }
            else {
                state = conn.getStateString();
            }

            ArrayList items = new ArrayList();
            items.add(proto);
            items.add(formatAddress(conn.getType(),
                                    conn.getLocalAddress(),
                                    conn.getLocalPort(),
                                    LADDR_LEN));
            items.add(formatAddress(conn.getType(),
                                    conn.getRemoteAddress(),
                                    conn.getRemotePort(),
                                    RADDR_LEN));
            items.add(state);

            String process = null;
            if (wantPid &&
                //XXX only works w/ listen ports
                (conn.getState() == NetFlags.TCP_LISTEN))
            {
                //try {
                    long pid = 0;
                        //getSigar().getProcPort(conn.getType(),
                        //                       conn.getLocalPort());
                    if (pid != 0) { //XXX another bug
                        String name = "null";
                            //getSigar().getProcState(pid).getName();
                        process = pid + "/" + name;
                    }
                //} catch (SigarException e) { }
            }

            if (process == null) {
                process = "";
            }

            items.add(process);

            Log.d(TAG, items.toString());
        }
    }

    public static void main(String[] args) throws Exception {
        new Netstat(args);
    }
}
