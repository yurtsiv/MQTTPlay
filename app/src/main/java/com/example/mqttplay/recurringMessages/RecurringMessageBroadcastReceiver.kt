package com.example.mqttplay.recurringMessages

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mqttplay.mqtt.ConnectionStatus
import com.example.mqttplay.mqtt.MQTTConnection
import com.example.mqttplay.repo.Broker
import com.example.mqttplay.repo.BrokerRepo
import com.example.mqttplay.repo.Tile
import com.example.mqttplay.repo.TileRepo
import com.example.mqttplay.viewmodel.StatusBarState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.paho.android.service.MqttService
import java.lang.Exception

class RecurringMessageBroadcastReceiver : BroadcastReceiver() {
    companion object {
        const val intentActionStartsWith = "SEND_RECURRING_MQTT_MESSAGE"
    }

    private suspend fun sendMessage(context: Context, broker: Broker, tile: Tile) {
        val service = context.getSystemService(MqttService::class.java)
        TODO("SMTH")
//        connection
//            .connect()
//            .collect {
//                when (it) {
//                    ConnectionStatus.CONNECTED -> {
//                        connection.publishMessage(
//                            tile.topic,
//                            tile.value,
//                            tile.qos,
//                            tile.retainMessage ?: false
//                        )
//                    }
//                    ConnectionStatus.FAILED_TO_CONNECT -> {
//                        throw Exception("Failed to connect to broker")
//                    }
//                }
//            }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null || intent.action?.startsWith(intentActionStartsWith) != true || intent.extras?.get(
                "tileId"
            ) == null
        ) {
            return;
        }

        val tileId = intent.extras?.get(
            "tileId"
        ) as String;

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tile = TileRepo.fetchSingle(tileId)
                val broker = BrokerRepo.fetchSingle(tile.brokerId)
                sendMessage(context, broker, tile)
            } catch (e: Exception) {
                TODO("HDNEL ERROR")
                // pass
            }

        }
    }
}