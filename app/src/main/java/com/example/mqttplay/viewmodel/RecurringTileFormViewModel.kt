package com.example.mqttplay.viewmodel

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mqttplay.R
import com.example.mqttplay.repo.RecurringTileTime
import com.example.mqttplay.repo.Tile
import com.example.mqttplay.repo.TileType
import java.lang.Exception

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
    private var tileId: String? = null

    // Form fields
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
    val saving = MediatorLiveData<Boolean>().apply { value = false }

    val saveBtnEnabled = MutableLiveData<Boolean>().apply {  }

    private fun formatTime(): String {
        val hourStr = hour.value.toString().padStart(2, '0')
        val minuteStr = minute.value.toString().padStart(2, '0')
        return "$hourStr:$minuteStr"
    }

    fun formDataToTile(commonFieldsViewModel: TileFormCommonFieldsViewModel): Tile {
        return Tile(
            tileId,
            brokerId,
            commonFieldsViewModel.topic.value as String,
            commonFieldsViewModel.value.value,
            QOS_ID_TO_VALUE[commonFieldsViewModel.qualityOfServiceID.value] as Int,
            commonFieldsViewModel.retainMessage.value,
            TileType.RECURRING,
            RecurringTileTime(
                hour.value as Int,
                minute.value as Int
            )
        );
    }

    fun initForm(brokerId: String, tileId: String?) {
        this.brokerId = brokerId
        this.tileId = tileId
    }
}