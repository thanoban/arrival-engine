package com.nearwake.domain.trip.model

enum class AlertTriggerMode {
    TIME,
    DISTANCE,
    BOTH,
    ;

    fun usesTimeTrigger(): Boolean = this != DISTANCE

    fun usesDistanceTrigger(): Boolean = this != TIME
}
