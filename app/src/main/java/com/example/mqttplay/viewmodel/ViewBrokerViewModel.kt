package com.example.mqttplay.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mqttplay.model.Broker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewBrokerViewModel : ViewModel() {
    lateinit var broker: Broker;

    val activityTitle = MutableLiveData<String>().apply { value = "Broker" }
    val brokerConnected = MutableLiveData<Boolean>().apply { value = false }
    val toast = MutableLiveData<String>()

    fun initialize(context: Context, brokerId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            broker = Broker.fetchSingle(brokerId)

            activityTitle.postValue(broker.label)

            val connected = broker.connect(context);

            brokerConnected.postValue(connected)

            toast.postValue(if (connected) "Connected" else "Failed to connect" )
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