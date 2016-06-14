package com.solodilov.evgen.pingservers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {
    final private String LOG_ = MyService.class.getCanonicalName();
    private List<String> mLog = new ArrayList<>();
    private MyRun mMyRun;
    private String mCommand;
    NotificationManager nm;

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Log.d(LOG_, "Service create");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mCommand = intent.getStringExtra(MainActivity.STRING_COMMAND);
        }
        mMyRun = new MyRun(mCommand);
        mMyRun.start();
        Log.d(LOG_, "Service start");
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_, "Service destroy");
        mMyRun.stopThread();
    }

    class MyRun extends Thread {
        String command;
        java.lang.Process p;
        int countErr = 0;
        boolean b = false;

        public MyRun(String command) {
            this.command = command;
        }

        @Override
        public void run() {

            try {
                p = Runtime.getRuntime().exec(command);
                String res = "";
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader bufferedReaderERR = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                while (true) {
                    readStreamWork(bufferedReader);
                    readStreamErr(bufferedReaderERR);
                    if (countErr > 5 && !b) {
                        activateAlarm();
                        b = true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void readStreamWork(BufferedReader bufferedReader) {
            try {
                if (bufferedReader.ready()) {
                    countErr = 0;
                    b = false;
                    readStream(bufferedReader);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void readStreamErr(BufferedReader bufferedReader) {
            try {
                if (bufferedReader.ready()) {
                    countErr++;
                    readStream(bufferedReader);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void readStream(BufferedReader bufferedReader) {
            String res = "";
            try {
                res = bufferedReader.readLine();
                mLog.add(res);
                if (mLog.size() > 5) {
                    mLog.remove(1);
                }
                if (res.length() > 0 && res.contains("avg")) {
                    stopSelf();
                }
                notifyApp("");
                Log.d(LOG_, " !!! " + res);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void stopThread() {
            p.destroy();
        }
    }

    private void activateAlarm() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pIntent)
                .setContentTitle("Alarm")
                .setContentText("произошел какойто сбой")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();
        Notification notif = builder.build();

        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        notif.defaults |= Notification.DEFAULT_SOUND;
        nm.notify(1, notif);
    }

    private void notifyApp(String key) {
        Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
        switch (key) {
            case MainActivity.START_KEY:
                intent.putExtra(MainActivity.SERVICE_MSG, MainActivity.START_KEY);
                break;
            case MainActivity.STOP_KEY:
                intent.putExtra(MainActivity.SERVICE_MSG, MainActivity.STOP_KEY);
                break;
            default:
                String s = "";
                for (String string : mLog) {
                    s += string + "\n";
                }
                Log.d(LOG_, s);
                intent.putExtra(MainActivity.SERVICE_MSG, s);
                sendBroadcast(intent);
        }
    }
}
