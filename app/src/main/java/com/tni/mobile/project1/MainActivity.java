package com.tni.mobile.project1;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tni.mobile.project1.CPU.CPUActivity;
import com.tni.mobile.project1.Memory.MemoryActivity;
import com.tni.mobile.project1.Network.NetworkActivity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private boolean firstRead = true;
    private int memTotal, pId;
    private long workT, totalT, total, totalBefore, work, workBefore;
    private float cpuUsage;

    TextView memory, cpu_usage;
    String mem;
    private DecimalFormat mFormat = new DecimalFormat("##,###,##0"), mFormatPercent = new DecimalFormat("##0.0");

    private Handler mHandler = new Handler();

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override

        public void onReceive(Context c, Intent i) {
            int level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            float temp = ((float) i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0) / 10);
            ProgressBar pb = (ProgressBar) findViewById(R.id.progressbar);

            pb.setProgress(level);
            TextView batteryLevel = (TextView )findViewById(R.id.batteryLevel);
            TextView batteryTemp = (TextView) findViewById(R.id.batteryTemp);
            batteryLevel.setText("Battery Level: " + Integer.toString(level) + "%");
            batteryTemp.setText("Battery Temperature : " + temp + " C");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pId = Process.myPid();
        initialize();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBatInfoReceiver);
        super.onDestroy();
    }

    private final Runnable mRunnable = new Runnable() {
        public void run() {
            float use = readUsage();
            String usage = mFormatPercent.format(use) + " %";
            cpu_usage.setText(usage);

            mHandler.postDelayed(mRunnable, 1000);
        }
    };

    private void initialize() {
        memory = (TextView) findViewById(R.id.memory);
        cpu_usage = (TextView) findViewById(R.id.cpu);
        mHandler.postDelayed(mRunnable, 1000);
        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    }

    public void lightSensor(View view) {
        Intent intent = new Intent(this, SensorActivity.class);
        startActivity(intent);
    }

    public void checkMemory(View view) {
        Intent intent = new Intent(this, MemoryActivity.class);
        startActivity(intent);
    }

    private float readUsage() {
        try {
            BufferedReader memReader = new BufferedReader(new FileReader("/proc/meminfo"));
            String s = memReader.readLine();
            while (s != null) {
                if (firstRead && s.startsWith("MemTotal:")) {
                    memTotal = Integer.parseInt(s.split("[ ]+", 3)[1]);
                    firstRead = false;
                }
                s = memReader.readLine();
            }
            memReader.close();

            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(mi);
            mem = ": " + mFormat.format(memTotal - mi.availMem/1024) + " kB";
            memory.setText(mem);

            BufferedReader reader = new BufferedReader(new FileReader("/proc/stat"));
            String sa[] = reader.readLine().split("[ ]+", 9);
            work = Long.parseLong(sa[1]) + Long.parseLong(sa[2]) + Long.parseLong(sa[3]);
            total = work + Long.parseLong(sa[4]) + Long.parseLong(sa[5]) + Long.parseLong(sa[6]) + Long.parseLong(sa[7]);
            reader.close();

            if (totalBefore != 0) {
                totalT = total - totalBefore;
                workT = work - workBefore;

                cpuUsage = restrictPercentage(workT * 100 / (float) totalT);
            }

            totalBefore = total;
            workBefore = work;

            reader.close();

            return cpuUsage;

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public void gravitySensor(View view) {
        Intent intent = new Intent(this, SensorTestActivity.class);
        startActivity(intent);
    }

    public void networkMonitor(View view) {
        Intent intent = new Intent(this, NetworkActivity.class);
        startActivity(intent);
    }

    private float restrictPercentage(float percentage) {
        if (percentage > 100)
            return 100;
        else if (percentage < 0)
            return 0;
        else return percentage;
    }

    public void CPUMonitor(View view) {
        Intent intent = new Intent(this, CPUActivity.class);
        startActivity(intent);
    }
}
