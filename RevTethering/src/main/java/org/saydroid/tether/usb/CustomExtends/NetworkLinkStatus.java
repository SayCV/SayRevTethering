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

package org.saydroid.tether.usb.CustomExtends;

import android.os.Parcel;
import android.os.Parcelable;

public class NetworkLinkStatus implements Parcelable {
    /**
     * Presence status
     */
    public enum State {
        None,
        Offline,
        Busy,
        Away,
        Online,
        BeRightBack,
        OnThePhone,
        OutToLunch,
        HyperAvailable
    }

    /**
     * The fine-grained state of a network connection. This level of detail
     * is probably of interest to few applications. Most should use
     * {@link android.net.NetworkInfo.State State} instead.
     */
    public enum DetailedState {
        /** Ready to start data connection setup. */
        IDLE,
        /** Searching for an available access point. */
        SCANNING,
        /** Currently setting up data connection. */
        CONNECTING,
        /** Network link established, performing authentication. */
        AUTHENTICATING,
        /** Awaiting response from DHCP server in order to assign IP address information. */
        OBTAINING_IPADDR,
        /** IP traffic should be available. */
        CONNECTED,
        /** IP traffic is suspended */
        SUSPENDED,
        /** Currently tearing down data connection. */
        DISCONNECTING,
        /** IP traffic not available. */
        DISCONNECTED,
        /** Attempt to connect failed. */
        FAILED,
        /** Access to this network is blocked. */
        BLOCKED,
        /** Link has poor connectivity. */
        VERIFYING_POOR_LINK,
        /** Checking if network is a captive portal */
        CAPTIVE_PORTAL_CHECK
    }

    private int mNetworkType;
    private int mSubtype;
    private String mTypeName;
    private String mSubtypeName;
    private State mState;
    private DetailedState mDetailedState;
    private String mReason;
    private String mExtraInfo;
    private boolean mIsFailover;
    private boolean mIsRoaming;
    private boolean mIsConnectedToProvisioningNetwork;
    private boolean mINetIP;

    /**
     * Indicates whether network connectivity is possible:
     */
    private boolean mIsAvailable;

    /**
     * @param type network type
     * @deprecated
     * @hide because this constructor was only meant for internal use (and
     * has now been superseded by the package-private constructor below).
     */
    public NetworkLinkStatus(int type) {}

    /** {@hide} */
    public NetworkLinkStatus(NetworkLinkStatus source) {
        if (source != null) {
            mNetworkType = source.mNetworkType;
            mSubtype = source.mSubtype;
            mTypeName = source.mTypeName;
            mSubtypeName = source.mSubtypeName;
            mState = source.mState;
            mDetailedState = source.mDetailedState;
            mReason = source.mReason;
            mExtraInfo = source.mExtraInfo;
            mIsFailover = source.mIsFailover;
            mIsRoaming = source.mIsRoaming;
            mIsAvailable = source.mIsAvailable;
            mIsConnectedToProvisioningNetwork = source.mIsConnectedToProvisioningNetwork;
        }
    }

    /**
     * Set the extraInfo field.
     * @param extraInfo an optional {@code String} providing addditional network state
     * information passed up from the lower networking layers.
     * @hide
     */
    public void setExtraInfo(String extraInfo) {
        synchronized (this) {
            this.mExtraInfo = extraInfo;
        }
    }

    public String getReason() {
        synchronized (this) {
            return mReason;
        }
    }

    public String getTypeName() {
        synchronized (this) {
            return mTypeName;
        }
    }

    public String getSubtypeName() {
        synchronized (this) {
            return mSubtypeName;
        }
    }

    /**
     * Report the extra information about the network state, if any was
     * provided by the lower networking layers.,
     * if one is available.
     * @return the extra information, or null if not available
     */
    public String getExtraInfo() {
        synchronized (this) {
            return mExtraInfo;
        }
    }

    @Override
    public String toString() {
        synchronized (this) {
            StringBuilder builder = new StringBuilder("NetworkLinkStatus: ");
            builder.append("type: ").append(getTypeName()).append("[").append(getSubtypeName()).
                    append("], state: ").append(mState).append("/").append(mDetailedState).
                    append(", reason: ").append(mReason == null ? "(unspecified)" : mReason).
                    append(", extra: ").append(mExtraInfo == null ? "(none)" : mExtraInfo).
                    append(", roaming: ").append(mIsRoaming).
                    append(", failover: ").append(mIsFailover).
                    append(", isAvailable: ").append(mIsAvailable).
                    append(", isConnectedToProvisioningNetwork: ").
                    append(mIsConnectedToProvisioningNetwork);
            return builder.toString();
        }
    }

    /**
     * Implement the Parcelable interface
     * @hide
     */
    public int describeContents() {
        return 0;
    }

    /**
     * Implement the Parcelable interface.
     * @hide
     */
    public void writeToParcel(Parcel dest, int flags) {
        synchronized (this) {
            dest.writeInt(mNetworkType);
            dest.writeInt(mSubtype);
            dest.writeString(mTypeName);
            dest.writeString(mSubtypeName);
            dest.writeString(mState.name());
            dest.writeString(mDetailedState.name());
            dest.writeInt(mIsFailover ? 1 : 0);
            dest.writeInt(mIsAvailable ? 1 : 0);
            dest.writeInt(mIsRoaming ? 1 : 0);
            dest.writeInt(mIsConnectedToProvisioningNetwork ? 1 : 0);
            dest.writeString(mReason);
            dest.writeString(mExtraInfo);
        }
    }

    /**
     * Implement the Parcelable interface.
     * @hide
     */
    public static final Creator<NetworkLinkStatus> CREATOR =
            new Creator<NetworkLinkStatus>() {
                public NetworkLinkStatus createFromParcel(Parcel in) {
                    int netType = in.readInt();
                    int subtype = in.readInt();
                    String typeName = in.readString();
                    String subtypeName = in.readString();
                    NetworkLinkStatus netInfo = new NetworkLinkStatus(netType);
                    netInfo.mState = State.valueOf(in.readString());
                    netInfo.mDetailedState = DetailedState.valueOf(in.readString());
                    netInfo.mIsFailover = in.readInt() != 0;
                    netInfo.mIsAvailable = in.readInt() != 0;
                    netInfo.mIsRoaming = in.readInt() != 0;
                    netInfo.mIsConnectedToProvisioningNetwork = in.readInt() != 0;
                    netInfo.mReason = in.readString();
                    netInfo.mExtraInfo = in.readString();
                    return netInfo;
                }

                public NetworkLinkStatus[] newArray(int size) {
                    return new NetworkLinkStatus[size];
                }
            };
}