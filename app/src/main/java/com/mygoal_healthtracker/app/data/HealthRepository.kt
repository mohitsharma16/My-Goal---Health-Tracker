package com.mygoal_healthtracker.app.data

import android.content.Context
import androidx.room.withTransaction
import com.mygoal_healthtracker.app.notifications.MealReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class HealthRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.get(context),
    private val dao: HealthDao = database.healthDao(),
    private val nutritionService: GeminiNutritionService = GeminiNutritionService(),
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = true },
) {
    val profile: Flow<UserProfileEntity?> = dao.observeProfile()
    val meals: Flow<List<MealWithOptions>> = dao.observeMeals()
    val waterGoal: Flow<DailyWaterGoalEntity?> = dao.observeWaterGoal()

    fun observeMealLogs(date: String): Flow<List<MealLogEntity>> = dao.observeMealLogs(date)

    fun observeMealLogsBetween(startDate: String, endDate: String): Flow<List<MealLogEntity>> {
        return dao.observeMealLogsBetween(startDate, endDate)
    }

    fun observeWaterTotal(date: String): Flow<Int> = dao.observeWaterTotal(date)

    fun observeWaterTotalBetween(startDate: String, endDate: String): Flow<Int> {
        return dao.observeWaterTotalBetween(startDate, endDate)
    }

    suspend fun saveProfile(name: String, heightCm: Float, weightKg: Float) {
        dao.upsertProfile(UserProfileEntity(name = name.trim(), heightCm = heightCm, weightKg = weightKg))
        seedStarterPlanIfNeeded()
    }

    suspend fun setWaterGoal(goalMl: Int) {
        if (goalMl > 0) dao.upsertWaterGoal(DailyWaterGoalEntity(goalMl = goalMl))
    }

    suspend fun addWater(amountMl: Int, date: String) {
        if (amountMl > 0) {
            dao.insertWaterLog(WaterLogEntity(amountMl = amountMl, date = date, timestamp = System.currentTimeMillis()))
        }
    }

    suspend fun saveMeal(
        mealName: String,
        time: String,
        reminderEnabled: Boolean,
        options: List<DraftMealOption>,
        mealId: Long = 0L,
    ) {
        if (mealName.isBlank() || options.isEmpty()) return
        val savedMealId = dao.upsertMeal(
            MealEntity(
                id = mealId,
                name = mealName.trim(),
                time = time,
                reminderEnabled = reminderEnabled,
            ),
        )

        val nutritionCache = dao.getFoodItems().associateBy { foodKey(it.name, it.quantity) }
        val existingOptions = if (mealId != 0L) {
            dao.getOptionsForMeal(savedMealId).associateBy { it.name.lowercase().trim() }
        } else {
            emptyMap()
        }
        val keptOptionIds = mutableSetOf<Long>()

        options.filter { it.name.isNotBlank() }.forEach { option ->
            val match = existingOptions[option.name.lowercase().trim()]
            val optionId = if (match != null) {
                dao.deleteFoodItemsForOption(match.id)
                match.id
            } else {
                dao.upsertMealOption(MealOptionEntity(mealId = savedMealId, name = option.name.trim()))
            }
            keptOptionIds.add(optionId)
            option.foodItems.filter { it.name.isNotBlank() && it.quantity.isNotBlank() }.forEach { food ->
                val cached = nutritionCache[foodKey(food.name, food.quantity)]
                val nutrition = cached?.let {
                    Nutrition(it.calories, it.protein, it.carbs, it.fat)
                } ?: nutritionService.estimateFood(food.name, food.quantity)
                dao.upsertFoodItem(
                    FoodItemEntity(
                        mealOptionId = optionId,
                        name = food.name.trim(),
                        quantity = food.quantity.trim(),
                        calories = nutrition.calories,
                        protein = nutrition.protein,
                        carbs = nutrition.carbs,
                        fat = nutrition.fat,
                    ),
                )
            }
        }

        existingOptions.values
            .filter { it.id !in keptOptionIds }
            .forEach { dao.deleteMealOption(it.id) }

        MealReminderScheduler.schedule(context, savedMealId, mealName, time, reminderEnabled)
    }

    private fun foodKey(name: String, quantity: String): String =
        "${name.lowercase().trim()}|${quantity.lowercase().trim()}"

    suspend fun deleteMeal(meal: MealEntity) {
        MealReminderScheduler.cancel(context, meal.id)
        dao.deleteMeal(meal.id)
    }

    suspend fun logMeal(mealId: Long, optionId: Long, status: String, date: String) {
        dao.upsertMealLog(
            MealLogEntity(
                mealId = mealId,
                selectedOptionId = optionId,
                date = date,
                status = status,
                timestamp = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun clearMealLog(mealId: Long, date: String) {
        dao.clearMealLog(mealId, date)
    }

    suspend fun exportJson(): String {
        return json.encodeToString(
            AppBackup(
                profile = dao.getProfile(),
                meals = dao.getMeals(),
                mealOptions = dao.getMealOptions(),
                foodItems = dao.getFoodItems(),
                mealLogs = dao.getMealLogs(),
                waterLogs = dao.getWaterLogs(),
                waterGoal = dao.getWaterGoal(),
            ),
        )
    }

    suspend fun importJson(rawJson: String): Result<Unit> {
        return runCatching {
            val backup = json.decodeFromString<AppBackup>(rawJson)
            validateBackup(backup)
            database.withTransaction {
                MealReminderScheduler.cancelAll(context, backup.meals.map { it.id })
                dao.clearProfile()
                dao.clearMeals()
                dao.clearMealLogs()
                dao.clearWaterLogs()
                dao.clearWaterGoal()
                backup.profile?.let { dao.upsertProfile(it) }
                backup.waterGoal?.let { dao.upsertWaterGoal(it) }
                backup.meals.forEach { dao.upsertMeal(it) }
                backup.mealOptions.forEach { dao.upsertMealOption(it) }
                backup.foodItems.forEach { dao.upsertFoodItem(it) }
                backup.mealLogs.forEach { dao.upsertMealLog(it) }
                backup.waterLogs.forEach { dao.insertWaterLog(it) }
            }
            backup.meals.forEach { MealReminderScheduler.schedule(context, it.id, it.name, it.time, it.reminderEnabled) }
        }.mapError { error ->
            if (error is SerializationException || error is IllegalArgumentException) {
                IllegalArgumentException("Invalid backup JSON")
            } else {
                error
            }
        }
    }

    private suspend fun seedStarterPlanIfNeeded() {
        if (dao.getMeals().isNotEmpty()) return
        dao.upsertWaterGoal(DailyWaterGoalEntity(goalMl = 2500))
        seedMeal(
            "Breakfast",
            "08:30",
            listOf(
                DraftMealOption("Paneer Sandwich", listOf(DraftFoodItem("Paneer sandwich", "2 pieces"))),
                DraftMealOption("Poha Bowl", listOf(DraftFoodItem("Poha", "1 bowl"))),
            ),
        )
        seedMeal(
            "Lunch",
            "13:00",
            listOf(
                DraftMealOption("Dal Rice", listOf(DraftFoodItem("Dal", "1 bowl"), DraftFoodItem("Rice", "1 bowl"))),
                DraftMealOption("Roti Sabzi", listOf(DraftFoodItem("Roti", "2 pieces"), DraftFoodItem("Mixed vegetables", "1 bowl"))),
            ),
        )
        seedMeal(
            "Dinner",
            "20:00",
            listOf(
                DraftMealOption("Moong Dal Dosa", listOf(DraftFoodItem("Moong dal dosa", "2 pieces"), DraftFoodItem("Vegetables", "1 bowl"))),
                DraftMealOption("Khichdi", listOf(DraftFoodItem("Vegetable khichdi", "1 bowl"))),
            ),
        )
    }

    private suspend fun seedMeal(name: String, time: String, options: List<DraftMealOption>) {
        val mealId = dao.upsertMeal(MealEntity(name = name, time = time, reminderEnabled = true))
        options.forEach { option ->
            val optionId = dao.upsertMealOption(MealOptionEntity(mealId = mealId, name = option.name))
            option.foodItems.forEach { food ->
                val nutrition = nutritionService.offlineEstimate(food.name, food.quantity)
                dao.upsertFoodItem(
                    FoodItemEntity(
                        mealOptionId = optionId,
                        name = food.name,
                        quantity = food.quantity,
                        calories = nutrition.calories,
                        protein = nutrition.protein,
                        carbs = nutrition.carbs,
                        fat = nutrition.fat,
                    ),
                )
            }
        }
        MealReminderScheduler.schedule(context, mealId, name, time, true)
    }

    private fun validateBackup(backup: AppBackup) {
        val mealIds = backup.meals.map { it.id }.toSet()
        val optionIds = backup.mealOptions.map { it.id }.toSet()
        require(backup.version == 1)
        require(backup.mealOptions.all { it.mealId in mealIds })
        require(backup.foodItems.all { it.mealOptionId in optionIds })
        require(backup.mealLogs.all { it.mealId in mealIds && (it.selectedOptionId == 0L || it.selectedOptionId in optionIds) })
    }
}

data class DraftFoodItem(val name: String, val quantity: String)

data class DraftMealOption(val name: String, val foodItems: List<DraftFoodItem>)

private inline fun <T> Result<T>.mapError(transform: (Throwable) -> Throwable): Result<T> {
    return fold(onSuccess = { Result.success(it) }, onFailure = { Result.failure(transform(it)) })
}
