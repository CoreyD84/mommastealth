package com.airnettie.mobile.features.guardian.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.airnettie.mobile.features.guardian.tabs.SafeScopeToggle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

class SafeScopeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val guardianId = FirebaseAuth.getInstance().currentUser?.uid

                val childId by produceState<String?>(initialValue = null, guardianId) {
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
                        Log.e("SafeScopeFragment", "Failed to fetch childId", e)
                    }
                }

                SafeScopeToggle(childId = childId)
            }
        }
    }
}