package com.airnettie.mobile.child

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.edit
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _: Map<String, Boolean> ->
            startService(Intent(this, ChildSyncService::class.java))
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("nettie_prefs", MODE_PRIVATE)

        // Log all intent information for debugging
        android.util.Log.d("MommaStealth", "=== MainActivity onCreate ===")
        android.util.Log.d("MommaStealth", "Intent action: ${intent?.action}")
        android.util.Log.d("MommaStealth", "Intent data: ${intent?.data}")
        android.util.Log.d("MommaStealth", "Intent data string: ${intent?.dataString}")

        val data: Uri? = intent?.data
        val token: String? = data?.getQueryParameter("token")
        android.util.Log.d("MommaStealth", "Extracted token: $token")
        val childId: String = resolveChildId()

        prefs.edit {
            putString("child_id", childId)
        }

        // NEW TOKEN-BASED LINKING: Only token is required, guardianId is fetched from Firebase
        if (!token.isNullOrBlank()) {
            android.util.Log.d("MommaStealth", "Token received: $token")

            // Sign in anonymously FIRST, then link
            android.util.Log.d("MommaStealth", "Starting anonymous sign-in...")
            FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener { result: AuthResult ->
                    val firebaseUid = result.user?.uid ?: "unknown"
                    prefs.edit {
                        putString("firebase_uid", firebaseUid)
                    }
                    android.util.Log.d("MommaStealth", "Signed in anonymously: $firebaseUid")
                    android.util.Log.d("MommaStealth", "Fetching token data from: linkingTokens/$token")

                    // Fetch token data from new global linkingTokens path
                    val tokenRef = FirebaseDatabase.getInstance()
                        .getReference("linkingTokens/$token")

                    tokenRef.get().addOnSuccessListener { snapshot: DataSnapshot ->
                        android.util.Log.d("MommaStealth", "Token read successful. Exists: ${snapshot.exists()}")

                        if (!snapshot.exists()) {
                            Toast.makeText(this, "Invalid or expired QR code", Toast.LENGTH_LONG).show()
                            requestLocationPermissions()
                            return@addOnSuccessListener
                        }

                        val guardianId = snapshot.child("guardianId").getValue(String::class.java)
                        val expiresAt = snapshot.child("expiresAt").getValue(Long::class.java)
                        val used = snapshot.child("used").getValue(Boolean::class.java) ?: false

                        // Validate token
                        when {
                            guardianId == null -> {
                                Toast.makeText(this, "Invalid QR code data", Toast.LENGTH_LONG).show()
                                requestLocationPermissions()
                                return@addOnSuccessListener
                            }
                            used -> {
                                Toast.makeText(this, "This QR code has already been used", Toast.LENGTH_LONG).show()
                                requestLocationPermissions()
                                return@addOnSuccessListener
                            }
                            expiresAt != null && System.currentTimeMillis() > expiresAt -> {
                                Toast.makeText(this, "QR code has expired. Please generate a new one.", Toast.LENGTH_LONG).show()
                                requestLocationPermissions()
                                return@addOnSuccessListener
                            }
                        }

                        // Mark token as used
                        tokenRef.child("used").setValue(true)

                        // Store guardian ID
                        prefs.edit {
                            putString("guardian_id", guardianId)
                        }

                        android.util.Log.d("MommaStealth", "Token is valid. Guardian ID: $guardianId")
                        android.util.Log.d("MommaStealth", "Creating link at: guardianLinks/$guardianId/linkedChildren/$childId")

                        // Create the link in guardianLinks path
                        val linkRef = FirebaseDatabase.getInstance()
                            .getReference("guardianLinks/$guardianId/linkedChildren/$childId")

                        val payload = mapOf(
                            "nickname" to android.os.Build.MODEL,
                            "last_seen" to System.currentTimeMillis(),
                            "mood" to "calm",
                            "linked_at" to System.currentTimeMillis(),
                            "deviceInfo" to mapOf(
                                "model" to android.os.Build.MODEL,
                                "os" to "Android ${android.os.Build.VERSION.RELEASE}"
                            )
                        )

                        android.util.Log.d("MommaStealth", "Payload: $payload")
                        linkRef.updateChildren(payload).addOnSuccessListener {
                            android.util.Log.d("MommaStealth", "Link created successfully!")

                            // Save guardian ID to SharedPreferences
                            prefs.edit().putString("guardian_id", guardianId).apply()
                            android.util.Log.d("MommaStealth", "Guardian ID saved: $guardianId")

                            Toast.makeText(this, "Successfully linked to guardian! 🎯", Toast.LENGTH_LONG).show()

                            // Hide the app icon from launcher after successful linking
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                try {
                                    val componentName = android.content.ComponentName(this, MainActivity::class.java)
                                    packageManager.setComponentEnabledSetting(
                                        componentName,
                                        android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                        android.content.pm.PackageManager.DONT_KILL_APP
                                    )
                                    android.util.Log.d("MommaStealth", "App icon hidden from launcher")
                                } catch (e: Exception) {
                                    android.util.Log.e("MommaStealth", "Failed to hide app icon", e)
                                }
                            }, 2000) // Wait 2 seconds before hiding to allow permissions request

                            requestLocationPermissions()
                        }.addOnFailureListener { e: Exception ->
                            android.util.Log.e("MommaStealth", "Link creation failed", e)
                            Toast.makeText(this, "Link failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            requestLocationPermissions()
                        }
                    }.addOnFailureListener { e: Exception ->
                        android.util.Log.e("MommaStealth", "Failed to read token", e)
                        Toast.makeText(this, "Failed to validate token: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        requestLocationPermissions()
                    }
                }
                .addOnFailureListener { e: Exception ->
                    android.util.Log.e("MommaStealth", "Anonymous sign-in failed", e)
                    Toast.makeText(this, "Sign-in failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    requestLocationPermissions()
                }
        } else {
            android.util.Log.d("MommaStealth", "No token found in deep link, skipping linking")
            android.util.Log.d("MommaStealth", "Intent action: ${intent?.action}")
            android.util.Log.d("MommaStealth", "Intent data: ${intent?.data}")
            android.util.Log.d("MommaStealth", "Intent extras: ${intent?.extras}")

            Toast.makeText(this, "No linking token found. Please scan the QR code to link this device.", Toast.LENGTH_LONG).show()
            requestLocationPermissions()
        }
    }

    private fun requestLocationPermissions() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            startService(Intent(this, ChildSyncService::class.java))
            finish()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun resolveChildId(): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown_child"
    }
}
