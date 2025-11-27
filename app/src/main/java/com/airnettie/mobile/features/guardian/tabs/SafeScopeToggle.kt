package com.airnettie.mobile.features.guardian.tabs

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SafeScopeToggle(modifier: Modifier = Modifier, childId: String?) {
    val guardianId = FirebaseAuth.getInstance().currentUser?.uid
    var debugText by remember { mutableStateOf("Waiting for Child ID...") }

    val toggleRef = remember(guardianId, childId) {
        if (guardianId != null && childId != null) {
            debugText = "Child ID received: $childId. Creating Firebase listener..."
            FirebaseDatabase.getInstance()
                .getReference("guardianLinks/$guardianId/safeScope/$childId")
        } else {
            debugText = if (guardianId == null) "Error: Guardian ID is null." else "Error: Child ID is null."
            null
        }
    }

    var isEnabled by remember { mutableStateOf(false) }
    var isLoaded by remember { mutableStateOf(false) }

    DisposableEffect(toggleRef) {
        if (toggleRef == null) {
            onDispose { }
        } else {
            debugText = "Attaching Firebase listener..."
            val listener = toggleRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isEnabled = snapshot.getValue(Boolean::class.java) ?: false
                    isLoaded = true
                    debugText = "Success! Firebase value received: $isEnabled"
                }

                override fun onCancelled(error: DatabaseError) {
                    debugText = "Firebase Listener Error: ${error.message}"
                    isLoaded = true // Stop loading on error
                }
            })

            onDispose {
                toggleRef.removeEventListener(listener)
            }
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text(debugText, color = Color.Blue, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Text("SafeScope Filter", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        if (!isLoaded) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.width(8.dp))
                Text("Loading from Firebase...")
            }
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