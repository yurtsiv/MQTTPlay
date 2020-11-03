package com.example.mqttplay.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mqttplay.model.Broker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BrokersListViewModel : ViewModel() {
    val brokers = MutableLiveData<List<Broker>>()

    fun loadBrokers() {
        CoroutineScope(Dispatchers.IO).launch {
            brokers.postValue(Broker.listAll())
        }
    }
}