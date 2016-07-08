
package com.solodilov.evgen.pingservers;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MyFragment.OnStartMyService {

    public static final String STRING_COMMAND = "command string";
    public static final String SERVICE_MSG = "msg service";
    public static final String STATUS_SERVICE = "status";
    public final static int STATUS_NORM = 100;
    private final static int STATUS_ALARM = 200;
    public static final String BROADCAST_ACTION = "com.solodilov.evgen.pingservers";
    private static final String SERVICE_ACTION = "com.solodilov.evgen.pingservers";
    public static final int START_KEY = 1;
    public static final int STOP_KEY = 2;
    public static final String CHACKABLE_SERVICE = "check";
    private static final int TASK_CODE = 1;
    public static final String PENDING_INTENT = "pi";
    private static final String SYSTEM_BIN_PING = "/system/bin/ping";
    private static final String LOG_ = MainActivity.class.getCanonicalName();

    private BroadcastReceiver receiver;
    private AlarmManager mAlarmManager;
    private Intent intentFromService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        getFragmentManager().beginTransaction()
                .add(android.R.id.content, MyFragment.newInstance())
                .commit();
        setBroadcastReceiver();
        initIntentBackground();
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    public void onStartService(String command, boolean taskServiceFragment) {

        List<String> list = getAttributeCommandLine();
        list.add(command.trim());
        intentFromService.putStringArrayListExtra(STRING_COMMAND, (ArrayList<String>) list);

        if (taskServiceFragment) {
            PendingIntent pi = createPendingResult(TASK_CODE, new Intent(), 0);
            intentFromService.putExtra(PENDING_INTENT, pi);
            startService(intentFromService);
        } else {
            mAlarmManager.setRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 3000,
                    5000,
                    backgroundPendingIntent());
        }
    }


    @Override
    public void onStopService() {
        if (mAlarmManager != null) {
            mAlarmManager.cancel(backgroundPendingIntent());
        }
        stopService(intentFromService);
        //найти сервис и остановить
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == STOP_KEY) {
            getFragment().setTextButton("Start");
        }
        if (resultCode == START_KEY) {
            getFragment().setTextButton("Stop");
        }
    }

    private MyFragment getFragment() {
        MyFragment myFragment = null;
        Fragment fragment = getFragmentManager().findFragmentById(android.R.id.content);
        if (fragment instanceof MyFragment)
            myFragment = (MyFragment) fragment;
        return myFragment;
    }

    private void setBroadcastReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(SERVICE_MSG);
                int status = intent.getIntExtra(STATUS_SERVICE, -1);
                MyFragment myFragment = getFragment();
                if (myFragment != null) {
                    switch (status) {
                        case STATUS_NORM:
                            myFragment.appendTextAndScroll(s);   break;
                        case STATUS_ALARM:
                            myFragment.appendTextAndScroll(s);
                            break;
                        case -1:
                            myFragment.appendTextAndScroll(getString(R.string.error));
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    private List<String> getAttributeCommandLine() {
        List<String> list = new ArrayList<>();
        list.add("ping");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int s;
        if ((s = sharedPreferences.getInt(SettingsFragment.COUNT_PACKET, 0)) > 0) {
            list.add("-c " + s);
        }
        if ((s = sharedPreferences.getInt(SettingsFragment.INTERVAL_REQUES, 0)) > 0) {
            list.add("-i " + s);
        }
        if ((s = sharedPreferences.getInt(SettingsFragment.PACKET_SIZE, 0)) > 0) {
            list.add("-s " + s);
        }
        return list;
    }

    private void initIntentBackground() {
        intentFromService = new Intent(this, MyService.class);
        intentFromService.setAction(SERVICE_ACTION);
    }

    private PendingIntent backgroundPendingIntent() {
        return PendingIntent.getService(this, 0, intentFromService, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment())
                    .addToBackStack("")
                    .commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
