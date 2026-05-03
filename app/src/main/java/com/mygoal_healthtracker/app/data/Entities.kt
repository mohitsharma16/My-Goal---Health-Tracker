package com.mygoal_healthtracker.app.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val heightCm: Float,
    val weightKg: Float,
)

@Serializable
@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val time: String,
    val reminderEnabled: Boolean,
)

@Serializable
@Entity(
    tableName = "meal_options",
    foreignKeys = [
        ForeignKey(
            entity = MealEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("mealId")],
)
data class MealOptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealId: Long,
    val name: String,
)

@Serializable
@Entity(
    tableName = "food_items",
    foreignKeys = [
        ForeignKey(
            entity = MealOptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealOptionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("mealOptionId")],
)
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealOptionId: Long,
    val name: String,
    val quantity: String,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
)

@Serializable
@Entity(
    tableName = "meal_logs",
    indices = [Index(value = ["mealId", "date"], unique = true), Index("selectedOptionId")],
)
data class MealLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealId: Long,
    val selectedOptionId: Long,
    val date: String,
    val status: String,
    val timestamp: Long,
)

@Serializable
@Entity(tableName = "water_logs", indices = [Index("date")])
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int,
    val date: String,
    val timestamp: Long,
)

@Serializable
@Entity(tableName = "water_goal")
data class DailyWaterGoalEntity(
    @PrimaryKey val id: Int = 1,
    val goalMl: Int,
)

data class MealWithOptions(
    @Embedded val meal: MealEntity,
    @Relation(
        entity = MealOptionEntity::class,
        parentColumn = "id",
        entityColumn = "mealId",
    )
    val options: List<OptionWithFood>,
)

data class OptionWithFood(
    @Embedded val option: MealOptionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "mealOptionId",
    )
    val foodItems: List<FoodItemEntity>,
)

@Serializable
data class Nutrition(
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
)

@Serializable
data class AppBackup(
    val version: Int = 1,
    val profile: UserProfileEntity?,
    val meals: List<MealEntity>,
    val mealOptions: List<MealOptionEntity>,
    val foodItems: List<FoodItemEntity>,
    val mealLogs: List<MealLogEntity>,
    val waterLogs: List<WaterLogEntity>,
    val waterGoal: DailyWaterGoalEntity?,
)
