package com.example.mqttplay.recurringMessages

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.mqttplay.R
import com.example.mqttplay.repo.Broker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import kotlinx.coroutines.flow.Flow

enum class ConnectionStatus {
    CONNECTING,
    NOT_CONNECTED,
    CONNECTED,
    FAILED_TO_CONNECT,
    CONNECTION_LOST
}

class MQTTMessagingService : Service() {
    companion object {
        const val MQTT_CLIENT_ID = "mqtt_play_client"
        const val NOTIFICATION_CHANNEL_ID = "foreground_mqtt_service_channel"
        const val NOTIFICATION_CHANNEL_NAME = "Foreground MQTT messaging service chanel"
    }

    private var mqttClient: MqttAndroidClient? = null
    private var connectionStatus: ConnectionStatus = ConnectionStatus.NOT_CONNECTED
    var currentBroker: Broker? = null

    fun isConnected(): Boolean {
        return mqttClient?.isConnected ?: false
    }

    @ExperimentalCoroutinesApi
    private fun doConnect(scope: ProducerScope<ConnectionStatus>) {
        scope.offer(ConnectionStatus.CONNECTING)

        try {
            mqttClient?.setCallback(object : MqttCallback {
                override fun messageArrived(topic: String?, message: MqttMessage?) {
                }

                override fun connectionLost(cause: Throwable?) {
                    connectionStatus = ConnectionStatus.CONNECTION_LOST
                    scope.offer(ConnectionStatus.CONNECTION_LOST)
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                }
            })

            val options = MqttConnectOptions()

            mqttClient?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    connectionStatus = ConnectionStatus.CONNECTED
                    scope.offer(ConnectionStatus.CONNECTED)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    connectionStatus = ConnectionStatus.FAILED_TO_CONNECT
                    scope.offer(ConnectionStatus.FAILED_TO_CONNECT)
                }
            })
        } catch (e: MqttException) {
            scope.close(e);
        }
    }

    @ExperimentalCoroutinesApi
    fun connect(broker: Broker): Flow<ConnectionStatus> {
        val currentBroker = this.currentBroker
        this.currentBroker = broker
        val serverURI = "tcp://${broker.address}:${broker.port}";

        return callbackFlow {
            if (currentBroker?.id != broker.id) {
                mqttClient?.disconnect()
                mqttClient = MqttAndroidClient(applicationContext, serverURI, "kotlin_client")
                doConnect(this)
            } else if (mqttClient?.isConnected == true) {
                offer(ConnectionStatus.CONNECTED)
            }

            awaitClose { cancel() }
        }
    }

    fun publishMessage(
        topic: String,
        message: String = "",
        msgQos: Int = 0,
        retained: Boolean = false
    ) {
        if (mqttClient == null || mqttClient?.isConnected != true) {
            throw Exception("Broker is not connected")
        }

        val mqttMsg = MqttMessage()
        mqttMsg.payload = message.toByteArray()
        mqttMsg.qos = msgQos
        mqttMsg.isRetained = retained

        try {
            mqttClient?.publish(topic, mqttMsg, null, object : IMqttActionListener {
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

    class MQTTMessagingBinder(val service: MQTTMessagingService) : Binder()

    override fun onBind(intent: Intent?): IBinder? {
        return MQTTMessagingBinder(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        );

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        );

        // TODO: add stop button and content intent
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.foreground_service_notification_title))
            .setContentText(getString(R.string.foreground_service_notification_text))
            .setSmallIcon(R.drawable.dashboard)
            .setTicker(getString(R.string.foreground_service_notification_text))
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            throw Exception("Failed to start the MQTTMessagingService. Intent is not provided")
        }

        return START_NOT_STICKY
    }
}