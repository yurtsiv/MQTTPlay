package com.example.mqttplay.recurringMessages

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.mqttplay.repo.RecurringTileTime
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class RecurringMessages {
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun calcNextMessageTime(time: RecurringTileTime): Long {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)

            val hourDiff = time.hour - currentHour
            val minuteDiff = time.minute - currentMinute

            val nextDay = hourDiff < 0 || (hourDiff == 0 && minuteDiff <= 0)

            var nextMessageDate = LocalDate.now().atTime(time.hour, time.minute)
            if (nextDay) {
                nextMessageDate = nextMessageDate.plusDays(1)
            }

            return nextMessageDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun scheduleMessage(context: Context, tileId: String, time: RecurringTileTime) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, RecurringMessageBroadcastReceiver::class.java)
            intent.action =
                "${RecurringMessageBroadcastReceiver.SEND_MESSAGE_INTENT_ACTION}_${tileId}_${System.currentTimeMillis()}"
            intent.putExtra("tileId", tileId)

            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)


            val alarmTimeAtUTC = calcNextMessageTime(time)

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTimeAtUTC,
                pendingIntent
            )
        }
    }
}