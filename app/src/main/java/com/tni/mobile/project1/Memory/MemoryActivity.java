package com.tni.mobile.project1.Memory;

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

import com.tni.mobile.project1.Adapter.MemoryAdapter;
import com.tni.mobile.project1.Dao.MemoryDao;
import com.tni.mobile.project1.Network.NetworkService;
import com.tni.mobile.project1.R;
import com.tni.mobile.project1.RecyclerView.DividerItemDecoration;

import java.util.ArrayList;

public class MemoryActivity extends AppCompatActivity {

    private ArrayList<MemoryDao> listMem;
    private MemoryAdapter memoryAdapter;
    RecyclerView recyclerView;
    BroadcastReceiver updateUIReceiver;
    int index = 0;
    PackageManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory);
        initialize();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((updateUIReceiver),
                new IntentFilter(MemoryService.COPA_RESULT)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateUIReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initialize() {
        pm = getPackageManager();
        listMem = new ArrayList<MemoryDao>();
        recyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        if(!isMyServiceRunning(NetworkService.class)){
            Intent intent = new Intent(this, MemoryService.class);
            startService(intent);
        }
        memoryAdapter = new MemoryAdapter(listMem, getPackageManager());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(memoryAdapter);

        updateUIReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //UI update here
                String packageName = intent.getStringExtra(MemoryService.COPA_MESSAGE_APP);
                String appName = intent.getStringExtra(MemoryService.COPA_MESSAGE_NAME);
                int usage = intent.getIntExtra(MemoryService.COPA_MESSAGE_USAGE, 0);
                int condition = intent.getIntExtra(MemoryService.COPA_MESSAGE_ROUND, 0);
                if(condition == 1){
                    memoryAdapter.clear();
                    index = 0;
                }
                else {
                    MemoryDao dao = new MemoryDao(packageName,appName,usage);

//                    if(index < listMem.size())
//                        listMem.remove(index);

                    listMem.add(index, dao);
                    memoryAdapter.notifyItemChanged(index);
                    index++;
                }
            }
        };
    }

    public void stopMonitor(View view) {
        Intent intent = new Intent(this, MemoryService.class);
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
