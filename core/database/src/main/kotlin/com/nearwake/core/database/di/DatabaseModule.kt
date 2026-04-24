package com.nearwake.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nearwake.core.database.NearWakeDatabase
import com.nearwake.core.database.dao.AlertEventDao
import com.nearwake.core.database.dao.DiagnosticsEventDao
import com.nearwake.core.database.dao.RouteSnapshotDao
import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.dao.TripDao
import com.nearwake.core.database.dao.TripSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideNearWakeDatabase(
        @ApplicationContext context: Context,
    ): NearWakeDatabase = Room.databaseBuilder(
        context,
        NearWakeDatabase::class.java,
        NearWakeDatabase.DATABASE_NAME,
    ).addMigrations(MIGRATION_1_2)
        .addMigrations(MIGRATION_2_3)
        .addMigrations(MIGRATION_3_4)
        .build()

    @Provides
    fun provideTripDao(database: NearWakeDatabase): TripDao = database.tripDao()

    @Provides
    fun provideTripSessionDao(database: NearWakeDatabase): TripSessionDao = database.tripSessionDao()

    @Provides
    fun provideSavedPlaceDao(database: NearWakeDatabase): SavedPlaceDao = database.savedPlaceDao()

    @Provides
    fun provideAlertEventDao(database: NearWakeDatabase): AlertEventDao = database.alertEventDao()

    @Provides
    fun provideDiagnosticsEventDao(database: NearWakeDatabase): DiagnosticsEventDao =
        database.diagnosticsEventDao()

    @Provides
    fun provideRouteSnapshotDao(database: NearWakeDatabase): RouteSnapshotDao =
        database.routeSnapshotDao()

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `route_snapshots` (
                    `trip_id` TEXT NOT NULL,
                    `stops_json` TEXT NOT NULL,
                    `transfers_json` TEXT NOT NULL,
                    `total_duration_minutes` INTEGER NOT NULL,
                    `fetched_at` TEXT NOT NULL,
                    `is_stale` INTEGER NOT NULL,
                    PRIMARY KEY(`trip_id`)
                )
                """.trimIndent(),
            )
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                ALTER TABLE `trips`
                ADD COLUMN `alert_mode` TEXT NOT NULL DEFAULT 'ACTIVE'
                """.trimIndent(),
            )
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                ALTER TABLE `trip_sessions`
                ADD COLUMN `alert_stage` TEXT NOT NULL DEFAULT 'MONITORING'
                """.trimIndent(),
            )
        }
    }
}
