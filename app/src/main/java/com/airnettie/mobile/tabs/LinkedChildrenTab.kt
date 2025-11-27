package com.airnettie.mobile.tabs

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.DateFormat

data class LinkedChild(
    val name: String = "Unnamed",
    val mood: String = "unknown",
    val lastSeen: Long = 0L,
    val isEscalated: Boolean = false
)

@Composable
fun LinkedChildrenTab(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var children by remember { mutableStateOf<List<LinkedChild>>(emptyList()) }

    val guardianId = FirebaseAuth.getInstance().currentUser?.uid

    // ✅ Load linked children
    LaunchedEffect(guardianId) {
        if (guardianId == null) {
            children = emptyList()
            return@LaunchedEffect
        }

        val ref = FirebaseDatabase.getInstance().getReference("guardianLinks/$guardianId/linkedChildren")
        ref.get().addOnSuccessListener { snapshot ->
            val tempList = mutableListOf<LinkedChild>()
            snapshot.children.forEach { childSnap ->
                val name = childSnap.child("nickname").getValue(String::class.java) ?: "Unnamed"
                val mood = childSnap.child("mood").getValue(String::class.java) ?: "unknown"
                val lastSeen = childSnap.child("last_seen").getValue(Long::class.java) ?: 0L
                val isEscalated = childSnap.child("isEscalated").getValue(Boolean::class.java) ?: false
                tempList.add(LinkedChild(name, mood, lastSeen, isEscalated))
            }
            children = tempList.sortedByDescending { it.lastSeen }
        }
    }

    Column(modifier = modifier.padding(24.dp)) {
        Text("Linked Children", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (children.isEmpty()) {
            Text("No children linked yet. Use the 'Link Child Device' tab to generate a QR code.")
        } else {
            LazyColumn {
                items(children) { child ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("👤 Name: ${child.name}")
                            Text("🎭 Mood: ${child.mood}")
                            Text("🕒 Last Seen: ${DateFormat.getDateTimeInstance().format(child.lastSeen)}")

                            if (child.isEscalated) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { /* TODO: Trigger Freeze logic */ }) {
                                    Text("🚨 Freeze")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
