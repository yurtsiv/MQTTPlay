package com.example.mqttplay.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mqttplay.R
import com.example.mqttplay.repo.Tile
import com.example.mqttplay.repo.TileRepo
import com.example.mqttplay.repo.TileType

class ButtonTileFormViewModel: ViewModel() {
    // TODO: don't repeat the code (BrokerFormViewModel)
    companion object {
        val QOS_ID_TO_VALUE = hashMapOf(
            R.id.QOS_0 to 0,
            R.id.QOS_1 to 1,
            R.id.QOS_2 to 2
        )
    }

    private lateinit var brokerId: String;
    private var tileId: String? = null

    val saving = MediatorLiveData<Boolean>().apply { value = false }
    val saveBtnEnabled = MutableLiveData<Boolean>().apply {  }

    fun formDataToTile(commonFieldsViewModel: TileFormCommonFieldsViewModel): Tile {
        return Tile(
            tileId,
            brokerId,
            commonFieldsViewModel.topic.value as String,
            commonFieldsViewModel.value.value,
            RecurringTileFormViewModel.QOS_ID_TO_VALUE[commonFieldsViewModel.qualityOfServiceID.value] as Int,
            commonFieldsViewModel.retainMessage.value,
            TileType.BUTTON,
        )
    }

    fun initForm(brokerId: String, tileId: String?) {
        this.brokerId = brokerId
        this.tileId = tileId
    }
}