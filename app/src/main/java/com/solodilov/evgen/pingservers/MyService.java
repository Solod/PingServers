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
import java.util.Arrays;
import java.util.List;

public class MyService extends Service {
    final private String LOG_ = MyService.class.getCanonicalName();
    private List<String> mCommand;
    private final Intent mIntentBR = new Intent(MainActivity.BROADCAST_ACTION);
    private NotificationManager nm;
    private MyRun mMyRun;

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
        PendingIntent pendingIntent = null;
        if (intent != null) {
            mCommand = intent.getStringArrayListExtra(MainActivity.STRING_COMMAND);
            Log.d(LOG_, Arrays.toString(mCommand.toArray()));
            pendingIntent = intent.getParcelableExtra(MainActivity.PENDING_INTENT);
        }
        mMyRun = new MyRun(mCommand, pendingIntent);
        mMyRun.start();

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mMyRun.flag = false;
        stopSelf();
        Log.d(LOG_, "Service destroy");
        super.onDestroy();
    }

    class MyRun extends Thread {
        private List<String> mCommand;
        private java.lang.Process p;
        boolean flag = true;
        private PendingIntent pi;

        public MyRun(List<String> command, PendingIntent pi) {
            mCommand = command;
            this.pi = pi;
        }

        @Override
        public void run() {
            try {
                if (pi != null) {
                    try {
                        pi.send(MainActivity.START_KEY);
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
                p = new ProcessBuilder().command(mCommand).start();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader bufferedReaderErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while (flag) {
                    readStreamWork(bufferedReader);
                    readStreamErr(bufferedReaderErr);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                p.destroy();
                if (pi != null) {
                    try {
                        pi.send(MainActivity.STOP_KEY);
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
                stopSelf();
            }
        }

        private void readStreamWork(BufferedReader bufferedReader) {
            try {
                String inputLine;
                while (bufferedReader.ready() && (inputLine = bufferedReader.readLine()) != null) {
                    if (inputLine.length() > 0) {
                        Log.d(LOG_, "ready! " + inputLine);
                        if (inputLine.contains("---") || inputLine.contains("ping")) {
                            // when we get to the last line of executed ping command
                            stringNameServer(inputLine);
                        }
                        if (inputLine.contains("bytes from")) {
                            stringPing(inputLine);
                        }
                        if (inputLine.contains("avg")) {
                            // when we get to the last line of executed ping command
                            flag = false;
                            stringReply(inputLine);
                        }
                        if (inputLine.contains("packets transmitted")) {
                            int lossPacket = analysisLossPacket(inputLine);
                            if (lossPacket > 30) {
                                activateAlarm();
                            }
                            stringPing(inputLine);
                        }
                        if (inputLine.contains("pipe")) {  // when we get to the last line of executed ping command
                            flag = false;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private void readStreamErr(BufferedReader bufferedReaderErr) {
            try {
                String inputLine;
                while (bufferedReaderErr.ready() && (inputLine = bufferedReaderErr.readLine()) != null) {
                    Log.d(LOG_, "Ready ERR " + inputLine);
                    if (inputLine.length() > 0) {
                        if (inputLine.contains("unknown")) {
                            activateAlarm();
                            flag = false;
                        }
                        // No ping server
                        if (inputLine.contains("unreachable")) {
                            activateAlarm();
                            flag = false;
                        }
                        if (inputLine.contains("destination")) {
                            flag = false;
                        }
                        sendBroadcastReciver(inputLine);
                        activateAlarm();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stringNameServer(String inputLine) {
        sendBroadcastReciver(inputLine);
        Log.d(LOG_, inputLine);
    }

    private void stringPing(String inputLine) {
        sendBroadcastReciver(inputLine);
        Log.d(LOG_, inputLine);
    }

    private void stringReply(String inputLine) {
        // Extracting the average round trip time from the inputLine string
        String afterEqual = inputLine.substring(inputLine.indexOf("="), inputLine.length()).trim();
        String afterFirstSlash = afterEqual.substring(afterEqual.indexOf('/') + 1, afterEqual.length()).trim();
        String strAvgRtt = afterFirstSlash.substring(0, afterFirstSlash.indexOf('/'));
        sendBroadcastReciver(strAvgRtt);
        Log.d(LOG_, strAvgRtt);
    }

    private int analysisLossPacket(String inputLine) {
        int anchor = inputLine.indexOf("%");
        String afterEqual = inputLine.substring(inputLine.lastIndexOf(",", anchor) + 1, anchor).trim();
        return Integer.valueOf(afterEqual);
    }

    private void sendBroadcastReciver(String inputLine) {
        mIntentBR.putExtra(MainActivity.SERVICE_MSG, inputLine);
        mIntentBR.putExtra(MainActivity.STATUS_SERVICE, MainActivity.STATUS_NORM);
        sendBroadcast(mIntentBR);
    }

    private void activateAlarm() {
        Intent intent = new Intent(MyService.this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(MyService.this, 0, intent, 0);

        Notification.Builder builder = new Notification.Builder(MyService.this);
        builder.setContentIntent(pIntent)
                .setContentTitle("Alarm")
                //TODO clarify any failure
                .setContentText("произошел какойто сбой")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();
        Notification notice = builder.build();

        notice.flags |= Notification.FLAG_AUTO_CANCEL;
        notice.defaults |= Notification.DEFAULT_SOUND;
        nm.notify(1, notice);
    }
}
