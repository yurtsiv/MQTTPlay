package com.example.mqttplay.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mqttplay.model.Broker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.eclipse.paho.android.service.MqttAndroidClient

class ViewBrokerViewModel : ViewModel() {
    lateinit var broker: Broker;

    val brokerConnected = MutableLiveData<Boolean>().apply { value = false }
    val toast = MutableLiveData<String>()

    fun initialize(context: Context, brokerId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            broker = Broker.fetchSingle(brokerId)

            val serverURI = "tcp://${broker.address}:${broker.port}";

            val mqttClient = MqttAndroidClient(context, serverURI, "kotlin_client")

            broker
                .connect(mqttClient)
                .collect {
                    brokerConnected.postValue(it == "CONNECTED")
                    toast.postValue(it)
                }

        }
    }

    fun sendTestMessage() {
        if (brokerConnected.value != true) return

        CoroutineScope(Dispatchers.IO).launch {
            val topic = "home/ding_dong"
            val msg = "hello"
            broker.publishMessage(topic, msg)
        }
    }
}