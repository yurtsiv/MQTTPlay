package com.example.mqttplay.viewmodel

import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mqttplay.R
import com.example.mqttplay.model.Broker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO_PARALLELISM_PROPERTY_NAME
import kotlinx.coroutines.launch
import java.lang.Exception

class AddBrokerViewModel : ViewModel() {
    val label = MutableLiveData<String>()
    val address = MutableLiveData<String>()
    val port = MutableLiveData<String>()
    val qualityOfService = MutableLiveData<Int>().apply { value = R.id.QOS_0 }
    val useSSL = MutableLiveData<Boolean>().apply {  value = true }
    private val saving = MutableLiveData<Boolean>().apply { value = false }
    private val valid = MediatorLiveData<Boolean>().apply {
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
    val saveBtnEnabled = MediatorLiveData<Boolean>().apply {
        addSource(saving) {
            value = isSaveBtnEnabled();
        }

        addSource(valid) {
            value = isSaveBtnEnabled()
        }
    }

    suspend fun save(): String {
        saving.postValue(true)

        try {
            return Broker(label.value ?: "").save();
        } catch (e: Exception) {
            throw e;
        } finally {
            saving.postValue(false)
        }
    }

    private fun isSaveBtnEnabled(): Boolean {
        return !(saving.value ?: false) && (valid.value ?: false)
    }

    private fun isFormValid(): Boolean {
        return isPortValid() && isLabelValid() && isAddressValid()
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