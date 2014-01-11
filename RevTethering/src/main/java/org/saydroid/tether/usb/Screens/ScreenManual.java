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

import org.saydroid.sgs.services.ISgsConfigurationService;
import org.saydroid.tether.usb.CustomExtends.NetworkLinkStatus;
import org.saydroid.tether.usb.SRTDroid;
import org.saydroid.tether.usb.R;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ScreenManual extends BaseScreen {
	private static final String TAG = ScreenAbout.class.getCanonicalName();

    private ListView mListView;
    private final static NetworkLinkStatusItem[] sListViewStatusItems = new NetworkLinkStatusItem[] {
            new NetworkLinkStatusItem(R.drawable.user_online_24, new NetworkLinkStatus(0)),
            new NetworkLinkStatusItem(R.drawable.user_busy_24, new NetworkLinkStatus(0)),
    };

    private final ISgsConfigurationService mConfigurationService;

	public ScreenManual() {
		super(SCREEN_TYPE.MANUAL_T, TAG);

        mConfigurationService = getEngine().getConfigurationService();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_manual);

        mListView = (ListView) findViewById(R.id.screen_manual_listView);
        mListView.setAdapter(new ScreenNetworkLinkAdapter(this));
	}

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    static class NetworkLinkStatusItem {
        private final int mDrawableId;
        private final NetworkLinkStatus mStatus;
        //private final String mText;

        private NetworkLinkStatusItem(int drawableId, NetworkLinkStatus status) {
            mDrawableId = drawableId;
            mStatus = status;
        }
    }

    static class ScreenNetworkLinkAdapter extends BaseAdapter {

        private final LayoutInflater mInflater;

        private ScreenNetworkLinkAdapter(Context context){
            mInflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return sListViewStatusItems.length;
        }

        @Override
        public Object getItem(int position) {
            return sListViewStatusItems[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final NetworkLinkStatusItem item = (NetworkLinkStatusItem)getItem(position);

            if (view == null) {
                view = mInflater.inflate(R.layout.screen_manual_item_link, null);
            }

            if (item == null) {
                return view;
            }

            /*((ListView) view .findViewById(R.id.screen_manual_listView))
                    .setImageResource(item.mDrawableId);
            ((TextView) view.findViewById(R.id.screen_presence_status_item_textView))
                    .setText(item.mText);*/

            return view;
        }
    }
}
