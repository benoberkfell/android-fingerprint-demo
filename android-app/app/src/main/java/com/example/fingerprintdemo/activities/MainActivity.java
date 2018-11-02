package com.example.fingerprintdemo.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.fingerprintdemo.MainPresenter;
import com.example.fingerprintdemo.R;
import com.example.fingerprintdemo.crypto.Signer;
import com.example.fingerprintdemo.fingerprintdialog.FingerprintDialogFragment;
import com.example.fingerprintdemo.rest.StoreService;
import com.example.fingerprintdemo.utils.SharedPrefHelper;

import java.security.Signature;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.fingerprintdemo.injects.Injector.getAppComponent;

public class MainActivity extends AppCompatActivity implements MainPresenter.View {

    @Inject FingerprintManagerCompat    fingerprintManager;
    @Inject StoreService                storeService;
    @Inject SharedPrefHelper            sharedPrefHelper;
    @Inject MainPresenter               mainPresenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        mainPresenter.attachView(this);

        setSupportActionBar(toolbar);
        toolbar.setTitle("Tuber");
    }

    @Override
    public void onResume() {
        super.onResume();
        mainPresenter.onReady();
    }

    @OnClick(R.id.potato_card_view)
    protected void authenticateFingerprint() {
        mainPresenter.placeOrder();
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
