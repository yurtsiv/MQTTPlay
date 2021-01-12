package com.example.mqttplay.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mqttplay.R
import com.example.mqttplay.repo.RecurringTileTime
import com.example.mqttplay.repo.Tile
import com.example.mqttplay.repo.TileType

class RecurringTileFormViewModel : ViewModel() {
    // TODO: don't repeat the code (BrokerFormViewModel)
    companion object {
        val QOS_ID_TO_VALUE = hashMapOf(
            R.id.QOS_0 to 0,
            R.id.QOS_1 to 1,
            R.id.QOS_2 to 2
        )

        val QOS_VALUE_TO_ID = QOS_ID_TO_VALUE.entries.associate {(k, v) -> v to k}
    }

    private lateinit var brokerId: String;
    private var tileId: String? = null;

    // Form fields
    val topic = MutableLiveData<String>()
    val value = MutableLiveData<String>()
    val qualityOfServiceID = MutableLiveData<Int>().apply { value = R.id.QOS_0 }
    val retainMessage = MutableLiveData<Boolean>().apply { value = false }
    val hour = MutableLiveData<Int>().apply { value = 0 }
    val minute = MutableLiveData<Int>().apply { value = 0 }

    val timeStr = MediatorLiveData<String>().apply {
        addSource(hour) {
           value = formatTime()
        }

        addSource(minute) {
            value = formatTime()
        }
    }
    val saving = MutableLiveData<Boolean>().apply { value = false }

    private val valid = MediatorLiveData<Boolean>().apply {
        addSource(topic) {
            value = isFormValid()
        }
    }

    val saveBtnEnabled = MediatorLiveData<Boolean>().apply {
        value = false

        addSource(saving) {
            value = isSaveBtnEnabled();
        }

        addSource(valid) {
            value = isSaveBtnEnabled()
        }
    }

    private fun formatTime(): String {
        val hourStr = hour.value.toString().padStart(2, '0')
        val minuteStr = minute.value.toString().padStart(2, '0')
        return "$hourStr:$minuteStr"
    }

    private fun isSaveBtnEnabled(): Boolean {
        return !(saving.value ?: false) && (valid.value ?: false)
    }

    private fun isFormValid(): Boolean {
        return isTopicValid();
    }

    private fun isTopicValid(): Boolean {
        val value: String? = topic.value
        return value != null && value.isNotBlank()
    }

    fun constructTile(): Tile {
        return Tile(
            tileId,
            brokerId,
            topic.value as String,
            value.value,
            QOS_ID_TO_VALUE[qualityOfServiceID.value] as Int,
            retainMessage.value,
            TileType.RECURRING,
            RecurringTileTime(
                hour.value as Int,
                minute.value as Int
            )
        );
    }

    fun initForm(brokerId: String, tileId: String?) {
        this.brokerId = brokerId
    }
}