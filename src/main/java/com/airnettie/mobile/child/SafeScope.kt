package com.airnettie.mobile.child

import android.content.Context
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.content.Intent
import com.google.firebase.database.FirebaseDatabase

object SafeScope {

    private const val TAG = "SafeScopeChild"

    private val blockedDomains = listOf(
        "suicideforum.com", "sanctionedsuicide.com", "selfharmhub.net", "darkthoughts.org", "lostallhope.com",
        "pornhub.com", "xvideos.com", "xnxx.com", "redtube.com", "youjizz.com", "brazzers.com", "onlyfans.com",
        "fapello.com", "rule34.xxx", "xhamster.com", "spankbang.com", "tnaflix.com", "camwhores.tv",
        "leakgirls.com", "nudostar.com",
        "omegle.com", "chatroulette.com", "chathub.cam", "dirtyroulette.com"
    )

    fun activate(context: Context) {
        Log.i(TAG, "🟢 SafeScope activated on child side")
        startVpnService(context)
    }

    fun deactivate(context: Context) {
        Log.i(TAG, "🔴 SafeScope deactivated on child side")
        stopVpnService(context)
    }

    fun checkAndBlock(context: Context, url: String): Boolean {
        val matched = blockedDomains.find { domain ->
            url.contains(domain, ignoreCase = true)
        }

        if (matched != null) {
            val caseId = System.currentTimeMillis().toString()
            val severity = "critical"
            val message = "Blocked access to $matched"

            // ✅ Sync flag back to Firebase
            val prefs = context.getSharedPreferences("nettie_prefs", Context.MODE_PRIVATE)
            val childId = prefs.getString("child_id", "unknown_child")
            val guardianId = prefs.getString("guardian_id", "unknown_guardian")

            val flagRef = FirebaseDatabase.getInstance()
                .getReference("flags/$guardianId/$childId/$caseId")

            val flagData = mapOf(
                "severity" to severity,
                "message" to message,
                "url" to matched,
                "timestamp" to System.currentTimeMillis()
            )

            flagRef.setValue(flagData)

            Log.w(TAG, "❌ Blocked unsafe domain: $matched — synced to Firebase")
            return true
        }
        return false
    }

    // 🔧 WebView Interceptor
    fun attachToWebView(webView: WebView) {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                val blocked = checkAndBlock(view?.context!!, url)
                return blocked // if true, block navigation
            }
        }
    }

    // 🔧 VPN Service Hooks
    private fun startVpnService(context: Context) {
        context.startService(Intent(context, SafeScopeVpnService::class.java))
        Log.d(TAG, "SafeScope VPN service started")
    }

    private fun stopVpnService(context: Context) {
        context.stopService(Intent(context, SafeScopeVpnService::class.java))
        Log.d(TAG, "SafeScope VPN service stopped")
    }
}