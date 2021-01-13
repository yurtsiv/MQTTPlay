package com.example.mqttplay.recurringMessages

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.mqttplay.R
import com.example.mqttplay.view.MainActivity
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.android.service.MqttService
import org.eclipse.paho.client.mqttv3.*

class MQTTMessagingService : Service() {
    private lateinit var mqttClient: MqttAndroidClient

    private fun doConnect(address: String, port: String) {
        val serverURI = "tcp://${address}:${port}";
        mqttClient = MqttAndroidClient(applicationContext, serverURI, "kotlin_client")

        val options = MqttConnectOptions()
        mqttClient.connect(options)
    }

    fun publishMessage(
        topic: String,
        message: String,
        msgQos: Int = 0,
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

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        val pendingIntent: PendingIntent =
            Intent(this, MQTTMessagingService::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val CHANNEL_ID = "my_channel_01"
        val channel = NotificationChannel(CHANNEL_ID,
            "Channel human readable title",
            NotificationManager.IMPORTANCE_DEFAULT);

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel);

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

//        doConnect(
//            intent.extras?.get("brokerAddress") as String,
//            intent.extras?.get("brokerPort") as String
//        )


        return START_NOT_STICKY
    }


}