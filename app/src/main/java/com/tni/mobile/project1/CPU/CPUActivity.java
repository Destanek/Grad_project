package com.tni.mobile.project1.CPU;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tni.mobile.project1.Adapter.CPUAdapter;
import com.tni.mobile.project1.Dao.CPUDao;
import com.tni.mobile.project1.Network.NetworkService;
import com.tni.mobile.project1.R;
import com.tni.mobile.project1.RecyclerView.DividerItemDecoration;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CPUActivity extends AppCompatActivity {

    private ArrayList<CPUDao> listCPU;
    private CPUAdapter cpuAdapter;
    RecyclerView recyclerView;
    BroadcastReceiver updateUIReceiver,updateAllCPUReceiver;
    int index = 0;
    PackageManager pm;

    DecimalFormat mFormatPercent = new DecimalFormat("##0.0");

    TextView totalCPU;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpu);
        initialize();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((updateUIReceiver),
                new IntentFilter(CPUService.CPU_RESULT)
        );
        LocalBroadcastManager.getInstance(this).registerReceiver((updateAllCPUReceiver),
                new IntentFilter(CPUService.CPU_ALL_RESULT)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateUIReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateAllCPUReceiver);
        super.onStop();
    }

    private void initialize() {
        totalCPU = (TextView) findViewById(R.id.totalCPU);
        pm = getPackageManager();
        listCPU = new ArrayList<CPUDao>();
        recyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        if(!isMyServiceRunning(NetworkService.class)){
            Intent intent = new Intent(this, CPUService.class);
            startService(intent);
        }
        cpuAdapter = new CPUAdapter(listCPU, getPackageManager());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(cpuAdapter);

        updateUIReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //UI update here
                String packageName = intent.getStringExtra(CPUService.CPU_MESSAGE_APP);
                String appName = intent.getStringExtra(CPUService.CPU_MESSAGE_NAME);
                float usage = intent.getFloatExtra(CPUService.CPU_MESSAGE_USAGE, 0);
                int condition = intent.getIntExtra(CPUService.CPU_MESSAGE_ROUND, 0);
                if(condition == 1){
                    cpuAdapter.clear();
                    index = 0;
                }
                else {
                    CPUDao dao = new CPUDao(packageName,appName,usage);

//                    if(index < listCPU.size())
//                        listCPU.remove(index);

                    listCPU.add(index, dao);
                    cpuAdapter.notifyItemChanged(index);
                    index++;
                }
            }
        };

        updateAllCPUReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                float totalcpu = intent.getFloatExtra(CPUService.CPU_ALL_USAGE, 0);
                String usage = mFormatPercent.format(totalcpu) + " %";
                totalCPU.setText(usage);
            }
        };
    }

    public void stopMonitor(View view) {
        Intent intent = new Intent(this, CPUService.class);
        intent.putExtra("count", 1);
        startService(intent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
