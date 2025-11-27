package com.airnettie.mobile

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("MainApplication", "✅ App started")

        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d("MainApplication", "✅ Firebase initialized")
            } else {
                Log.d("MainApplication", "ℹ️ Firebase already initialized")
            }
        } catch (e: Exception) {
            Log.e("MainApplication", "❌ Firebase init failed", e)
        }
    }
}