package com.example.mqttplay.recurringMessages

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
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

        val pendingIntent: PendingIntent =
            Intent(this, MQTTMessagingService::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val CHANNEL_ID = "my_channel_01"
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Channel human readable title",
            NotificationManager.IMPORTANCE_DEFAULT
        );

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        );

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("HELLO")
            .setContentText("THERE")
            .setSmallIcon(R.drawable.button)
            .setContentIntent(pendingIntent)
            .setTicker("TICKER")
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