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
    companion object {
        private val db = Firebase.firestore
        const val COLLECTION = "brokers"

        suspend fun listAll(): List<Broker> {
            return suspendCoroutine { cont ->
                db.collection(COLLECTION)
                    .get()
                    .addOnSuccessListener {documents ->
                        val res = mutableListOf<Broker>()
                        for (document in documents) {
                            val label = document.data["label"] as String
                            res.add(Broker(label))
                        }

                        cont.resume(res)
                    }
                    .addOnFailureListener {
                        print(it)
                    }
            }
        }
    }

    suspend fun save(): String {
        val broker = hashMapOf(
            "label" to label
        )

        return suspendCoroutine { cont ->
            db.collection(COLLECTION)
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