package com.tni.mobile.project1.Dao;

import java.io.Serializable;

public class CPUDao implements Serializable {
    private String packageName, appName;
    private int pid;
    private float cpuUsed;

    public CPUDao() {
    }

    public CPUDao(String packageName, String appName, float cpuUsed, int pid) {
        this.packageName = packageName;
        this.cpuUsed = cpuUsed;
        this.appName = appName;
        this.pid = pid;
    }

    public CPUDao(String packageName, String appName, float cpuUsed) {
        this.packageName = packageName;
        this.cpuUsed = cpuUsed;
        this.appName = appName;
    }

    public String getPName() {
        return packageName;
    }

    public Float getCPU() {
        return cpuUsed;
    }

    public String getAppName() {
        return appName;
    }

    public Integer getPID() {
        return pid;
    }
}
