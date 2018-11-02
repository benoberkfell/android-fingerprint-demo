package com.example.fingerprintdemo.injects;

import com.example.fingerprintdemo.FingerprintDemoApplication;

public class Injector {

    private static Injector instance;

    private ApplicationComponent applicationComponent;

    private Injector(FingerprintDemoApplication application) {
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(application))
                .build();
    }

    public static void init(FingerprintDemoApplication application) {
        instance = new Injector(application);
    }

    public static ApplicationComponent getAppComponent() {
        return instance.applicationComponent;
    }
}
