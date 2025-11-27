package com.airnettie.mobile.child.features

import android.content.Context
import android.util.Log
import com.airnettie.mobile.child.modules.FirebaseSync
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirebaseLogger {

    private const val TAG = "FirebaseLogger"

    // 🔍 Logs emotional detections (phrases, emojis, SMS, etc.)
    fun logDetection(
        context: Context,
        severity: String,
        message: String,
        matchedPhrases: List<String>,
        category: String? = null,
        source: String = "phrase",       // "phrase", "emoji", "sms", "chat"
        sourceApp: String? = null,       // e.g. "Messenger", "Discord"
        isEscalated: Boolean = false     // freeze reflex or alert triggered
    ) {
        val householdId = getHouseholdId(context)
        val guardianId = getGuardianId(context)
        val childId = getChildId(context)
        val timestamp = getTimestamp()

        if (householdId.isNullOrEmpty() || guardianId.isNullOrEmpty() || childId.isNullOrEmpty()) {
            Log.e(TAG, "❌ Missing householdId/guardianId/childId — cannot log detection.")
            Log.e(TAG, "Message: \"$message\" | Severity: $severity | Source: $source")
            return
        }

        val basePath = if (source == "emoji") "emojis" else "detections"
        val detectionRef = FirebaseDatabase.getInstance()
            .getReference("feelscope/$basePath/$guardianId/$childId")
            .push()

        val payload = mutableMapOf(
            "message" to message,
            "matchedPhrases" to matchedPhrases,
            "severity" to severity,
            "timestamp" to timestamp,
            "source" to source,
            "isEscalated" to isEscalated,
            "package" to context.packageName,
            "householdId" to householdId
        ).apply {
            category?.let { this["category"] = it }
            sourceApp?.let { this["sourceApp"] = it }
        }

        Log.d(TAG, "🧠 Logging $source detection payload: $payload")

        detectionRef.setValue(payload)
            .addOnSuccessListener {
                Log.i(TAG, "✅ Detection logged to $basePath: $severity | ${matchedPhrases.size} matches")
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "❌ Failed to log detection: ${error.localizedMessage}")
            }

        // 🚨 Auto-escalation for critical severity
        if (severity.equals("critical", ignoreCase = true)) {
            val caseId = System.currentTimeMillis().toString()
            FirebaseSync.syncFlag(caseId, severity, message, guardianId, childId)
            Log.w(TAG, "🚨 Critical detection escalated to FirebaseSync: $caseId")
        }
    }

    // 🆕 Convenience wrapper for critical detections
    fun logCritical(
        context: Context,
        message: String,
        matchedPhrases: List<String>,
        sourceApp: String? = null,
        category: String? = null
    ) {
        logDetection(
            context = context,
            severity = "critical",
            message = message,
            matchedPhrases = matchedPhrases,
            category = category,
            sourceApp = sourceApp,
            isEscalated = true
        )
    }

    // 🧠 Logs general events under /logs/{guardianId}/{childId}
    fun logEvent(guardianId: String, childId: String, type: String, message: String) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("logs/$guardianId/$childId")
            .push()

        val payload = mapOf(
            "type" to type,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )

        ref.setValue(payload)
            .addOnSuccessListener {
                Log.i(TAG, "✅ Logged event: [$type] $message")
            }
            .addOnFailureListener {
                Log.e(TAG, "❌ Failed to log event: ${it.localizedMessage}")
            }
    }

    // 🔐 Logs system-level events using current Firebase user
    fun logSystem(type: String, message: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance()
            .getReference("logs/system/$uid")
            .push()

        val payload = mapOf(
            "type" to type,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )

        ref.setValue(payload)
            .addOnSuccessListener {
                Log.i(TAG, "✅ Logged system event: [$type] $message")
            }
            .addOnFailureListener {
                Log.e(TAG, "❌ Failed to log system event: ${it.localizedMessage}")
            }
    }

    private fun getTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.US)
        return sdf.format(Date())
    }

    private fun getHouseholdId(context: Context): String? =
        context.getSharedPreferences("nettie_prefs", Context.MODE_PRIVATE)
            .getString("household_id", null)

    private fun getGuardianId(context: Context): String? =
        context.getSharedPreferences("nettie_prefs", Context.MODE_PRIVATE)
            .getString("guardian_id", null)

    private fun getChildId(context: Context): String? =
        context.getSharedPreferences("nettie_prefs", Context.MODE_PRIVATE)
            .getString("child_id", null)
}
