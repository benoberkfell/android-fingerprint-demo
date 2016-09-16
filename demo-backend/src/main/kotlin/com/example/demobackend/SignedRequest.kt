package com.example.demobackend

class SignedRequest<T : SignableRequestPayload> {
    lateinit var payload: T
    lateinit var signature: String
}
