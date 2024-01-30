package com.example.blockapp;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BlackListResponse {
    @SerializedName("apps")
    private List<AppInfo> apps;

    public List<AppInfo> getApps() {
        return apps;
    }
}

class AppInfo {
    @SerializedName("nameApp")
    private String nameApp;

    @SerializedName("systemNameApp")
    private String systemNameApp;

    public String getNameApp() {
        return nameApp;
    }

    public String getSystemNameApp() {
        return systemNameApp;
    }
}
