package com.tni.mobile.project1.Network;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.TrafficStats;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.tni.mobile.project1.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NetworkService extends Service {

    TrafficSnapshot latest = null;
    TrafficSnapshot previous = null;

    LocalBroadcastManager broadcaster;

    static final public String NET_RESULT = "Network Traffic", NET_MESSAGE_APP = "ProcessName", NET_MESSAGE_NAME = "appName";
    static final public String NET_MESSAGE_TX = "Transmit", NET_MESSAGE_RX = "Receive", NET_MESSAGE_ROUND = "round", NET_UID = "AppUID";
    static final public String NET_ALL_RESULT = "All Traffic", NET_ALL_TX = "All Transmit", NET_ALL_RX = "All Receive";
    int count, condition = 1, uid;
    Long maxTxUsed = 0L, maxRxUsed = 0L, maxTrafficUsed = 0L;
    String appName = "", appSelectedName = "", appPackage = "", notiHeader="";
    long currentRx, currentTx, previousRx, previousTx, lastestRx = 0L, lastestTx = 0L;
    long currentAllRx, currentAllTx;

    DecimalFormat mFormat = new DecimalFormat("##,###,##0");

    @Override
    public void onCreate() {
        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, startId, startId);
//        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        int count = intent.getIntExtra("count", 0);
        Log.d("First Count", ": " + count);
        this.count = count;
        appSelectedName = intent.getStringExtra("appName");
        appPackage = intent.getStringExtra("appPackage");
        uid = intent.getIntExtra("uid", 0);

        if(this.count == 1) {
            // ... do shutdown stuff
            stopSelf();
        }
        else{
            ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
            scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    getNetworkData();
                }
            }, 0, 2, TimeUnit.SECONDS);
        }

        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void getNetworkData() {
        sendResult(" ", " ", 0L, 0L, 0, condition);
        condition = 0;
        if(count == 2){
            Log.d("MonitorType", "Monitor one app");

            previousRx = lastestRx;
            previousTx = lastestTx;

            lastestRx = TrafficStats.getUidRxBytes(uid);
            lastestTx = TrafficStats.getUidTxBytes(uid);

            currentRx = lastestRx - previousRx;
            currentTx = lastestTx - previousTx;

            currentAllRx = currentRx;
            currentAllTx = currentTx;

            sendAllTraffic(currentAllRx,currentAllTx);

            notiHeader = appSelectedName;

            sendResult(appPackage, appSelectedName, currentTx, currentRx, uid, condition);
        } else {
            Log.d("MonitorType", "Monitor all app");
            previous = latest;
            latest = new TrafficSnapshot(this);

            if(previous != null){
                currentAllRx = latest.device.rx-previous.device.rx;
                currentAllTx = latest.device.tx-previous.device.tx;
            }

            sendAllTraffic(currentAllRx,currentAllTx);

            ArrayList<String> log = new ArrayList<String>();
            HashSet<Integer> intersection = new HashSet<Integer>(latest.apps.keySet());

            if (previous != null) {
                intersection.retainAll(previous.apps.keySet());
            }

            for (Integer uid : intersection) {
                TrafficRecord latest_rec = latest.apps.get(uid);
                TrafficRecord previous_rec =
                        (previous == null ? null : previous.apps.get(uid));

                Long currentRx = 0L;
                Long currentTx= 0L;

                if (latest_rec.rx >- 1 || latest_rec.tx >- 1){
                    if (previous_rec != null) {
                        currentRx = latest_rec.rx - previous_rec.rx;
                        currentTx = latest_rec.tx - previous_rec.tx;
                    }
                }

                if(maxTrafficUsed < (currentTx + currentRx)){
                    maxRxUsed = currentRx;
                    maxTxUsed = currentTx;
                    appName = latest_rec.appName;
                }

                sendResult(latest_rec.tag, latest_rec.appName, currentTx, currentRx, latest_rec.appUid, condition);
                emitLog(latest_rec.tag, latest_rec, previous_rec, log);
            }

            Collections.sort(log);

            for (String row : log) {
                Log.d("TrafficMonitor", row);
            }
            notiHeader = "Network Traffic Used";
        }
        showNotification();
//        com.tni.mobile.project1.NotificationManager noti =
//                new com.tni.mobile.project1.NotificationManager(notiHeader ,currentAllRx, currentAllTx, this);
//        noti.showNotification();
        condition = 1;
    }

    private void sendAllTraffic(long currentAllRx, long currentAllTx) {
        Intent intent = new Intent(NET_ALL_RESULT);
        intent.putExtra(NET_ALL_RX, currentAllRx);
        intent.putExtra(NET_ALL_TX, currentAllTx);
        broadcaster.sendBroadcast(intent);
    }

    public void sendResult(String process, String appName, Long tx, Long rx , int uid, int condition) {
        Log.d("Update UI", "Sending Result..");
        Intent intent = new Intent(NET_RESULT);
        if(process != null) {
            intent.putExtra(NET_MESSAGE_APP, process);
            intent.putExtra(NET_MESSAGE_NAME, appName);
            intent.putExtra(NET_MESSAGE_TX, tx);
            intent.putExtra(NET_MESSAGE_RX, rx);
            intent.putExtra(NET_UID, uid);
            intent.putExtra(NET_MESSAGE_ROUND, condition);
        }
        broadcaster.sendBroadcast(intent);
    }

    private void emitLog(CharSequence name, TrafficRecord latest_rec,
                         TrafficRecord previous_rec,
                         ArrayList<String> rows) {

        if (latest_rec.rx >- 1 || latest_rec.tx >- 1) {
            StringBuilder buf = new StringBuilder(name);

            buf.append(" : ");
            buf.append("Received Rate = ");

            if (previous_rec != null) {
                buf.append(String.valueOf(latest_rec.rx - previous_rec.rx));
            }

            buf.append(", ");
            buf.append(" Sent Rate = ");

            if (previous_rec != null) {
                buf.append(String.valueOf(latest_rec.tx - previous_rec.tx));
            }

            rows.add(buf.toString());
        }
    }

    @Override
    public void onDestroy() {
//        Toast.makeText(this, "Network Service Done", Toast.LENGTH_SHORT).show();
//        Log.d("Network Count", ": " + this.count);
        if(this.count == 0){
            Intent intent = new Intent("com.tni.mobile.project1.network");
//            Toast.makeText(this, "Network service done try to restart", Toast.LENGTH_SHORT).show();
            sendBroadcast(intent);
        }
//        else {
//            Toast.makeText(this, "Network service done and not restart", Toast.LENGTH_SHORT).show();
//        }
    }

    private void showNotification() {
        Intent targetIntent = new Intent(this, NetworkService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, targetIntent, 0);
        NotificationCompat.Builder n  = new NotificationCompat.Builder(this)
                .setContentTitle(notiHeader)
                .setContentText("TX = " + mFormat.format(currentAllTx) + " bytes , Rx = " + mFormat.format(currentAllRx) + " bytes")
                .setSmallIcon(R.drawable.ic_network_traffic)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_network_traffic, null))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);

        notificationManager.notify(2, n.build());
    }
}
