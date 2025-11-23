package com.airnettie.mobile.child

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.firebase.database.FirebaseDatabase

class ChildSyncService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = getSharedPreferences("nettie_prefs", MODE_PRIVATE)
        val childId = prefs.getString("child_id", null) ?: return START_NOT_STICKY

        val ref = FirebaseDatabase.getInstance().getReference("childProfiles/$childId")
        ref.child("lastSeen").setValue(System.currentTimeMillis())
        ref.child("mood").setValue("calm")

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}