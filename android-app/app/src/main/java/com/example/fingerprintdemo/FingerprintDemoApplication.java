package com.example.fingerprintdemo;

import android.app.Application;
import com.example.fingerprintdemo.injects.Injector;

public class FingerprintDemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Injector.init(this);
    }
}
