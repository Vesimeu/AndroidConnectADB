package com.example.projectconnectandroid;

import android.app.Application;

import com.dadino.barcodescanner.adb.BuildConfig;

import timber.log.Timber;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Инициализация Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
