package com.example.mqttplay.model

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
                    .addOnFailureListener {e ->
                        cont.resumeWithException(e)
                    }
            }
        }
    }

    fun clearMqttResources() {
        mqttClient?.unregisterResources()
    }

    suspend fun connect(context: Context): Boolean {
        val serverURI = "tcp://${address}:${port}";

        mqttClient = MqttAndroidClient(context, serverURI, "kotlin_client")

        try {
            mqttClient.setCallback(object : MqttCallback {
                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    Log.d(TAG, "Receive message: ${message.toString()} from topic: $topic")
                }

                override fun connectionLost(cause: Throwable?) {
                    Log.d(TAG, "Connection lost ${cause.toString()}")
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()

        }

        val options = MqttConnectOptions()

        return suspendCoroutine {cont ->
            try {
                mqttClient.connect(options, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(TAG, "Connection success")
                        cont.resume(true)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(TAG, "Connection failure ${exception?.message}")
                        cont.resume(false)
                    }
                })
            } catch(e: MqttException) {
                e.printStackTrace()
            }
        }
    }

    fun publishMessage(topic: String, message: String, msgQos: Int = qos, retained: Boolean = false) {
        if (mqttClient == null || !mqttClient.isConnected) {
            throw Exception("Broker is not connected")
        }

        val mqttMsg = MqttMessage()
        mqttMsg.payload = message.toByteArray()
        mqttMsg.qos = qos
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