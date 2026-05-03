package com.mygoal_healthtracker.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        MealEntity::class,
        MealOptionEntity::class,
        FoodItemEntity::class,
        MealLogEntity::class,
        WaterLogEntity::class,
        DailyWaterGoalEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun healthDao(): HealthDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_goal_health_tracker.db",
                ).build().also { instance = it }
            }
        }
    }
}
