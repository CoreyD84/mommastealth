package com.airnettie.mobile.tabs

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.airnettie.mobile.components.ConsentModal
import com.airnettie.mobile.tabs.theme.MommaMobileTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

@Composable
fun PlatformControlTab(modifier: Modifier = Modifier) {
    MommaMobileTheme {
        val context = LocalContext.current
        val platforms = listOf("Discord", "Roblox", "TikTok", "Messenger")
        val toggles = remember { mutableStateMapOf<String, Boolean>() }
        val currentTip = remember { mutableStateOf("📄 Share stories of online safety wins...") }

        // ✅ Fetch the dynamically linked childId from Firebase
        @Suppress("ProduceStateDoesNotAssignValue")
        val childId by produceState<String?>(initialValue = null) {
            val guardianId = FirebaseAuth.getInstance().currentUser?.uid
            if (guardianId == null) {
                value = null
                return@produceState
            }
            val linkRef = FirebaseDatabase.getInstance()
                .getReference("guardianLinks/$guardianId/linkedChildren")
            try {
                val snapshot = linkRef.limitToFirst(1).get().await()
                value = snapshot.children.firstOrNull()?.key
            } catch (e: Exception) {
                Log.e("PlatformControlTab", "Failed to fetch childId", e)
            }
        }

        // ✅ Load platform toggles from guardianLinks path
        LaunchedEffect(childId) {
            val guardianId = FirebaseAuth.getInstance().currentUser?.uid
            if (childId == null || guardianId == null) return@LaunchedEffect
            val ref = FirebaseDatabase.getInstance()
                .getReference("guardianLinks/$guardianId/platformControls/$childId")
            ref.get().addOnSuccessListener { snapshot ->
                platforms.forEach { platform ->
                    val key = platform.lowercase()
                    val enabled = snapshot.child(key).getValue(Boolean::class.java) ?: true
                    toggles[platform] = enabled
                }
            }
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Emergency Contacts", style = MaterialTheme.typography.titleMedium)
                        Text("• Killbuck Police Dept: (330) 555-1212")
                        Text("• Killbuck Elementary: (330) 555-3434")
                        Text("• Holmes County Sheriff: (330) 555-9876")
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Online Safety Tip", style = MaterialTheme.typography.titleMedium)
                        Text(currentTip.value)
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val tips = listOf(
                            "📄 Share stories of online safety wins...",
                            "🛡️ Review flagged messages together.",
                            "💬 Ask your child how Nettie feels to them.",
                            "📱 Keep platform access aligned with emotional state.",
                            "👂 Listen before reacting to emotional spikes."
                        )
                        currentTip.value = tips.random()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Tip")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refresh Safety Tip")
                }
            }

            item {
                Text("Platform Controls", style = MaterialTheme.typography.headlineSmall)
            }

            items(platforms.size) { index ->
                val platform = platforms[index]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(platform)
                    Switch(
                        checked = toggles[platform] ?: true,
                        onCheckedChange = { isEnabled ->
                            val guardianId = FirebaseAuth.getInstance().currentUser?.uid
                            if (childId == null || guardianId == null) return@Switch
                            toggles[platform] = isEnabled
                            val key = platform.lowercase()
                            val ref = FirebaseDatabase.getInstance()
                                .getReference("guardianLinks/$guardianId/platformControls/$childId/$key")
                            ref.setValue(isEnabled)
                                .addOnFailureListener {
                                    Toast.makeText(context, "Failed to update $platform", Toast.LENGTH_SHORT).show()
                                }

                            if (isEnabled) {
                                ConsentModal.show(context, platform)
                            }
                        }
                    )
                }
            }

            item {
                Text(
                    "Toggles above control both access and Nettie’s emotional radar per platform.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            item {
                HorizontalDivider()
                SafeScopeToggle(childId = childId)
                HorizontalDivider()
                val id = childId
                if (id != null) {
                    ConsentSection(childId = id)
                }
            }
        }
    }
}

@Composable
fun SafeScopeToggle(modifier: Modifier = Modifier, childId: String?) {
    val guardianId = FirebaseAuth.getInstance().currentUser?.uid
    var isEnabled by remember { mutableStateOf(false) }
    var isLoaded by remember { mutableStateOf(false) }

    // ✅ Correct path under guardianLinks
    val toggleRef = remember(guardianId, childId) {
        if (guardianId != null && childId != null) {
            FirebaseDatabase.getInstance()
                .getReference("guardianLinks/$guardianId/safeScope/$childId")
        } else {
            null
        }
    }

    DisposableEffect(toggleRef) {
        val listener = toggleRef?.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                isEnabled = snapshot.getValue(Boolean::class.java) ?: false
                isLoaded = true
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.e("SafeScopeToggle", "Failed to load toggle: ${error.message}")
                isLoaded = true
            }
        })

        onDispose {
            if (listener != null) {
                toggleRef.removeEventListener(listener)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text("SafeScope Filter", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        if (childId == null) {
            Text("Link a child device to enable SafeScope.", style = MaterialTheme.typography.bodyMedium)
        } else if (!isLoaded) {
            CircularProgressIndicator()
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enabled", modifier = Modifier.weight(1f))
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { newState ->
                        isEnabled = newState
                        toggleRef?.setValue(newState)
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isEnabled)
                    "SafeScope is actively filtering harmful content."
                else
                    "SafeScope is currently disabled.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ConsentSection(childId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var showToast by remember { mutableStateOf<String?>(null) }
    val guardianId = FirebaseAuth.getInstance().currentUser?.uid
    var consentGranted by remember { mutableStateOf<Boolean?>(null) }

    // 🔔 Show toast feedback
    showToast?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            showToast = null
        }
    }

    // 📡 Listen for current consent state
    LaunchedEffect(guardianId, childId) {
        if (guardianId != null && childId.isNotEmpty()) {
            val ref = FirebaseDatabase.getInstance()
                .getReference("guardian_profiles/$guardianId/consent/$childId")
            ref.get().addOnSuccessListener { snapshot ->
                consentGranted = snapshot.getValue(Boolean::class.java)
            }.addOnFailureListener {
                Log.e("ConsentSection", "Failed to fetch consent state", it)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text("Consent & Emotional Monitoring", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("By granting consent, you allow Nettie to:", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(12.dp))
                Text("• Monitor emotional signals across supported platforms (SMS, Discord, Roblox, etc.)")
                Text("• Detect harmful patterns and escalate when needed")
                Text("• Share emotional insights with you through the guardian dashboard")
                Text("• Respect boundaries and only intervene when safety is at risk")
                Text("• Log emotional spikes and matched phrases for review")
                Text("• Use mascot mood overlays to gently reflect your child’s emotional state")
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "This consent can be revoked at any time. Nettie will never override your authority — she’s here to support, not replace.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🟢 Live status indicator
        consentGranted?.let {
            val statusText = if (it) "Consent currently granted" else "Consent currently revoked"
            val statusColor = if (it) Color(0xFF4CAF50) else Color(0xFFF44336) // green/red
            Text(statusText, color = statusColor, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    if (guardianId != null) {
                        val ref = FirebaseDatabase.getInstance()
                            .getReference("guardian_profiles/$guardianId/consent/$childId")
                        ref.setValue(true)
                            .addOnSuccessListener {
                                consentGranted = true
                                showToast = "Consent granted for $childId"
                            }
                            .addOnFailureListener {
                                showToast = "Failed to grant consent"
                            }
                    } else {
                        showToast = "Missing guardian info — please sign in again"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // green
            ) {
                Icon(Icons.Default.Check, contentDescription = "Grant Consent")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Grant Consent")
            }

            Button(
                onClick = {
                    if (guardianId != null) {
                        val ref = FirebaseDatabase.getInstance()
                            .getReference("guardian_profiles/$guardianId/consent/$childId")
                        ref.setValue(false)
                            .addOnSuccessListener {
                                consentGranted = false
                                showToast = "Consent revoked for $childId"
                            }
                            .addOnFailureListener {
                                showToast = "Failed to revoke consent"
                            }
                    } else {
                        showToast = "Missing guardian info — please sign in again"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)) // red
            ) {
                Icon(Icons.Default.Close, contentDescription = "Revoke Consent")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Revoke Consent")
            }
        }
    }
}