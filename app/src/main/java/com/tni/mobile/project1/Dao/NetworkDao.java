package com.tni.mobile.project1.Dao;

import java.io.Serializable;


public class NetworkDao implements Serializable {
    private String packageName, appName;
    private long tx, rx;
    private int uid;

    public NetworkDao() {
    }

    public NetworkDao(String packageName, String appName , long tx, long rx, int uid) {
        this.packageName = packageName;
        this.appName = appName;
        this.tx = tx;
        this.rx = rx;
        this.uid = uid;
    }

    public String getPName() {
        return packageName;
    }

    public String getAppName() {
        return appName;
    }

    public Long getTx() {
        return tx;
    }

    public Long getRx(){
        return rx;
    }

    public int getUid(){
        return uid;
    }

}
