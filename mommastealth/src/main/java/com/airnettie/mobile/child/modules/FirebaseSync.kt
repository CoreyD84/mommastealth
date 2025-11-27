package com.airnettie.mobile.child.modules

import android.util.Log
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
}
