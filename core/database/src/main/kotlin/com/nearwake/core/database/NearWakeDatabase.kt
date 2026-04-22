package com.nearwake.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nearwake.core.database.converter.InstantConverter
import com.nearwake.core.database.converter.ListConverter
import com.nearwake.core.database.converter.TripStateConverter
import com.nearwake.core.database.dao.AlertEventDao
import com.nearwake.core.database.dao.DiagnosticsEventDao
import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.dao.TripDao
import com.nearwake.core.database.dao.TripSessionDao
import com.nearwake.core.database.entity.AlertEventEntity
import com.nearwake.core.database.entity.DiagnosticsEventEntity
import com.nearwake.core.database.entity.SavedPlaceEntity
import com.nearwake.core.database.entity.TripEntity
import com.nearwake.core.database.entity.TripSessionEntity

@Database(
    entities = [
        TripEntity::class,
        TripSessionEntity::class,
        SavedPlaceEntity::class,
        AlertEventEntity::class,
        DiagnosticsEventEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(
    InstantConverter::class,
    ListConverter::class,
    TripStateConverter::class,
)
abstract class NearWakeDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao

    abstract fun tripSessionDao(): TripSessionDao

    abstract fun savedPlaceDao(): SavedPlaceDao

    abstract fun alertEventDao(): AlertEventDao

    abstract fun diagnosticsEventDao(): DiagnosticsEventDao

    companion object {
        const val DATABASE_NAME = "nearwake.db"
    }
}
