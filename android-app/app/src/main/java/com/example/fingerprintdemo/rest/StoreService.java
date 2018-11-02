package com.example.fingerprintdemo.rest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface StoreService {

    @POST("/enrollments")
    Call<EnrollmentResponse> enroll(@Body SignedRequest<Enrollment> signedRequest);

    @POST("/purchases")
    Call<PurchaseResponse> makePurchase(@Body SignedRequest<Purchase> signedRequest);

}
