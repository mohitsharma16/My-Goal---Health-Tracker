package com.mygoal_healthtracker.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)

    @Transaction
    @Query("SELECT * FROM meals ORDER BY time")
    fun observeMeals(): Flow<List<MealWithOptions>>

    @Transaction
    @Query("SELECT * FROM meals ORDER BY time")
    suspend fun getMealsWithOptions(): List<MealWithOptions>

    @Query("SELECT * FROM meals ORDER BY time")
    suspend fun getMeals(): List<MealEntity>

    @Query("SELECT * FROM meal_options ORDER BY id")
    suspend fun getMealOptions(): List<MealOptionEntity>

    @Query("SELECT * FROM food_items ORDER BY id")
    suspend fun getFoodItems(): List<FoodItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeal(meal: MealEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMealOption(option: MealOptionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFoodItem(foodItem: FoodItemEntity): Long

    @Query("DELETE FROM meals WHERE id = :mealId")
    suspend fun deleteMeal(mealId: Long)

    @Query("DELETE FROM meal_options WHERE mealId = :mealId")
    suspend fun deleteMealOptions(mealId: Long)

    @Query("SELECT * FROM meal_options WHERE mealId = :mealId")
    suspend fun getOptionsForMeal(mealId: Long): List<MealOptionEntity>

    @Query("DELETE FROM meal_options WHERE id = :optionId")
    suspend fun deleteMealOption(optionId: Long)

    @Query("DELETE FROM food_items WHERE mealOptionId = :optionId")
    suspend fun deleteFoodItemsForOption(optionId: Long)

    @Query("DELETE FROM meal_logs WHERE mealId = :mealId")
    suspend fun deleteLogsForMeal(mealId: Long)

    @Query("SELECT * FROM meal_logs WHERE date = :date")
    fun observeMealLogs(date: String): Flow<List<MealLogEntity>>

    @Query("SELECT * FROM meal_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    fun observeMealLogsBetween(startDate: String, endDate: String): Flow<List<MealLogEntity>>

    @Query("SELECT * FROM meal_logs ORDER BY date DESC, timestamp DESC")
    suspend fun getMealLogs(): List<MealLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMealLog(log: MealLogEntity)

    @Query("DELETE FROM meal_logs WHERE mealId = :mealId AND date = :date")
    suspend fun clearMealLog(mealId: Long, date: String)

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_logs WHERE date = :date")
    fun observeWaterTotal(date: String): Flow<Int>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_logs WHERE date BETWEEN :startDate AND :endDate")
    fun observeWaterTotalBetween(startDate: String, endDate: String): Flow<Int>

    @Query("SELECT * FROM water_logs ORDER BY date DESC, timestamp DESC")
    suspend fun getWaterLogs(): List<WaterLogEntity>

    @Insert
    suspend fun insertWaterLog(log: WaterLogEntity)

    @Query("SELECT * FROM water_goal WHERE id = 1")
    fun observeWaterGoal(): Flow<DailyWaterGoalEntity?>

    @Query("SELECT * FROM water_goal WHERE id = 1")
    suspend fun getWaterGoal(): DailyWaterGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWaterGoal(goal: DailyWaterGoalEntity)

    @Query("DELETE FROM user_profile")
    suspend fun clearProfile()

    @Query("DELETE FROM meals")
    suspend fun clearMeals()

    @Query("DELETE FROM meal_logs")
    suspend fun clearMealLogs()

    @Query("DELETE FROM water_logs")
    suspend fun clearWaterLogs()

    @Query("DELETE FROM water_goal")
    suspend fun clearWaterGoal()
}
