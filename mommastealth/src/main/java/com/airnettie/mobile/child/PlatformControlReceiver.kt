package com.airnettie.mobile.child

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PlatformControlReceiver : BroadcastReceiver() {
    private val TAG = "PlatformControlReceiver"

    private val platformPackageMap = mapOf(
        // Core apps
        "messenger" to "com.facebook.orca",
        "discord" to "com.discord",
        "roblox" to "com.roblox.client",
        // TikTok has multiple package names depending on region/version
        "tiktok" to "com.zhiliaoapp.musically",
        "tiktok_alt" to "com.ss.android.ugc.trill",

        // Extended platforms
        "snapchat" to "com.snapchat.android",
        "instagram" to "com.instagram.android",
        "youtube" to "com.google.android.youtube",
        "facebook" to "com.facebook.katana",
        "twitter" to "com.twitter.android"
    )

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.airnettie.mobile.PLATFORM_CONTROL") {
            val platform = intent.getStringExtra("platform")?.lowercase()
            val isEnabled = intent.getBooleanExtra("enabled", true)

            Log.d(TAG, "Received platform control update: $platform, enabled=$isEnabled")

            val packageName = platformPackageMap[platform]

            if (packageName != null) {
                val prefs = context.getSharedPreferences("blocked_apps", Context.MODE_PRIVATE)
                val editor = prefs.edit()

                val isBlocked = !isEnabled
                editor.putBoolean(packageName, isBlocked)
                editor.apply()

                Log.d(TAG, "Updated blocked status for $packageName to $isBlocked")
            } else {
                Log.w(TAG, "No package mapping found for platform key: $platform")
            }
        }
        // SafeScope VPN control is handled by ChildSyncService.
    }
}