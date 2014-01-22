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
import java.io.FilenameFilter;
import java.util.HashMap;

import android.os.Environment;

public class FilePersistence {

	public static final int FILE_TYPE_UNKNOWN = 100;
	public static final int FILE_TYPE_DIRECTORY = 101;
	public static final int FILE_TYPE_PICTURE = 102;
    public static final int FILE_TYPE_RUNNABLE = 103;
	// add more file types here
	
	public boolean isExternalStorageAvailable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		} else if (Environment.MEDIA_BAD_REMOVAL.equals(state)
				|| Environment.MEDIA_CHECKING.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)
				|| Environment.MEDIA_NOFS.equals(state)
				|| Environment.MEDIA_REMOVED.equals(state)
				|| Environment.MEDIA_SHARED.equals(state)
				|| Environment.MEDIA_UNMOUNTABLE.equals(state)
				|| Environment.MEDIA_UNMOUNTED.equals(state)) {
			return false;
		} else {
			return false;
		}
	}
	
	public boolean initializeFileListAdapter(FileListAdapter fileListAdapter, File directory, int fileType) {
		boolean successful = false;
        String[] fileNames;
        HashMap<String,Integer> fileTypes;
		if (isExternalStorageAvailable()) {
            switch (fileType) {
                case FILE_TYPE_PICTURE:
                    fileNames = directory.list(getPictureFileFilter());
			        fileTypes = getFileTypesForDirectory(directory, fileNames, FILE_TYPE_PICTURE);
                    break;
                case FILE_TYPE_RUNNABLE:
                    fileNames = directory.list(getRunnableFileFilter());
                    fileTypes = getFileTypesForDirectory(directory, fileNames, FILE_TYPE_RUNNABLE);
                    break;
                default:
                    successful = false;
                    return successful;
            }
			fileListAdapter.setFileNames(fileNames);
			fileListAdapter.setFileTypes(fileTypes);
			successful = true;
		}
		return successful;
	}
	
	/**
	 * Gets an indicator map to determine which files are directories.
	 * @param directory parent
	 * @param fileNames contained in the directory
	 * @param fileTypeOtherThanDirectory depends on the file filter that has been applied to get the file name
	 * @return indicator map with file types
	 */
	public HashMap<String, Integer> getFileTypesForDirectory(File directory, String[] fileNames, int fileTypeOtherThanDirectory) {
		HashMap<String, Integer> fileTypes = null;
		if (fileNames != null && fileNames.length > 0) {
			fileTypes = new HashMap<String, Integer>();
			for (String fileName : fileNames) {
				File file = new File(directory, fileName);
				if (file.exists()) {
					if (file.isDirectory()) {
						fileTypes.put(fileName, FILE_TYPE_DIRECTORY);
					} else {
						fileTypes.put(fileName, fileTypeOtherThanDirectory);
					}
				} else {
					fileTypes.put(fileName, FILE_TYPE_UNKNOWN);
				}
			}
		}
		return fileTypes;
	}
	
	public FilenameFilter getPictureFileFilter() {
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String filename) {
		        File sel = new File(dir, filename);
		        return !filename.startsWith(".") 
		        		&& (filename.endsWith(".png") || filename.endsWith(".jpg")
		        				|| filename.endsWith(".jpeg") || sel.isDirectory());
		    }
		};
		return filter;
	}

    public FilenameFilter getRunnableFileFilter() {
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                File sel = new File(dir, filename);
                return !filename.startsWith(".")
                        && (filename.endsWith(".exe") || filename.endsWith(".sh")
                        || filename.endsWith(".so")
                        || !(filename.lastIndexOf('.') > -1) || sel.isDirectory());
            }
        };
        return filter;
    }

	public boolean isExternalStorageDirectoryRoot(File directory) {
		final File externalStorageDirectory = Environment.getExternalStorageDirectory();
		return directory != null && directory.isDirectory() 
				&& directory.equals(externalStorageDirectory);
	}
	
}
