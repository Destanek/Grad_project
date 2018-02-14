package com.tni.mobile.project1.Network;

import android.net.TrafficStats;

public class TrafficRecord {
    long tx = 0;
    long rx = 0;
    String tag = null;
    String appName = null;
    int appUid = 0;

    TrafficRecord() {

        tx = TrafficStats.getTotalTxBytes();
        rx = TrafficStats.getTotalRxBytes();

    }

    TrafficRecord(int uid, String tag, String appName) {

        tx = TrafficStats.getUidTxBytes(uid);
        rx = TrafficStats.getUidRxBytes(uid);

        this.appName = appName;
        this.tag = tag;
        this.appUid = uid;
    }
}
