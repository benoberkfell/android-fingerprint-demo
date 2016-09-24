package com.example.fingerprintdemo.fingerprintdialog;

import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat.CryptoObject;
import android.support.v4.os.CancellationSignal;

import com.example.fingerprintdemo.R;

import javax.inject.Inject;

public class FingerprintAuthPresenter {

    private boolean canceled = false;
    private CancellationSignal cancellationSignal;
    private FingerprintView fingerprintView;
    private final FingerprintManagerCompat fingerprintManager;

    @Inject
    FingerprintAuthPresenter(FingerprintManagerCompat fingerprintManager) {
        this.fingerprintManager = fingerprintManager;
    }

    void attachView(FingerprintView view) {
        this.fingerprintView = view;
    }


    void beginAuthentication() {
        cancellationSignal = new CancellationSignal();

        FingerprintManagerCompat.AuthenticationCallback callback = new FingerprintManagerCompat.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                super.onAuthenticationHelp(errMsgId, errString);
                if (!canceled) {
                    fingerprintView.onError(errString.toString(), true);
                }
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                super.onAuthenticationHelp(helpMsgId, helpString);
                if (!canceled) {
                    fingerprintView.onError(helpString.toString(), false);
                }
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                fingerprintView.onSuccess(result.getCryptoObject());
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                fingerprintView.onError(R.string.fingerprint_not_recognized);
            }
        };

        fingerprintManager.authenticate(fingerprintView.cryptoObject(), 0, cancellationSignal, callback, null);
    }

    void cancel() {
        cancellationSignal.cancel();
        this.canceled = true;
    }


    interface FingerprintView {
        CryptoObject cryptoObject();
        void onSuccess(CryptoObject cryptoObject);
        void onError(String errorString, boolean isHardError);
        void onError(int errorStringRes);
    }
}
