package com.airnettie.mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.airnettie.mobile.auth.LoginActivity
import com.airnettie.mobile.safescope.SafeScope
import com.airnettie.mobile.mobilenettie.GuardianDashboard
import com.google.firebase.database.*

class MainActivity : ComponentActivity() {

    private var toggleRef: DatabaseReference? = null
    private var valueEventListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Decide whether to launch dashboard or login
        val prefs = getSharedPreferences("nettie_prefs", MODE_PRIVATE)
        val guardianId = prefs.getString("guardian_id", null)
        val childId = prefs.getString("child_id", null)

        if (guardianId.isNullOrBlank() || childId.isNullOrBlank()) {
            // Missing identity → go to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // ✅ Scoped SafeScope toggle listener
        toggleRef = FirebaseDatabase.getInstance()
            .getReference("guardianLinks/$guardianId/safeScope/$childId")

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isEnabled = snapshot.getValue(Boolean::class.java) ?: false
                if (isEnabled) {
                    SafeScope.activate(applicationContext)
                } else {
                    SafeScope.deactivate(applicationContext)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("MainActivity", "❌ Firebase listener cancelled: ${error.message}")
            }
        }

        // ✅ Launch GuardianDashboard
        startActivity(Intent(this, GuardianDashboard::class.java))
        finish()
    }

    override fun onStart() {
        super.onStart()
        valueEventListener?.let { toggleRef?.addValueEventListener(it) }
    }

    override fun onStop() {
        super.onStop()
        valueEventListener?.let { toggleRef?.removeEventListener(it) }
    }
}