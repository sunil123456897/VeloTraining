package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        CustomWorkoutEntity::class,
        WorkoutSeriesBlockEntity::class,
        WorkoutIntervalStepEntity::class,
        BikeSessionLogEntity::class,
        CalisthenicsLogEntity::class,
        BandMaxSetEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(CockpitTypeConverters::class)
abstract class CockpitDatabase : RoomDatabase() {
    abstract fun cockpitDao(): CockpitDao

    companion object {
        @Volatile
        private var INSTANCE: CockpitDatabase? = null

        fun getInstance(context: Context): CockpitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CockpitDatabase::class.java,
                    "ovicx_q200_cockpit.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
