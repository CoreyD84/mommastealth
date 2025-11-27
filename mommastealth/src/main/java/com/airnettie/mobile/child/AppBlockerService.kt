package com.airnettie.mobile.child

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.airnettie.mobile.child.ui.BlockedAppActivity

class AppBlockerService : AccessibilityService() {

    private val TAG = "AppBlockerService"
    private lateinit var blockedAppsPrefs: SharedPreferences

    override fun onServiceConnected() {
        super.onServiceConnected()
        blockedAppsPrefs = getSharedPreferences("blocked_apps", MODE_PRIVATE)
        Log.d(TAG, "App Blocker service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.packageName != null) {
            val packageName = event.packageName.toString()
            val isBlocked = blockedAppsPrefs.getBoolean(packageName, false)

            if (isBlocked) {
                Log.d(TAG, "Blocked app opened: $packageName. Showing CLOSED overlay.")

                // Launch BlockedAppActivity overlay instead of returning to home
                val intent = Intent(this, BlockedAppActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "App Blocker service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "App Blocker service destroyed")
    }
}