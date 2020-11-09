package com.example.mqttplay.model

import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Exception

data class Broker(
    val label: String,
    val address: String,
    val port: String,
    val qos: Int,
    val useSSL: Boolean
) {
    companion object {
        private val db = Firebase.firestore
        const val COLLECTION = "brokers"

        private fun docToBroker(document: QueryDocumentSnapshot): Broker {
            return Broker(
                document.data["label"] as String,
                document.data["address"] as String,
                document.data["port"] as String,
                (document.data["qos"] as Long).toInt(),
                document.data["useSSL"] as Boolean,
            )
        }

        private fun brokerToHashMap(broker: Broker): HashMap<String, Any> {
            return hashMapOf(
                "label" to broker.label,
                "address" to broker.address,
                "port" to broker.port,
                "qos" to broker.qos,
                "useSSL" to broker.useSSL
            )
        }

        suspend fun listAll(): List<Broker> {
            return suspendCoroutine { cont ->
                db.collection(COLLECTION)
                    .get()
                    .addOnSuccessListener {documents ->
                        val res = documents.map { docToBroker(it) }
                        cont.resume(res)
                    }
                    .addOnFailureListener {
                        print(it)
                    }
            }
        }
    }

    suspend fun save(): String {
        return suspendCoroutine { cont ->
            db.collection(COLLECTION)
                .add(brokerToHashMap(this))
                .addOnSuccessListener { documentReference ->
                    cont.resume(documentReference.id)
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }

        }
    }
}