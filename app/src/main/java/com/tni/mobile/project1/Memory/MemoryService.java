package com.tni.mobile.project1.Memory;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Debug;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.tni.mobile.project1.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MemoryService extends Service {

    List<ActivityManager.RunningAppProcessInfo> runApp;
    Debug.MemoryInfo[] memInfo;
    String appPackage =" ";
    static final public String COPA_RESULT = "Memory Usage", COPA_MESSAGE_APP = "ProcessName", COPA_MESSAGE_NAME = "AppName";
    static final public String COPA_MESSAGE_ROUND = "Round", COPA_MESSAGE_USAGE = "MemoryUsage";
    int memUsed = 0, maxMemUsed = Integer.MIN_VALUE;
    int count, condition = 1;
    int numMessage;
    LocalBroadcastManager broadcaster;
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

        int count = intent.getIntExtra("count",0);
        Log.d("First Count", ": " + count);

        this.count = count;
        if(this.count == 1) {
            // ... do shutdown stuff
            stopSelf();
        }
        else{
            ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
            scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    checkMemory();
                }
            }, 0, 10, TimeUnit.SECONDS);
        }

        return START_REDELIVER_INTENT;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void checkMemory() {
        maxMemUsed = 0;
        sendResult(" ", " ", 0, condition);
        condition = 0;
//        sendRound(condition);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);

        runApp = activityManager.getRunningAppProcesses();
        ArrayList<Integer> ArrayID = new ArrayList<Integer>();
        ArrayList<String> ArrayName = new ArrayList<String>();
        ArrayList<String> AppName = new ArrayList<String>();
        PackageManager pm = this.getPackageManager();
        for(int i = 0; i < runApp.size(); i++){
            ApplicationInfo app;
            try {

                app = pm.getApplicationInfo(runApp.get(i).processName, 0);
                if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1) {
                    //it's a system app, not interested
                } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                    //Discard this one
                    //in this case, it should be a user-installed app
                } else {

                    ActivityManager.RunningAppProcessInfo info = runApp.get(i);
                    ArrayID.add(info.pid);
                    ArrayName.add(info.processName);
                    String appName = (String) pm.getApplicationLabel(pm.getApplicationInfo(
                            info.pkgList != null && info.pkgList.length > 0 ? info.pkgList[0] : info.processName, 0));
                    pm.getApplicationIcon(info.processName);
                    AppName.add(appName);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        int[] id = new int[ArrayID.size()];
        for(int i = 0; i < id.length; i++){
            id[i] = ArrayID.get(i);
        }

        memInfo = activityManager.getProcessMemoryInfo(id);
        int chunk = 0;
        for (Debug.MemoryInfo aMemInfo : memInfo) {

            memUsed = aMemInfo.getTotalPrivateDirty();

            if(maxMemUsed < memUsed){
                maxMemUsed = memUsed;
                appPackage = AppName.get(chunk);
            }
            Log.d("MemMonitor", ArrayName.get(chunk) + " :" + aMemInfo.getTotalPrivateDirty() + " kB\n");
            sendResult(ArrayName.get(chunk), AppName.get(chunk) , memUsed, condition);
            chunk++;
        }

        numMessage++;
        showNotification();
        condition = 1;
    }

    public void sendResult(String process, String appName, int usage, int condition) {
        Log.d("Update UI", "Sending Result..");
        Intent intent = new Intent(COPA_RESULT);
        if(process != null) {
            intent.putExtra(COPA_MESSAGE_APP, process);
            intent.putExtra(COPA_MESSAGE_NAME, appName);
            intent.putExtra(COPA_MESSAGE_USAGE, usage);
            intent.putExtra(COPA_MESSAGE_ROUND, condition);
        }
        broadcaster.sendBroadcast(intent);
    }

    private void showNotification() {
        Intent targetIntent = new Intent(this, MemoryService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, targetIntent, 0);
        NotificationCompat.Builder n  = new NotificationCompat.Builder(this)
                .setContentTitle("Max Memory Usage")
                .setContentText(appPackage + " : " + mFormat.format(maxMemUsed) + "kB")
                .setSmallIcon(R.drawable.ic_monitoring)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_monitoring, null))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);

        notificationManager.notify(1, n.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
//        Toast.makeText(this, "Memory Service Done", Toast.LENGTH_SHORT).show();
//        Log.d("Memory Count", ": " + this.count);
        if(this.count == 0){
            Intent intent = new Intent("com.tni.mobile.project1");
//            Toast.makeText(this, "Memory service done try to restart", Toast.LENGTH_SHORT).show();
            sendBroadcast(intent);
        }
//        else {
//            Toast.makeText(this, "Memory service done and not restart", Toast.LENGTH_SHORT).show();
//        }
    }

}
