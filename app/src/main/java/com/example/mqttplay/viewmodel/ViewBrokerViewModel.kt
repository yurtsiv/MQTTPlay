package com.example.mqttplay.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mqttplay.model.Broker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewBrokerViewModel : ViewModel() {
    private var brokerId: String? = null;

    val activityTitle = MutableLiveData<String>().apply { value = "Broker" }

    fun initialize(brokerId: String) {
        this.brokerId = brokerId;

        CoroutineScope(Dispatchers.IO).launch {
            val broker = Broker.fetchSingle(brokerId)

            activityTitle.postValue(broker.label)
        }
    }
}