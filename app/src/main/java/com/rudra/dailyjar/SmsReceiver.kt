package com.rudra.dailyjar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        if (i.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val body = Telephony.Sms.Intents.getMessagesFromIntent(i).joinToString("") { it.messageBody ?: "" }
        val r = Parser.parse(body) ?: return
        if (!Store.addIfNew(c, r.first, r.second, body)) return
        Notifier.show(c, 2, "Spent " + inr(r.first) + " at " + r.second, Notifier.summary(Store.calc(c)))
    }
}
