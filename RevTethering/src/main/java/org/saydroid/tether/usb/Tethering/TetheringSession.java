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
				// getSession().delete();
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
            mId = 0;
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
    
    public void delete(){
		// getSession().delete();
	}

    // protected abstract TetheringSession getSession();

    protected void init(){
        // Sip Headers (common to all sessions)
        // getSession().addCaps("+g.oma.sip-im");
        // getSession().addCaps("language", "\"en,fr\"");
    }

	@Override
	public int compareTo(TetheringSession arg0) {
		// return (int)(getId() - arg0.getId());
        return 0;
	}
}
