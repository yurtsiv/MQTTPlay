package com.example.mqttplay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.example.mqttplay.adapter.BrokerItemAdapter
import com.example.mqttplay.data.DataSource
import com.example.mqttplay.model.Broker
import com.example.mqttplay.viewmodel.BrokersListViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private val viewModel = BrokersListViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupToolbar()
        setupBrokersList()
        setupAddBrokerBtn()

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
            intent = Intent(this, AddBrokerActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.mainActivityToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Brokers"
    }
}