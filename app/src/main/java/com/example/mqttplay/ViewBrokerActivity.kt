package com.example.mqttplay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.mqttplay.databinding.ActivityViewBrokerBinding
import com.example.mqttplay.viewmodel.ViewBrokerViewModel
import com.google.android.material.appbar.MaterialToolbar

class ViewBrokerActivity : AppCompatActivity() {
    lateinit var binding: ActivityViewBrokerBinding;
    private val viewModel = ViewBrokerViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_broker)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_broker)
        binding.liveData = viewModel
        binding.lifecycleOwner = this

        setupToolbar();

        val brokerId = intent.extras?.get("brokerId") as String
        viewModel.initialize(brokerId)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.activity_view__broker_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
    }
}