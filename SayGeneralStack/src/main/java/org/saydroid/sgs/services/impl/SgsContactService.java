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

import java.util.ArrayList;
import java.util.List;

import org.saydroid.sgs.SgsApplication;
import org.saydroid.sgs.SgsEngine;
import org.saydroid.sgs.model.SgsContact;
import org.saydroid.sgs.model.SgsPhoneNumber;
import org.saydroid.sgs.services.ISgsContactService;
import org.saydroid.sgs.utils.SgsCallbackFunc;
import org.saydroid.sgs.utils.SgsListUtils;
import org.saydroid.sgs.utils.SgsObservableList;
import org.saydroid.sgs.utils.SgsPredicate;
import org.saydroid.sgs.utils.SgsStringUtils;
import org.saydroid.tinyWRAP.SipUri;

import android.app.Activity;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.util.Log;

/**@page SgsContactService_page Contact Service
 * The Contact service is used to retrieve contacts from the native address book.
 * 
 */

/**
 * Service used to retrieve contacts from the native address book.
 */
public class SgsContactService  extends SgsBaseService implements ISgsContactService{
	private final static String TAG = SgsContactService.class.getCanonicalName();
	public final static String PARAM_ARG_CONTACT = "SgsContact";
	
	protected SgsObservableList<SgsContact> mContacts;
	protected boolean mLoading;
	protected boolean mReady;
	protected Looper mLocalContactObserverLooper;
	protected ContentObserver mLocalContactObserver;
	
	private SgsCallbackFunc<Object> mOnBeginLoadCallback;
	private SgsCallbackFunc<String> mOnNewPhoneNumberCallback;
	private SgsCallbackFunc<Object> mOnEndLoadCallback;
	
	public SgsContactService(){
		super();
	}
	
	@Override
	public boolean start() {
		Log.d(TAG, "starting...");
		
		if(mContacts == null){
			mContacts = getObservableContacts();
		}
		
		if(mLocalContactObserver == null && mLocalContactObserverLooper == null){
			new Thread(new Runnable() { // avoid locking calling thread
				@Override
				public void run() {
					Log.d(TAG, "Observer Looper enter()");
					Looper.prepare();
					mLocalContactObserverLooper = Looper.myLooper();
					final Handler handler = new Handler();
					handler.post(new Runnable() { // Observer thread. Will allow us to get notifications even if the application is on background
						@Override
						public void run() {
							mLocalContactObserver = new ContentObserver(handler) {
								@Override
								public void onChange(boolean selfChange) {
									super.onChange(selfChange);
									Log.d(TAG, "Native address book changed");
									load();
								}
							};
							SgsApplication.getContext().getContentResolver().registerContentObserver(CommonDataKinds.Phone.CONTENT_URI, 
									true, mLocalContactObserver);
						}
					});
					Looper.loop();// loop() until quit() is called
					Log.d(TAG, "Observer Looper exit()");
				}
			}).start();
		};
		
		return true;
	}

	@Override
	public boolean stop() {
		Log.d(TAG, "stopping...");
		
		try{
			if(mLocalContactObserver != null){
				SgsApplication.getContext().getContentResolver().unregisterContentObserver(mLocalContactObserver);
				mLocalContactObserver = null;
			}
			if(mLocalContactObserverLooper != null){
				mLocalContactObserverLooper.quit();
				mLocalContactObserverLooper = null;
			}
		}
		catch(Exception e){
			Log.e(TAG, e.toString());
		}
		return true;
	}
	
	@Override
	public void setOnBeginLoadCallback(SgsCallbackFunc<Object> callback){
		mOnBeginLoadCallback = callback;
	}
	
	@Override
	public void setOnNewPhoneNumberCallback(SgsCallbackFunc<String> callback){
		mOnNewPhoneNumberCallback = callback;
	}
	
	@Override
	public void setOnEndLoadCallback(SgsCallbackFunc<Object> callback){
		mOnEndLoadCallback = callback;
	}
	
	@Override
	public boolean load(){
		return load2();
	}
	
	public boolean load2(){
		mLoading = true;
		boolean bOK = false;
		Cursor managedCursor = null;
		final Activity activity = SgsEngine.getInstance().getMainActivity();
		final List<SgsContact> contactsCopy = new ArrayList<SgsContact>();
		
		if(mOnBeginLoadCallback != null){
			mOnBeginLoadCallback.callback(this);
		}
		
		try{
			String phoneNumber, displayName, label;
			SgsContact contact = null;
			int id, type, photoId;
			final Resources res = SgsApplication.getContext().getResources();
			
			if(SgsApplication.getSDKVersion() >=5 && activity != null){
				/*synchronized(mContacts)*/{
					final String[] projectionContacts = new String[] { 
							android.provider.BaseColumns._ID,
							android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER,
							android.provider.ContactsContract.CommonDataKinds.Phone.TYPE,
							android.provider.ContactsContract.CommonDataKinds.Phone.LABEL,
							android.provider.ContactsContract.Contacts.DISPLAY_NAME,
							android.provider.ContactsContract.Contacts.PHOTO_ID,
							android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
							};
					
					managedCursor = activity.managedQuery(android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							projectionContacts, // Which columns to return
							null,       // Which rows to return (all rows)
							null,       // Selection arguments (none)
							// Put the results in ascending order by name
							"UPPER(" + android.provider.ContactsContract.Contacts.DISPLAY_NAME + ") ASC"
						);
					
					
					int indexPhoneContactId = managedCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
					int indexPhoneType = managedCursor .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
					int indexPhoneLabel = managedCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL);
					int indexPhoneNumber = managedCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
					int indexPhonePhotoId = managedCursor .getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID);
					
					int indexContactDisplayName = managedCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
					
					 while(managedCursor.moveToNext()){
						 id = managedCursor.getInt(indexPhoneContactId);
						 type = managedCursor.getInt(indexPhoneType);
						 label = managedCursor.getString(indexPhoneLabel);
						 phoneNumber = managedCursor.getString(indexPhoneNumber);
						 photoId = managedCursor.getInt(indexPhonePhotoId);
						 
						 if(phoneNumber != null){
							 phoneNumber = phoneNumber.replace("-", "");
							if(SgsStringUtils.isNullOrEmpty(label)){
								final int resId = android.provider.ContactsContract.CommonDataKinds.Phone.getTypeLabelResource(type);
								label = res.getString(resId);
							}
							 if(contact == null || contact.getId() != id){
								displayName = managedCursor.getString(indexContactDisplayName);
								contact = newContact(id, displayName);
								if(photoId != 0){
									 contact.setPhotoId(photoId);
								}
								contactsCopy.add(contact);
							 }
							 contact.addPhoneNumber(SgsPhoneNumber.fromAndroid2LocalType(type), phoneNumber, label);
							 if(mOnNewPhoneNumberCallback != null){
								 mOnNewPhoneNumberCallback.callback(phoneNumber);
							 }
						 }
					 }
					 
					 mLoading = false;
					 mReady = true;
					 bOK = true;
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			
			mLoading = false;
			mReady = false;
		}
		finally{
			if(managedCursor != null){
				activity.stopManagingCursor(managedCursor);
				
				managedCursor.close();
			}
		}
		
		if(bOK){
			synchronized(mContacts){
				mContacts.clear();
				mContacts.add(contactsCopy);
			}
		}
		
		if(mOnEndLoadCallback != null){
			mOnEndLoadCallback.callback(this);
		}
		
		return bOK;
	}

	@Override
	public boolean isLoading() {
		return mLoading;
	}

	@Override
	public boolean isReady(){
		return mReady;
	}
	
	@Override
	public SgsObservableList <SgsContact> getObservableContacts() {
		if(mContacts == null){
			mContacts = new SgsObservableList<SgsContact>(true);
		}
		return mContacts;
	}

	@Override
	public SgsContact newContact(int id, String displayName){
		return new SgsContact(id, displayName);
	}
	
	@Override
	public SgsContact getContactByUri(String uri) {
		final SipUri sipUri = new SipUri(uri);
		SgsContact contact = null;
		if(sipUri.isValid()){
			contact = getContactByPhoneNumber(sipUri.getUserName());
		}
		sipUri.delete();
		return contact;
	}

	@Override
	public SgsContact getContactByPhoneNumber(String anyPhoneNumber) {
		return SgsListUtils.getFirstOrDefault(mContacts.getList(), new SgsContact.ContactFilterByAnyPhoneNumber(anyPhoneNumber));
	}
	
	/**
	 * ContactFilterById
	 */
	static class ContactFilterById implements SgsPredicate<SgsContact>{
		private final int mId;
		ContactFilterById(int id){
			mId = id;
		}
		@Override
		public boolean apply(SgsContact contact) {
			return (contact != null && contact.getId() == mId);
		}
	}
}
