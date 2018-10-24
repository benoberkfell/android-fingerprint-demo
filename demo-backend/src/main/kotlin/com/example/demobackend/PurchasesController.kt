package com.example.demobackend

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class PurchasesController {

    val dataStore = DataStore.instance()
    val helper = SignatureHelper<Purchase>()

    @RequestMapping(path = ["/purchases"], method = [RequestMethod.POST])
    fun purchase(@RequestBody signedRequest: SignedRequest<Purchase>): ResponseEntity<PurchaseResponse> {
        val purchase = signedRequest.payload

        val key = dataStore.publicKeyForEnrolledDevice(purchase.token)

        return if (key != null
            && helper.isRequestValidlyStamped(signedRequest)
            && helper.isRequestValidlySigned(signedRequest, key)) {
            val response = PurchaseResponse("Thank you for ordering a ${purchase.itemName}, we will send it to "
                    + "${purchase.deliveryAddress} right away.")

            ResponseEntity(response, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }
    }
}
