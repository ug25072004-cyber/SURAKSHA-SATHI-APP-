package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        IncidentEntity::class,
        ResourceEntity::class,
        AllocationLogEntity::class,
        EmergencyContactEntity::class,
        EmergencyResourceEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun incidentDao(): IncidentDao
    abstract fun resourceDao(): ResourceDao
    abstract fun allocationLogDao(): AllocationLogDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun emergencyResourceDao(): EmergencyResourceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "surakshasathi_offline_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
