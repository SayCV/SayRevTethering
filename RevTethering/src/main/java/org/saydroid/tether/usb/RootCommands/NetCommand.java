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
public class NetCommand extends Command {
    private final static String TAG = Ifconfig.class.getCanonicalName();

    private StringBuilder sb = new StringBuilder();
    private int mExitCode;
    private Sigar mSigar;

    protected static NetCommand sInstance;

    public static void initialize(){

    }

    public static NetCommand getInstance(){
        if(sInstance == null){
            sInstance = new NetCommand();
        }
        return sInstance;
    }

    public static Sigar getSigar(){
        return Sigar.getInstance();
    }

    public NetCommand(String... command) {
        super(command);
    }

    public NetCommand(int timeout, String... command) {
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
        return args.length <= 1;
    }

    public String getSyntaxArgs() {
        return "[interface]";
    }

    public String getUsageShort() {
        return "Network interface information";
    }
}
