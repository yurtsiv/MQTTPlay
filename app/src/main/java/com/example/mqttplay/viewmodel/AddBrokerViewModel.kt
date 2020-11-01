package com.example.mqttplay.viewmodel

import android.util.Patterns
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mqttplay.R

class AddBrokerViewModel : ViewModel() {
    val label = MutableLiveData<String>()
    val address = MutableLiveData<String>()
    val port = MutableLiveData<String>()
    val qualityOfService = MutableLiveData<Int>().apply { value = R.id.QOS_0 }
    val useSSL = MutableLiveData<Boolean>().apply {  value = true }

    val valid = MediatorLiveData<Boolean>().apply {
        addSource(label) {
            value = isFormValid()
        }
        addSource(address) {
            value = isFormValid()
        }
        addSource(port) {
            value = isFormValid()
        }
    }

    private fun isFormValid(): Boolean {
        return isPortValid() && isLabelValid() && isAddressValid();
    }

    private fun isPortValid(): Boolean {
        val value: String? = port.value;
        return value != null && value.isNotBlank()
    }

    private fun isLabelValid(): Boolean {
        val value: String? = label.value
        return value != null && value.isNotBlank()
    }

    private fun isAddressValid(): Boolean {
        return Patterns.WEB_URL.matcher(address.value ?: "").matches()
    }
}