package com.example.qr_demo_application;

import android.app.Application;

import com.example.qr_demo_application.Utilities.ImageLoader;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ImageLoader.initImageLoader(this);
    }
}
