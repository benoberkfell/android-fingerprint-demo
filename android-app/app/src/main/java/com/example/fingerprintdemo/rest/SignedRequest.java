package com.example.fingerprintdemo.rest;

import com.example.fingerprintdemo.crypto.Signer;

public class SignedRequest<T extends SignableRequestPayload> {

    private final T payload;
    private final String signature;

    public SignedRequest(T payload, Signer signer) {
        this.payload = payload;
        this.signature = signer.signRequest(payload.concatenatedValuesForSigning());
    }
}
