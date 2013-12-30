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

import org.sufficientlysecure.rootcommands.command.Command;

public class SimpleCommand extends Command {
    private StringBuilder sb = new StringBuilder();
    private int mExitCode;

    public SimpleCommand(String... command) {
        super(command);
    }

    public SimpleCommand(int timeout, String... command) {
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

}