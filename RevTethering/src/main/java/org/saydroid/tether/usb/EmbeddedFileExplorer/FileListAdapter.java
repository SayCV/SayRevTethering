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

import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.saydroid.tether.usb.R;
import org.saydroid.tether.usb.Screens.ScreenExtra;

public class FileListAdapter extends BaseAdapter {

	private final LayoutInflater mInflater;
    private final ScreenExtra mBaseScreen;

	private final int row_background_color;
	private final int row_background_color_selected;
	private final int filename_label_color;
	private final int filename_label_color_selected;

	// list adapter model
	private String[] fileNames;
	private HashMap<String, Integer> fileTypes;
	private boolean[] selectedIndex;// = EmbeddedFileExplorerConstants.INVALID_POSITION;

    public FileListAdapter(ScreenExtra baseScreen){
        mBaseScreen = baseScreen;
        mInflater = LayoutInflater.from(baseScreen);

        row_background_color = EmbeddedFileExplorerConstants.DEFAULT_ROW_BACKGROUND_COLOR;
        row_background_color_selected = EmbeddedFileExplorerConstants.DEFAULT_SELECTED_ROW_BACKGROUND_COLOR;
        filename_label_color = EmbeddedFileExplorerConstants.DEFAULT_FILENAME_LABEL_COLOR;
        filename_label_color_selected = EmbeddedFileExplorerConstants.DEFAULT_SELECTED_FILENAME_LABEL_COLOR;
    }
	
	public FileListAdapter(ScreenExtra baseScreen,
			int rowBackgroundColor, int rowBackgroundColorSelected,
			int filenameLabelColor, int filenameLabelColorSelected) {
        mBaseScreen = baseScreen;
		this.mInflater = (LayoutInflater) mBaseScreen.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		row_background_color = rowBackgroundColor;
		row_background_color_selected = rowBackgroundColorSelected;
		filename_label_color = filenameLabelColor;
		filename_label_color_selected = filenameLabelColorSelected;
	}

    public void initSelectableIndex(int count) {
        this.selectedIndex = new boolean[count];
    }

	public void setFileNames(String[] fileNames) {
		this.fileNames = fileNames;
	}

    public String[] getFileNames() { return this.fileNames;}
	
	public void setFileTypes(HashMap<String, Integer> fileTypes) {
		this.fileTypes = fileTypes;
	}
	
	public void setSelectedIndex(int index, boolean checked) {
		this.selectedIndex[index] = checked;
	}
	
	public int getFileType(int position) {
		int fileType = FilePersistence.FILE_TYPE_UNKNOWN;
		if (fileNames != null && fileTypes != null) {
			Integer fileTypeValue = fileTypes.get(fileNames[position]);
			fileType = fileTypeValue != null ? fileTypeValue.intValue() : fileType; 
		}
		return fileType;
	}
	
	public boolean[] getSelectedIndex() {
		return selectedIndex;
	}

    public String getSelectedFileName(int index) {
        String selectedFileName = null;
        if (index != EmbeddedFileExplorerConstants.INVALID_POSITION && fileNames != null && fileNames.length > index
                && selectedIndex[index]) {
            selectedFileName = fileNames[index];
        }
        return selectedFileName;
    }

	public String[] getSelectedFileName() {
        String[] selectedFileName = new String[getSelectedCount()];
        int index = 0;
		for (int _selectedIndex = EmbeddedFileExplorerConstants.INVALID_POSITION + 1; _selectedIndex < selectedIndex.length; _selectedIndex++) {
			if (fileNames != null && fileNames.length > _selectedIndex && selectedIndex[_selectedIndex]) {
                //selectedFileName.append(fileNames[_selectedIndex]).append(" ");
                selectedFileName[index++] = fileNames[_selectedIndex];
			}
		}
		return selectedFileName;
	}
	
	@Override
	public int getCount() {
		return (fileNames != null) ? fileNames.length : 0;
	}

    public int getSelectedCount() {
        int _selectedCount = 0;
        for(_selectedCount = EmbeddedFileExplorerConstants.INVALID_POSITION + 1; _selectedCount < selectedIndex.length; _selectedCount++) {
            if(selectedIndex[_selectedCount]) _selectedCount++;
        }
        return _selectedCount;
        //return (getSelectedFileName() != null) ? getSelectedFileName().length : 0;
    }

	@Override
	public Object getItem(int position) {
		return (fileNames != null && position < fileNames.length) ? fileNames[position] : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

    void refresh(){
        notifyDataSetChanged();
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.screen_extral_item_file, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		final String fileName = fileNames[position];
		holder.getFileNameTextView().setText(fileName);
        if(mBaseScreen.isFileExplorerMultiChoiceEnabled()) {
            //
        } else {
		    holder.getFileTypeImageView().setImageResource(getFileTypeImageResource(fileName));
        }
		holder.getRowView().setBackgroundColor((!selectedIndex[position])
				? row_background_color
				: row_background_color_selected);
		holder.getFileNameTextView().setTextColor((!selectedIndex[position])
				? filename_label_color
				: filename_label_color_selected);
        convertView.refreshDrawableState();
		return convertView;
	}

	private int getFileTypeImageResource(String fileName) {
		int fileType = FilePersistence.FILE_TYPE_UNKNOWN;
		Integer fileTypeValue = fileTypes.get(fileName);
		if (fileTypeValue != null) {
			fileType = fileTypeValue.intValue();
		}
		
		switch (fileType) {
			case FilePersistence.FILE_TYPE_DIRECTORY:
				return R.drawable.holo_collections_collection_48;
			case FilePersistence.FILE_TYPE_PICTURE:
				return R.drawable.holo_content_picture_48;
            case FilePersistence.FILE_TYPE_RUNNABLE:
                return R.drawable.ic_hideable_item;
			case FilePersistence.FILE_TYPE_UNKNOWN:
			default:
				return android.R.drawable.ic_menu_help;
		}
	}

	
	class ViewHolder {
		private View row;
		private ImageView fileTypeImageView;
		private TextView fileNameTextView;
		
		public ViewHolder(View existingView) {
			this.row = existingView;
		}
		
		public View getRowView() {
			return row;
		}
		
		public ImageView getFileTypeImageView() {
			if (fileTypeImageView == null) {
				fileTypeImageView = (ImageView) row.findViewById(R.id.screen_extra_item_file_imageView_fileType);
			}
			return fileTypeImageView;
		}

		public TextView getFileNameTextView() {
			if (fileNameTextView == null) {
				fileNameTextView = (TextView) row.findViewById(R.id.screen_extra_item_file_textView_fileName);
			}
			return fileNameTextView;
		}
	}
}
