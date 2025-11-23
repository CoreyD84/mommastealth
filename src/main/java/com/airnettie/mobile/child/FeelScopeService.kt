package com.airnettie.mobile.child

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.Settings
import com.google.firebase.database.*

class FeelScopeService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val childId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val ref = FirebaseDatabase.getInstance().getReference("guardianControls/$childId")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val safeScope = snapshot.child("safeScope").getValue(Boolean::class.java) ?: false
                val blockMessenger = snapshot.child("blockMessenger").getValue(Boolean::class.java) ?: false

                if (safeScope) {
                    // TODO: Apply SafeScope logic (e.g., block porn domains)
                }

                if (blockMessenger) {
                    // TODO: Disable Messenger via PlatformControlReceiver
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}