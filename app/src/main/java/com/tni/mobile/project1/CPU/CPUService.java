package com.tni.mobile.project1.CPU;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.tni.mobile.project1.Memory.MemoryService;
import com.tni.mobile.project1.R;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CPUService extends Service {

    List<ActivityManager.RunningAppProcessInfo> runApp;
    static final public String CPU_RESULT = "CPU Usage", CPU_MESSAGE_APP = "ProcessName", CPU_MESSAGE_NAME = "AppName";
    static final public String CPU_MESSAGE_ROUND = "Round", CPU_MESSAGE_USAGE = "CPUUsage";
    static final public String CPU_ALL_RESULT = "All Traffic", CPU_ALL_USAGE = "Total CPU Usage";
    int count, condition = 1;
    LocalBroadcastManager broadcaster;
    DecimalFormat mFormat = new DecimalFormat("##0.0");

    private Handler mHandler = new Handler();

    private long workT, totalT, total, totalBefore, work, workBefore;
    private float totalCPUUsage, currentCPUUsage, tempWork;
    Float[] workABefore = new Float[20];

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
            mHandler.postDelayed(mRunnable, 2000);
//            ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
//            scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
//                public void run() {
//                    CPUUsage();
//                }
//            }, 0, 5, TimeUnit.SECONDS);
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final Runnable mRunnable = new Runnable() {
        public void run() {
            CPUUsage();
            mHandler.postDelayed(mRunnable, 2000);
        }
    };

    private void CPUUsage() {
        sendResult(" ", " ", 0, condition);
        condition = 0;
        ArrayList<Integer> ArrayID = new ArrayList<Integer>();
        ArrayList<String> ArrayName = new ArrayList<String>();
        ArrayList<String> AppName = new ArrayList<String>();

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        runApp = activityManager.getRunningAppProcesses();
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
        try {

            BufferedReader reader = new BufferedReader(new FileReader("/proc/stat"));
            String sa[] = reader.readLine().split("[ ]+", 9);
            work = Long.parseLong(sa[1]) + Long.parseLong(sa[2]) + Long.parseLong(sa[3]);
            total = work + Long.parseLong(sa[4]) + Long.parseLong(sa[5]) + Long.parseLong(sa[6]) + Long.parseLong(sa[7]);
            reader.close();

            Float[] workNow = new Float[ArrayID.size()];
            int n = 0;
            for (int p : ArrayID)
            {
                try {
                    if (p != 0) {

                        reader = new BufferedReader(new FileReader("/proc/" + p + "/stat"));
                        sa = reader.readLine().split("[ ]+", 18);
                        tempWork = (float) Long.parseLong(sa[13]) + Long.parseLong(sa[14]) +
                                Long.parseLong(sa[15]) + Long.parseLong(sa[16]);

                        workNow[n] = tempWork;
                        ++n;
                        reader.close();
                    }
                } catch (FileNotFoundException e) {
                    Log.d("Error", "CPUUsage: " + e);
                }
            }

            int workPT;

            if (totalBefore != 0)
            {
                totalT = total - totalBefore;
                workT = work - workBefore;
                totalCPUUsage = restrictPercentage(workT * 100 / (float) totalT);
                for (int i = 0; i < workNow.length; i++) {
                    if(workABefore[i] == null){
                        workABefore[i] = workNow[i];
                    }

                    workPT = (int) (workNow[i] - workABefore[i]);

                    currentCPUUsage = restrictPercentage(workPT * 100 / (float) totalT);
                    workABefore[i] = workNow[i];
                }
            }

            totalBefore = total;
            workBefore = work;

            reader.close();

            sendAllUsage(totalCPUUsage);
//            com.tni.mobile.project1.NotificationManager noti =
//                    new com.tni.mobile.project1.NotificationManager(totalCPUUsage, this);
//            noti.showNotification();
            showNotification();
            condition = 1;

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void sendAllUsage(float totalCPUUsage) {
        Intent intent = new Intent(CPU_ALL_RESULT);
        intent.putExtra(CPU_ALL_USAGE, totalCPUUsage);
        broadcaster.sendBroadcast(intent);
    }

    public void sendResult(String process, String appName, float CPUUsage, int condition) {
        Log.d("Update UI", "Sending Result..");
        Intent intent = new Intent(CPU_RESULT);
        if(process != null) {
            intent.putExtra(CPU_MESSAGE_APP, process);
            intent.putExtra(CPU_MESSAGE_NAME, appName);
            intent.putExtra(CPU_MESSAGE_USAGE, CPUUsage);
            intent.putExtra(CPU_MESSAGE_ROUND, condition);
        }
        broadcaster.sendBroadcast(intent);
    }

    private float restrictPercentage(float percentage) {
        if (percentage > 100)
            return 100;
        else if (percentage < 0)
            return 0;
        else return percentage;
    }

    private void showNotification() {
        Intent targetIntent = new Intent(this, MemoryService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, targetIntent, 0);
        NotificationCompat.Builder n  = new NotificationCompat.Builder(this)
                .setContentTitle("CPU Usage")
                .setContentText("Total : " + mFormat.format(totalCPUUsage) + " %")
                .setSmallIcon(R.drawable.ic_cpu)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_cpu, null))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);

        notificationManager.notify(3, n.build());
    }

    @Override
    public void onDestroy() {
        if(this.count == 0){
            Intent intent = new Intent("com.tni.mobile.project1.cpu");
            sendBroadcast(intent);
        }
//        else {
//            Toast.makeText(this, "Network service done and not restart", Toast.LENGTH_SHORT).show();
//        }
    }

}
