package com.rudra.dailyjar

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private val ink = 0xFF14262B.toInt()
    private val mute = 0xFF5F7377.toInt()
    private val teal = 0xFF0F766E.toInt()
    private val amber = 0xFFB45309.toInt()

    private lateinit var greet: TextView
    private lateinit var left: TextView
    private lateinit var sub: TextView
    private lateinit var jar: TextView
    private lateinit var status: TextView
    private lateinit var list: LinearLayout
    private lateinit var amt: EditText
    private lateinit var note: EditText
    private lateinit var modeSw: Switch
    private lateinit var salary: EditText
    private lateinit var fixed: EditText
    private lateinit var budget: EditText
    private lateinit var opening: EditText
    private lateinit var earnBox: LinearLayout
    private lateinit var budBox: LinearLayout

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun tv(t: String, sp: Float = 16f, bold: Boolean = false, color: Int = ink) = TextView(this).apply {
        text = t; textSize = sp; setTextColor(color)
        if (bold) setTypeface(null, Typeface.BOLD)
    }
    private fun field(h: String, numeric: Boolean = true) = EditText(this).apply {
        hint = h; setSingleLine()
        inputType = if (numeric) InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL else InputType.TYPE_CLASS_TEXT
    }
    private fun EditText.num(): Long = text.toString().toDoubleOrNull()?.toLong() ?: 0L
    private fun gap(v: Int) = View(this).apply { layoutParams = LinearLayout.LayoutParams(1, dp(v)) }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val sv = ScrollView(this)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(48), dp(20), dp(40)) }
        sv.addView(root); setContentView(sv)

        greet = tv("", 22f, true); root.addView(greet); root.addView(gap(16))
        left = tv("", 52f, true); root.addView(left)
        sub = tv("", 15f, false, mute); root.addView(sub); root.addView(gap(10))
        jar = tv("", 18f, true, teal); root.addView(jar)
        status = tv("", 13f, false, mute); root.addView(status)

        root.addView(gap(20)); root.addView(tv("Add an expense", 18f, true))
        amt = field("Amount in ₹"); note = field("What for?", false)
        root.addView(amt); root.addView(note)
        root.addView(Button(this).apply {
            text = "Add expense"
            setOnClickListener {
                val a = amt.text.toString().toDoubleOrNull()
                if (a == null || a <= 0) { Toast.makeText(context, "Enter an amount above zero", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                Store.add(context, a, note.text.toString().trim().ifEmpty { "Expense" })
                amt.setText(""); note.setText(""); render()
            }
        })

        root.addView(gap(16)); root.addView(tv("Today's spending", 18f, true))
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }; root.addView(list)

        root.addView(gap(24)); root.addView(tv("Settings", 18f, true))
        modeSw = Switch(this).apply { text = "I'm not earning (use a monthly budget)"; setPadding(0, dp(8), 0, dp(8)) }
        root.addView(modeSw)
        salary = field("Monthly salary (₹)"); fixed = field("Fixed bills: rent, EMIs (₹)")
        earnBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(salary); addView(fixed) }
        budget = field("Total to spend this month (₹)")
        budBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(budget) }
        opening = field("Savings already in the jar (₹)")
        root.addView(earnBox); root.addView(budBox); root.addView(opening)
        modeSw.setOnCheckedChangeListener { _, on ->
            earnBox.visibility = if (on) View.GONE else View.VISIBLE
            budBox.visibility = if (on) View.VISIBLE else View.GONE
        }
        val p = Store.prefs(this)
        modeSw.isChecked = p.getString("mode", "earning") == "budget"
        salary.setText(p.getLong("salary", 100000).toString()); fixed.setText(p.getLong("fixed", 0).toString())
        budget.setText(p.getLong("budget", 30000).toString()); opening.setText(p.getLong("opening", 0).toString())
        earnBox.visibility = if (modeSw.isChecked) View.GONE else View.VISIBLE
        budBox.visibility = if (modeSw.isChecked) View.VISIBLE else View.GONE
        root.addView(Button(this).apply {
            text = "Save settings"
            setOnClickListener {
                Store.setSettings(context, if (modeSw.isChecked) "budget" else "earning",
                    salary.num(), fixed.num(), budget.num(), opening.num())
                render(); Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            }
        })
        root.addView(Button(this).apply {
            text = "Clear all expenses"
            setOnClickListener { Store.clear(context); render() }
        })

        val need = ArrayList<String>()
        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) need.add(Manifest.permission.RECEIVE_SMS)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            need.add(Manifest.permission.POST_NOTIFICATIONS)
        if (need.isNotEmpty()) requestPermissions(need.toTypedArray(), 1)
    }

    override fun onResume() { super.onResume(); Scheduler.schedule(this); render() }
    override fun onRequestPermissionsResult(r: Int, p: Array<out String>, g: IntArray) { render() }

    private fun render() {
        val c = Store.calc(this)
        val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val g = if (h < 12) "Good morning" else if (h < 17) "Good afternoon" else "Good evening"
        greet.text = g + ". Today's limit is " + inr(c.limit.toDouble())
        left.text = inr(c.left); left.setTextColor(if (c.left < 0) amber else ink)
        sub.text = if (c.left < 0) "over today's limit. Savings pay the difference." else "left today, out of " + inr(c.limit.toDouble())
        jar.text = "Savings jar: " + (if (c.jar < 0) "-" else "") + inr(c.jar) + (if (c.jar < 0) " (in debt)" else "")
        jar.setTextColor(if (c.jar < 0) amber else teal)
        val sms = checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        status.text = if (sms) "Bank SMS tracking is on. Debits update this screen automatically."
        else "SMS tracking is off. Allow SMS permission in the app's settings to track payments automatically."
        list.removeAllViews()
        val today = Store.all(this).filter { it.date == Store.today() }.reversed()
        if (today.isEmpty()) list.addView(tv("Nothing spent yet today.", 15f, false, mute))
        today.forEach { e ->
            val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(4), 0, dp(4)) }
            row.addView(tv(e.note, 16f).apply { layoutParams = LinearLayout.LayoutParams(0, -2, 1f) })
            row.addView(tv("-" + inr(e.amt), 16f, true))
            row.addView(Button(this).apply { text = "Remove"; textSize = 11f; setOnClickListener { Store.remove(context, e.id); render() } })
            list.addView(row)
        }
    }
}
