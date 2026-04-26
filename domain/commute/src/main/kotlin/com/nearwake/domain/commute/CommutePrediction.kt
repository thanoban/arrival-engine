package com.nearwake.domain.commute

import kotlinx.serialization.Serializable

@Serializable
data class CommutePrediction(
    val id: String,
    val originId: String?,
    val destinationId: String,
    val destinationName: String,
    val daysOfWeek: Set<Int>,
    val typicalDepartureHour: Int,
    val typicalDepartureMinute: Int,
    val avgDurationMinutes: Int,
    val tripCount: Int,
)
