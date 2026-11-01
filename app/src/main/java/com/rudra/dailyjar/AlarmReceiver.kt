package com.rudra.dailyjar

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Calendar

object Scheduler {
    // Morning message at 7:00. The alarm re-arms itself each time it fires.
    fun schedule(c: Context) {
        val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 7); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_MONTH, 1)
        }
        val pi = PendingIntent.getBroadcast(c, 1,
            Intent(c, AlarmReceiver::class.java).setAction("MORNING"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        when (i.action) {
            "MORNING" -> {
                val x = Store.calc(c)
                Notifier.show(c, 1, "Good morning. Today's limit is " + inr(x.limit.toDouble()),
                    "Savings jar: " + (if (x.jar < 0) "-" else "") + inr(x.jar))
                Scheduler.schedule(c)
            }
            Intent.ACTION_BOOT_COMPLETED -> Scheduler.schedule(c)
        }
    }
}
