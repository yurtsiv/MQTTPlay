package com.example.mqttplay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.mqttplay.databinding.FragmentEditBrokerBinding
import com.example.mqttplay.model.Broker
import com.example.mqttplay.viewmodel.AddBrokerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditBrokerFragment : Fragment() {
    lateinit var onSave: OnBrokerFormSaveListener
    lateinit var binding: FragmentEditBrokerBinding
    private val viewModel = AddBrokerViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_broker, container, false)
        binding.liveData = viewModel
        binding.lifecycleOwner = this

        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSaveBtn()
    }

    fun setOnBrokerFormSaveListener(callback: OnBrokerFormSaveListener) {
        this.onSave = callback
    }

    interface OnBrokerFormSaveListener {
        suspend fun onBrokerFormSave(broker: Broker)
    }

    private fun setupSaveBtn() {
        val saveBtn = view?.findViewById<Button>(R.id.saveBrokerBtn)
        saveBtn?.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    viewModel.saving.postValue(true)
                    onSave.onBrokerFormSave(viewModel.constructBroker())
                } finally {
                    viewModel.saving.postValue(false)
                }
            }
        }
    }
}