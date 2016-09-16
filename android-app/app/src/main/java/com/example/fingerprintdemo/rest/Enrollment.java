package com.example.fingerprintdemo.rest;

public class Enrollment implements SignableRequestPayload {

    private final String username;
    private final String password;
    private final String deviceId;
    private final String publicKey;
    private final long timestamp;

    public Enrollment(String username, String password, String deviceId, String publicKey) {
        this.username = username;
        this.password = password;
        this.deviceId = deviceId;
        this.publicKey = publicKey;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String concatenatedValuesForSigning() {
        return username
                + "|"
                + password
                + "|"
                + deviceId
                + "|"
                + publicKey
                + "|"
                + timestamp;
    }
}
