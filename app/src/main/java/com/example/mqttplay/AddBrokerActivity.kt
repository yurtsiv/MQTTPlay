package com.example.mqttplay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.mqttplay.databinding.ActivityAddBrokerBinding
import com.example.mqttplay.viewmodel.AddBrokerViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class AddBrokerActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddBrokerBinding
    private val viewModel = AddBrokerViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_broker)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_add_broker
        );
        binding.liveData = viewModel
        binding.lifecycleOwner = this

        setupToolbar()
        setupSaveBtn()
    }

    private fun setupSaveBtn() {
        val saveBtn = findViewById<Button>(R.id.saveBrokerBtn)
        saveBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    viewModel.save()

                    withContext(Dispatchers.Main) {
                        showToast("Broker added")
                        goToBrokersList()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        showToast(e.message ?: "An error occurred")
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.addBrokerToolbar)

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            goToBrokersList()
        }
        supportActionBar?.title = "Add broker"
    }

    private fun goToBrokersList() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}