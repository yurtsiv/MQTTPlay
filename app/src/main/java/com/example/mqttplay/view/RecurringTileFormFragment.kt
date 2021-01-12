package com.example.mqttplay.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import com.example.mqttplay.R
import com.example.mqttplay.databinding.FragmentRecurringTileFormBinding
import com.example.mqttplay.repo.Tile
import com.example.mqttplay.viewmodel.RecurringTileFormViewModel
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

    fun setOnRecurringTileFormSaveListener(listener: OnRecurringTileFormSaveListener) {
        onSave = listener;
    }
}