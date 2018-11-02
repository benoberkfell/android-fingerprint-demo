package com.example.fingerprintdemo.injects;

import android.app.KeyguardManager;

import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import dagger.Component;

@Component(  modules = {FingerprintModule.class},
        dependencies = {ApplicationComponent.class})
public interface FingerprintComponent {

    KeyguardManager provideKeyguardManager();
    FingerprintManagerCompat provideFingerprintManager();

}
