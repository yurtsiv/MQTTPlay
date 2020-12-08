package com.example.mqttplay.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mqttplay.model.Broker
import com.example.mqttplay.model.ConnectionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.eclipse.paho.android.service.MqttAndroidClient

enum class StatusBarState {
    INVISIBLE,
    CONNECTING,
    CONNECTED,
    CONNECTION_ERROR
}

class ViewBrokerViewModel : ViewModel() {
    lateinit var broker: Broker;

    val toast = MutableLiveData<String>()

    val statusBarState = MutableLiveData<StatusBarState>()

    fun initialize(context: Context, brokerId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            statusBarState.postValue(StatusBarState.CONNECTING)

            broker = Broker.fetchSingle(brokerId)

            val serverURI = "tcp://${broker.address}:${broker.port}";

            val mqttClient = MqttAndroidClient(context, serverURI, "kotlin_client")

            broker
                .connect(mqttClient)
                .collect {
                    when(it) {
                        ConnectionStatus.CONNECTED ->
                            statusBarState.postValue(StatusBarState.CONNECTED)
                        ConnectionStatus.FAILED_TO_CONNECT, ConnectionStatus.CONNECTION_LOST ->
                            statusBarState.postValue(StatusBarState.CONNECTION_ERROR)
                    }
                }

        }
    }

    fun sendTestMessage() {
        if (broker.connectionStatus != ConnectionStatus.CONNECTED) return

        CoroutineScope(Dispatchers.IO).launch {
            val topic = "home/ding_dong"
            val msg = "hello"
            broker.publishMessage(topic, msg)
        }
    }
}