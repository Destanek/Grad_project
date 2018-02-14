package com.tni.mobile.project1.Dao;

import java.io.Serializable;

public class MemoryDao implements Serializable {
    private String packageName, appName;
    private int memUsed, pid;

    public MemoryDao() {
    }

    public MemoryDao(String packageName, String appName, int memUsed, int pid) {
        this.packageName = packageName;
        this.memUsed = memUsed;
        this.appName = appName;
        this.pid = pid;
    }

    public MemoryDao(String packageName, String appName, int memUsed) {
        this.packageName = packageName;
        this.memUsed = memUsed;
        this.appName = appName;
    }

    public String getPName() {
        return packageName;
    }

    public Integer getMemory() {
        return memUsed;
    }

    public String getAppName() {
        return appName;
    }

    public Integer getPID() {
        return pid;
    }
}

