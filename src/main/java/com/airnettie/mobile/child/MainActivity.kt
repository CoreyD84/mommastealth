package com.airnettie.mobile.child

import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.database.*

class MainActivity : ComponentActivity() {

    // Modern Activity Result launcher for VPN permission
    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.i("MainActivity", "✅ VPN permission granted")
            startSafeScope()
        } else {
            Log.w("MainActivity", "❌ VPN permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data: Uri? = intent?.data
        val token = data?.getQueryParameter("token")
        val guardianId = data?.getQueryParameter("guardianId")

        if (!token.isNullOrBlank() && !guardianId.isNullOrBlank()) {
            val ref = FirebaseDatabase.getInstance()
                .getReference("guardianLinks/$guardianId/pendingTokens/$token")

            ref.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val childId = resolveChildId()

                    // ✅ Link child to guardian
                    val linkRef = FirebaseDatabase.getInstance()
                        .getReference("guardianLinks/$guardianId/children/$childId")
                    linkRef.setValue(true)

                    // ✅ Persist child ID locally
                    getSharedPreferences("nettie_prefs", MODE_PRIVATE)
                        .edit()
                        .putString("child_id", childId)
                        .putString("guardian_id", guardianId)
                        .apply()

                    // ✅ Remove token to prevent reuse
                    ref.removeValue()

                    // ✅ Start background services
                    startService(Intent(this, ChildSyncService::class.java))
                    startService(Intent(this, FeelScopeService::class.java))

                    // ✅ Attach listeners for platform controls + SafeScope
                    attachPlatformControlListener(childId)
                    attachSafeScopeListener(childId)
                }
            }
        } else {
            // Fallback: start services without deep link
            val childId = resolveChildId()
            startService(Intent(this, ChildSyncService::class.java))
            startService(Intent(this, FeelScopeService::class.java))

            // ✅ Attach listeners anyway
            attachPlatformControlListener(childId)
            attachSafeScopeListener(childId)
        }

        // ✅ Exit silently
        finish()
    }

    private fun resolveChildId(): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown_child"
    }

    // 🔧 Listen for platform control changes
    private fun attachPlatformControlListener(childId: String) {
        val controlsRef = FirebaseDatabase.getInstance()
            .getReference("platform_controls/$childId")

        controlsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { platform ->
                    val enabled = platform.getValue(Boolean::class.java) ?: true
                    val intent = Intent("com.airnettie.mobile.PLATFORM_CONTROL")
                    intent.putExtra("platform", platform.key)
                    intent.putExtra("enabled", enabled)
                    sendBroadcast(intent)
                    Log.d("PlatformControl", "Broadcast sent for ${platform.key} = $enabled")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PlatformControl", "Listener cancelled: ${error.message}")
            }
        })
    }

    // 🔧 Listen for SafeScope toggle
    private fun attachSafeScopeListener(childId: String) {
        val safeScopeRef = FirebaseDatabase.getInstance()
            .getReference("safescope/$childId/enabled")

        safeScopeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val enabled = snapshot.getValue(Boolean::class.java) ?: false
                Log.d("SafeScope", "Broadcast sent for SafeScope = $enabled")

                if (enabled) {
                    prepareVpn() // ✅ request VPN permission before starting SafeScope
                } else {
                    val intent = Intent("com.airnettie.mobile.SAFESCOPE_CONTROL")
                    intent.putExtra("enabled", false)
                    sendBroadcast(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SafeScope", "Listener cancelled: ${error.message}")
            }
        })
    }

    // 🔧 Request VPN permission
    private fun prepareVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent) // ✅ modern API
        } else {
            // Already has permission
            startSafeScope()
        }
    }

    private fun startSafeScope() {
        val intent = Intent("com.airnettie.mobile.SAFESCOPE_CONTROL")
        intent.putExtra("enabled", true)
        sendBroadcast(intent)
    }
}