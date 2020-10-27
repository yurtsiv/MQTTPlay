package com.example.mqttplay.viewmodel

import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddBrokerViewModel : ViewModel() {
    val label = MutableLiveData<String>()
}