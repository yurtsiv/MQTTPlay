package com.example.mqttplay.view

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.mqttplay.R
import com.example.mqttplay.recurringMessages.MQTTMessagingService
import com.google.android.material.appbar.MaterialToolbar
import org.eclipse.paho.android.service.MqttService

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        findViewById<MaterialToolbar>(R.id.app_toolbar)
            .setupWithNavController(navController, appBarConfiguration)

        val intent = Intent(this, MQTTMessagingService::class.java)
        startForegroundService(intent)
    }
}