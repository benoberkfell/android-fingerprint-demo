package com.example.fingerprintdemo.injects;

import com.example.fingerprintdemo.FingerprintDemoApplication;
import com.example.fingerprintdemo.activities.MainActivity;
import com.example.fingerprintdemo.fingerprintdialog.FingerprintDialogFragment;
import com.example.fingerprintdemo.utils.SharedPrefHelper;

import javax.inject.Named;

import dagger.Component;

@Component(modules={ApplicationModule.class})
public interface ApplicationComponent {
    FingerprintDemoApplication application();
    SharedPrefHelper sharedPrefHelper();
    @Named("InstanceId") String instanceId();


    void inject(MainActivity mainActivity);
    void inject(FingerprintDialogFragment fingerprintDialogFragment);
}
