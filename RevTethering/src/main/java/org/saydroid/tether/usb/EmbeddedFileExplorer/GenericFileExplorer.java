/**
 *               DO WHAT YOU WANT TO PUBLIC LICENSE
 *                    Version 2, December 2004
 *
 * Copyright (C) 2004 Sam Hocevar <sam@hocevar.net>
 *
 * Everyone is permitted to copy and distribute verbatim or modified
 * copies of this license document, and changing it is allowed as long
 * as the name is changed.
 *
 *            DO WHAT YOU WANT TO PUBLIC LICENSE
 *   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 *  0. You just DO WHAT YOU WANT TO.
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

package org.saydroid.tether.usb.EmbeddedFileExplorer;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import org.saydroid.tether.usb.R;
import org.saydroid.tether.usb.Screens.ScreenExtra;


public class GenericFileExplorer {

	private final Context context;
	private final ListView mLvFileExplorer;
    private final TextView mTvFileExplorerNoMatchesIndicator;
	private final Button mBtnFileExploreCopyAll;
	private final Button mBtnFileExploreCopy;
    private final Button mBtnFileExplorerRunTest;
    private RadioButton mRbFileExplorerSelectedAll;
    private RadioButton mRbFileExplorerUnselectedAll;

	private final String no_file_system_access_message_text;
	
	private boolean isFileExplorerEnabled = false;
	private File currentDirectory;
	
	public GenericFileExplorer(ScreenExtra context, View containerView, int preselectionIndex, String noFileSystemAccessMessageText) {
		this.context = context;
		
		this.mLvFileExplorer = (ListView) containerView.findViewById(R.id.screen_extra_listView);
        this.mTvFileExplorerNoMatchesIndicator = (TextView) containerView.findViewById(R.id.screen_extra_textView_fileExplorer_noMatches_indicator);
		this.mBtnFileExploreCopyAll = (Button) containerView.findViewById(R.id.screen_extra_button_fileExplorer_copyAll);
		this.mBtnFileExploreCopy = (Button) containerView.findViewById(R.id.screen_extra_button_fileExplorer_copy);
        this.mBtnFileExplorerRunTest = (Button) containerView.findViewById(R.id.screen_extra_button_fileExplorer_runTest);
        this.mRbFileExplorerSelectedAll = (RadioButton) containerView.findViewById(R.id.screen_extra_radioButton_fileExplorer_selectedAll);
        this.mRbFileExplorerUnselectedAll = (RadioButton) containerView.findViewById(R.id.screen_extra_radioButton_fileExplorer_unselectedAll);
		
		this.no_file_system_access_message_text = noFileSystemAccessMessageText;
		
		FileListAdapter fileListAdapter = new FileListAdapter(context);
        mLvFileExplorer.setAdapter(fileListAdapter);
		fileListAdapter.setSelectedIndex(preselectionIndex, true);
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
		Toast.makeText(context, no_file_system_access_message_text, Toast.LENGTH_SHORT).show();
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
		filePersistence.initializeFileListAdapter(fileListAdapter, currentDirectory, FilePersistence.FILE_TYPE_PICTURE);
		
		//fileExplorerUseButton.setEnabled(fileListAdapter.getSelectedIndex() != EmbeddedFileExplorerConstants.INVALID_POSITION);
        mLvFileExplorer.setOnItemClickListener(new OnItemClickListener() {
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
							
							if (filePersistence.initializeFileListAdapter(fileListAdapter, currentDirectory, FilePersistence.FILE_TYPE_PICTURE)) {
								fileListAdapter.setSelectedIndex(EmbeddedFileExplorerConstants.INVALID_POSITION, true);
								fileListAdapter.notifyDataSetChanged();
								setNoMatchingFilesInDirectoryIndicatorVisibility(fileListAdapter.getCount() == 0);
							} else {
								indicateThatFileSystemIsNotAccessible();
							}
						} else {
							fileListAdapter.setSelectedIndex(position, true);
							fileListAdapter.notifyDataSetChanged();
							//fileExplorerUseButton.setEnabled(true);
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
				if (filePersistence.initializeFileListAdapter(fileListAdapter, currentDirectory, FilePersistence.FILE_TYPE_PICTURE)) {
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
