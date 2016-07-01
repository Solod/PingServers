package com.solodilov.evgen.pingservers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

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

    private BroadcastReceiver receiver;
    private AlarmManager mAlarmManager;
    private Intent intentFromService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.activity_container, MyFragment.newInstance())
                .commit();
        setBroadcastReceiver();
        initIntentBackground();
    }

    @Override
    public void onStartService(String command, boolean taskServiceFragment) {
        String mCommand = "/system/bin/ping -c 4 " + command;
        intentFromService.putExtra(STRING_COMMAND, mCommand);

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
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_container);
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
                            //myFragment.mPingLog.setTextColor(Color.BLACK);
                            myFragment.appendTextAndScroll(s);
                            break;
                        case STATUS_ALARM:
                            //myFragment.mPingLog.setTextColor(Color.RED);
                            myFragment.appendTextAndScroll(s);
                            break;
                        case -1:
                            //myFragment.mPingLog.setTextColor(Color.YELLOW);
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

    private void initIntentBackground() {
        intentFromService = new Intent(this, MyService.class);
        intentFromService.setAction(SERVICE_ACTION);
    }

    private PendingIntent backgroundPendingIntent() {
        return PendingIntent.getService(this, 0, intentFromService, PendingIntent.FLAG_CANCEL_CURRENT);
    }

}
