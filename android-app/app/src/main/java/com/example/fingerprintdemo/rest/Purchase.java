package com.example.fingerprintdemo.rest;

public class Purchase implements SignableRequestPayload {

    private final String itemName;
    private final String deliveryAddress;
    private final String token;
    private final long timestamp;

    public Purchase(String itemName, String deliveryAddress, String token) {
        this.itemName = itemName;
        this.deliveryAddress = deliveryAddress;
        this.token = token;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String concatenatedValuesForSigning() {
        return itemName
                + "|"
                + deliveryAddress
                + "|"
                + token
                + "|"
                + timestamp;
    }
}
