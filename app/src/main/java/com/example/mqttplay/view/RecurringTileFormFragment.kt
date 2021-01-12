package com.example.mqttplay.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import com.example.mqttplay.R
import com.example.mqttplay.databinding.FragmentRecurringTileFormBinding
import com.example.mqttplay.repo.Tile
import com.example.mqttplay.viewmodel.RecurringTileFormViewModel
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecurringTileFormFragment : Fragment() {
    interface OnRecurringTileFormSaveListener {
        suspend fun onRecurringTileFormSave(tile: Tile)
    }

    lateinit var onSave: OnRecurringTileFormSaveListener
    lateinit var binding: FragmentRecurringTileFormBinding
    private val viewModel = RecurringTileFormViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recurring_tile_form, container, false)
        binding.liveData = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSaveBtn()
        setupSetTimeBtn()
    }

    fun initForm(brokerId: String, tileId: String?) {
        viewModel.initForm(brokerId, tileId)
    }

    private fun setupSaveBtn() {
        val saveBtn = view?.findViewById<Button>(R.id.save_tile_btn)
        saveBtn?.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    viewModel.saving.postValue(true)
                    onSave.onRecurringTileFormSave(viewModel.constructTile())
                } finally {
                    viewModel.saving.postValue(false)
                }
            }
        }
    }

    private fun setupSetTimeBtn() {
        val btn = view?.findViewById<TextView>(R.id.set_time_button)

        btn?.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(viewModel.hour.value as Int)
                .setMinute(viewModel.minute.value as Int)
                .setTitleText("When to send the message")
                .build()

            picker.show(childFragmentManager, "tag")

            picker.addOnPositiveButtonClickListener {
                viewModel.hour.postValue(picker.hour)
                viewModel.minute.postValue(picker.minute)
            }
        }
    }

    fun setOnRecurringTileFormSaveListener(listener: OnRecurringTileFormSaveListener) {
        onSave = listener;
    }
}