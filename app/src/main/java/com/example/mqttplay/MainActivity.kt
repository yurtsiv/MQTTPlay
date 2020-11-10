package com.example.mqttplay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mqttplay.adapter.BrokerItemAdapter
import com.example.mqttplay.viewmodel.BrokersListViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private val viewModel = BrokersListViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupToolbar()
        setupBrokersList()
        setupAddBrokerBtn()

    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.remove_broker_menu_item ->
                showToast("Not implemented")
            R.id.edit_broker_menu_item -> {
                val brokerId = viewModel.brokers.value?.get(item.groupId)?.id;
                Log.v("BrokerID", item.groupId.toString());
                if (brokerId != null) goToBrokerEdit(brokerId)
            }
        }
        return false;
    }

    private fun goToBrokerEdit(brokerId: String) {
        val intent = Intent(this, EditBrokerActivity::class.java)
        intent.putExtra("brokerId", brokerId);

        startActivity(intent)
    }

    private fun setupBrokersList() {
        viewModel.brokers.observe(this, {brokers ->
            val recyclerView = findViewById<RecyclerView>(R.id.brokersRecyclerView)
            recyclerView.adapter = BrokerItemAdapter(this, brokers)
            recyclerView.setHasFixedSize(true)
        })

        viewModel.loadBrokers()
    }

    private fun setupAddBrokerBtn() {
        val addBrokerBtn = findViewById<FloatingActionButton>( R.id.addBrokerBtn)
        addBrokerBtn.setOnClickListener {
            val intent = Intent(this, AddBrokerActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.mainActivityToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Brokers"
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}