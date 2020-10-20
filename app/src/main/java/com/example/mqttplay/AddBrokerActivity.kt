package com.example.mqttplay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.MaterialToolbar

class AddBrokerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_broker)

        setupToolbar()
        setupSaveBtn()
    }

    private fun setupSaveBtn() {
        val saveBtn = findViewById<Button>(R.id.saveBrokerBtn)
        saveBtn.setOnClickListener {
            Toast.makeText(this,"Submit", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.addBrokerToolbar)

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        supportActionBar?.title = "Add broker"  
    }
}