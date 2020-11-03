package com.example.mqttplay.model

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class Broker(
    val label: String
//    val address: String,
//    val port: String,
//    val qos: Int,
//    val useSSL: Boolean
) {
    private val db = Firebase.firestore

    suspend fun save(): String {
        val broker = hashMapOf(
            "label" to label
        )

        return suspendCoroutine { cont ->
            db.collection("brokers")
                .add(broker)
                .addOnSuccessListener { documentReference ->
                    cont.resume(documentReference.id)
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }

        }
    }
}