package com.example.mqttplay.mqtt

import android.content.Context
import android.util.Log
import com.example.mqttplay.repo.Broker
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.lang.Exception

enum class ConnectionStatus {
    NOT_CONNECTED,
    CONNECTED,
    FAILED_TO_CONNECT,
    CONNECTION_LOST
}

class MQTTConnection(val broker: Broker, val context: Context) {
    val serverURI = "tcp://${broker.address}:${broker.port}";

    val mqttClient = MqttAndroidClient(context, serverURI, "kotlin_client")

    var connectionStatus: ConnectionStatus = ConnectionStatus.NOT_CONNECTED

    fun clearResources() {
        mqttClient?.unregisterResources()
    }

    fun connect(): Flow<ConnectionStatus> {
        return callbackFlow {
            try {
                mqttClient.setCallback(object : MqttCallback {
                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                    }

                    override fun connectionLost(cause: Throwable?) {
                        connectionStatus = ConnectionStatus.CONNECTION_LOST
                        offer(ConnectionStatus.CONNECTION_LOST)
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    }
                })

                val options = MqttConnectOptions()
                mqttClient.connect(options, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        connectionStatus = ConnectionStatus.CONNECTED
                        offer(ConnectionStatus.CONNECTED)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        connectionStatus = ConnectionStatus.FAILED_TO_CONNECT
                        offer(ConnectionStatus.FAILED_TO_CONNECT)
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
        msgQos: Int = broker.qos,
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
                    Log.d("HELLO", "Message sent successfully")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d("HELLO", "Error sending the message ${exception?.message}")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
}