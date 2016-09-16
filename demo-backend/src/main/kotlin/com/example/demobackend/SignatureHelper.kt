package com.example.demobackend

import org.slf4j.LoggerFactory
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.*

class SignatureHelper<T : SignableRequestPayload> {

    private val log = LoggerFactory.getLogger(this.javaClass)

    fun isRequestValidlyStamped(request: SignedRequest<T>) : Boolean {
        val payload = request.payload;

        // 10 seconds of slop allowed in each side
        return (Math.abs(System.currentTimeMillis() - payload.timestamp) < 10000)
    }

    fun isRequestValidlySigned(request: SignedRequest<T>, publicKey: String) : Boolean {
        val pubKeyBytes = Base64.getDecoder().decode(publicKey)
        val signatureBytes = Base64.getDecoder().decode(request.signature)

        val kf = KeyFactory.getInstance("RSA")
        val key = kf.generatePublic(X509EncodedKeySpec(pubKeyBytes))
        val verify = Signature.getInstance("SHA256withRSA")
        verify.initVerify(key)
        verify.update(request.payload.messageForVerification().toByteArray())
        return verify.verify(signatureBytes)
    }
}