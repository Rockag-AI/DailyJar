package com.rudra.dailyjar

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object Notifier {
    fun show(c: Context, id: Int, title: String, text: String) {
        val nm = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel("jar", "Daily Jar", NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(c, 0, Intent(c, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        nm.notify(id, Notification.Builder(c, "jar")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title).setContentText(text)
            .setContentIntent(open).setAutoCancel(true).build())
    }

    fun summary(x: Calc): String =
        if (x.left >= 0) inr(x.left) + " left today. Jar: " + (if (x.jar < 0) "-" else "") + inr(x.jar)
        else inr(x.left) + " over today's limit. The extra comes from your savings jar."
}
