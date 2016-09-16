package com.example.demobackend

import java.util.*

class DataStore {

    val enrollments: HashMap<String, Enrollment> = HashMap()

    fun verifyUsernameAndPassword(username: String, password: String) : Boolean{
        return username.equals("android") and password.equals("androidrocks")
    }

    fun enrollDevice(username: String, deviceId: String, publicKey: String) : Enrollment {
        val enrollment = Enrollment(UUID.randomUUID().toString(), publicKey, username, deviceId)
        enrollments.put(enrollment.token, enrollment)
        return enrollment
    }

    fun publicKeyForEnrolledDevice(token: String) : String? {
        return enrollments.get(token)?.publicKey
    }

    class Enrollment(val token: String, val publicKey: String, val username: String, val deviceId: String)

    companion object {
        val dataStore = DataStore()

        fun instance() : DataStore {
            return dataStore
        }
    }
}