package com.airnettie.mobile.modules

import android.content.Context
import android.util.Log
import com.airnettie.mobile.models.Flag
import com.google.firebase.database.FirebaseDatabase

object FirebaseSync {
    private val db = FirebaseDatabase.getInstance().reference

    // 🔄 Unified sync (scoped to guardian + child)
    fun syncFlag(
        caseId: String,
        severity: String,
        message: String,
        guardianId: String,
        childId: String
    ) {
        val flagData = mapOf(
            "severity" to severity,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )
        db.child("flags").child(guardianId).child(childId).child(caseId).setValue(flagData)
            .addOnSuccessListener { Log.d("FirebaseSync", "Flag synced: $caseId") }
            .addOnFailureListener { Log.e("FirebaseSync", "Failed to sync flag", it) }
    }

    // 🧠 Sync using full Flag object (same guardian/child scoping)
    fun syncFlagObject(flag: Flag, guardianId: String, childId: String) {
        db.child("flags").child(guardianId).child(childId).child(flag.caseId).setValue(flag)
            .addOnSuccessListener { Log.d("FirebaseSync", "Flag object synced: ${flag.caseId}") }
            .addOnFailureListener { Log.e("FirebaseSync", "Failed to sync Flag object", it) }
    }

    // 🎭 Mascot mood sync (scoped to household/child if available)
    fun syncMood(context: Context, mood: String) {
        val prefs = context.getSharedPreferences("nettie_prefs", Context.MODE_PRIVATE)
        val householdId = prefs.getString("household_id", null)
        val childId = prefs.getString("child_id", null)

        if (householdId.isNullOrBlank() || childId.isNullOrBlank()) {
            Log.w("FirebaseSync", "Missing householdId or childId — skipping mood sync.")
            return
        }

        db.child("mascotMood").child(householdId).child(childId).push().setValue(
            mapOf(
                "mood" to mood,
                "timestamp" to System.currentTimeMillis()
            )
        ).addOnSuccessListener {
            Log.d("FirebaseSync", "Mood synced: $mood")
        }.addOnFailureListener {
            Log.e("FirebaseSync", "Failed to sync mood", it)
        }
    }
}