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

import org.saydroid.sgs.events.SgsRegistrationEventTypes;
import org.saydroid.sgs.utils.SgsConfigurationEntry;
import org.saydroid.sgs.utils.SgsUriUtils;
import org.saydroid.tether.usb.CustomDialog;
import org.saydroid.tether.usb.CustomExtends.GridViewItemContainer;
import org.saydroid.tether.usb.Engine;
import org.saydroid.tether.usb.Events.TrafficCountEventArgs;
import org.saydroid.tether.usb.Events.TrafficCountEventTypes;
import org.saydroid.tether.usb.MainActivity;
import org.saydroid.tether.usb.R;
import org.saydroid.sgs.events.SgsEventArgs;
import org.saydroid.sgs.events.SgsRegistrationEventArgs;
import org.saydroid.sgs.services.ISgsSipService;
import org.saydroid.tether.usb.Tethering.TetheringRegistrationSession;
import org.saydroid.tether.usb.Tethering.TetheringSession.ConnectionState;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import org.saydroid.logger.Log;
import org.saydroid.tether.usb.Services.ITetheringService;

public class ScreenHome extends BaseScreen {
    private static String TAG = ScreenHome.class.getCanonicalName();

    private static final int MENU_EXIT = 0;
    private static final int MENU_SETTINGS = 1;

    private GridView mGridView;

    private ProgressDialog mStartStopProgressDialog;
    private static int ID_DIALOG_STARTING = 0;
    private static int ID_DIALOG_STOPPING = 1;

    private RelativeLayout mTrafficRow = null;
    private TextView mDownloadText = null;
    private TextView mUploadText = null;
    private TextView mDownloadRateText = null;
    private TextView mUploadRateText = null;

    private final ITetheringService mTetheringService;

    private BroadcastReceiver mTetheringBroadCastRecv;

    public ScreenHome() {
        super(SCREEN_TYPE.HOME_T, TAG);

        mTetheringService = getEngine().getTetheringService();
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

    protected Dialog onCreateDialog(int id) {
        if (id == ID_DIALOG_STARTING) {
            mStartStopProgressDialog = new ProgressDialog(this);
            mStartStopProgressDialog.setTitle("Start Tethering");
            mStartStopProgressDialog.setMessage("Please wait while starting...");
            mStartStopProgressDialog.setIndeterminate(false);
            mStartStopProgressDialog.setCancelable(true);
            return mStartStopProgressDialog;
        }
        else if (id == ID_DIALOG_STOPPING) {
            mStartStopProgressDialog = new ProgressDialog(this);
            mStartStopProgressDialog.setTitle("Stop Tethering");
            mStartStopProgressDialog.setMessage("Please wait while stopping...");
            mStartStopProgressDialog.setIndeterminate(false);
            mStartStopProgressDialog.setCancelable(true);
            return mStartStopProgressDialog;
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_home);

        mTrafficRow = (RelativeLayout)findViewById(R.id.screen_home_trafficRow);
        mDownloadText = (TextView)findViewById(R.id.screen_home_trafficDown);
        mUploadText = (TextView)findViewById(R.id.screen_home_trafficUp);
        mDownloadRateText = (TextView)findViewById(R.id.screen_home_trafficDownRate);
        mUploadRateText = (TextView)findViewById(R.id.screen_home_trafficUpRate);
        /*if(getEngine().getConfigurationService().getBoolean(SgsConfigurationEntry.NETWORK_CONNECTED,
                SgsConfigurationEntry.DEFAULT_NETWORK_CONNECTED)){
            mTrafficRow.setVisibility(View.VISIBLE);
            //TetheringRegistrationSession.setTrafficCounterThreadClassEnabled(true);
        }*/

        mGridView = (GridView) findViewById(R.id.screen_home_gridview);
        mGridView.setAdapter(new ScreenHomeAdapter(this));
        mGridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ScreenHomeItem item = (ScreenHomeItem)parent.getItemAtPosition(position);
                if (item != null) {
                    if(position == ScreenHomeItem.ITEM_SIGNIN_SIGNOUT_POS){
                        if(mTetheringService.getRegistrationState() == ConnectionState.CONNECTING || mTetheringService.getRegistrationState() == ConnectionState.TERMINATING){
                            mTetheringService.stopStack();
                        } else if (mTetheringService.isRegistered()){
                            //showDialog(ID_DIALOG_STOPPING);
                            mStartStopProgressDialog = ProgressDialog.show(
                                    ScreenHome.this,
                                    //R.drawable.exit_48,
                                    "Stop Tethering",
                                    "Please wait while stopping...",
                                    true, false,
                                    new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            //finish();
                                        }
                                    });
                            new Thread(new Runnable(){
                                public void run(){
                                    if(mTetheringService.unRegister() == false){
                                        if(mStartStopProgressDialog != null && mStartStopProgressDialog.isShowing()){
                                            mStartStopProgressDialog.dismiss();
                                        }
                                    }
                                    //dismissDialog(ID_DIALOG_STOPPING);
                                }
                            }).start();
                        } else {
                            //showDialog(ID_DIALOG_STARTING);
                            mStartStopProgressDialog = ProgressDialog.show(
                                    ScreenHome.this,
                                    //R.drawable.exit_48,
                                    "Start Tethering",
                                    "Please wait while starting...",
                                    true, false,
                                    new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            //finish();
                                        }
                                    });
                            new Thread(new Runnable(){
                                public void run(){
                                    if(mTetheringService.register(ScreenHome.this) == false){
                                        if(mStartStopProgressDialog != null && mStartStopProgressDialog.isShowing()){
                                            mStartStopProgressDialog.dismiss();
                                        }
                                    }
                                    //dismissDialog(ID_DIALOG_STARTING);
                                }
                            }).start();
                        }
                    } else if (position == ScreenHomeItem.ITEM_EXIT_POS){
                        CustomDialog.show(
                                ScreenHome.this,
                                R.drawable.exit_48,
                                null,
                                "Are you sure you want to exit?",
                                "Yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((MainActivity)(getEngine().getMainActivity())).exit();
                                    }
                                }, "No",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                    } else {
                        mScreenService.show(item.mClass, item.mClass.getCanonicalName());
                    }
                }
            }
        });

        mTetheringBroadCastRecv = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                // Registration Event
                if(SgsRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)){
                    SgsRegistrationEventArgs args = intent.getParcelableExtra(SgsEventArgs.EXTRA_EMBEDDED);
                    if(args == null){
                        Log.e(TAG, "Invalid event args");
                        return;
                    }
                    switch(args.getEventType()){
                        /*case REGISTRATION_INPROGRESS:
                            //if(mStartStopProgressDialog.isShowing()) { mStartStopProgressDialog.dismiss(); }
                            mStartStopProgressDialog = ProgressDialog.show(
                                    ScreenHome.this,
                                    //R.drawable.exit_48,
                                    "Start Tethering",
                                    "Please wait while starting...",
                                    true, false,
                                    new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            //finish();
                                        }
                                    });
                            break;
                        case UNREGISTRATION_INPROGRESS:
                            //if(mStartStopProgressDialog.isShowing()) { mStartStopProgressDialog.dismiss(); }
                            mStartStopProgressDialog = ProgressDialog.show(
                                    ScreenHome.this,
                                    //R.drawable.exit_48,
                                    "Stop Tethering",
                                    "Please wait while stopping...",
                                    true, false,
                                    new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            //finish();
                                        }
                                    });
                            break;*/
                        case REGISTRATION_NOK:
                        case REGISTRATION_OK:
                            //dismissDialog(ID_DIALOG_STARTING);
                            //break;
                        case UNREGISTRATION_OK:
                        case UNREGISTRATION_NOK:
                            //dismissDialog(ID_DIALOG_STOPPING);
                            mStartStopProgressDialog.dismiss();
                            break;
                        default:
                            //if(mStartStopProgressDialog.isShowing()) { mStartStopProgressDialog.dismiss(); }

                            break;
                    }
                    switch(args.getEventType()){
                        case REGISTRATION_NOK:
                        case UNREGISTRATION_OK:
                        case REGISTRATION_OK:
                        case REGISTRATION_INPROGRESS:
                        case UNREGISTRATION_INPROGRESS:
                        case UNREGISTRATION_NOK:
                        default:
                            ((ScreenHomeAdapter)mGridView.getAdapter()).refresh();
                            break;
                    }
                }
                if(TrafficCountEventArgs.ACTION_TRAFFIC_COUNT_EVENT.equals(action)){
                    TrafficCountEventArgs args = intent.getParcelableExtra(SgsEventArgs.EXTRA_EMBEDDED);
                    final TrafficCountEventTypes type;
                    if(args == null){
                        Log.e(TAG, "Invalid event args");
                        return;
                    }
                    switch((type = args.getEventType())){
                        case COUNTING:
                            //TrafficCountEventArgs.DataCount dataCount = new TrafficCountEventArgs.DataCount();
                            String dateString = intent.getStringExtra(TrafficCountEventArgs.EXTRA_DATE);
                            mTrafficRow.setVisibility(View.VISIBLE);
                            long uploadTraffic = args.getTotalUpload();
                            long downloadTraffic = args.getTotalDownload();
                            long uploadRate = args.getUploadRate();
                            long downloadRate = args.getDownloadRate();

                            // Set rates to 0 if values are negative
                            if (uploadRate < 0)
                                uploadRate = 0;
                            if (downloadRate < 0)
                                downloadRate = 0;

                            mUploadText.setText(formatCount(uploadTraffic, false));
                            mDownloadText.setText(formatCount(downloadTraffic, false));
                            mDownloadText.invalidate();
                            mUploadText.invalidate();

                            mUploadRateText.setText(formatCount(uploadRate, true));
                            mDownloadRateText.setText(formatCount(downloadRate, true));
                            mDownloadRateText.invalidate();
                            mUploadRateText.invalidate();
                            break;
                        case END:
                        default:
                            mTrafficRow.setVisibility(View.INVISIBLE);
                            Log.d(TAG, "Traffic Count thread has disposed.");
                            break;
                    }
                }
            }
        };
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SgsRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
        intentFilter.addAction(TrafficCountEventArgs.ACTION_TRAFFIC_COUNT_EVENT);
        registerReceiver(mTetheringBroadCastRecv, intentFilter);
    }

    @Override
    protected void onDestroy() {
        if(mTetheringBroadCastRecv != null){
            unregisterReceiver(mTetheringBroadCastRecv);
            mTetheringBroadCastRecv = null;
        }

        super.onDestroy();
    }

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public boolean createOptionsMenu(Menu menu) {
        menu.add(0, ScreenHome.MENU_SETTINGS, 0, "Settings");
		/*MenuItem itemExit =*/ menu.add(0, ScreenHome.MENU_EXIT, 0, "Exit");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case ScreenHome.MENU_EXIT:
                ((MainActivity)getEngine().getMainActivity()).exit();
                break;
            case ScreenHome.MENU_SETTINGS:
                mScreenService.show(ScreenSettings.class);
                break;
        }
        return true;
    }


    /**
     * ScreenHomeItem
     */
    static class ScreenHomeItem {
        static final int ITEM_SIGNIN_SIGNOUT_POS = 0;
        static final int ITEM_EXIT_POS = 3;
        final int mIconResId;
        final String mText;
        final Class<? extends Activity> mClass;

        private ScreenHomeItem(int iconResId, String text, Class<? extends Activity> _class) {
            mIconResId = iconResId;
            mText = text;
            mClass = _class;
        }
    }

    /**
     * ScreenHomeAdapter
     */
    static class ScreenHomeAdapter extends BaseAdapter{
        static final int ALWAYS_VISIBLE_ITEMS_COUNT = 5;
        static final ScreenHomeItem[] sItems =  new ScreenHomeItem[]{
                // always visible
                //new ScreenHomeItem(R.drawable.start_48, "Start Tethering", null),
                new ScreenHomeItem(R.drawable.start_48, "Tethering", null),
                new ScreenHomeItem(R.drawable.options_48, "Options", ScreenSettings.class),
                new ScreenHomeItem(R.drawable.about_48, "About", ScreenAbout.class),
                new ScreenHomeItem(R.drawable.exit_48, "Exit/Quit", null),
                new ScreenHomeItem(R.drawable.history_48, "History", ScreenTabHistory.class),
                new ScreenHomeItem(R.drawable.loaded_hand_truck_48, "Manual", ScreenTabHistory.class),
                // visible only if connected
                //new ScreenHomeItem(R.drawable.stop_48, "Stop Tethering", null),
                //new ScreenHomeItem(R.drawable.dialer_48, "Dialer", ScreenTabDialer.class),
                //new ScreenHomeItem(R.drawable.eab2_48, "Address Book", ScreenTabContacts.class),
                //new ScreenHomeItem(R.drawable.history_48, "History", ScreenTabHistory.class),
                //new ScreenHomeItem(R.drawable.chat_48, "Messages", ScreenTabMessages.class),
        };

        private final LayoutInflater mInflater;
        private final ScreenHome mBaseScreen;

        ScreenHomeAdapter(ScreenHome baseScreen){
            mInflater = LayoutInflater.from(baseScreen);
            mBaseScreen = baseScreen;
        }

        void refresh(){
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return ALWAYS_VISIBLE_ITEMS_COUNT;
        }

        @Override
        public Object getItem(int position) {
            return sItems[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ScreenHomeItem item = (ScreenHomeItem)getItem(position);

            if(item == null){
                return null;
            }

            if (view == null) {
                view = mInflater.inflate(R.layout.screen_home_item, null);
            }

            if(position == ScreenHomeItem.ITEM_SIGNIN_SIGNOUT_POS){
                if(mBaseScreen.mTetheringService.getRegistrationState() == ConnectionState.CONNECTING ||
                        mBaseScreen.mTetheringService.getRegistrationState() == ConnectionState.TERMINATING){
                    ((TextView) view.findViewById(R.id.screen_home_item_text)).setText("Cancel");
                    ((ImageView) view .findViewById(R.id.screen_home_item_icon)).setImageResource(R.drawable.start_48);
                } else if(mBaseScreen.mTetheringService.isRegistered()/* ||
                       Engine.getInstance().getConfigurationService().getBoolean(SgsConfigurationEntry.NETWORK_CONNECTED,
                                SgsConfigurationEntry.DEFAULT_NETWORK_CONNECTED)*/){
                    //((TextView) view.findViewById(R.id.screen_home_item_text)).setText("Stop tethering");
                    ((TextView) view.findViewById(R.id.screen_home_item_text)).setText("Tethering");
                    ((ImageView) view .findViewById(R.id.screen_home_item_icon)).setImageResource(R.drawable.stop_48);
                } else {
                    //((TextView) view.findViewById(R.id.screen_home_item_text)).setText("Start tethering");
                    ((TextView) view.findViewById(R.id.screen_home_item_text)).setText("Tethering");
                    ((ImageView) view .findViewById(R.id.screen_home_item_icon)).setImageResource(R.drawable.start_48);
                }
            } else {
                ((TextView) view.findViewById(R.id.screen_home_item_text)).setText(item.mText);
                ((ImageView) view .findViewById(R.id.screen_home_item_icon)).setImageResource(item.mIconResId);
            }
            /*
            // convertView has been just been inflated or came from getView parameter.
            if (!(view instanceof GridViewItemContainer)) {
                ViewGroup container = new GridViewItemContainer(mInflater.getContext());

                // If you have tags, move them to the new top element. E.g.:
                //container.setTag(convertView.getTag());
                //convertView.setTag(null);

                container.addView(view);
                view = container;
            }

            // Where numColumns is the number of columns in the GridView and
            // 'viewsInRow' is an list of View on the current row of where 'position' is located.
            int numColumns = ((GridView)convertView).getNumColumns();
            View[] viewsInRow = new View[numColumns];
            viewsInRow[position % numColumns] = view;
            GridViewItemContainer referenceView = (GridViewItemContainer)view;
            if ((position % numColumns == (numColumns-1)) || (position == getCount()-1)) {
                referenceView.setViewsInRow(viewsInRow);
            }
            else {
                referenceView.setViewsInRow(null);
            }
			*/
            return view;
        }

    }
}
