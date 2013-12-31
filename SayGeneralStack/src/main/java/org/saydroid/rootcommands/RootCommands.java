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

package org.saydroid.rootcommands;

import org.saydroid.logger.Log;
import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.Toolbox;

public final class RootCommands {
    private static String TAG = RootCommands.class.getCanonicalName();

    public static boolean run(String command, StringBuilder sb) {
        Log.d(TAG, "Root-Commands ==> su" + " -c \"" + command + "\"");
        int returnCode = -1;
        try {
            //MyCommand binaryCommand = new MyCommand("su" + " -c \""+command+"\"", "");
            SimpleCommand binaryCommand = new SimpleCommand(command);

            // start root shell
            Shell shell = Shell.startRootShell();

            //shell.add(binaryCommand);
            shell.add(binaryCommand).waitForFinish();

            Log.d(TAG, "Output of command: " + binaryCommand.getOutput());
            returnCode = binaryCommand.getExitCode();
            sb.append(binaryCommand.getOutput());

            // close root shell
            shell.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception!", e);
        }

        if (returnCode == 0) {
            return true;
        }
        Log.d(TAG, "Root-Commands error, return code: " + returnCode);
        return false;
    }

    public static boolean run(String command) {
        Log.d(TAG, "Root-Commands ==> su" + " -c \"" + command + "\"");
        int returnCode = -1;
        try {
            //MyCommand binaryCommand = new MyCommand("su" + " -c \""+command+"\"", "");
            SimpleCommand binaryCommand = new SimpleCommand(command);

            // start root shell
            Shell shell = Shell.startRootShell();

            //shell.add(binaryCommand);
            shell.add(binaryCommand).waitForFinish();

            Log.d(TAG, "Output of command: " + binaryCommand.getOutput());
            returnCode = binaryCommand.getExitCode();

            // close root shell
            shell.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception!", e);
        }

        if (returnCode == 0) {
            return true;
        }
        Log.d(TAG, "Root-Commands error, return code: " + returnCode);
        return false;
    }

    public static boolean run(int timeout, String command) {
        Log.d(TAG, "Root-Commands ==> su" + " -c \"" + command + "\"");
        int returnCode = -1;
        try {
            //MyCommand binaryCommand = new MyCommand("su" + " -c \""+command+"\"", "");
            SimpleCommand binaryCommand = new SimpleCommand(timeout, command);

            // start root shell
            Shell shell = Shell.startRootShell();

            //shell.add(binaryCommand);
            shell.add(binaryCommand).waitForFinish();

            Log.d(TAG, "Output of command: " + binaryCommand.getOutput());
            returnCode = binaryCommand.getExitCode();

            // close root shell
            shell.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception!", e);
        }

        if (returnCode == 0) {
            return true;
        }
        Log.d(TAG, "Root-Commands error, return code: " + returnCode);
        return false;
    }

    public static boolean hasRootPermission() {
        boolean resultCode = false;
        try {
            Shell shell = Shell.startRootShell();

            Toolbox tb = new Toolbox(shell);

            if (tb.isRootAccessGiven()) {
                Log.d(TAG, "Root access given!");
                resultCode = true;
            } else {
                Log.d(TAG, "No root access!");
                resultCode = false;
            }
            shell.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception!", e);
        }
        return resultCode;
    }

}
