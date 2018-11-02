package com.example.fingerprintdemo.crypto;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.SignatureException;

import androidx.annotation.RequiresApi;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

import static android.os.Build.VERSION_CODES.M;

public class Signer {

    private final FingerprintManagerCompat.CryptoObject cryptoObject;

    @RequiresApi(M)
    public Signer(FingerprintManagerCompat.CryptoObject cryptoObject) {
        this.cryptoObject = cryptoObject;
    }

    public String signRequest(String data) {
        try {
            cryptoObject.getSignature().update(data.getBytes("UTF8"));
            byte[] bytes = cryptoObject.getSignature().sign();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (SignatureException | UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to sign request", e);
        }
    }
}
