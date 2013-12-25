package org.saydroid.tether.usb;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import org.saydroid.logger.Log;
import org.saydroid.logger.LogConfiguration;
import org.saydroid.sgs.utils.SgsStringUtils;


import org.saydroid.tether.usb.Screens.BaseScreen;
import org.saydroid.tether.usb.Screens.BaseScreen.SCREEN_TYPE;
import org.saydroid.tether.usb.Screens.IBaseScreen;
import org.saydroid.tether.usb.Screens.ScreenHome;
import org.saydroid.tether.usb.Screens.ScreenSplash;
import org.saydroid.tether.usb.Services.IScreenService;

public class MainActivity extends ActivityGroup {
    private static String TAG = MainActivity.class.getCanonicalName();

    public static final int ACTION_NONE = 0;
    public static final int ACTION_RESTORE_LAST_STATE = 1;
    public static final int ACTION_SHOW_AVSCREEN = 2;
    public static final int ACTION_SHOW_CONTSHARE_SCREEN = 3;
    public static final int ACTION_SHOW_SMS = 4;
    public static final int ACTION_SHOW_CHAT_SCREEN = 5;

    private static final int RC_SPLASH = 0;

    private Handler mHandler;
    private final Engine mEngine;
    private final IScreenService mScreenService;

    public MainActivity(){
        super();

        // Sets main activity (should be done before starting services)
        mEngine = (Engine)Engine.getInstance();
        mEngine.setMainActivity(this);
        mScreenService = ((Engine)Engine.getInstance()).getScreenService();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        if(!Engine.getInstance().isStarted()){
            startActivityForResult(new Intent(this, ScreenSplash.class), MainActivity.RC_SPLASH);
            return;
        }

        Bundle bundle = savedInstanceState;
        if(bundle == null){
            Intent intent = getIntent();
            bundle = intent == null ? null : intent.getExtras();
        }
        if(bundle != null && bundle.getInt("action", MainActivity.ACTION_NONE) != MainActivity.ACTION_NONE){
            handleAction(bundle);
        }
        else if(mScreenService != null){
            mScreenService.show(ScreenHome.class);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Bundle bundle = intent.getExtras();
        if(bundle != null){
            handleAction(bundle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if(mScreenService.getCurrentScreen().hasMenu()){
            return mScreenService.getCurrentScreen().createOptionsMenu(menu);
        }

        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        Log.d(TAG, "Krupa - onPrepareOptionsMenu");
        if(mScreenService.getCurrentScreen().hasMenu()){
            menu.clear();
            return mScreenService.getCurrentScreen().createOptionsMenu(menu);
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "Krupa - onOptionsItemSelected");
        IBaseScreen baseScreen = mScreenService.getCurrentScreen();
        if(baseScreen instanceof Activity){
            return ((Activity)baseScreen).onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(mScreenService == null){
            super.onSaveInstanceState(outState);
            return;
        }

        IBaseScreen screen = mScreenService.getCurrentScreen();
        if(screen != null){
            outState.putInt("action", MainActivity.ACTION_RESTORE_LAST_STATE);
            outState.putString("screen-id", screen.getId());
            outState.putString("screen-type", screen.getType().toString());
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        this.handleAction(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult("+requestCode+","+resultCode+")");
        if(resultCode == RESULT_OK){
            if(requestCode == MainActivity.RC_SPLASH){
                Log.d(TAG, "Result from splash screen");
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(!BaseScreen.processKeyDown(keyCode, event)){
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    public void exit(){
        mHandler.post(new Runnable() {
            public void run() {
                if (!Engine.getInstance().stop()) {
                    Log.e(TAG, "Failed to stop engine");
                }
                finish();
            }
        });
    }

    private void handleAction(Bundle bundle){
        final String id;
        switch(bundle.getInt("action", MainActivity.ACTION_NONE)){
            // Default or ACTION_RESTORE_LAST_STATE
            default:
            case ACTION_RESTORE_LAST_STATE:
                id = bundle.getString("screen-id");
                final String screenTypeStr = bundle.getString("screen-type");
                final SCREEN_TYPE screenType = SgsStringUtils.isNullOrEmpty(screenTypeStr) ? BaseScreen.SCREEN_TYPE.HOME_T :
                        SCREEN_TYPE.valueOf(screenTypeStr);
                switch(screenType){
                    default:
                        if(!mScreenService.show(id)){
                            mScreenService.show(ScreenHome.class);
                        }
                        break;
                }
                break;

            // Notify for new SMSs
            case ACTION_SHOW_SMS:
                //mScreenService.show(ScreenTabMessages.class);
                break;

            // Show Audio/Video Calls
            case ACTION_SHOW_AVSCREEN:
                Log.d(TAG, "Main.ACTION_SHOW_AVSCREEN");


                break;

            // Show Content Share Queue
            case ACTION_SHOW_CONTSHARE_SCREEN:
                //mScreenService.show(ScreenFileTransferQueue.class);
                break;

            // Show Chat Queue
            case ACTION_SHOW_CHAT_SCREEN:
                //mScreenService.show(ScreenChatQueue.class);
                break;
        }
    }
}
