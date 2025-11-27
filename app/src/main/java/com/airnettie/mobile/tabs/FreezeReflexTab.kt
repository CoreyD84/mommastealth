package com.airnettie.mobile.tabs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.airnettie.mobile.R
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class FreezeReflexTab : Fragment() {

    private lateinit var containerLayout: LinearLayout
    private lateinit var database: FirebaseDatabase
    private var guardianId: String? = null
    private var childId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.tab_freeze_reflex, container, false)
        containerLayout = view.findViewById(R.id.freezeReflexContainer)
        database = FirebaseDatabase.getInstance()

        if (!isAdded) return view

        val prefs = requireContext().getSharedPreferences("nettie_prefs", Context.MODE_PRIVATE)
        guardianId = prefs.getString("guardian_id", null)
        childId = prefs.getString("child_id", null)

        if (!guardianId.isNullOrBlank() && !childId.isNullOrBlank()) {
            loadFreezeEvents()
        } else {
            showMessage("Missing guardian or child identity. Please log in again.")
        }

        return view
    }

    private fun loadFreezeEvents() {
        val ref = database.getReference("feelscope/detections/$guardianId/$childId")
        ref.orderByChild("isEscalated").equalTo(true) // ✅ use boolean, not string
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return
                    containerLayout.removeAllViews()

                    if (!snapshot.exists()) {
                        showMessage("No freeze reflex events recorded.")
                        return
                    }

                    val sorted = snapshot.children.sortedByDescending { it.key }
                    for (event in sorted) {
                        val category = event.child("category").getValue(String::class.java) ?: "Unknown"
                        val matched = event.child("matchedPhrases").children.mapNotNull { it.getValue(String::class.java) }
                        val sourceApp = event.child("sourceApp").getValue(String::class.java) ?: "Unknown"
                        val timestamp = event.child("timestamp").getValue(String::class.java) ?: "Unknown"
                        val deflection = event.child("deflectionUsed").getValue(String::class.java)

                        val card = MaterialCardView(requireContext()).apply {
                            setContentPadding(24, 16, 24, 16)
                            radius = 12f
                            cardElevation = 6f
                            val textView = TextView(requireContext()).apply {
                                text = buildString {
                                    append("🚨 Freeze Reflex Triggered\n")
                                    append("🧭 Category: $category\n")
                                    append("🔍 Matched: ${matched.joinToString(", ")}\n")
                                    append("📱 App: $sourceApp\n")
                                    append("🕒 Time: $timestamp\n")
                                    if (!deflection.isNullOrBlank()) {
                                        append("🛡️ Deflection: \"$deflection\"\n")
                                    }
                                }
                                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                                textSize = 15f
                            }
                            addView(textView)
                        }
                        containerLayout.addView(card)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (!isAdded) return
                    showMessage("Failed to load freeze reflex history: ${error.message}")
                }
            })
    }

    private fun showMessage(message: String) {
        val errorText = TextView(requireContext()).apply {
            text = message
            setPadding(16, 16, 16, 16)
        }
        containerLayout.addView(errorText)
    }
}