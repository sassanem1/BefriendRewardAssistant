package com.example.befriendassistant

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences(RewardAccessibilityService.PREFS, Context.MODE_PRIVATE) }
    private lateinit var state: TextView
    private lateinit var log: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 28, 24, 24)
        }
        root.addView(TextView(this).apply {
            text = "Befriend Reward Assistant"
            textSize = 23f
        })
        root.addView(TextView(this).apply {
            text = "Waits for an ad's available close control, then looks for Befriend's reward-claim control. It does not skip or fake ad views."
            textSize = 15f
            setPadding(0, 12, 0, 16)
        })
        state = TextView(this).apply { textSize = 16f; setPadding(0, 8, 0, 12) }
        root.addView(state)
        root.addView(Button(this).apply {
            text = "1. Enable Accessibility Service"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })
        root.addView(Button(this).apply {
            text = "2. Start automation"
            setOnClickListener { prefs.edit().putBoolean(RewardAccessibilityService.KEY_ENABLED, true).apply(); refresh() }
        })
        root.addView(Button(this).apply {
            text = "Stop automation"
            setOnClickListener { prefs.edit().putBoolean(RewardAccessibilityService.KEY_ENABLED, false).apply(); refresh() }
        })
        root.addView(Button(this).apply {
            text = "Refresh activity log"
            setOnClickListener { refresh() }
        })
        root.addView(TextView(this).apply {
            text = "Activity log"
            textSize = 18f
            setPadding(0, 20, 0, 8)
        })
        log = TextView(this).apply { textSize = 13f; text = "No activity yet." }
        val scroll = ScrollView(this).apply { addView(log) }
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
        refresh()
    }

    override fun onResume() { super.onResume(); if (::state.isInitialized) refresh() }

    private fun refresh() {
        val enabled = prefs.getBoolean(RewardAccessibilityService.KEY_ENABLED, false)
        state.text = "Automation: ${if (enabled) "ON" else "OFF"}\nAccessibility service: ${if (RewardAccessibilityService.instance != null) "connected" else "not connected"}"
        log.text = prefs.getString(RewardAccessibilityService.KEY_LOG, "No activity yet.")
    }
}
