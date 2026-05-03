package com.mygoal_healthtracker.app.ui

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mygoal_healthtracker.app.data.DraftMealOption
import com.mygoal_healthtracker.app.data.FoodItemEntity
import com.mygoal_healthtracker.app.data.HealthRepository
import com.mygoal_healthtracker.app.data.MealEntity
import com.mygoal_healthtracker.app.data.MealLogEntity
import com.mygoal_healthtracker.app.data.MealWithOptions
import com.mygoal_healthtracker.app.data.NutritionTotals
import com.mygoal_healthtracker.app.data.ProgressRange
import com.mygoal_healthtracker.app.data.ProgressStats
import com.mygoal_healthtracker.app.data.UserProfileEntity
import com.mygoal_healthtracker.app.data.dateRangeFor
import com.mygoal_healthtracker.app.data.millisToDate
import com.mygoal_healthtracker.app.data.today
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class HealthTrackerViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val repository = HealthRepository(application)
    private val selectedDate = MutableStateFlow(today())
    private val progressRange = MutableStateFlow(ProgressRange.Daily)
    private val clock = MutableStateFlow(clockText())
    private val dateRange = combine(selectedDate, progressRange) { date, range ->
        dateRangeFor(date, range)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, dateRangeFor(today(), ProgressRange.Daily))
    private val selectedLogs = selectedDate.flatMapLatest(repository::observeMealLogs)
    private val selectedWaterTotal = selectedDate.flatMapLatest(repository::observeWaterTotal)
    private val rangeLogs = dateRange.flatMapLatest { range ->
        repository.observeMealLogsBetween(range.start, range.end)
    }
    private val rangeWaterTotal = dateRange.flatMapLatest { range ->
        repository.observeWaterTotalBetween(range.start, range.end)
    }

    private val selectedDayData = combine(
        repository.profile,
        repository.meals,
        selectedLogs,
        selectedWaterTotal,
        repository.waterGoal,
    ) { profile, meals, logs, waterTotal, waterGoal ->
        SelectedDayData(
            profile = profile,
            meals = meals,
            logs = logs,
            waterTotalMl = waterTotal,
            waterGoalMl = waterGoal?.goalMl ?: 2500,
        )
    }

    private val progressData = combine(
        rangeLogs,
        rangeWaterTotal,
        progressRange,
        dateRange,
    ) { logs, waterTotal, range, dateRange ->
        RangeData(logs, waterTotal, range, dateRange)
    }

    private val dataState = combine(selectedDayData, progressData) { selected, progress ->
        val progressNutrition = nutritionFor(progress.logs, selected.meals)
        DataState(
            profile = selected.profile,
            meals = selected.meals,
            logs = selected.logs,
            waterTotalMl = selected.waterTotalMl,
            waterGoalMl = selected.waterGoalMl,
            progressStats = ProgressStats(
                range = progress.range,
                dateRange = progress.dateRange,
                nutrition = progressNutrition,
                waterMl = progress.waterTotalMl,
            ),
        )
    }

    val state = combine(dataState, selectedDate, clock) { data, date, time ->
        DashboardState(
            profile = data.profile,
            meals = data.meals,
            logs = data.logs,
            waterTotalMl = data.waterTotalMl,
            waterGoalMl = data.waterGoalMl,
            selectedDate = date,
            clock = time,
            progressStats = data.progressStats,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())

    init {
        viewModelScope.launch {
            while (true) {
                clock.value = clockText()
                delay(1_000)
            }
        }
    }

    fun saveProfile(name: String, height: String, weight: String) = viewModelScope.launch {
        repository.saveProfile(name, height.toFloatOrNull() ?: 0f, weight.toFloatOrNull() ?: 0f)
    }

    fun addWater(amountMl: Int) = viewModelScope.launch { repository.addWater(amountMl, selectedDate.value) }

    fun setWaterGoal(goal: String) = viewModelScope.launch { repository.setWaterGoal(goal.toIntOrNull() ?: 0) }

    fun addMeal(name: String, time: String, reminder: Boolean, options: List<DraftMealOption>) = viewModelScope.launch {
        repository.saveMeal(name, time, reminder, options)
    }

    fun updateMeal(mealId: Long, name: String, time: String, reminder: Boolean, options: List<DraftMealOption>) = viewModelScope.launch {
        repository.saveMeal(name, time, reminder, options, mealId)
    }

    fun deleteMeal(meal: MealEntity) = viewModelScope.launch { repository.deleteMeal(meal) }

    fun completeMeal(mealId: Long, optionId: Long) = viewModelScope.launch {
        repository.logMeal(mealId, optionId, "COMPLETED", selectedDate.value)
    }

    fun skipMeal(mealId: Long) = viewModelScope.launch {
        repository.logMeal(mealId, 0L, "SKIPPED", selectedDate.value)
    }

    fun clearMeal(mealId: Long) = viewModelScope.launch { repository.clearMealLog(mealId, selectedDate.value) }

    fun importJson(rawJson: String) = viewModelScope.launch { repository.importJson(rawJson) }

    fun exportJson(onReady: (String) -> Unit) = viewModelScope.launch {
        onReady(repository.exportJson())
    }

    fun selectDate(millis: Long?) {
        if (millis != null) {
            selectedDate.value = millisToDate(millis)
        }
    }

    fun setProgressRange(range: ProgressRange) {
        progressRange.value = range
    }

    private fun clockText(): String = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())

    private fun nutritionFor(logs: List<MealLogEntity>, meals: List<MealWithOptions>): NutritionTotals {
        val food = logs.filter { it.status == "COMPLETED" }.flatMap { log ->
            meals.flatMap { it.options }.firstOrNull { it.option.id == log.selectedOptionId }?.foodItems.orEmpty()
        }
        return NutritionTotals(
            calories = food.sumOf { it.calories.toDouble() }.toFloat(),
            protein = food.sumOf { it.protein.toDouble() }.toFloat(),
            carbs = food.sumOf { it.carbs.toDouble() }.toFloat(),
            fat = food.sumOf { it.fat.toDouble() }.toFloat(),
        )
    }
}

class HealthTrackerFactory(private val application: android.app.Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HealthTrackerViewModel(application) as T
    }
}

private data class DataState(
    val profile: UserProfileEntity?,
    val meals: List<MealWithOptions>,
    val logs: List<MealLogEntity>,
    val waterTotalMl: Int,
    val waterGoalMl: Int,
    val progressStats: ProgressStats,
)

private data class SelectedDayData(
    val profile: UserProfileEntity?,
    val meals: List<MealWithOptions>,
    val logs: List<MealLogEntity>,
    val waterTotalMl: Int,
    val waterGoalMl: Int,
)

private data class RangeData(
    val logs: List<MealLogEntity>,
    val waterTotalMl: Int,
    val range: ProgressRange,
    val dateRange: com.mygoal_healthtracker.app.data.DateRange,
)

data class DashboardState(
    val profile: UserProfileEntity? = null,
    val meals: List<MealWithOptions> = emptyList(),
    val logs: List<MealLogEntity> = emptyList(),
    val waterTotalMl: Int = 0,
    val waterGoalMl: Int = 2500,
    val selectedDate: String = today(),
    val clock: String = "--:--",
    val progressStats: ProgressStats = ProgressStats(),
) {
    val calories: Float get() = selectedFood.sumOf { it.calories.toDouble() }.toFloat()
    val protein: Float get() = selectedFood.sumOf { it.protein.toDouble() }.toFloat()
    val carbs: Float get() = selectedFood.sumOf { it.carbs.toDouble() }.toFloat()
    val fat: Float get() = selectedFood.sumOf { it.fat.toDouble() }.toFloat()
    val completedMeals: Int get() = logs.count { it.status == "COMPLETED" }
    val waterProgress: Float get() = (waterTotalMl / waterGoalMl.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    private val selectedFood: List<FoodItemEntity>
        get() = logs.filter { it.status == "COMPLETED" }.flatMap { log ->
            meals.flatMap { it.options }.firstOrNull { it.option.id == log.selectedOptionId }?.foodItems.orEmpty()
        }
}
