package com.example.fingerprintdemo.fingerprintdialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fingerprintdemo.R;
import com.example.fingerprintdemo.crypto.Signer;

import java.security.Signature;

import javax.inject.Inject;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.fragment.app.DialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.os.Build.VERSION_CODES.M;
import static com.example.fingerprintdemo.injects.Injector.getAppComponent;

public class FingerprintDialogFragment extends DialogFragment implements FingerprintAuthPresenter.FingerprintView {

    public static final String DIALOG_TITLE_KEY = "DialogTitle";

    private static final long SUCCESS_TIMEOUT_MS = 1000;
    private static final long ERROR_TIMEOUT_MS = 1200;

    @Inject FingerprintManagerCompat    fingerprintManager;
    @Inject FingerprintAuthPresenter    helper;

    @BindView(R.id.fingerprint_icon)    ImageView   fingerprintIconImageView;
    @BindView(R.id.fingerprint_status)  TextView    fingerprintStatusTextView;

    private Signature signature;
    private Callback callback;
    private String title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getAppComponent().inject(this);

        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);

        Bundle args = getArguments();
        title = args.getString(DIALOG_TITLE_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(inflater, viewGroup, bundle);

        View view = inflater.inflate(R.layout.fragment_fingerprint_dialog, viewGroup);
        ButterKnife.bind(this, view);

        helper.attachView(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().setTitle(title);
        helper.beginAuthentication();
    }

    @Override
    public void onPause() {
        helper.cancel();
        super.onPause();
    }

    @OnClick(R.id.cancel_button)
    public void onCancelClicked() {
        helper.cancel();
        dismiss();
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        fingerprintIconImageView.removeCallbacks(resetFingerprintHint);
    }

    @Override
    public FingerprintManagerCompat.CryptoObject cryptoObject() {
        return new FingerprintManagerCompat.CryptoObject(signature);
    }

    @Override
    @RequiresApi(M)
    public void onSuccess(final FingerprintManagerCompat.CryptoObject cryptoObject) {
        fingerprintIconImageView.removeCallbacks(resetFingerprintHint);

        fingerprintStatusTextView.setText(getString(R.string.fingerprint_success));
        fingerprintStatusTextView.setTextColor(ContextCompat.getColor(getActivity(),R.color.success_color));
        fingerprintIconImageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_fingerprint_success));
        fingerprintStatusTextView.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();

                if (callback != null) {
                    callback.onAuthenticated(new Signer(cryptoObject));
                }
            }
        }, SUCCESS_TIMEOUT_MS);
    }

    @Override
    public void onError(String errorString, boolean isHardError) {
        fingerprintIconImageView.removeCallbacks(resetFingerprintHint);

        fingerprintStatusTextView.setTextColor(ContextCompat.getColor(getActivity(),R.color.warning_color));
        fingerprintStatusTextView.setText(errorString);
        fingerprintIconImageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_fingerprint_error));

        if (!isHardError) {
            fingerprintIconImageView.postDelayed(resetFingerprintHint, ERROR_TIMEOUT_MS);
        }
    }

    @Override
    public void onError(int errorStringRes) {
        onError(getString(errorStringRes), false);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    public interface Callback {
        void onAuthenticated(Signer signer);
    }

    Runnable resetFingerprintHint = new Runnable() {
        @Override
        public void run() {
            if (FingerprintDialogFragment.this.isVisible()) {
                fingerprintStatusTextView.setText(R.string.fingerprint_hint);
                fingerprintIconImageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_fp_40px));
                fingerprintStatusTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.hint_color));
            }
        }
    };
}
