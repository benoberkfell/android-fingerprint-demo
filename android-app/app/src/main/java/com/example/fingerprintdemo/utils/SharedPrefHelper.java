package com.example.fingerprintdemo.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.fingerprintdemo.FingerprintDemoApplication;

import javax.inject.Inject;

public class SharedPrefHelper {

    private static final String HAS_ENROLLED_KEY = "HAS_ENROLLED";
    private static final String TOKEN_KEY = "token";

    private final SharedPreferences prefs;

    @Inject
    public SharedPrefHelper(FingerprintDemoApplication application) {
        prefs = application.getSharedPreferences("enrollmentPrefs", Context.MODE_PRIVATE);
    }

    public boolean hasRegisteredFingerprintsWithBackend() {
        return prefs.getBoolean(HAS_ENROLLED_KEY, false);
    }

    public void setHasRegisteredFingerprintsWithBackend(boolean value) {
        prefs.edit().putBoolean(HAS_ENROLLED_KEY, value).apply();
    }

    public String getToken() {
        return prefs.getString(TOKEN_KEY, null);
    }

    public void setToken(String value) {
        prefs.edit().putString(TOKEN_KEY, value).apply();
    }
}
