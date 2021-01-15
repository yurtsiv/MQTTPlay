package com.example.mqttplay.recurringMessages

import android.content.*
import com.example.mqttplay.repo.RecurringTileTime
import com.example.mqttplay.repo.Tile
import com.example.mqttplay.repo.TileRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class RecurringMessageBroadcastReceiver : BroadcastReceiver() {
    companion object {
        const val SEND_MESSAGE_INTENT_ACTION = "SEND_RECURRING_MESSAGE_INTENT_ACTION_START"
        const val STOP_MESSAGING_SERVICE_INTENT_ACTION = "STOP_MESSAGING_SERVICE_INTENT_ACTION"
    }

    lateinit var mqttService: MQTTMessagingService

    private fun sendMessage(tile: Tile) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                mqttService.publishMessage(
                    tile.topic,
                    tile.value ?: "",
                    tile.qos,
                    tile.retainMessage ?: false
                )
            } catch (e: Exception) {
                // TODO: handle error somehow
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null || intent.action?.startsWith(SEND_MESSAGE_INTENT_ACTION) != true || intent.extras?.get(
                "tileId"
            ) == null
        ) {
            return;
        }

        val serviceIntent = Intent(context, MQTTMessagingService::class.java)
        val binder = peekService(context, serviceIntent) as MQTTMessagingService.MQTTMessagingBinder
        mqttService = binder.service

        if (!mqttService.isConnected()) {
            // TODO: log it somewhere and maybe try to reconnect
            return;
        }

        val tileId = intent.extras?.get(
            "tileId"
        ) as String;

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