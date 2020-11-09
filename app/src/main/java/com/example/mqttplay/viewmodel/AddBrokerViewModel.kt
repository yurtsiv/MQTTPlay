package com.example.mqttplay.viewmodel

import android.util.Patterns
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mqttplay.R
import com.example.mqttplay.model.Broker
import java.lang.Exception

class AddBrokerViewModel : ViewModel() {
    companion object {
        val QOS_VALUES = hashMapOf(
            R.id.QOS_0 to 0,
            R.id.QOS_1 to 1,
            R.id.QOS_2 to 2
        )
    }

    val label = MutableLiveData<String>()
    val address = MutableLiveData<String>()
    val port = MutableLiveData<String>()
    val qualityOfServiceID = MutableLiveData<Int>().apply { value = R.id.QOS_0 }
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
            return Broker(
                label.value as String,
                address.value as String,
                port.value as String,
                QOS_VALUES[qualityOfServiceID.value] as Int,
                useSSL.value as Boolean
            ).save();
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