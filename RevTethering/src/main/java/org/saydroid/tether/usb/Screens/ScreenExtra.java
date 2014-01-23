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

package org.saydroid.tether.usb.Screens;

import org.saydroid.logger.Log;
import org.saydroid.rootcommands.RootCommands;
import org.saydroid.sgs.events.SgsEventArgs;
import org.saydroid.sgs.events.SgsRegistrationEventArgs;
import org.saydroid.sgs.services.ISgsConfigurationService;
import org.saydroid.sgs.utils.SgsConfigurationEntry;
import org.saydroid.sgs.utils.SgsStringUtils;
import org.saydroid.tether.usb.CustomExtends.NetworkLinkStatus;
import org.saydroid.tether.usb.EmbeddedFileExplorer.EmbeddedFileExplorerConstants;
import org.saydroid.tether.usb.EmbeddedFileExplorer.FileListAdapter;
import org.saydroid.tether.usb.EmbeddedFileExplorer.FilePersistence;
import org.saydroid.tether.usb.EmbeddedFileExplorer.GenericFileExplorer;
import org.saydroid.tether.usb.Events.TrafficCountEventArgs;
import org.saydroid.tether.usb.RootCommands.NetInfo;
import org.saydroid.tether.usb.SRTDroid;
import org.saydroid.tether.usb.R;
import org.saydroid.tether.usb.Services.ITetheringNetworkService;
import org.saydroid.tether.usb.Services.ITetheringService;
import org.saydroid.tether.usb.Services.Impl.TetheringNetworkService;
import org.saydroid.tether.usb.Services.Impl.TetheringService;
import org.sufficientlysecure.rootcommands.Shell;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class ScreenExtra extends BaseScreen {
    private static final String TAG = ScreenAbout.class.getCanonicalName();

    private Thread mLinkUpdateThread = null;

    private ListView mLvFileExplorer;
    private TextView mTvFileExplorerNoMatchesIndicator;
    private Button mBtnFileExploreCopyAll;
    private Button mBtnFileExploreCopy;
    private Button mBtnFileExplorerRunTest;
    private RadioButton mRbFileExplorerSelectedAll;
    private RadioButton mRbFileExplorerUnselectedAll;

    private final String no_file_system_access_message_text = "No File System Access";

    private boolean isFileExplorerEnabled = false;
    private boolean isFileExplorerMultiChoiceEnabled = false;
    private File currentDirectory;

    public StringBuilder sbMultiChoiceFileNames;
    private final String mRunTestDirectory;
    private final String SYSTEM_BIN_FOLDER = "/system/bin";
    private final String SYSTEM_LIB_FOLDER = "/system/lib";

    //private final GenericFileExplorer mGenericFileExplorer;
    private final ISgsConfigurationService mConfigurationService;

    private BroadcastReceiver mLinkUpdateBroadCastRecv;

    public ScreenExtra() {
        super(SCREEN_TYPE.Extra_T, TAG);

        mConfigurationService = getEngine().getConfigurationService();
        sbMultiChoiceFileNames = new StringBuilder();
        mRunTestDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()).toString() + "/tests";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_extral);

        this.mLvFileExplorer = (ListView) findViewById(R.id.screen_extra_listView);
        this.mLvFileExplorer.setAdapter(new FileListAdapter(this));

        this.mTvFileExplorerNoMatchesIndicator = (TextView) findViewById(R.id.screen_extra_textView_fileExplorer_noMatches_indicator);

        this.mBtnFileExploreCopyAll = (Button) findViewById(R.id.screen_extra_button_fileExplorer_copyAll);
        this.mBtnFileExploreCopy = (Button) findViewById(R.id.screen_extra_button_fileExplorer_copy);
        this.mBtnFileExplorerRunTest = (Button) findViewById(R.id.screen_extra_button_fileExplorer_runTest);
        this.mRbFileExplorerSelectedAll = (RadioButton) findViewById(R.id.screen_extra_radioButton_fileExplorer_selectedAll);
        this.mRbFileExplorerUnselectedAll = (RadioButton) findViewById(R.id.screen_extra_radioButton_fileExplorer_unselectedAll);

        mBtnFileExploreCopyAll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                String command;
                command = "mount -o remount /dev/block/mtdblock0 /system";
                //Log.d(TAG, "command to RunTest is :" + command);
                if(RootCommands.run(10000, command)==false){ //10s
                    Log.d(TAG, "command to RunTest failed");
                }
                //Log.d(TAG, "command to RunTest successful");
                if(sbMultiChoiceFileNames.length() > 0) {
                    for (String fileName : ((FileListAdapter)mLvFileExplorer.getAdapter()).getFileNames()) {

                        //command = "cp -rf " + mRunTestDirectory + "/" + sbMultiChoiceFileNames;
                        if(fileName.endsWith(".so")) {
                            command = "cp -rf " + mRunTestDirectory + "/" + fileName + " " + SYSTEM_LIB_FOLDER;
                        } else {
                            command = "cp -rf " + mRunTestDirectory + "/" + fileName + " " + SYSTEM_BIN_FOLDER;
                        }
                        //Log.d(TAG, "command to RunTest is :" + command);
                        if(RootCommands.run(10000, command)==false){ //10s
                            Log.d(TAG, "command to RunTest failed");
                        }
                        //Log.d(TAG, "command to RunTest successful");

                        if(fileName.endsWith(".so")) {
                            command = "chmod 0755 " + SYSTEM_LIB_FOLDER + "/" + fileName;
                        } else {
                            command = "chmod 0755 " + SYSTEM_BIN_FOLDER + "/" + fileName;
                        }
                        //Log.d(TAG, "command to RunTest is :" + command);
                        if(RootCommands.run(10000, command)==false){ //10s
                            Log.d(TAG, "command to RunTest failed");
                        }
                        //Log.d(TAG, "command to RunTest successful");
                    }
                }
            }
        });
        mBtnFileExploreCopy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                String command;
                command = "mount -o remount /dev/block/mtdblock0 /system";
                //Log.d(TAG, "command to RunTest is :" + command);
                if(RootCommands.run(10000, command)==false){ //10s
                    Log.d(TAG, "command to RunTest failed");
                }
                //Log.d(TAG, "command to RunTest successful");
                if(sbMultiChoiceFileNames.length() > 0) {
                    for (String fileName : ((FileListAdapter)mLvFileExplorer.getAdapter()).getFileNames()) {

                        //command = "cp -rf " + mRunTestDirectory + "/" + sbMultiChoiceFileNames;
                        if(fileName.endsWith(".so")) {
                            command = "cp -rf " + mRunTestDirectory + "/" + fileName + " " + SYSTEM_LIB_FOLDER;
                        } else {
                            command = "cp -rf " + mRunTestDirectory + "/" + fileName + " " + SYSTEM_BIN_FOLDER;
                        }
                        //Log.d(TAG, "command to RunTest is :" + command);
                        if(RootCommands.run(10000, command)==false){ //10s
                            Log.d(TAG, "command to RunTest failed");
                        }
                        //Log.d(TAG, "command to RunTest successful");

                        if(fileName.endsWith(".so")) {
                            command = "chmod 0755 " + SYSTEM_LIB_FOLDER + "/" + fileName;
                        } else {
                            command = "chmod 0755 " + SYSTEM_BIN_FOLDER + "/" + fileName;
                        }
                        //Log.d(TAG, "command to RunTest is :" + command);
                        if(RootCommands.run(10000, command)==false){ //10s
                            Log.d(TAG, "command to RunTest failed");
                        }
                        //Log.d(TAG, "command to RunTest successful");
                    }
                }
            }
        });
        mBtnFileExplorerRunTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                if(sbMultiChoiceFileNames.length() == 1) {
                    String command;
                    command = sbMultiChoiceFileNames.substring(0);
                    //Log.d(TAG, "command to RunTest is :" + command);
                    if(RootCommands.run(10000, command)==false){ //10s
                        Log.d(TAG, "command to RunTest failed");
                    }
                    //Log.d(TAG, "command to RunTest successful");
                } else {
                    getEngine().showAppMessage("Only operated when selected only one file!");
                }
            }
        });

        setFileExplorerEnabled(true);
        setFileExplorerMultiChoiceEnabled(true);
        setFileBrowsingDirectory(mRunTestDirectory);
        initializeFileExplorer();

        // add listeners (for the configuration)
        //super.addConfigurationListener(mRbFileExplorerSelectedAll);
        //super.addConfigurationListener(mRbFileExplorerUnselectedAll);

        mRbFileExplorerSelectedAll.setOnCheckedChangeListener(rbLocal_OnCheckedChangeListener);
        mRbFileExplorerUnselectedAll.setOnCheckedChangeListener(rbLocal_OnCheckedChangeListener);

        mLinkUpdateBroadCastRecv = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                // Registration Event
                if(SgsRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)){
                    SgsRegistrationEventArgs args = intent.getParcelableExtra(SgsEventArgs.EXTRA_EMBEDDED);
                    if(args == null){
                        Log.e(TAG, "Invalid event args");
                        return;
                    }
                    switch(args.getEventType()){
                        default:
                            //((ScreenNetworkLinkAdapter)mListView.getAdapter()).refresh();
                            break;
                    }
                }
            }
        };
        final IntentFilter intentFilter = new IntentFilter();
        //intentFilter.addAction(SgsRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
        //intentFilter.addAction(TrafficCountEventArgs.ACTION_TRAFFIC_COUNT_EVENT);
        registerReceiver(mLinkUpdateBroadCastRecv, intentFilter);
        //setLinkUpdateThreadClassEnabled(true);

        /*try {
            NetInfo binaryCommand = new NetInfo(null);

            // start root shell
            //Shell shell = Shell.startRootShell();

            //shell.add(binaryCommand);
            shell.add(binaryCommand).waitForFinish();

            Log.d(TAG, "Output of command: " + binaryCommand.getOutput());
            returnCode = binaryCommand.getExitCode();
            sb.append(binaryCommand.getOutput());

            // close root shell
            shell.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception!", e);
        }*/
    }

    private CompoundButton.OnCheckedChangeListener rbLocal_OnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener(){
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int selectedIndex = EmbeddedFileExplorerConstants.INVALID_POSITION;
            if(mRbFileExplorerSelectedAll.isChecked()) {
                for (String fileName : ((FileListAdapter)mLvFileExplorer.getAdapter()).getFileNames()) {
                    selectedIndex++;
                    ((FileListAdapter)mLvFileExplorer.getAdapter()).setSelectedIndex(selectedIndex, true);
                    sbMultiChoiceFileNames.append(fileName).append(' ');
                }
                ((FileListAdapter)mLvFileExplorer.getAdapter()).notifyDataSetChanged();
            } else if(mRbFileExplorerUnselectedAll.isChecked()) {
                int end = sbMultiChoiceFileNames.length();
                sbMultiChoiceFileNames.delete(0, end);
                for (String fileName : ((FileListAdapter)mLvFileExplorer.getAdapter()).getFileNames()) {
                    selectedIndex++;
                    ((FileListAdapter)mLvFileExplorer.getAdapter()).setSelectedIndex(selectedIndex, false);
                    sbMultiChoiceFileNames.append(fileName).append(' ');
                }
                ((FileListAdapter)mLvFileExplorer.getAdapter()).notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onDestroy() {
        setLinkUpdateThreadClassEnabled(false);
        if(mLinkUpdateBroadCastRecv != null){
            unregisterReceiver(mLinkUpdateBroadCastRecv);
            mLinkUpdateBroadCastRecv = null;
        }

        super.onDestroy();
    }

    protected void onPause() {
        if(super.mComputeConfiguration){


            //mConfigurationService.putBoolean(SgsConfigurationEntry.MANUAL_ENABLE_USB_TETHER_CONNECT,
            //        mRbFileExplorerSelectedAll.isChecked());

            // Compute
            if(!mConfigurationService.commit()){
                Log.e(TAG, "Failed to commit() configuration");
            }

            super.mComputeConfiguration = false;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void setLinkUpdateThreadClassEnabled(boolean enabled) {
        this.setLinkUpdateThreadClassEnabled(null, enabled);
    }

    public void setLinkUpdateThreadClassEnabled(String[] dns, boolean enabled) {
        if (enabled == true) {
            if (this.mLinkUpdateThread == null || this.mLinkUpdateThread.isAlive() == false) {
                this.mLinkUpdateThread = new Thread(new LinkUpdateThreadClass(dns));
                this.mLinkUpdateThread.start();
            }
        } else {
            if (this.mLinkUpdateThread != null)
                this.mLinkUpdateThread.interrupt();
        }
    }

    // todo
    class LinkUpdateThreadClass implements Runnable {
        String[] dns;

        public LinkUpdateThreadClass(String[] dns) {
            this.dns = dns;
        }
        //@Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {

                // Taking a nap
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public boolean isFileExplorerMultiChoiceEnabled() {
        return isFileExplorerMultiChoiceEnabled;
    }

    public void setFileExplorerMultiChoiceEnabled(boolean isEnabled) {
        isFileExplorerMultiChoiceEnabled = isEnabled;
    }

    public boolean isFileExplorerEnabled() {
        return isFileExplorerEnabled;
    }

    public void setFileExplorerEnabled(boolean isEnabled) {
        isFileExplorerEnabled = isEnabled;
    }

    public String getCurrentDirectoryPath() {
        return (currentDirectory != null) ? currentDirectory.getAbsolutePath() : null;
    }

    public void setFileBrowsingDirectory(String directoryPath) {
        if (directoryPath != null) {
            File presetDirectory = new File(directoryPath);
            if (presetDirectory.exists() && presetDirectory.isDirectory()) {
                currentDirectory = presetDirectory;
            }
        }
    }

    private void indicateThatFileSystemIsNotAccessible() {
        //Toast.makeText(context, no_file_system_access_message_text, Toast.LENGTH_SHORT).show();
        getEngine().showAppMessage(no_file_system_access_message_text);
    }

    public void initializeFileExplorer() {
        if (currentDirectory == null) {
            currentDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        }

        FilePersistence filePersistence = new FilePersistence();
        if (filePersistence.isExternalStorageDirectoryRoot(currentDirectory)) {
            //fileExplorerUpButton.setEnabled(false);
        }

        FileListAdapter fileListAdapter = (FileListAdapter) mLvFileExplorer.getAdapter();
        filePersistence.initializeFileListAdapter(fileListAdapter, currentDirectory, FilePersistence.FILE_TYPE_RUNNABLE);

        //fileExplorerUseButton.setEnabled(fileListAdapter.getSelectedIndex() != EmbeddedFileExplorerConstants.INVALID_POSITION);
        mLvFileExplorer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Object adapterObj = adapterView.getAdapter();
                if (adapterObj != null && adapterObj instanceof FileListAdapter) {
                    FileListAdapter fileListAdapter = (FileListAdapter) adapterObj;
                    Object selectedFileName = fileListAdapter.getItem(position);
                    if (selectedFileName != null) {
                        int selectedFileType = fileListAdapter.getFileType(position);

                        if (selectedFileType == FilePersistence.FILE_TYPE_DIRECTORY) {
                            //fileExplorerUseButton.setEnabled(false);
                            currentDirectory = new File(currentDirectory, (String) selectedFileName);

                            FilePersistence filePersistence = new FilePersistence();
                            if (!filePersistence.isExternalStorageDirectoryRoot(currentDirectory)) {
                                //fileExplorerUpButton.setEnabled(true);
                            }
                            if (filePersistence.initializeFileListAdapter(fileListAdapter, currentDirectory, FilePersistence.FILE_TYPE_RUNNABLE)) {
                                fileListAdapter.setSelectedIndex(EmbeddedFileExplorerConstants.INVALID_POSITION, true);
                                fileListAdapter.notifyDataSetChanged();
                                setNoMatchingFilesInDirectoryIndicatorVisibility(fileListAdapter.getCount() == 0);
                            } else {
                                indicateThatFileSystemIsNotAccessible();
                            }
                        } else {
                            fileListAdapter.setSelectedIndex(position, !fileListAdapter.getSelectedIndex()[position]);
                            fileListAdapter.notifyDataSetChanged();
                            //fileExplorerUseButton.setEnabled(true);
                            if(fileListAdapter.getSelectedIndex()[position]) {
                                sbMultiChoiceFileNames.append(fileListAdapter.getSelectedFileName(position)).append(" ");
                            }
                        }

                    } else {
                        // log, ignore!?
                    }
                }
            }
        });
    }

    public void goToParentDirectory() {
        if (currentDirectory != null) {
            FilePersistence filePersistence = new FilePersistence();
            if (!filePersistence.isExternalStorageDirectoryRoot(currentDirectory)) {
                //fileExplorerUseButton.setEnabled(false);
                FileListAdapter fileListAdapter = (FileListAdapter) mLvFileExplorer.getAdapter();

                currentDirectory = currentDirectory.getParentFile();
                if (filePersistence.initializeFileListAdapter(fileListAdapter, currentDirectory, FilePersistence.FILE_TYPE_RUNNABLE)) {
                    fileListAdapter.setSelectedIndex(EmbeddedFileExplorerConstants.INVALID_POSITION, true);
                    fileListAdapter.notifyDataSetChanged();
                    setNoMatchingFilesInDirectoryIndicatorVisibility(fileListAdapter.getCount() == 0);
                } else {
                    indicateThatFileSystemIsNotAccessible();
                }

                if (filePersistence.isExternalStorageDirectoryRoot(currentDirectory)) {
                    //fileExplorerUpButton.setEnabled(false);
                }
            }
        }
    }

    public boolean[] getSelectedFileIndex() {
        return ((FileListAdapter) mLvFileExplorer.getAdapter()).getSelectedIndex();
    }

    public String getSelectedFilePath(int index) {
        File selectedFile = getSelectedFile(index);
        return (selectedFile != null) ? selectedFile.getAbsolutePath() : null;
    }

    public File getSelectedFile(int index) {
        if (currentDirectory != null) {
            String selectedFileName = getSelectedFileName(index);
            if (selectedFileName != null) {
                return new File(currentDirectory, selectedFileName);
            }
        }
        return null;
    }

    public String getSelectedFileName(int index) {
        FileListAdapter fileListAdapter = (FileListAdapter) mLvFileExplorer.getAdapter();
        return fileListAdapter.getSelectedFileName(index);
    }

    public StringBuilder getSelectedFileName() {
        FileListAdapter fileListAdapter = (FileListAdapter) mLvFileExplorer.getAdapter();
        return fileListAdapter.getSelectedFileName();
    }

    public void setNoMatchingFilesInDirectoryIndicatorVisibility(boolean isDirectoryEmpty) {
        mTvFileExplorerNoMatchesIndicator.setVisibility(isDirectoryEmpty ? View.VISIBLE : View.GONE);
        mLvFileExplorer.setVisibility(isDirectoryEmpty ? View.GONE : View.VISIBLE);
    }
}
