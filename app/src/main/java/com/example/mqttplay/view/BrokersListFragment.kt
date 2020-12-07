package com.example.mqttplay.view

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.mqttplay.R
import com.example.mqttplay.adapter.BrokerItemAdapter
import com.example.mqttplay.databinding.FragmentBrokersListBinding
import com.example.mqttplay.model.Broker
import com.example.mqttplay.viewmodel.BrokersListViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BrokersListFragment : Fragment() {
    lateinit var binding: FragmentBrokersListBinding
    private val viewModel = BrokersListViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_brokers_list, container, false)
        binding.liveData = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBrokersList()
        setupAddBrokerBtn()

        viewModel.toast.observe(viewLifecycleOwner) {
            showToast(it)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.remove_broker_menu_item -> {
                val broker = viewModel.brokers.value?.get(item.groupId)

                if (broker != null) {
                    confirmBrokerRemove(broker)
                }
            }
            R.id.edit_broker_menu_item -> {
                val brokerId = viewModel.brokers.value?.get(item.groupId)?.id
                if (brokerId != null) goToBrokerEdit(brokerId)
            }
        }
        return false;
    }

    private fun confirmBrokerRemove(broker: Broker) {
        val mBuilder = AlertDialog.Builder(view?.context)
        mBuilder.setMessage(getString(R.string.broker_remove_confirm, broker.label))
        mBuilder.setPositiveButton(R.string.yes) { dialog, which ->
            viewModel.removeBroker(broker)
        }
        mBuilder.setNegativeButton(R.string.cancel) { dialog, which -> }
        mBuilder.show()
    }

    private fun goToBrokerEdit(brokerId: String) {
        val action =
            BrokersListFragmentDirections.actionBrokersListFragmentToEditBrokerFragment2(brokerId)

        findNavController().navigate(action)
    }

    private fun onItemClick(broker: Broker) {
        if (broker.id != null) {
            val action =
                BrokersListFragmentDirections.actionBrokersListFragmentToViewBrokerFragment(broker.id, broker.label)
            findNavController().navigate(action)
        }
    }

    private fun setupBrokersList() {
        if (context == null) return;

        viewModel.brokers.observe(viewLifecycleOwner, { brokers ->
            val recyclerView = view?.findViewById<RecyclerView>(R.id.brokersRecyclerView)
            val adapter = BrokerItemAdapter(context as Context, brokers) {
                onItemClick(it)
            }
            recyclerView?.adapter = adapter
            recyclerView?.setHasFixedSize(true)
        })

        viewModel.loadBrokers()
    }

    private fun setupAddBrokerBtn() {
        val addBrokerBtn = view?.findViewById<FloatingActionButton>(R.id.addBrokerBtn);
        addBrokerBtn?.setOnClickListener {
            val action =
                BrokersListFragmentDirections.actionBrokersListFragmentToAddBrokerFragment2();
            findNavController().navigate(action)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(view?.context, message, Toast.LENGTH_SHORT).show()
    }
}