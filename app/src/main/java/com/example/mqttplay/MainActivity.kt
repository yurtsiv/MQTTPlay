package com.example.mqttplay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var addBrokerBtn = findViewById<FloatingActionButton>( R.id.addBrokerBtn)
        addBrokerBtn.setOnClickListener {
            intent = Intent(this, AddBrokerActivity::class.java)
            startActivity(intent)
        }
    }
}