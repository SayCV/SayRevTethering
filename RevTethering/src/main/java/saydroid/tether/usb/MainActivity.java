package saydroid.tether.usb;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import org.saydroid.common.SimpleTextFragment;
import org.saydroid.common.logger.Log;
import org.saydroid.common.logger.LogWrapper;
import org.saydroid.common.logger.MessageOnlyLogFilter;

//import org.slf4j.LoggerFactory;

public class MainActivity extends FragmentActivity {
    private final static String TAG = MainActivity.class.getCanonicalName();

    // Reference to the fragment showing events, so we can clear it with a button
    // as necessary.
    private LogFragment mLogFragment;

    //final NgnEngine mEngine = NgnEngine.getInstance();
    //private final static org.slf4j.Logger sLogger = LoggerFactory.getLogger(MainActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Start log to file from here
        //LogConfiguration.getInstance().setLoggerName(MainActivity.class.getCanonicalName());
        //LogConfiguration.getInstance().setFileName(this.getFileStreamPath("SayRevTethering.log").getAbsolutePath());
        //LogConfiguration.getInstance().configure();

        Log.d(TAG, "Calling onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
            // Initialize the logging framework.
            initializeLogging();
        }*/
        // Initialize text fragment that displays intro text.
        SimpleTextFragment introFragment = (SimpleTextFragment)
                getSupportFragmentManager().findFragmentById(R.id.intro_fragment);
        introFragment.setText(R.string.intro_message);
        introFragment.getTextView().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f);

        // Initialize the logging framework.
        initializeLogging();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                //startActivityForResult(new Intent(
                //        MainActivity.this, SetupActivity.class), 0);
                return true;
            case R.id.action_log:
                startActivityForResult(new Intent(
                        MainActivity.this, LogFragment.class), 0);
                return true;
            case R.id.action_more_log:
                startActivityForResult(new Intent(
                        MainActivity.this, LogFragment.class), 1);
                return true;
            case R.id.action_about:
                //this.openAboutDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    /** Create a chain of targets that will receive log data */
    public void initializeLogging() {

        // Using Log, front-end to the logging chain, emulates
        // android.util.log method signatures.

        // Wraps Android's native log framework
        LogWrapper logWrapper = new LogWrapper();
        Log.setLogNode(logWrapper);

        // A filter that strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.
        mLogFragment =
                (LogFragment) getSupportFragmentManager().findFragmentById(R.id.log_fragment);
        msgFilter.setNext(mLogFragment.getLogView());
    }
}
