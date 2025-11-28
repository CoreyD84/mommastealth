package com.airnettie.mobile.features.guardian.activities

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airnettie.mobile.R
import com.airnettie.mobile.utils.QRUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class GenerateLinkQrActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_link_qr)

        val guardianId = FirebaseAuth.getInstance().currentUser?.uid
        if (guardianId == null) {
            Toast.makeText(this, "Guardian not signed in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Debug: Show guardian ID
        android.util.Log.d("GenerateLinkQr", "Guardian ID: $guardianId")
        Toast.makeText(this, "Guardian ID: $guardianId", Toast.LENGTH_LONG).show()

        // ✅ Generate one-time token (NO guardianId in QR - it will be created on scan!)
        val token = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val expiresAt = timestamp + (15 * 60 * 1000) // Token expires in 15 minutes

        // ✅ Build deep link URL for QR payload (ONLY token - guardian link created on scan)
        // Use custom scheme for direct app opening, with web fallback
        val redirectUrl = "nettielink://child/link?token=$token&fallback=https://coreyd84.github.io/mommastealth/link/?token=$token"

        // ✅ Store token in Firebase with guardian info for later linking
        val tokenData = mapOf(
            "guardianId" to guardianId,
            "timestamp" to timestamp,
            "expiresAt" to expiresAt,
            "used" to false
        )

        // Store in global pending tokens (not under guardian ID)
        val ref = FirebaseDatabase.getInstance()
            .getReference("linkingTokens/$token")

        android.util.Log.d("GenerateLinkQr", "Attempting to write to: linkingTokens/$token")
        android.util.Log.d("GenerateLinkQr", "Token data: $tokenData")
        android.util.Log.d("GenerateLinkQr", "Current user: ${FirebaseAuth.getInstance().currentUser?.email}")

        ref.setValue(tokenData).addOnSuccessListener {
            Toast.makeText(this, "QR Code ready! Token valid for 15 minutes", Toast.LENGTH_SHORT).show()
            android.util.Log.d("GenerateLinkQr", "Linking token stored successfully")
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to generate QR: ${e.message}", Toast.LENGTH_LONG).show()
            android.util.Log.e("GenerateLinkQr", "Failed to store linking token", e)
        }

        // ✅ Generate QR code using QRUtils
        val qrBitmap = QRUtils.generateQRCode(redirectUrl)

        // ✅ Display QR code in your layout
        findViewById<ImageView>(R.id.qrImageView).setImageBitmap(qrBitmap)
    }
}