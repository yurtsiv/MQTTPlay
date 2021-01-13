package com.example.mqttplay.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.example.mqttplay.R
import com.example.mqttplay.adapter.ArrayAdapterWithIcon
import com.example.mqttplay.mqtt.ConnectionStatus
import com.example.mqttplay.mqtt.MQTTConnection
import com.example.mqttplay.recurringMessages.MQTTMessagingService
import com.example.mqttplay.repo.Broker
import com.example.mqttplay.repo.BrokerRepo
import com.example.mqttplay.repo.Tile
import com.example.mqttplay.repo.TileRepo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.Exception

enum class StatusBarState {
    INVISIBLE,
    CONNECTING,
    CONNECTED,
    CONNECTION_ERROR
}


class ViewBrokerViewModel : ViewModel() {
    lateinit var broker: Broker
    lateinit var mqttConnection: MQTTConnection

    val loading = MutableLiveData<Boolean>().apply {  value = false }
    val tiles = MutableLiveData<List<Tile>>()
    val toast = MutableLiveData<String>()
    val statusBarState = MutableLiveData<StatusBarState>()

    private suspend fun loadTiles(brokerId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            loading.postValue(true)

            try {
                tiles.postValue(TileRepo.listAllForBroker(brokerId))
            } catch (e: Exception) {
                toast.postValue(e.message)
            }

            loading.postValue(false)
        }
    }

    fun initialize(context: Context, brokerId: String) {
        val s = context.getSystemService(MQTTMessagingService::class.java)
        TODO("HELLO")
//        CoroutineScope(Dispatchers.IO).launch {
//            loadTiles(brokerId)
//            statusBarState.postValue(StatusBarState.CONNECTING)
//
//            broker = BrokerRepo.fetchSingle(brokerId)
//            mqttConnection = MQTTConnection(broker, context)
//            mqttConnection
//                .connect()
//                .collect {
//                    when(it) {
//                        ConnectionStatus.CONNECTED ->
//                            statusBarState.postValue(StatusBarState.CONNECTED)
//                        ConnectionStatus.FAILED_TO_CONNECT, ConnectionStatus.CONNECTION_LOST ->
//                            statusBarState.postValue(StatusBarState.CONNECTION_ERROR)
//                    }
//                }

//        }
    }

    fun sendTestMessage() {
//        if (mqttConnection.connectionStatus != ConnectionStatus.CONNECTED) return

//        CoroutineScope(Dispatchers.IO).launch {
//            val topic = "home/ding_dong"
//            val msg = "hello"
//            mqttConnection.publishMessage(topic, msg)
//        }
    }
}