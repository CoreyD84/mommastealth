package com.airnettie.mobile.child

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.firebase.database.*

class ChildSyncService : Service() {

    private val TAG = "ChildSyncService"

    private var safeScopeRef: DatabaseReference? = null
    private var safeScopeListener: ValueEventListener? = null
    private var platformControlsRef: DatabaseReference? = null
    private var platformControlsListener: ValueEventListener? = null

    private lateinit var locationManager: LocationManager
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val prefs = getSharedPreferences("nettie_prefs", MODE_PRIVATE)
            val childId = prefs.getString("child_id", null)
            val guardianId = prefs.getString("guardian_id", null)
            if (childId != null && guardianId != null) {
                val payload = mapOf(
                    "latitude" to location.latitude,
                    "longitude" to location.longitude,
                    "timestamp" to System.currentTimeMillis()
                )
                FirebaseDatabase.getInstance()
                    .getReference("guardianLinks/$guardianId/location/$childId")
                    .setValue(payload)
                Log.d(TAG, "Location updated: ${location.latitude}, ${location.longitude}")
            }
        }
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        val prefs = getSharedPreferences("nettie_prefs", MODE_PRIVATE)
        val childId = prefs.getString("child_id", null)
        val guardianId = prefs.getString("guardian_id", null)

        if (childId == null) {
            Log.e(TAG, "Child ID is null. Stopping service.")
            stopSelf()
            return
        }

        Log.d(TAG, "Starting sync for child: $childId and guardian: $guardianId")

        if (guardianId != null) {
            updateHeartbeat(guardianId, childId)
            setupPlatformControlsListener(guardianId, childId)
            setupSafeScopeListener(guardianId, childId)
        } else {
            Log.w(TAG, "Guardian ID not found, listeners not attached.")
        }

        setupLocationSync()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "ChildSyncService started")
        return START_STICKY
    }

    private fun setupSafeScopeListener(guardianId: String, childId: String) {
        safeScopeRef = FirebaseDatabase.getInstance()
            .getReference("guardianLinks/$guardianId/safeScope/$childId")
        safeScopeListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isEnabled = snapshot.getValue(Boolean::class.java) ?: false
                Log.d(TAG, "SafeScope toggle changed to: $isEnabled")

                val vpnIntent = Intent(this@ChildSyncService, SafeScopeVpnService::class.java)
                if (isEnabled) {
                    Log.d(TAG, "Starting SafeScopeVpnService")
                    startService(vpnIntent)
                } else {
                    Log.d(TAG, "Stopping SafeScopeVpnService")
                    stopService(vpnIntent)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "SafeScope listener cancelled", error.toException())
            }
        }
        safeScopeRef?.addValueEventListener(safeScopeListener!!)
    }

    private fun setupPlatformControlsListener(guardianId: String, childId: String) {
        platformControlsRef = FirebaseDatabase.getInstance()
            .getReference("guardianLinks/$guardianId/platformControls/$childId")
        platformControlsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Platform controls changed.")
                snapshot.children.forEach { platformSnapshot ->
                    val platformName = platformSnapshot.key
                    val isEnabled = platformSnapshot.getValue(Boolean::class.java) ?: true
                    if (platformName != null) {
                        Log.d(TAG, "Broadcasting update for $platformName, enabled: $isEnabled")
                        val intent = Intent("com.airnettie.mobile.PLATFORM_CONTROL")
                        intent.setPackage(this@ChildSyncService.packageName)
                        intent.putExtra("platform", platformName.lowercase())
                        intent.putExtra("enabled", isEnabled)
                        sendBroadcast(intent)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Platform controls listener cancelled", error.toException())
            }
        }
        platformControlsRef?.addValueEventListener(platformControlsListener!!)
    }

    private fun setupLocationSync() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000L, 10f, locationListener)
                Log.d(TAG, "Location updates requested.")
            } catch (e: SecurityException) {
                Log.e(TAG, "Failed to request location updates.", e)
            } catch (e: Exception) {
                Log.e(TAG, "An exception occurred when setting up location sync", e)
            }
        } else {
            Log.w(TAG, "Location permission not granted.")
        }
    }

    private fun updateHeartbeat(guardianId: String, childId: String) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("guardianLinks/$guardianId/childProfiles/$childId")
        ref.child("lastSeen").setValue(System.currentTimeMillis())
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "ChildSyncService destroyed")

        safeScopeRef?.removeEventListener(safeScopeListener!!)
        platformControlsRef?.removeEventListener(platformControlsListener!!)
        locationManager.removeUpdates(locationListener)
    }

    private fun startForegroundServiceNotification() {
        val channelId = "nettielocation"
        val channelName = "Nettie Location Sync"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_MIN)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Nettie Child Sync")
            .setContentText("Syncing with guardian device")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}