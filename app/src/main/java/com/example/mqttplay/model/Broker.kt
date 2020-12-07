package com.example.mqttplay.model

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.lang.Exception

data class Broker(
    val label: String,
    val address: String,
    val port: String,
    val qos: Int,
    val useSSL: Boolean,
    val id: String? = null
) {
    private lateinit var mqttClient: MqttAndroidClient

    companion object {
        private val db = Firebase.firestore
        const val COLLECTION = "brokers"
        const val TAG = "AndroidMqttClient"

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
                        cont.resumeWithException(it)
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
                        cont.resumeWithException(it)
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

    suspend fun remove() {
        return suspendCoroutine { cont ->
            if (id == null) {
                cont.resumeWithException(Exception("Broker is not saved"))
            } else {
                db.document("${COLLECTION}/${id}")
                    .delete()
                    .addOnSuccessListener {
                        cont.resume(Unit)
                    }
                    .addOnFailureListener { e ->
                        cont.resumeWithException(e)
                    }
            }
        }
    }

    fun clearMqttResources() {
        mqttClient?.unregisterResources()
    }

    fun connect(mqttClient: MqttAndroidClient): Flow<String> {
        this.mqttClient = mqttClient

        return callbackFlow {
            try {
                mqttClient.setCallback(object : MqttCallback {
                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        offer("MESSAGE_RECEIVED")
                    }

                    override fun connectionLost(cause: Throwable?) {
                        offer("CONNECTION_LOST")
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    }
                })

                val options = MqttConnectOptions()
                mqttClient.connect(options, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        offer("CONNECTED")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        offer(exception?.message)
                    }
                })
            } catch (e: MqttException) {
                close(e);
            }

            awaitClose { cancel() }
        }
    }

    fun publishMessage(
        topic: String,
        message: String,
        msgQos: Int = qos,
        retained: Boolean = false
    ) {
        if (mqttClient == null || !mqttClient.isConnected) {
            throw Exception("Broker is not connected")
        }

        val mqttMsg = MqttMessage()
        mqttMsg.payload = message.toByteArray()
        mqttMsg.qos = msgQos
        mqttMsg.isRetained = retained

        try {
            mqttClient.publish(topic, mqttMsg, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Message sent successfully")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Error sending the message ${exception?.message}")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
}