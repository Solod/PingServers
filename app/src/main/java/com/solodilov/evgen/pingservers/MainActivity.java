package com.solodilov.evgen.pingservers;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity implements MyFragment.OnStartMyService {

    public static final String STRING_COMMAND = "command string";
    public static final String SERVICE_MSG = "msg service";
    public static final String BROADCAST_ACTION = "com.solodilov.evgen.pingservers";
    public static final String START_KEY = "start";
    public static final String STOP_KEY = "stop";

    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.activity_container, MyFragment.newInstance())
                .commit();
        setBroadcastReceiver();
    }

    private void setBroadcastReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(SERVICE_MSG);
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_container);
                if (fragment instanceof MyFragment) {
                    MyFragment myFragment = (MyFragment) fragment;
                    switch (s) {
                        case START_KEY:
                            myFragment.mBtn.setText("Stop");
                            break;
                        case STOP_KEY:
                            myFragment.mBtn.setText("Start");
                            break;
                        default:
                            myFragment.mPingLog.setText(s);
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();

    }

    @Override
    public void onStartService(String command) {
        String mCommand = "/system/bin/ping -i 10 "+command;
        startService(new Intent(this, MyService.class).putExtra(STRING_COMMAND, mCommand));
        }

    @Override
    public void onStopService() {
        stopService(new Intent(this, MyService.class));
    }

    @Override
    public boolean onIsService() {
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(100);
        for (int i = 0; i < rs.size(); i++) {
            ActivityManager.RunningServiceInfo
                    rsi = rs.get(i);
            if (MyService.class.getName().equalsIgnoreCase(rsi.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
