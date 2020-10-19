package com.example.mqttplay

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.mqttplay.adapter.BrokerItemAdapter
import com.example.mqttplay.data.DataSource
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupToolbar()
        setupBrokersList()
        setupAddBrokerBtn()
    }

    private fun setupBrokersList() {
        val dataSet = DataSource().loadBrokers()
        val recyclerView = findViewById<RecyclerView>(R.id.brokersRecyclerView)
        recyclerView.adapter = BrokerItemAdapter(this, dataSet)
        recyclerView.setHasFixedSize(true)
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