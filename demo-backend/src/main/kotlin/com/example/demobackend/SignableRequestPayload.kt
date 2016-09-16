package com.example.demobackend

interface SignableRequestPayload {
    fun messageForVerification() : String

    var timestamp: Long
}