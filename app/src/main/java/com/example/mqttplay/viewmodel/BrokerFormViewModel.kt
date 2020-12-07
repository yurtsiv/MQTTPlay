package com.example.mqttplay.viewmodel

import android.util.Patterns
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mqttplay.R
import com.example.mqttplay.model.Broker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BrokerFormViewModel : ViewModel() {
    companion object {
        val QOS_ID_TO_VALUE = hashMapOf(
            R.id.QOS_0 to 0,
            R.id.QOS_1 to 1,
            R.id.QOS_2 to 2
        )

        val QOS_VALUE_TO_ID = QOS_ID_TO_VALUE.entries.associate {(k, v) -> v to k}
    }

    private var brokerId: String? = null;

    // Form fields
    val label = MutableLiveData<String>()
    val address = MutableLiveData<String>()
    val port = MutableLiveData<String>()
    val qualityOfServiceID = MutableLiveData<Int>().apply { value = R.id.QOS_0 }
    val useSSL = MutableLiveData<Boolean>().apply {  value = true }

    val saving = MutableLiveData<Boolean>().apply { value = false }
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

    fun constructBroker(): Broker {
        return Broker(
            label.value as String,
            address.value as String,
            port.value as String,
            QOS_ID_TO_VALUE[qualityOfServiceID.value] as Int,
            useSSL.value as Boolean,
            brokerId
        )
    }

    fun fillForm(brokerId: String) {
        this.brokerId = brokerId

        CoroutineScope(Dispatchers.IO).launch {
            val broker = Broker.fetchSingle(brokerId)

            label.postValue(broker.label)
            address.postValue(broker.address)
            port.postValue(broker.port)
            qualityOfServiceID.postValue(QOS_VALUE_TO_ID[broker.qos])
            useSSL.postValue(broker.useSSL)
        }
    }

    private fun isSaveBtnEnabled(): Boolean {
        return !(saving.value ?: false) && (valid.value ?: false)
    }

    private fun isFormValid(): Boolean {
        return isPortValid() && isLabelValid() && isAddressValid()
    }

    private fun isPortValid(): Boolean {
        val value: String? = port.value
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