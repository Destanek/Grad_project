package com.tni.mobile.project1.Network;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.HashMap;
import java.util.List;

public class TrafficSnapshot {

    TrafficRecord device = null;
    HashMap<Integer, TrafficRecord> apps = new HashMap<Integer, TrafficRecord>();
    PackageManager pm;

    public TrafficSnapshot(Context ctxt) {
        int j = 0;
        device = new TrafficRecord();
//        HashMap<Integer, String> appNames = new HashMap<Integer, String>();
        pm = ctxt.getPackageManager();
        ActivityManager activityManager = (ActivityManager) ctxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (int i = 0; i < appProcesses.size(); i++) {
            ApplicationInfo app;
            try {
                app = pm.getApplicationInfo(appProcesses.get(i).processName, 0);
                if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1) {
                    //it's a system app, not interested
                } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                    //Discard this one
                    //in this case, it should be a user-installed app
                } else {
                    ActivityManager.RunningAppProcessInfo info = appProcesses.get(i);
                    String appName = (String) pm.getApplicationLabel(pm.getApplicationInfo(
                            info.pkgList != null && info.pkgList.length > 0 ? info.pkgList[0] : info.processName, 0));
                    apps.put(info.uid, new TrafficRecord(app.uid, app.packageName, appName));
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

//        for (ApplicationInfo app :
//                ctxt.getPackageManager().getInstalledApplications(0)) {
//            appNames.put(app.uid, app.packageName);
//        }

//        for (Integer uid : appNames.keySet()) {
//            apps.put(uid, new TrafficRecord(uid, appNames.get(uid)));
//
//        }
    }

    public TrafficSnapshot(String tag, String appName ,int uid){
        apps.put(uid, new TrafficRecord(uid, tag, appName));
    }

}
