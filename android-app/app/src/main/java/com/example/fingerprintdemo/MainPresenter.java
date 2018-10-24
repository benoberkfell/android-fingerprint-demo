package com.example.fingerprintdemo;

import android.os.Build;
import android.util.Base64;

import com.example.fingerprintdemo.crypto.CryptoHelper;
import com.example.fingerprintdemo.crypto.Signer;
import com.example.fingerprintdemo.rest.Enrollment;
import com.example.fingerprintdemo.rest.EnrollmentResponse;
import com.example.fingerprintdemo.rest.Purchase;
import com.example.fingerprintdemo.rest.PurchaseResponse;
import com.example.fingerprintdemo.rest.SignedRequest;
import com.example.fingerprintdemo.rest.StoreService;
import com.example.fingerprintdemo.utils.SharedPrefHelper;

import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.Signature;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.annotation.RequiresApi;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Build.VERSION_CODES.M;

public class MainPresenter  {

    private final FingerprintManagerCompat fingerprintManager;
    private final CryptoHelper cryptoHelper;
    private final StoreService storeService;
    private final SharedPrefHelper sharedPrefHelper;
    private final String deviceId;

    private View view;

    @Inject
    MainPresenter(FingerprintManagerCompat fingerprintManager,
                  CryptoHelper cryptoHelper,
                  StoreService storeService,
                  SharedPrefHelper sharedPrefHelper,
                  @Named("InstanceId") String deviceId) {

        this.fingerprintManager = fingerprintManager;
        this.cryptoHelper = cryptoHelper;
        this.storeService = storeService;
        this.sharedPrefHelper = sharedPrefHelper;
        this.deviceId = deviceId;
    }

    public void attachView(View view) {
        this.view = view;
    }

    public void onReady() {
        if (Build.VERSION.SDK_INT < M) {
            view.presentMessageDialog("Sorry", "You're not on Android M or better. This demo's not going to be that fun.");
        } else {

            if (!fingerprintManager.isHardwareDetected()) {
                view.presentMessageDialog("Sorry", "No fingerprint hardware detected. This demo's not too fun that way.");
                return;
            }

            if (!fingerprintManager.hasEnrolledFingerprints()) {
                view.presentMessageDialog("Enroll Fingerprints",
                        "You don't have any fingerprints enrolled.\n\n" +
                                "Please go do that first."
                );

                return;
            }

            if (!sharedPrefHelper.hasRegisteredFingerprintsWithBackend()) {
                registerFingerprintWithBackend();
            }
        }
    }

    @RequiresApi(M)
    public void registerFingerprintWithBackend() {
        Signature signature;

        try {
            cryptoHelper.createKeyPair();
            signature = cryptoHelper.getSignature();
        } catch (InvalidKeyException e) {
            view.presentMessageDialog("Error", "The key was invalid");
            return;
        }

        PublicKey key = cryptoHelper.getVerificationPublicKey();

        final Enrollment enrollment =
                new Enrollment("android",
                        "androidrocks",
                        deviceId,
                        Base64.encodeToString(key.getEncoded(), Base64.NO_WRAP));

        AuthenticationCallback callback = new AuthenticationCallback() {
            @Override
            public void onAuthenticated(Signer signer) {
                SignedRequest<Enrollment> request = new SignedRequest<>(enrollment, signer);
                storeService.enroll(request).enqueue(new Callback<EnrollmentResponse>() {
                    @Override
                    public void onResponse(Call<EnrollmentResponse> call, Response<EnrollmentResponse> response) {
                        if (response.isSuccessful()) {
                            sharedPrefHelper.setHasRegisteredFingerprintsWithBackend(true);
                            sharedPrefHelper.setToken(response.body().getToken());
                            view.presentMessageDialog("Success", "You've successfully enabled your fingerprint for purchases.");
                        } else {
                            view.presentMessageDialog("Error", "An error occurred registering the fingerprint.");
                        }
                    }

                    @Override
                    public void onFailure(Call<EnrollmentResponse> call, Throwable t) {
                        view.presentMessageDialog("Error", "An error occurred registering the fingerprint.");
                    }
                });
            }
        };

        view.presentFingerprintAuth("Enroll Fingerprint", signature, callback);
    }

    public void placeOrder() {
        if (canUseFingerprints()) {
            Signature signature;
            try {
                signature = cryptoHelper.getSignature();
            } catch (InvalidKeyException e) {
                view.presentMessageDialog("Security Error",
                        "Either the lockscreen was disabled or new fingerprints were added. " +
                                "You must re-enroll your fingerprint.");
                return;
            }

            final Purchase purchase = new Purchase(
                    "Twice Baked Potato",
                    "742 Evergreen Terrace",
                    sharedPrefHelper.getToken());

            AuthenticationCallback callback = new AuthenticationCallback() {
                @Override
                public void onAuthenticated(Signer signer) {
                    SignedRequest<Purchase> request = new SignedRequest<>(purchase, signer);
                    storeService.makePurchase(request).enqueue(new Callback<PurchaseResponse>() {
                        @Override
                        public void onResponse(Call<PurchaseResponse> call, Response<PurchaseResponse> response) {
                            if (response.isSuccessful()) {
                                view.presentMessageDialog("Success", response.body().getConfirmationMessage());
                            } else {
                                view.presentMessageDialog("Error", "An error occurred making the purchase.");
                            }
                        }

                        @Override
                        public void onFailure(Call<PurchaseResponse> call, Throwable t) {
                            view.presentMessageDialog("Error", "An error occurred making the purchase.");
                        }
                    });
                }
            };

            view.presentFingerprintAuth("Make a Purchase", signature, callback);
        } else {
            view.presentMessageDialog("Sorry", "We'd use this with fingerprint auth here if you had the ability to do so.");
        }
    }


    private boolean canUseFingerprints() {
        return (Build.VERSION.SDK_INT > M
                && fingerprintManager.hasEnrolledFingerprints()
                && fingerprintManager.isHardwareDetected());
    }

    public interface View {
        void presentFingerprintAuth(String dialogTitle, Signature signature, AuthenticationCallback callback);
        void presentMessageDialog(String title, String message);
    }

    public interface AuthenticationCallback {
        void onAuthenticated(Signer signer);
    }
}
