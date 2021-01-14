package com.example.mqttplay.recurringMessages

import android.content.*
import android.util.Log
import com.example.mqttplay.repo.RecurringTileTime
import com.example.mqttplay.repo.Tile
import com.example.mqttplay.repo.TileRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class RecurringMessageBroadcastReceiver : BroadcastReceiver() {
    companion object {
        const val intentActionStartsWith = "SEND_RECURRING_MQTT_MESSAGE"
    }

    lateinit var service: MQTTMessagingService

    private fun sendMessage(tile: Tile) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                service.publishMessage(
                    tile.topic,
                    tile.value ?: "",
                    tile.qos,
                    tile.retainMessage ?: false
                )
            } catch (e: Exception) {
                TODO("HDNEL ERROR")
            }

        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null || intent.action?.startsWith(intentActionStartsWith) != true || intent.extras?.get(
                "tileId"
            ) == null
        ) {
            return;
        }

        val serviceIntent = Intent(context, MQTTMessagingService::class.java)
        val binder = peekService(context, serviceIntent) as MQTTMessagingService.MQTTMessagingBinder
        service = binder.service

        if (!service.isConnected()) {
            // TODO: log it somewhere
            return;
        }

        val tileId = intent.extras?.get(
            "tileId"
        ) as String;

        Log.v("SENDING_MESSAGE", "$tileId")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tile = TileRepo.fetchSingle(tileId)
                sendMessage(tile)
                RecurringMessages.scheduleMessage(context, tileId, tile.recurringTime as RecurringTileTime)
            } catch (e: Exception) {
                // TODO: log it somewhere
            }

        }
    }
}