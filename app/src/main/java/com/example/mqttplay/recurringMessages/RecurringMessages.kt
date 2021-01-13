package com.example.mqttplay.recurringMessages

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.mqttplay.repo.RecurringTileTime

class RecurringMessages {
    companion object {
        @RequiresApi(Build.VERSION_CODES.M)
        fun scheduleMessage(context: Context, tileId: String, time: RecurringTileTime) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, RecurringMessageBroadcastReceiver::class.java)
            intent.action = "${RecurringMessageBroadcastReceiver.intentActionStartsWith}_${tileId}"
            intent.putExtra("tileId", tileId)

            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

            val ALARM_DELAY_IN_SECOND = 3
            val alarmTimeAtUTC = System.currentTimeMillis() + ALARM_DELAY_IN_SECOND * 1_000L

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeAtUTC, pendingIntent)
        }
    }
}