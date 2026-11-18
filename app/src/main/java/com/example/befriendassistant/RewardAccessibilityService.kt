package com.example.befriendassistant

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.os.SystemClock
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class RewardAccessibilityService : AccessibilityService() {
    companion object {
        const val PREFS = "reward_assistant"
        const val KEY_ENABLED = "enabled"
        const val KEY_LOG = "activity_log"
        const val TARGET_PACKAGE = "com.guppir.swipr"
        @Volatile var instance: RewardAccessibilityService? = null
    }

    private val prefs by lazy { getSharedPreferences(PREFS, MODE_PRIVATE) }
    private var windowManager: WindowManager? = null
    private var panel: View? = null
    private var lastActionAt = 0L
    private var lastActionKey = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showPanel()
        addLog("Accessibility service connected.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !prefs.getBoolean(KEY_ENABLED, false)) return
        val root = rootInActiveWindow ?: return
        val pkg = root.packageName?.toString() ?: ""
        val now = SystemClock.elapsedRealtime()
        if (now - lastActionAt < 1800) return

        val adContext = containsAdMarker(root)
        if (pkg == TARGET_PACKAGE) {
            val claim = findClickable(root) { label, id ->
                val s = label.lowercase()
                (s == "claim" || s == "collect" || s.contains("claim reward") ||
                    s.contains("collect reward") || s.contains("collect gems") ||
                    s.contains("claim gems")) || (id.contains("claim") && (s.contains("reward") || s.contains("gem")))
            }
            if (claim != null) {
                val key = "claim:${claim.first}"
                if (key != lastActionKey && clickNode(claim.second)) {
                    lastActionAt = now; lastActionKey = key
                    addLog("Tapped Befriend reward control: ${claim.first}")
                }
            } else if (adContext) {
                tapAdClose(root, now)
            }
        } else if (adContext) {
            // Do not tap generic close buttons in unrelated apps. Require ad/sponsored context.
            tapAdClose(root, now)
        }
        root.recycle()
    }


    private fun containsAdMarker(root: AccessibilityNodeInfo): Boolean {
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.add(root)
        var visited = 0
        while (stack.isNotEmpty() && visited < 300) {
            val node = stack.removeLast()
            visited++
            val text = (node.text?.toString().orEmpty() + " " + node.contentDescription?.toString().orEmpty()).lowercase()
            if (text == "ad" || text.contains("advertisement") || text.contains("sponsored") || text.contains("rewarded ad")) {
                if (node !== root) node.recycle()
                stack.forEach { it.recycle() }
                return true
            }
            for (i in 0 until node.childCount) node.getChild(i)?.let { stack.add(it) }
            if (node !== root) node.recycle()
        }
        return false
    }

    private fun tapAdClose(root: AccessibilityNodeInfo, now: Long) {
        val close = findClickable(root) { label, id ->
            val s = label.trim().lowercase()
            s == "close" || s == "dismiss" || s == "close ad" ||
                s == "close advertisement" || (id.contains("close") && s.isNotBlank())
        }
        if (close != null) {
            val key = "close:${close.first}"
            if (key != lastActionKey && clickNode(close.second)) {
                lastActionAt = now; lastActionKey = key
                addLog("Tapped available ad close control: ${close.first}")
            }
        }
    }

    private fun findClickable(root: AccessibilityNodeInfo, match: (String, String) -> Boolean): Pair<String, AccessibilityNodeInfo>? {
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.add(root)
        var visited = 0
        while (stack.isNotEmpty() && visited < 450) {
            val node = stack.removeLast()
            visited++
            val label = listOfNotNull(node.text?.toString(), node.contentDescription?.toString()).joinToString(" ").trim()
            val id = node.viewIdResourceName.orEmpty().lowercase()
            if (node.isVisibleToUser && node.isEnabled && match(label, id)) {
                var clickable: AccessibilityNodeInfo? = node
                while (clickable != null && !clickable.isClickable) clickable = clickable.parent
                if (clickable != null) {
                    val result = clickable to label.ifBlank { id }
                    stack.forEach { it.recycle() }
                    return result
                }
            }
            for (i in 0 until node.childCount) node.getChild(i)?.let { stack.add(it) }
            if (node !== root) node.recycle()
        }
        return null
    }

    private fun clickNode(node: AccessibilityNodeInfo): Boolean {
        val ok = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        node.recycle()
        return ok
    }

    private fun showPanel() {
        if (panel != null) return
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
            setBackgroundColor(0xEE20242A.toInt())
        }
        box.addView(TextView(this).apply { text = "Befriend Assistant"; setTextColor(-1); textSize = 14f })
        val status = TextView(this).apply { text = "Ready"; setTextColor(-1); textSize = 12f }
        box.addView(status)
        box.addView(Button(this).apply {
            text = "Start / Stop"
            setOnClickListener {
                val next = !prefs.getBoolean(KEY_ENABLED, false)
                prefs.edit().putBoolean(KEY_ENABLED, next).apply()
                status.text = if (next) "Automation ON" else "Automation OFF"
                addLog(if (next) "Automation started from floating panel." else "Automation stopped from floating panel.")
            }
        })
        box.addView(Button(this).apply {
            text = "Hide panel"
            setOnClickListener { hidePanel() }
        })
        panel = box
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.END; x = 12; y = 140 }
        try { windowManager?.addView(box, params) } catch (_: Exception) { panel = null }
    }

    private fun hidePanel() {
        try { panel?.let { windowManager?.removeView(it) } } catch (_: Exception) {}
        panel = null
    }

    private fun addLog(message: String) {
        val old = prefs.getString(KEY_LOG, "").orEmpty()
        val lines = (listOf("${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}  $message") + old.lines())
            .filter { it.isNotBlank() }.take(35)
        prefs.edit().putString(KEY_LOG, lines.joinToString("\n")).apply()
    }

    override fun onInterrupt() { addLog("Accessibility service interrupted.") }
    override fun onDestroy() {
        hidePanel()
        instance = null
        addLog("Accessibility service disconnected.")
        super.onDestroy()
    }
}
