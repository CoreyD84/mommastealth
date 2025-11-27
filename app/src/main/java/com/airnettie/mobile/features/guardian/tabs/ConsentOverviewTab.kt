package com.airnettie.mobile.features.guardian.tabs

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

class ConsentOverviewTab : Fragment() {

    private lateinit var containerLayout: LinearLayout
    private lateinit var database: FirebaseDatabase
    private var householdId: String? = null
    private var guardianId: String? = null
    private var consentListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.tab_consent_overview, container, false)
        containerLayout = view.findViewById(R.id.consentContainer)
        database = FirebaseDatabase.getInstance()

        val prefs = requireContext().getSharedPreferences("nettie_prefs", android.content.Context.MODE_PRIVATE)
        householdId = prefs.getString("household_id", null)
        guardianId = prefs.getString("guardian_id", null)

        if (!householdId.isNullOrEmpty() && !guardianId.isNullOrEmpty()) {
            loadConsentStatus()
        } else {
            showMessage("Guardian identity missing. Please log in again.")
        }

        return view
    }

    private fun loadConsentStatus() {
        val ref = database.getReference("consent_status/$householdId/$guardianId")

        consentListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                containerLayout.removeAllViews()
                if (!snapshot.exists()) {
                    showMessage("No consent records found.")
                    return
                }

                snapshot.children.forEach { platformSnapshot ->
                    val platform = platformSnapshot.key ?: return@forEach
                    val granted = platformSnapshot.getValue(Boolean::class.java) ?: false

                    val statusColor = if (granted) {
                        ContextCompat.getColor(requireContext(), R.color.green)
                    } else {
                        ContextCompat.getColor(requireContext(), R.color.red)
                    }

                    val card = MaterialCardView(requireContext()).apply {
                        setContentPadding(24, 16, 24, 16)
                        radius = 12f
                        cardElevation = 6f
                        val statusTextView = TextView(requireContext()).apply {
                            text = "$platform: ${if (granted) "Granted ✅" else "Revoked ❌"}"
                            setTextColor(statusColor)
                            textSize = 16f
                        }
                        addView(statusTextView)
                    }
                    containerLayout.addView(card)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showMessage("Failed to load consent status: ${error.message}")
            }
        }

        ref.addValueEventListener(consentListener as ValueEventListener)
    }

    private fun showMessage(message: String) {
        val errorText = TextView(requireContext()).apply {
            text = message
            setPadding(16, 16, 16, 16)
        }
        containerLayout.addView(errorText)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        containerLayout.removeAllViews()
        // Remove listener to avoid leaks
        if (!householdId.isNullOrEmpty() && !guardianId.isNullOrEmpty() && consentListener != null) {
            val ref = database.getReference("consent_status/$householdId/$guardianId")
            ref.removeEventListener(consentListener!!)
        }
    }
}