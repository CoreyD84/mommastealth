package com.airnettie.mobile.safescope

import com.airnettie.mobile.safescope.EscalationMatrix.Severity

data class Flag(
    val caseId: String,
    val severity: Severity,
    val message: String,
    val source: String,
    val timestamp: Long,
    val scope: String
)
