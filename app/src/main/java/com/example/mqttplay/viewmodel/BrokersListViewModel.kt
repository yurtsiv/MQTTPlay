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
    val toast = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>().apply { value = false }

    fun loadBrokers() {
        CoroutineScope(Dispatchers.IO).launch {
            loading.postValue(true)

            try {
                brokers.postValue(Broker.listAll())
            } catch (e: Exception) {
                toast.postValue(e.message)
            }

            loading.postValue(false)
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