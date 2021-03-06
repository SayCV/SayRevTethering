/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)saydroid(dot)org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package org.saydroid.sgs.services.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.saydroid.logger.Log;
import org.saydroid.sgs.SgsEngine;
import org.saydroid.sgs.model.SgsHistoryEvent;
import org.saydroid.sgs.model.SgsHistoryList;
import org.saydroid.sgs.services.ISgsHistoryService;
import org.saydroid.sgs.utils.SgsObservableList;
import org.saydroid.sgs.utils.SgsPredicate;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;


public class SgsHistoryService extends SgsBaseService implements ISgsHistoryService {
	private final static String TAG = SgsHistoryService.class.getCanonicalName();
	private final static String HISTORY_FILE = "history.xml";
	
	private File mHistoryFile;
	private SgsHistoryList mEventsList;
	private final Serializer mSerializer;
	private boolean mLoadingHistory;
	
	public SgsHistoryService(){
		super();
		
		mSerializer = new Persister();
		mEventsList = new SgsHistoryList();
	}
	
	@Override
	public boolean start() {
		Log.d(TAG, "Starting...");
		boolean result = true;
		
		/*	http://code.google.com/p/dalvik/wiki/JavaxPackages
	     * Ensure the factory implementation is loaded from the application
	     * classpath (which contains the implementation classes), rather than the
	     * system classpath (which doesn't).
	     */
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		
		mHistoryFile = new File(String.format("%s/%s", SgsEngine.getInstance().getStorageService().getCurrentDir(), SgsHistoryService.HISTORY_FILE));
		if(!mHistoryFile.exists()){
			try {
				mHistoryFile.createNewFile();
				result = compute(); /* to create an empty but valid document */
			} catch (IOException e) {
				Log.e(TAG, e.toString());
				mHistoryFile = null;
				result =  false;
			}
		}
		
		return result;
	}

	@Override
	public boolean stop() {
		Log.d(TAG, "Stopping");
		return true;
	}

	@Override
	public boolean load(){
		boolean result = true;
		
		try {
			mLoadingHistory = true;
			Log.d(TAG, "Loading history");
			mEventsList = mSerializer.read(mEventsList.getClass(), mHistoryFile);
			Log.d(TAG, "History loaded");
		} catch (Exception ex) {
			ex.printStackTrace();
			result = false;
            mLoadingHistory = false;
		}
        mLoadingHistory = false;
		return result;
	}
	
	@Override
	public boolean isLoading() {
		return mLoadingHistory;
	}

	@Override
	public void addEvent(SgsHistoryEvent event) {
		mEventsList.addEvent(event);
		new Thread(new Runnable(){
			@Override
			public void run() {
				compute();
			}
		}).start();
	}

	@Override
	public void updateEvent(SgsHistoryEvent event) {
		Log.e(TAG, "Not impleented");
		//throw new Exception("Not implemented");
	}

	@Override
	public void deleteEvent(SgsHistoryEvent event) {
		mEventsList.removeEvent(event);
		new Thread(new Runnable(){
			@Override
			public void run() {
				compute();
			}
		}).start();
	}

	@Override
	public void deleteEvent(int location) {
		mEventsList.removeEvent(location);
		new Thread(new Runnable(){
			@Override
			public void run() {
				compute();
			}
		}).start();
	}
	
	@Override
	public void deleteEvents(SgsPredicate<SgsHistoryEvent> predicate){
		mEventsList.removeEvents(predicate);
		new Thread(new Runnable(){
			@Override
			public void run() {
				compute();
			}
		}).start();
	}

	@Override
	public void clear() {
		mEventsList.clear();
		new Thread(new Runnable(){
			@Override
			public void run() {
				compute();
			}
		}).start();
	}

	@Override
	public List<SgsHistoryEvent> getEvents() {
		return mEventsList.getList().getList();
	}
	
	@Override
	public SgsObservableList<SgsHistoryEvent> getObservableEvents() {
		return mEventsList.getList();
	}
	
	private synchronized boolean compute(){
		synchronized(this){
			if(mHistoryFile == null || mSerializer == null){
				Log.e(TAG, "Invalid arguments");
				return false;
			}
			try{
				mSerializer.write(mEventsList, mHistoryFile);
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}
}
