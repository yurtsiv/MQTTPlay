package com.example.mqttplay.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mqttplay.R
import com.example.mqttplay.model.Broker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class EditBrokerFragment : Fragment(), BrokerFormFragment.OnBrokerFormSaveListener {
    private val args: EditBrokerFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_broker, container, false);

        return view;
    }

    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is BrokerFormFragment) {
            val brokerId = args.brokerId
            fragment.fillForm(brokerId)
            fragment.setOnBrokerFormSaveListener(this)
        }
    }

    override suspend fun onBrokerFormSave(broker: Broker) {
        try {
            broker.save()
            withContext(Dispatchers.Main) {
                showToast(getString(R.string.broker_save_success))
                goToBrokersList()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                showToast(e.message ?: getString(R.string.generic_error))
            }
        }
    }

    private fun goToBrokersList() {
        val action = EditBrokerFragmentDirections.actionEditBrokerFragment2ToBrokersListFragment()
        findNavController().navigate(action)
    }

    private fun showToast(message: String) {
        Toast.makeText(view?.context, message, Toast.LENGTH_SHORT).show()
    }
}