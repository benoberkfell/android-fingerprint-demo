package com.example.fingerprintdemo.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.fingerprintdemo.MainPresenter;
import com.example.fingerprintdemo.R;
import com.example.fingerprintdemo.crypto.Signer;
import com.example.fingerprintdemo.fingerprintdialog.FingerprintDialogFragment;
import com.example.fingerprintdemo.rest.StoreService;
import com.example.fingerprintdemo.utils.SharedPrefHelper;

import java.security.Signature;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.os.Build.VERSION_CODES.M;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.fingerprintdemo.injects.Injector.getAppComponent;

public class MainActivity extends AppCompatActivity implements MainPresenter.View {

    @Inject FingerprintManagerCompat    fingerprintManager;
    @Inject StoreService                storeService;
    @Inject SharedPrefHelper            sharedPrefHelper;
    @Inject MainPresenter               mainPresenter;

    @BindView(R.id.fingerprint_sensor_detected) TextView    fingerprintSensorDetectedTextView;
    @BindView(R.id.fingerprints_enrolled)       TextView    fingerprintsEnrolledTextView;
    @BindView(R.id.fingerprint_demo_contents)   View        fingerprintDemoContentsView;
    @BindView(R.id.authenticate_fingerprint)    Button      authenticateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        mainPresenter.attachView(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mainPresenter.onResume();
    }

    @OnClick(R.id.register_fingerprint)
    @RequiresApi(M)
    protected void registerFingerprint() {
        mainPresenter.registerFingerprintWithBackend();
    }

    @OnClick(R.id.authenticate_fingerprint)
    @RequiresApi(M)
    protected void authenticateFingerprint() {
        mainPresenter.placeOrderWithFingerprintAuth();
    }

    @Override
    public void hardwareDetectedText(String text) {
        fingerprintSensorDetectedTextView.setText(text);
    }

    @Override
    public void fingerprintEnrollmentStatusText(String text) {
        fingerprintsEnrolledTextView.setText(text);
    }

    @Override
    public void shouldShowFingerprintEnrollmentStatusText(boolean value) {
        fingerprintsEnrolledTextView.setVisibility(value ? VISIBLE : GONE);
    }

    @Override
    public void shouldShowFingerprintButtons(boolean value) {
        fingerprintDemoContentsView.setVisibility(value ? VISIBLE : GONE);
    }

    @Override
    public void shouldShowAuthButton(boolean value) {
        authenticateButton.setVisibility(value ? VISIBLE : GONE);
    }

    @Override
    public void presentFingerprintAuth(String dialogTitle,
                                       Signature signature,
                                       final MainPresenter.AuthenticationCallback callback) {
        FingerprintDialogFragment fragment = new FingerprintDialogFragment();
        Bundle args = new Bundle();
        args.putString(FingerprintDialogFragment.DIALOG_TITLE_KEY, dialogTitle);

        fragment.setArguments(args);
        fragment.setSignature(signature);
        fragment.setCallback(new FingerprintDialogFragment.Callback() {
            @Override
            public void onAuthenticated(Signer signer) {
                callback.onAuthenticated(signer);
            }
        });
        fragment.show(getSupportFragmentManager(), "fingerprint_dialog");
    }

    @Override
    public void presentMessageDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create().show();
    }
}
