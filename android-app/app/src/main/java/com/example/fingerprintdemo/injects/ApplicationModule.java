package com.example.fingerprintdemo.injects;

import com.example.fingerprintdemo.FingerprintDemoApplication;
import com.example.fingerprintdemo.utils.SharedPrefHelper;
import com.google.android.gms.iid.InstanceID;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module(includes = {
        FingerprintModule.class,
        StoreModule.class
        })
class ApplicationModule {

    private FingerprintDemoApplication application;

    public ApplicationModule(FingerprintDemoApplication application) {
        this.application = application;
    }

    @Provides
    FingerprintDemoApplication application() {
        return application;
    }

    @Provides
    SharedPrefHelper prefHelper(FingerprintDemoApplication application) {
        return new SharedPrefHelper(application);
    }

    @Provides @Named("InstanceId")
    String instanceId() {
        return InstanceID.getInstance(application).getId();
    }
}
