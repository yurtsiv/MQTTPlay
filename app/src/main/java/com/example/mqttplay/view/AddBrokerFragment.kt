package com.example.mqttplay.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mqttplay.R
import com.example.mqttplay.model.Broker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class AddBrokerFragment : Fragment(), BrokerFormFragment.OnBrokerFormSaveListener {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_broker, container, false);

        return view;
    }

    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is BrokerFormFragment) {
            fragment.setOnBrokerFormSaveListener(this)
        }
    }

    override suspend fun onBrokerFormSave(broker: Broker) {
        try {
            broker.save()
            withContext(Dispatchers.Main) {
                showToast(getString(R.string.broker_add_success))
                goToBrokersList()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                showToast(e.message ?: getString(R.string.generic_error))
            }
        }
    }

    private fun goToBrokersList() {
        val action = AddBrokerFragmentDirections.actionAddBrokerFragment2ToBrokersListFragment()
        findNavController().navigate(action)
    }

    private fun showToast(message: String) {
        Toast.makeText(view?.context, message, Toast.LENGTH_SHORT).show()
    }
}