package com.nearwake.core.remoteconfig

data class ThresholdConfig(
    val approachGeofenceRadiusM: Float = 1_500f,
    val approachGeofenceBatterySaverM: Float = 2_250f,
    val biasMultiplierHighActive: Double = 1.0,
    val biasMultiplierHighSleep: Double = 1.05,
    val biasMultiplierDegradedActive: Double = 1.15,
    val biasMultiplierDegradedSleep: Double = 1.20,
    val biasMultiplierOffline: Double = 1.25,
    val imminentEtaMinutes: Double = 2.0,
    val imminentDistanceMeters: Double = 150.0,
    val minTripsForClustering: Int = 2,
)
