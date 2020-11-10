package com.example.mqttplay.model

import com.google.firebase.firestore.DocumentSnapshot
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
    val useSSL: Boolean,
    val id: String? = null
) {
    companion object {
        private val db = Firebase.firestore
        const val COLLECTION = "brokers"

        private fun docToBroker(document: DocumentSnapshot): Broker {
            return Broker(
                document.data?.get("label") as String,
                document.data?.get("address") as String,
                document.data?.get("port") as String,
                (document.data?.get("qos") as Long).toInt(),
                document.data?.get("useSSL") as Boolean,
                document.id
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
                    .addOnSuccessListener { documents ->
                        val res = documents.map { docToBroker(it) }
                        cont.resume(res)
                    }
                    .addOnFailureListener {
                        print(it)
                    }
            }
        }

        suspend fun fetchSingle(id: String): Broker {
            return suspendCoroutine { cont ->
                db.document("${COLLECTION}/${id}")
                    .get()
                    .addOnSuccessListener { doc ->
                        cont.resume(docToBroker(doc))
                    }
                    .addOnFailureListener {
                        print(it)
                    }
            }
        }
    }

    private suspend fun create(): String {
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

    private suspend fun edit(): String {
        return suspendCoroutine { cont ->
            db.document("${COLLECTION}/${id}")
                .set(brokerToHashMap(this))
                .addOnSuccessListener {
                    cont.resume(id ?: "")
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }
    }

    suspend fun save(): String {
        return if (id == null) {
            create();
        } else {
            edit();
        }
    }
}