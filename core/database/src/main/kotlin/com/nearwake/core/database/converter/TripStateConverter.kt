package com.nearwake.core.database.converter

import androidx.room.TypeConverter
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.AlertType
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.TripState

class TripStateConverter {
    @TypeConverter
    fun fromTripState(value: TripState): String = value.name

    @TypeConverter
    fun toTripState(value: String): TripState = TripState.valueOf(value)

    @TypeConverter
    fun fromMonitoringMode(value: MonitoringMode): String = value.name

    @TypeConverter
    fun toMonitoringMode(value: String): MonitoringMode = MonitoringMode.valueOf(value)

    @TypeConverter
    fun fromConfidence(value: Confidence): String = value.name

    @TypeConverter
    fun toConfidence(value: String): Confidence = Confidence.valueOf(value)

    @TypeConverter
    fun fromAlertIntensity(value: AlertIntensity): String = value.name

    @TypeConverter
    fun toAlertIntensity(value: String): AlertIntensity = AlertIntensity.valueOf(value)

    @TypeConverter
    fun fromAlertMode(value: AlertMode): String = value.name

    @TypeConverter
    fun toAlertMode(value: String): AlertMode = AlertMode.valueOf(value)

    @TypeConverter
    fun fromAlertStage(value: AlertStage): String = value.name

    @TypeConverter
    fun toAlertStage(value: String): AlertStage = AlertStage.valueOf(value)

    @TypeConverter
    fun fromAlertType(value: AlertType): String = value.name

    @TypeConverter
    fun toAlertType(value: String): AlertType = AlertType.valueOf(value)
}
