package com.mygoal_healthtracker.app.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupValidationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun backupRoundTripPreservesMealHierarchy() {
        val backup = AppBackup(
            profile = UserProfileEntity(name = "Mohit", heightCm = 175f, weightKg = 72f),
            meals = listOf(MealEntity(id = 1, name = "Breakfast", time = "08:30", reminderEnabled = true)),
            mealOptions = listOf(MealOptionEntity(id = 2, mealId = 1, name = "Poha")),
            foodItems = listOf(
                FoodItemEntity(
                    id = 3,
                    mealOptionId = 2,
                    name = "Poha",
                    quantity = "1 bowl",
                    calories = 250f,
                    protein = 7f,
                    carbs = 40f,
                    fat = 8f,
                ),
            ),
            mealLogs = listOf(MealLogEntity(mealId = 1, selectedOptionId = 2, date = "2026-05-03", status = "COMPLETED", timestamp = 1L)),
            waterLogs = listOf(WaterLogEntity(amountMl = 500, date = "2026-05-03", timestamp = 2L)),
            waterGoal = DailyWaterGoalEntity(goalMl = 2500),
        )

        val restored = json.decodeFromString<AppBackup>(json.encodeToString(AppBackup.serializer(), backup))

        assertEquals("Breakfast", restored.meals.first().name)
        assertEquals("Poha", restored.mealOptions.first().name)
        assertEquals(500, restored.waterLogs.first().amountMl)
    }
}
