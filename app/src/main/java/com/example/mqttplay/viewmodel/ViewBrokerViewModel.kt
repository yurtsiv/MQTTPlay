package com.example.mqttplay.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mqttplay.recurringMessages.ConnectionStatus
import com.example.mqttplay.recurringMessages.MQTTMessagingService
import com.example.mqttplay.repo.*
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
    lateinit var service: MQTTMessagingService

    private val loading = MutableLiveData<Boolean>().apply {  value = false }
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

    fun onServiceConnected() {
        CoroutineScope(Dispatchers.IO).launch {
            service
                .connect(broker)
                .collect {
                    when(it) {
                        ConnectionStatus.CONNECTING ->
                            statusBarState.postValue(StatusBarState.CONNECTING)
                        ConnectionStatus.CONNECTED ->
                            statusBarState.postValue(StatusBarState.CONNECTED)
                        ConnectionStatus.FAILED_TO_CONNECT, ConnectionStatus.CONNECTION_LOST ->
                            statusBarState.postValue(StatusBarState.CONNECTION_ERROR)
                    }
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initialize(context: Context, brokerId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            loadTiles(brokerId)
            broker = BrokerRepo.fetchSingle(brokerId)

            val intent = Intent(context, MQTTMessagingService::class.java)

            context.startForegroundService(intent)
            context.bindService(intent, object : ServiceConnection {
                override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
                    service = (binder as MQTTMessagingService.MQTTMessagingBinder).service
                    onServiceConnected()
                }

                override fun onServiceDisconnected(p0: ComponentName?) {
                    TODO("Not yet implemented")
                }
            }, Context.BIND_AUTO_CREATE)
        }
    }

    fun onTileCLick(tile: Tile) {
        if (!service.isConnected()) {
            toast.value = "Not connected to the broker"
            return
        }

        when (tile.type) {
            TileType.BUTTON ->
                service.publishMessage(
                    tile.topic,
                    tile.value,
                    tile.qos,
                    tile.retainMessage
                )
            else -> {}
        }
    }

    fun removeTile(tile: Tile) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (tile.id != null) {
                    TileRepo.remove(tile.id)
                    tiles.postValue(tiles.value?.filter { it.id != tile.id })
                }
            } catch (e: Exception) {
                toast.postValue(e.message)
            }
        }
    }
}