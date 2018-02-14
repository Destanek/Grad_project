package com.tni.mobile.project1.Network;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tni.mobile.project1.Adapter.NetworkAdapter;
import com.tni.mobile.project1.Dao.NetworkDao;
import com.tni.mobile.project1.R;
import com.tni.mobile.project1.RecyclerView.DividerItemDecoration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class NetworkActivity extends AppCompatActivity {

    private ArrayList<NetworkDao> listNetwork;
    PackageManager pm;
    private NetworkAdapter networkAdapter;
    RecyclerView recyclerView;
    BroadcastReceiver updateNetworkUIReceiver, updateAllTrafficReceiver;
    int index = 0;
    TextView RX,TX;
    String appName;

    DecimalFormat mFormat = new DecimalFormat("##,###,##0");

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((updateNetworkUIReceiver),
                new IntentFilter(NetworkService.NET_RESULT)
        );
        LocalBroadcastManager.getInstance(this).registerReceiver((updateAllTrafficReceiver),
                new IntentFilter(NetworkService.NET_ALL_RESULT)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateNetworkUIReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateAllTrafficReceiver);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);
        initialize();
    }

    private void initialize() {
        pm = getPackageManager();
        RX = (TextView)findViewById(R.id.RX);
        TX = (TextView)findViewById(R.id.TX);
        listNetwork = new ArrayList<NetworkDao>();
        recyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        networkAdapter = new NetworkAdapter(listNetwork, getPackageManager());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(networkAdapter);
        if(!isMyServiceRunning(NetworkService.class)){
//            addInstalledApp();
            Intent intent = new Intent(this, NetworkService.class);
            startService(intent);
        }
        updateNetworkUIReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //UI update here
                String packageName = intent.getStringExtra(NetworkService.NET_MESSAGE_APP);
                String appName = intent.getStringExtra(NetworkService.NET_MESSAGE_NAME);
                Long tx = intent.getLongExtra(NetworkService.NET_MESSAGE_TX, 0L);
                Long rx = intent.getLongExtra(NetworkService.NET_MESSAGE_RX, 0L);
                int uid = intent.getIntExtra(NetworkService.NET_UID, 0);
                int condition = intent.getIntExtra(NetworkService.NET_MESSAGE_ROUND, 0);
                if(condition == 1){
                    networkAdapter.clear();
                    index = 0;
                }
                else {
                    NetworkDao dao = new NetworkDao(packageName, appName, tx, rx, uid);

//                    if(index < listNetwork.size())
//                        listNetwork.remove(index);

                    listNetwork.add(index, dao);
                    networkAdapter.notifyItemChanged(index);
                    index++;
                }
            }
        };

        updateAllTrafficReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Long tx = intent.getLongExtra(NetworkService.NET_ALL_TX, 0L);
                Long rx = intent.getLongExtra(NetworkService.NET_ALL_RX, 0L);
                RX.setText(mFormat.format(rx));
                TX.setText(mFormat.format(tx));
            }
        };

//        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new ClickListener() {
//            @Override
//            public void onClick(View view, int position) {
//                monitorSpecificApp(position);
//            }
//
//            @Override
//            public void onLongClick(View view, int position) {
//                monitorSpecificApp(position);
//            }
//        }));
    }

    public void monitorSpecificApp(int position){
        NetworkDao net = listNetwork.get(position);
        networkAdapter.clear();
        Log.d("Selected", "Start Monitoring " + net.getAppName());
        Intent intent = new Intent(this, NetworkService.class);
        intent.putExtra("count", 2);
        intent.putExtra("uid", net.getUid());
        intent.putExtra("appName", net.getAppName());
        intent.putExtra("appPackage", net.getPName());
        startService(intent);
    }

    public void addInstalledApp() {
        //Get all installed application
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (int i = 0; i < packages.size(); i++) {
            ApplicationInfo app;
            try {
                app = pm.getApplicationInfo(packages.get(i).processName, 0);
                if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1) {
                    //it's a system app, not interested
                } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                    //Discard this one
                    //in this case, it should be a user-installed app
                } else {
                    appName = (String) pm.getApplicationLabel(pm.getApplicationInfo(packages.get(i).packageName, PackageManager.GET_META_DATA));
                    NetworkDao dao = new NetworkDao(app.packageName, appName, 0, 0, app.uid);
                    listNetwork.add(dao);
                    networkAdapter.notifyDataSetChanged();
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    public void stopMonitor(View view) {
        Intent intent = new Intent(this, NetworkService.class);
        intent.putExtra("count", 1);
        startService(intent);
        networkAdapter.clear();
        addInstalledApp();
    }

    public void startMonitor(View view) {
        networkAdapter.clear();
        Intent intent = new Intent(this, NetworkService.class);
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
