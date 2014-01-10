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

import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import org.saydroid.tether.usb.CustomExtends.AutoResizeTextView;
import org.saydroid.tether.usb.Model.HistoryTrafficCountEvent;
import org.saydroid.tether.usb.Model.HistoryTrafficCountEvent.HistoryEventTrafficCountFilter;
import org.saydroid.tether.usb.R;
import org.saydroid.sgs.model.SgsHistoryAVCallEvent.HistoryEventAVFilter;
import org.saydroid.sgs.model.SgsHistoryEvent;
import org.saydroid.sgs.services.ISgsHistoryService;
import org.saydroid.sgs.services.ISgsSipService;
import org.saydroid.sgs.utils.SgsStringUtils;
import org.saydroid.sgs.utils.SgsUriUtils;
import org.saydroid.tether.usb.QuickAction.ActionItem;
import org.saydroid.tether.usb.QuickAction.QuickAction;
import org.saydroid.tether.usb.Services.ITetheringService;
import org.saydroid.tether.usb.Utils.DateTimeUtils;

public class ScreenTabHistory extends BaseScreen {
	private static final String TAG = ScreenTabHistory.class.getCanonicalName();
	private static final int SELECT_CONTENT = 1;
	
	private final ISgsHistoryService mHistorytService;
	private final ITetheringService mTetheringService;
	
	private ScreenTabHistoryAdapter mAdapter;
	private ListView mListView;
	
	private final ActionItem mAItemVoiceCall;
	private final ActionItem mAItemVideoCall;
	private final ActionItem mAItemChat;
	private final ActionItem mAItemSMS;
    private final ActionItem mAItemTrafficCount;
	private final ActionItem mAItemShare;
	
	private SgsHistoryEvent mSelectedEvent;
	private QuickAction mLasQuickAction;
	
	public ScreenTabHistory() {
		super(SCREEN_TYPE.TAB_HISTORY_T, TAG);
		
		mHistorytService = getEngine().getHistoryService();
		mTetheringService = getEngine().getTetheringService();
		
		mAItemVoiceCall = new ActionItem();
		mAItemVoiceCall.setTitle("Voice");
		mAItemVoiceCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSelectedEvent != null){
					//ScreenAV.makeCall(mSelectedEvent.getRemoteParty(), SgsMediaType.Audio);
					if(mLasQuickAction != null){
						mLasQuickAction.dismiss();
					}
				}
			}
		});
		
		mAItemVideoCall = new ActionItem();
		mAItemVideoCall.setTitle("Video");
		mAItemVideoCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSelectedEvent != null){
					//ScreenAV.makeCall(mSelectedEvent.getRemoteParty(), SgsMediaType.AudioVideo);
					if(mLasQuickAction != null){
						mLasQuickAction.dismiss();
					}
				}
			}
		});
		
		mAItemChat = new ActionItem();
		mAItemChat.setTitle("Chat");
		mAItemChat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSelectedEvent != null){
					//ScreenChat.startChat(mSelectedEvent.getRemoteParty(), false);
					if(mLasQuickAction != null){
						mLasQuickAction.dismiss();
					}
				}
			}
		});
		
		mAItemSMS = new ActionItem();
		mAItemSMS.setTitle("SMS");
		mAItemSMS.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSelectedEvent != null){
					//ScreenChat.startChat(mSelectedEvent.getRemoteParty(), true);
					if(mLasQuickAction != null){
						mLasQuickAction.dismiss();
					}
				}
			}
		});
		
		mAItemShare = new ActionItem();
		mAItemShare.setTitle("Share");
		mAItemShare.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSelectedEvent != null){
					Intent intent = new Intent();
                    intent.setType("*/*")
                    	.addCategory(Intent.CATEGORY_OPENABLE)
                    	.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select content"), SELECT_CONTENT);   
					if(mLasQuickAction != null){
						mLasQuickAction.dismiss();
					}
				}
			}
		});

        mAItemTrafficCount = new ActionItem();
        mAItemTrafficCount.setTitle("TrafficCount");
        mAItemTrafficCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSelectedEvent != null){
                    //ScreenChat.startChat(mSelectedEvent.getRemoteParty(), true);
                    if(mLasQuickAction != null){
                        mLasQuickAction.dismiss();
                    }
                }
            }
        });
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_tab_history);
		
		mAdapter = new ScreenTabHistoryAdapter(this);
		mListView = (ListView) findViewById(R.id.screen_tab_history_listView);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(mOnItemListViewClickListener);
		mListView.setOnItemLongClickListener(mOnItemListViewLongClickListener);
		
		mAItemVoiceCall.setIcon(getResources().getDrawable(R.drawable.voice_call_25));
		mAItemVideoCall.setIcon(getResources().getDrawable(R.drawable.visio_call_25));
		mAItemChat.setIcon(getResources().getDrawable(R.drawable.chat_25));
		mAItemSMS.setIcon(getResources().getDrawable(R.drawable.sms_25));
		mAItemShare.setIcon(getResources().getDrawable(R.drawable.image_gallery_25));
        mAItemTrafficCount.setIcon(getResources().getDrawable(R.drawable.sms_25));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(mHistorytService.isLoading()){
			Toast.makeText(this, "Loading history...", Toast.LENGTH_SHORT).show();
		}
	}
	
	private final OnItemClickListener mOnItemListViewClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if(!mTetheringService.isRegistered()){
				Log.e(TAG, "Not registered yet");
				return;
			}
			
			mSelectedEvent = (SgsHistoryEvent)parent.getItemAtPosition(position);
			if(mSelectedEvent != null){
				mLasQuickAction = new QuickAction(view);
				if(!SgsStringUtils.isNullOrEmpty(mSelectedEvent.getRemoteParty())){
					mLasQuickAction.addActionItem(mAItemVoiceCall);
					mLasQuickAction.addActionItem(mAItemVideoCall);
					mLasQuickAction.addActionItem(mAItemChat);
					mLasQuickAction.addActionItem(mAItemSMS);
					mLasQuickAction.addActionItem(mAItemShare);
                    mLasQuickAction.addActionItem(mAItemTrafficCount);
				}
				mLasQuickAction.setAnimStyle(QuickAction.ANIM_AUTO);
				mLasQuickAction.show();
			}
		}
	};
	
	private final OnItemLongClickListener mOnItemListViewLongClickListener = new OnItemLongClickListener(){
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			return false;
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case SELECT_CONTENT:
					if (mSelectedEvent != null) {
						Uri selectedContentUri = data.getData();
						String selectedContentPath = super.getPath(selectedContentUri);
						//ScreenFileTransferView.sendFile(mSelectedEvent.getRemoteParty(), selectedContentPath);
					}
					break;
			}
		}
	}

	
	//
	// ScreenTabHistoryAdapter
	//
	static class ScreenTabHistoryAdapter extends BaseAdapter implements Observer {
		private List<SgsHistoryEvent> mEvents;
		private final LayoutInflater mInflater;
		private final Handler mHandler;
		private final ScreenTabHistory mBaseScreen;
		
		private final static int TYPE_ITEM_AV = 0;
		private final static int TYPE_ITEM_SMS = 1;
		private final static int TYPE_ITEM_FILE_TRANSFER = 2;
        private final static int TYPE_ITEM_TRAFFIC_COUNT = 3;
		private final static int TYPE_COUNT = 4;
		
		ScreenTabHistoryAdapter(ScreenTabHistory baseSceen) {
			mBaseScreen = baseSceen;
			mHandler = new Handler();
			mInflater = LayoutInflater.from(mBaseScreen);
			/*mEvents = mBaseScreen.mHistorytService.getObservableEvents()
					.filter(new HistoryEventAVFilter());*/
            mEvents = mBaseScreen.mHistorytService.getObservableEvents()
                    .filter(new HistoryEventTrafficCountFilter());
			mBaseScreen.mHistorytService.getObservableEvents().addObserver(this);
		}
		
		@Override
		protected void finalize() throws Throwable {
			mBaseScreen.mHistorytService.getObservableEvents().deleteObserver(this);
			super.finalize();
		}
		
		@Override
		public int getViewTypeCount() {
			return TYPE_COUNT;
		}
		
		@Override
		public int getItemViewType(int position) {
			final SgsHistoryEvent event = (SgsHistoryEvent)getItem(position);
			if(event != null){
				switch(event.getMediaType()){
					case Audio:
					case AudioVideo:
						default:
						return TYPE_ITEM_AV;
					case FileTransfer:
						return TYPE_ITEM_FILE_TRANSFER;
					case SMS:
						return TYPE_ITEM_SMS;
                    case TrafficCount:
                        return TYPE_ITEM_TRAFFIC_COUNT;
				}
			}
			return TYPE_ITEM_AV;
		}
		
		@Override
		public int getCount() {
			return mEvents.size();
		}

		@Override
		public Object getItem(int position) {
			return mEvents.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public void update(Observable observable, Object data) {
			//mEvents = mBaseScreen.mHistorytService.getObservableEvents().filter(new HistoryEventAVFilter());
            mEvents = mBaseScreen.mHistorytService.getObservableEvents().filter(new HistoryEventTrafficCountFilter());

			if(Thread.currentThread() == Looper.getMainLooper().getThread()){
				notifyDataSetChanged();
			}
			else{
				mHandler.post(new Runnable(){
					@Override
					public void run() {
						notifyDataSetChanged();
					}
				});
			}
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			
			final SgsHistoryEvent event = (SgsHistoryEvent)getItem(position);
			if(event == null){
				return null;
			}
			if (view == null) {
				switch(event.getMediaType()){
					case Audio:
					case AudioVideo:
						view = mInflater.inflate(R.layout.screen_tab_history_item_av, null);
						break;
                    case TrafficCount:
                        view = mInflater.inflate(R.layout.screen_tab_history_item_traffic_count, null);
                        break;
					case FileTransfer:
					case SMS:
					default:
						Log.e(TAG, "Invalid media type");
						return null;
				}
			}
			
			//String remoteParty = SgsUriUtils.getDisplayName(event.getRemoteParty());
            String remoteParty = "HistoryTrafficCountEvent";
			
			if(event != null){
				switch(event.getMediaType()){
					case Audio:
					case AudioVideo:
						final ImageView ivType = (ImageView)view.findViewById(R.id.screen_tab_history_item_av_imageView_type);
						final TextView tvRemote = (TextView)view.findViewById(R.id.screen_tab_history_item_av_textView_remote);
						final TextView tvDate = (TextView)view.findViewById(R.id.screen_tab_history_item_av_textView_date);
						final String date = DateTimeUtils.getFriendlyDateString(new Date(event.getStartTime()));
						tvDate.setText(date);
						tvRemote.setText(remoteParty);
						switch(event.getStatus()){
							case Outgoing:
								ivType.setImageResource(R.drawable.call_outgoing_45);
								break;
							case Incoming:
								ivType.setImageResource(R.drawable.call_incoming_45);
								break;
							case Failed:
							case Missed:
								ivType.setImageResource(R.drawable.call_missed_45);
								break;
						}
						break;
                    case TrafficCount:
                        final ImageView ivTrafficCountType = (ImageView)view.findViewById(R.id.screen_tab_history_item_trafficCount_imageView_type);
                        final TextView tvSend = (TextView)view.findViewById(R.id.screen_tab_history_item_trafficCount_textView_upTotal);
                        final TextView tvReceive = (TextView)view.findViewById(R.id.screen_tab_history_item_trafficCount_textView_downloadTotal);
                        final AutoResizeTextView tvStartDate = (AutoResizeTextView)view.findViewById(R.id.screen_tab_history_item_trafficCount_textView_startDate);
                        final AutoResizeTextView tvEndDate = (AutoResizeTextView)view.findViewById(R.id.screen_tab_history_item_trafficCount_textView_endDate);
                        final String startDate = DateTimeUtils.getFriendlyDateString(new Date(event.getStartTime()));
                        final String endDate = DateTimeUtils.getFriendlyDateString(new Date(event.getEndTime()));
                        tvStartDate.setText(startDate);
                        tvEndDate.setText(endDate);
                        tvStartDate.setMinTextSize(4f);
                        tvStartDate.setTextSize(10f);
                        tvStartDate.resizeText();
                        tvEndDate.setMinTextSize(4f);
                        tvEndDate.setTextSize(10f);
                        tvEndDate.resizeText();

                        final HistoryTrafficCountEvent TrafficCountEvent = (HistoryTrafficCountEvent)event;
                        //final String content = TrafficCountEvent.getContent();
                        //final boolean bIncoming = TrafficCountEvent.getStatus() == SgsHistoryEvent.StatusType.Incoming;
                        // Up and Download is defines by based on Tether
                        // Now RevTether is oppose to the defines: Upload, Download
                        tvSend.setText(formatCount(Long.parseLong(TrafficCountEvent.getTotalDownload()), false));
                        tvReceive.setText(formatCount(Long.parseLong(TrafficCountEvent.getTotalUpload()), false));
                        switch(event.getStatus()){
                            case Outgoing:
                                ivTrafficCountType.setImageResource(R.drawable.call_outgoing_45);
                                break;
                            case Incoming:
                                ivTrafficCountType.setImageResource(R.drawable.call_incoming_45);
                                break;
                            case Failed:
                            case Missed:
                                ivTrafficCountType.setImageResource(R.drawable.traffic_48);
                                break;
                        }
                        break;
				}
			}
			
			return view;
		}

        private String formatCount(long count, boolean rate) {
            // Converts the supplied argument into a string.
            // 'rate' indicates whether is a total bytes, or bits per sec.
            // Under 2Mb, returns "xxx.xKb"
            // Over 2Mb, returns "xxx.xxMb"
            //if (count < 1e6 * 2)
            if (count < 1e6 * 1)
                return ((float)((int)(count*10/1024))/10 + (rate ? "KB/s" : "KB"));
            return ((float)((int)(count*100/1024/1024))/100 + (rate ? "MB/s" : "MB"));
        }
	}
}
