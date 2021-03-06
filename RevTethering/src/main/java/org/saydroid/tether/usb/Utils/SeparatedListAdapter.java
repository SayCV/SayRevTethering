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

package org.saydroid.tether.usb.Utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class SeparatedListAdapter extends BaseAdapter {

	public final Map<String,Adapter> mSections = new LinkedHashMap<String,Adapter>();
	public final static int TYPE_SECTION_HEADER = 0;

	public SeparatedListAdapter(Context context) {
	}

	protected abstract View getHeaderView(int position, View convertView, ViewGroup parent, final Adapter adapter);
	
	public void addSection(String section, Adapter adapter) {
		synchronized (mSections) {
			mSections.put(section, adapter);
		}
	}

	public void clearSections(){
		synchronized (mSections) {
			mSections.clear();
		}
	}
	
	@Override
	public Object getItem(int position) {
		synchronized (mSections) {
			for(Object section : this.mSections.keySet()) {
				final Adapter adapter = mSections.get(section);
				final int size = adapter.getCount() + 1;

				if(position == 0){
					return section;
				}
				if(position < size){
					return adapter.getItem(position - 1);
				}
				position -= size;
			}
			return null;	
		}
	}

	@Override
	public int getCount() {
		synchronized (mSections) {
			int total = 0;
			for(Adapter adapter : mSections.values()){
				total += adapter.getCount() + 1;
			}
			return total;
		}
	}

	@Override
	public int getViewTypeCount() {
		synchronized (mSections) {
			int total = 1;
			for(Adapter adapter : mSections.values()){
				total += adapter.getViewTypeCount();
			}
			return total;
		}
	}

	@Override
	public int getItemViewType(int position) {
		synchronized (mSections) {
			int type = 1;
			for(Object section : mSections.keySet()) {
				final Adapter adapter = mSections.get(section);
				final int size = adapter.getCount() + 1;
				
				if(position == 0){
					return TYPE_SECTION_HEADER;
				}
				if(position < size){
					return type + adapter.getItemViewType(position - 1);
				}
				
				position -= size;
				type += adapter.getViewTypeCount();
			}
			return -1;
		}
	}

	@Override
	public boolean isEnabled(int position) {
		return (getItemViewType(position) != TYPE_SECTION_HEADER);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		synchronized (mSections) {
			int sectionNum = 0;
			for(Object section : mSections.keySet()) {
				final Adapter adapter = mSections.get(section);
				final int size = adapter.getCount() + 1;

				if(position == 0){
					return getHeaderView(sectionNum, convertView, parent, adapter);
				}
				if(position < size){
					return adapter.getView(position - 1, convertView, parent);
				}

				// otherwise jump into next section
				position -= size;
				sectionNum++;
			}
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}

