package com.example.fingerprintdemo.crypto;

import android.annotation.TargetApi;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.inject.Inject;

import static android.os.Build.VERSION_CODES.M;
import static android.security.keystore.KeyProperties.DIGEST_SHA256;
import static android.security.keystore.KeyProperties.PURPOSE_SIGN;

public class CryptoHelper {

    public static final String KEY_NAME = "ANDROID_FINGERPRINT_DEMO";
    private static final String ANDROID_KEY_STORE_PROVIDER = "AndroidKeyStore";

    @Inject
    public CryptoHelper() {

    }

    private KeyStore getKeyStore() {
        try {
            return KeyStore.getInstance(ANDROID_KEY_STORE_PROVIDER);
        } catch (KeyStoreException e) {
            throw new RuntimeException("Failed to get the KeyStore instance");
        }
    }

    @TargetApi(M)
    public void createKeyPair() {
        KeyPairGenerator keyPairGenerator;

        try {
            keyPairGenerator =
                    KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get an instance of KeyPairGenerator", e);
        }

        /*
        By calling setUserAuthenticationRequired(true), we are indicating that any time
        the private key for this pair so to be used, we have to be authed via fingerprint.

        This is what enforces the invariant that the successful verification of the signature
        implies that an authorized individual has touched the fingerprint sensor.
         */

        try {
            keyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(KEY_NAME, PURPOSE_SIGN)
                            .setKeySize(2048)
                            .setDigests(DIGEST_SHA256)
                            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                            .setUserAuthenticationRequired(true)
                    .build());

            keyPairGenerator.generateKeyPair();

        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException("failed to generate key pair", e);
        }
    }

    private PrivateKey getPrivateKey() throws KeyPermanentlyInvalidatedException {
        try {
            KeyStore keyStore = getKeyStore();
            keyStore.load(null);
            return (PrivateKey) keyStore.getKey(KEY_NAME, null);
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException e) {
            throw new RuntimeException("failed to load private key from keystore", e);
        }
    }

    public PublicKey getVerificationPublicKey() {
        try {
            KeyStore keyStore = getKeyStore();
            keyStore.load(null);
            return keyStore.getCertificate(KEY_NAME).getPublicKey();
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
                IOException e) {
            throw new RuntimeException("Failed to get public key", e);
        }
    }

    public Signature getSignature() throws InvalidKeyException {
        Signature signature;
        try {
            signature = Signature.getInstance("SHA256withRSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to init Signature with SHA256withRSA algo", e);
        }

        signature.initSign(getPrivateKey());

        return signature;
    }

}
