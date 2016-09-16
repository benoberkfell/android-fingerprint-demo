package com.example.fingerprintdemo.injects;

import android.app.KeyguardManager;
import android.content.Context;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.example.fingerprintdemo.FingerprintDemoApplication;

import dagger.Module;
import dagger.Provides;

@Module
class FingerprintModule {

    @Provides
    FingerprintManagerCompat providesFingerprintManager(FingerprintDemoApplication app) {
        return FingerprintManagerCompat.from(app);
    }

    @Provides
    KeyguardManager providesKeyguardManager(FingerprintDemoApplication app) {
        return (KeyguardManager) app.getSystemService(Context.KEYGUARD_SERVICE);
    }
}
