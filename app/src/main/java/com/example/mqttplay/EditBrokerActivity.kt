package com.example.mqttplay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mqttplay.model.Broker
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class EditBrokerActivity : AppCompatActivity(), EditBrokerFragment.OnBrokerFormSaveListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_broker)

        setupToolbar()
    }

    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is EditBrokerFragment) {
            val brokerId = intent.extras?.get("brokerId") as String;
            fragment.fillForm(brokerId)
            fragment.setOnBrokerFormSaveListener(this)
        }
    }

    override suspend fun onBrokerFormSave(broker: Broker) {
        try {
            broker.save()
            withContext(Dispatchers.Main) {
                showToast("Broker saved")
                goToBrokersList()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                showToast(e.message ?: "An error occurred")
            }
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.editBrokerToolbar)

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            goToBrokersList()
        }

        supportActionBar?.title = "Edit broker"
    }

    private fun goToBrokersList() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}