package com.example.demobackend;

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.POST
import org.springframework.web.bind.annotation.RestController

@RestController
class EnrollmentsController {

    val dataStore = DataStore.instance()
    val helper = SignatureHelper<Enrollment>()

    @RequestMapping(path = ["/enrollments"], method = [POST])
    fun enroll(@RequestBody signedRequest: SignedRequest<Enrollment>): ResponseEntity<EnrollmentResponse> {

        val payload = signedRequest.payload

        return if (helper.isRequestValidlyStamped(signedRequest)
            && helper.isRequestValidlySigned(signedRequest, payload.publicKey)
            && dataStore.verifyUsernameAndPassword(payload.username, payload.password)
        ) {
            val enrollment = DataStore.instance().enrollDevice(payload.username, payload.deviceId, payload.publicKey)
            val enrollmentResponse = EnrollmentResponse(enrollment.token)
            ResponseEntity(enrollmentResponse, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }
    }
}