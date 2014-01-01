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

package org.saydroid.tether.usb.Tethering;

import org.saydroid.sgs.utils.SgsObservableObject;
import org.saydroid.sgs.utils.SgsStringUtils;
import org.saydroid.sgs.utils.SgsUriUtils;
import org.saydroid.tinyWRAP.SipSession;
import org.saydroid.tinyWRAP.SipUri;

import org.saydroid.logger.Log;

/**
 * Abstract class defining a SIP Session (Registration, Subscription, Publication, Call, ...)
 */
public abstract class TetheringSession extends SgsObservableObject implements Comparable<TetheringSession>{
	private static final String TAG = TetheringSession.class.getCanonicalName();
	
	protected TetheringStack mTetheringStack;
    protected boolean mOutgoing;
    protected String mFromUri;
    protected String mToUri;
    protected String mCompId;
    protected String mRemotePartyUri;
    protected String mRemotePartyDisplayName = null;
    protected long mId = -1;
    protected int mRefCount = 1;
    protected ConnectionState mConnectionState;
    
    /**
     * The connection state
     */
    public enum ConnectionState{
        NONE,
        CONNECTING,
        CONNECTED,
        TERMINATING,
        TERMINATED,
    }

    /**
     * Creates new SIP session
     * @param tetheringStack the sip stack to use to create the session
     */
    protected TetheringSession(TetheringStack tetheringStack){
        mTetheringStack = tetheringStack;
        mOutgoing = false;
        mConnectionState = ConnectionState.NONE;
        /* init must be called by the child class after session_create() */
        /* this.init(); */
    }
    
    @Override
	protected void finalize() throws Throwable {
		Log.d(TAG, "finalize()");
		delete();
		super.finalize();
	}

    /**
     * Increments the reference counting
     * @return the new reference counting value
     * @sa @ref decRef()
     */
	public int incRef(){
    	synchronized (this) {
    		if(mRefCount>0){
    			mRefCount++;
    		}
    		Log.d(TAG, "mRefCount="+mRefCount);
    		return mRefCount;
		}
    }
    
	/**
	 * Decrements the reference counting
	 * @return the new reference counting value
	 * @sa @ref incRef()
	 */
    public int decRef(){
    	synchronized (this) {
			if(--mRefCount == 0){
				getSession().delete();
			}
			Log.d(TAG, "mRefCount="+mRefCount);
			return mRefCount;
		}
    }
    
    /**
     * Gets a unique identifier defining a session
     * @return a unique identifier defining the session
     */
    public long getId(){
    	if(mId == -1){
            mId = getSession().getId(); 
        }
        return mId;
    }

    public boolean isOutgoing(){
    	return mOutgoing;
    }
    
    /**
     * Gets the associated SIP stack
     * @return a SIP stack
     */
    public TetheringStack getStack(){
        return mTetheringStack;
    }

    /**
     * Adds a new SIP header to the session
     * @param name the name of the header
     * @param value the value of the header
     * @return true if succeed and false otherwise
     * @sa @ref removeHeader()
     * @code
     * mSipSession.addHeader("User-Agent", "IM-OMAv1.0");
     * @endcode
     */
    public boolean addHeader(String name, String value){
    	return getSession().addHeader(name, value);
    }
    
    /**
     * Removes a SIP header from the session
     * @param name the name of the sip header to remove
     * @return true if succeed and false otherwise
     * @sa @ref addHeader()
     * @code
     * mSipSession.removeHeader("User-Agent");
     * @endcode
     */
    public boolean removeHeader(String name){
    	return getSession().removeHeader(name);
    }
    
    /**
     * Adds sip capabilities to the session. The capability will be added in a separate
     * "Accept-Contact" header if the session is dialogless or in the "Contact" header otherwise
     * @param name the name of capability to add
     * @return true if succeed and false otherwise
     * @sa @ref removeCaps()
     * @code
     * mSipSession.addCaps("+g.3gpp.smsip");
     * @endcode
     */
    public boolean addCaps(String name){
    	return getSession().addCaps(name);
    }
    
    /**
     * Adds sip capabilities to the session. The capability will be added in a separate
     * "Accept-Contact" header if the session is dialogless or in the "Contact" header otherwise
     * @param name the name of capability to add
     * @param value the value of the capability
     * @return true if succeed and false otherwise
     * @sa @ref removeCaps()
     * @code
     * mSipSession.addCaps("+g.3gpp.icsi-ref", "\"urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel\"");
     * @endcode
     */
    public boolean addCaps(String name, String value){
    	return getSession().addCaps(name, value);
    }
    
    /**
     * Removes a sip capability from the session
     * @param name the name of the capability to remove
     * @return true if succeed and false otherwise
     * @sa @ref addCaps()
     * @code
     * mSipSession.removeCaps("+g.3gpp.smsip");
     * @endcode
     */
    public boolean removeCaps(String name){
    	return getSession().removeCaps(name);
    }
    
    /**
     * Checks whether the session established or not. For example, you can only send files when the session
     * is connected. You can use @ref getConnectionState() to have the exact state
     * @return true is session is established and false otherwise
     * @sa @ref getConnectionState()
     */
    public boolean isConnected(){
    	return (mConnectionState == ConnectionState.CONNECTED);
    }
    
    /**
     * Sets the connection state of the session. You should not call this function by yourself
     * @param state the new state
     */
    public void setConnectionState(ConnectionState state){
    	mConnectionState = state;
    }
    
    /**
     * Gets the connection state of the session
     * @return the connection state
     * @sa @ref isConnected()
     */
    public ConnectionState getConnectionState(){
    	return mConnectionState;
    }
    
    /**
     * Gets the sip from uri
     * @return the sip from uri
     */
    public String getFromUri(){
    	return mFromUri;
    }
    
    /**
     * Sets the sip from uri
     * @param uri the new sip from uri
     * @return true if succeed and false otherwise
     * @sa ref setToUri()
     */
    public boolean setFromUri(String uri){
    	if (!getSession().setFromUri(uri)){
            Log.e(TAG, String.format("%s is invalid as FromUri", uri));
            return false;
        }
        mFromUri = uri;
        return true;
    }
    
    public boolean setFromUri(SipUri uri){
    	if (!getSession().setFromUri(uri)){
            Log.e(TAG, "Failed to set FromUri");
            return false;
        }
        mFromUri = String.format("%s:%s@%s", uri.getScheme(), uri.getUserName(), uri.getHost());
        return true;
    }
    
    public String getToUri(){
    	return mToUri;
    }
    
    public boolean setToUri(String uri){
    	if (!getSession().setToUri(uri)){
            Log.e(TAG, String.format("%s is invalid as toUri", uri));
            return false;
        }
    	mToUri = uri;
        return true;
    }
    
    public boolean setToUri(SipUri uri){
    	if (!getSession().setToUri(uri)){
            Log.e(TAG, "Failed to set ToUri");
            return false;
        }
    	mToUri = String.format("%s:%s@%s", uri.getScheme(), uri.getUserName(), uri.getHost());
        return true;
    }
    
    public String getRemotePartyUri(){
    	if (SgsStringUtils.isNullOrEmpty(mRemotePartyUri)){
            mRemotePartyUri =  mOutgoing ? mToUri : mFromUri;
        }
        return SgsStringUtils.isNullOrEmpty(mRemotePartyUri) ? "(null)" : mRemotePartyUri;
    }
    
    public void setRemotePartyUri(String uri){
    	mRemotePartyUri = uri;
    }
    
    public String getRemotePartyDisplayName(){
    	if (SgsStringUtils.isNullOrEmpty(mRemotePartyDisplayName)){
            mRemotePartyDisplayName = SgsUriUtils.getDisplayName(getRemotePartyUri());
            mRemotePartyDisplayName = SgsStringUtils.isNullOrEmpty(mRemotePartyDisplayName) ? "(null)" : mRemotePartyDisplayName;
        }
        return mRemotePartyDisplayName;
    }

    public void setSigCompId(String compId){
		if(compId != null && mCompId != compId){
			//getSession().removeSigCompCompartment();
		}
		if((mCompId = compId) != null){
			//getSession().addSigCompCompartment(mCompId);
		}
	}
    
    public void delete(){
		getSession().delete();
	}

    protected abstract TetheringSession getSession();

    protected void init(){
        // Sip Headers (common to all sessions)
        getSession().addCaps("+g.oma.sip-im");
        getSession().addCaps("language", "\"en,fr\"");
    }

	@Override
	public int compareTo(TetheringSession arg0) {
		return (int)(getId() - arg0.getId());
	}
}
