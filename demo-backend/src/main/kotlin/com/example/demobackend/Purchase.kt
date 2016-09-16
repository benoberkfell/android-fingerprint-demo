package com.example.demobackend

class Purchase : SignableRequestPayload {
    lateinit var itemName: String
    lateinit var deliveryAddress: String
    lateinit var token: String

    override var timestamp: Long = 0

    override fun messageForVerification(): String {
        return arrayOf(itemName,
                deliveryAddress,
                token,
                timestamp).joinToString("|")
    }

}
