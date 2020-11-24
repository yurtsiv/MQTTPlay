package com.example.mqttplay.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mqttplay.model.Broker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class BrokersListViewModel : ViewModel() {
    val brokers = MutableLiveData<List<Broker>>()
    val toast = MutableLiveData<String>();

    fun loadBrokers() {
        CoroutineScope(Dispatchers.IO).launch {
            brokers.postValue(Broker.listAll())
        }
    }

    fun removeBroker(broker: Broker) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                broker.remove()
                brokers.postValue(brokers.value?.filter { it.id != broker.id })
            } catch (e: Exception) {
                toast.postValue(e.message)
            }
        }
    }
}