package com.example.mqttplay.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mqttplay.R

class TileFormCommonFieldsViewModel : ViewModel() {
    // TODO: don't repeat the code (BrokerFormViewModel)
    companion object {
        val QOS_ID_TO_VALUE = hashMapOf(
            R.id.QOS_0 to 0,
            R.id.QOS_1 to 1,
            R.id.QOS_2 to 2
        )

        val QOS_VALUE_TO_ID = QOS_ID_TO_VALUE.entries.associate {(k, v) -> v to k}
    }

    // Form fields
    val topic = MutableLiveData<String>()
    val value = MutableLiveData<String>()
    val qualityOfServiceID = MutableLiveData<Int>().apply { value = R.id.QOS_0 }
    val retainMessage = MutableLiveData<Boolean>().apply { value = false }

    val valid = MediatorLiveData<Boolean>().apply {
        addSource(topic) {
            value = isFormValid()
        }
    }

    private fun isFormValid(): Boolean {
        return isTopicValid();
    }

    private fun isTopicValid(): Boolean {
        val value: String? = topic.value
        return value != null && value.isNotBlank()
    }
}
