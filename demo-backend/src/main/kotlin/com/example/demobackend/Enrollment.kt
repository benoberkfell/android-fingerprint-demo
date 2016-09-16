package com.example.demobackend

class Enrollment : SignableRequestPayload {
    lateinit var username: String
    lateinit var password: String
    lateinit var deviceId: String
    lateinit var publicKey: String

    override var timestamp: Long = 0


    override fun messageForVerification(): String {
        return listOf(username,
                password,
                deviceId,
                publicKey,
                timestamp).joinToString("|")
    }
}


