package com.example.mqttplay.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.mqttplay.R
import com.example.mqttplay.databinding.FragmentBrokerFormBinding
import com.example.mqttplay.model.Broker
import com.example.mqttplay.viewmodel.BrokerFormViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BrokerFormFragment : Fragment() {
    lateinit var onSave: OnBrokerFormSaveListener
    lateinit var binding: FragmentBrokerFormBinding
    private val viewModel = BrokerFormViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_broker_form, container, false)
        binding.liveData = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSaveBtn()

        viewModel.toast.observe(viewLifecycleOwner) {
            showToast(it)
        }
    }

    fun fillForm(brokerId: String) {
        viewModel.fillForm(brokerId)
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

    private fun showToast(message: String) {
        Toast.makeText(view?.context, message, Toast.LENGTH_SHORT).show()
    }
}