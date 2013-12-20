package saydroid.tether.usb;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import org.saydroid.log.Log;
import org.saydroid.log.LogConfiguration;
import org.saydroid.ngn.NgnEngine;
import org.slf4j.LoggerFactory;

public class MainActivity extends ActionBarActivity {
    private final static String TAG = MainActivity.class.getCanonicalName();

    //final NgnEngine mEngine = NgnEngine.getInstance();
    private final static org.slf4j.Logger sLogger = LoggerFactory.getLogger(MainActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Start log to file from here
        LogConfiguration.getInstance().setLoggerName(MainActivity.class.getCanonicalName());
        LogConfiguration.getInstance().setFileName(this.getFileStreamPath("SayRevTethering.log").getAbsolutePath());
        LogConfiguration.getInstance().configure();

        Log.d(TAG, "Calling onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
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

}
