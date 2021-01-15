package com.example.mqttplay.view

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mqttplay.R
import com.example.mqttplay.databinding.FragmentRecurringTileFormBinding
import com.example.mqttplay.recurringMessages.RecurringMessages
import com.example.mqttplay.repo.RecurringTileTime
import com.example.mqttplay.repo.TileRepo
import com.example.mqttplay.viewmodel.RecurringTileFormViewModel
import com.example.mqttplay.viewmodel.TileFormCommonFieldsViewModel
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecurringTileFormFragment : Fragment() {
    private val args: RecurringTileFormFragmentArgs by navArgs()
    lateinit var binding: FragmentRecurringTileFormBinding
    private val viewModel: RecurringTileFormViewModel by viewModels()
    private val commonFieldsViewModel: TileFormCommonFieldsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_recurring_tile_form,
            container,
            false
        )
        binding.liveData = viewModel
        binding.lifecycleOwner = this


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.initForm(
            args.brokerId,
            args.tileId
        )

        if (args.tileId != null) {
            fillForm(args.tileId as String)
        }

        viewModel.saving.observe(viewLifecycleOwner) {
            viewModel.saveBtnEnabled.value = isSaveBtnEnabled()
        }
        commonFieldsViewModel.valid.observe(viewLifecycleOwner) {
            viewModel.saveBtnEnabled.value = isSaveBtnEnabled()
        }

        setupSaveBtn()
        setupSetTimeBtn()
    }

    private fun isSaveBtnEnabled(): Boolean {
        return !(viewModel.saving.value ?: false) && (commonFieldsViewModel.valid.value ?: false)
    }

    private fun setupSaveBtn() {
        val saveBtn = view?.findViewById<Button>(R.id.save_recurring_tile_btn)
        saveBtn?.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    viewModel.saving.postValue(true)
                    val tile = viewModel.formDataToTile(commonFieldsViewModel)
                    val tileId = TileRepo.save(tile)
                    RecurringMessages.scheduleMessage(
                        context as Context,
                        tileId,
                        tile.recurringTime as RecurringTileTime
                    )

                    withContext(Dispatchers.Main) {
                        showToast(getString(R.string.tile_save_success))
                        goToBrokerView()
                    }
                } catch (e: Exception) {
                    viewModel.saving.postValue(false)
                    withContext(Dispatchers.Main) {
                        // TODO: more specific errors
                        showToast(e.message ?: getString(R.string.generic_error))
                    }
                }
            }
        }
    }

    private fun fillForm(tileId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tile = TileRepo.fetchSingle(tileId)
                commonFieldsViewModel.populateFields(tile)
                viewModel.hour.postValue(tile.recurringTime?.hour)
                viewModel.minute.postValue(tile.recurringTime?.minute)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast(e.message ?: getString(R.string.generic_error))
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

    private fun goToBrokerView() {
        val action =
            RecurringTileFormFragmentDirections.actionRecurringTileFormFragmentToViewBrokerFragment(
                args.brokerId,
                args.brokerLabel
            )
        findNavController().navigate(action)
    }

    private fun showToast(message: String) {
        Toast.makeText(view?.context, message, Toast.LENGTH_SHORT).show()
    }
}