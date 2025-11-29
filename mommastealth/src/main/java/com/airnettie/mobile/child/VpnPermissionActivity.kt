package com.airnettie.mobile.child

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase

/**
 * Transparent activity to request VPN permission for SafeScope
 * This is needed because VPN permission can only be requested from an Activity, not a Service
 */
class VpnPermissionActivity : Activity() {

    companion object {
        private const val TAG = "VpnPermissionActivity"
        private const val VPN_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "VpnPermissionActivity started")

        // Request VPN permission
        val prepareIntent = VpnService.prepare(this)
        if (prepareIntent != null) {
            Log.d(TAG, "Requesting VPN permission from user")
            startActivityForResult(prepareIntent, VPN_REQUEST_CODE)
        } else {
            Log.d(TAG, "VPN permission already granted")
            onVpnPermissionGranted()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == VPN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "VPN permission granted by user")
                onVpnPermissionGranted()
            } else {
                Log.w(TAG, "VPN permission denied by user")
                Toast.makeText(this, "SafeScope requires VPN permission to filter content", Toast.LENGTH_LONG).show()
                onVpnPermissionDenied()
            }
        }
        finish()
    }

    private fun onVpnPermissionGranted() {
        // Start the VPN service
        val vpnIntent = Intent(this, SafeScopeVpnService::class.java)
        startService(vpnIntent)

        Toast.makeText(this, "SafeScope enabled", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "SafeScopeVpnService started after permission grant")
    }

    private fun onVpnPermissionDenied() {
        // Reset the SafeScope toggle in Firebase
        val prefs = getSharedPreferences("nettie_prefs", MODE_PRIVATE)
        val guardianId = prefs.getString("guardian_id", null)
        val childId = prefs.getString("child_id", null)

        if (guardianId != null && childId != null) {
            FirebaseDatabase.getInstance()
                .getReference("guardianLinks/$guardianId/safeScope/$childId")
                .setValue(false)
            Log.d(TAG, "SafeScope toggle reset to false in Firebase")
        }
    }
}
