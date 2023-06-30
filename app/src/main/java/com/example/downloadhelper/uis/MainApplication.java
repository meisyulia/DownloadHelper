package com.example.downloadhelper.uis;

import android.app.Application;

import com.raizlabs.android.dbflow.config.FlowManager;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(this);
    }
}
