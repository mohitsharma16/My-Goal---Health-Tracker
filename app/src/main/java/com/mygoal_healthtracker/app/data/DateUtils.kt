package com.mygoal_healthtracker.app.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val dateFormat = ThreadLocal.withInitial {
    SimpleDateFormat("yyyy-MM-dd", Locale.US)
}

private fun formatter(): SimpleDateFormat = checkNotNull(dateFormat.get())

fun today(): String = formatter().format(Date())

fun millisToDate(millis: Long): String = formatter().format(Date(millis))

fun dateRangeFor(date: String, range: ProgressRange): DateRange {
    val calendar = Calendar.getInstance().apply {
        time = formatter().parse(date) ?: Date()
    }
    return when (range) {
        ProgressRange.Daily -> DateRange(date, date)
        ProgressRange.Weekly -> {
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            val start = formatter().format(calendar.time)
            calendar.add(Calendar.DAY_OF_WEEK, 6)
            DateRange(start, formatter().format(calendar.time))
        }
        ProgressRange.Monthly -> {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val start = formatter().format(calendar.time)
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            DateRange(start, formatter().format(calendar.time))
        }
    }
}

enum class ProgressRange(val label: String) {
    Daily("Daily"),
    Weekly("Weekly"),
    Monthly("Monthly"),
}

data class DateRange(val start: String, val end: String)

data class NutritionTotals(
    val calories: Float = 0f,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
)

data class ProgressStats(
    val range: ProgressRange = ProgressRange.Daily,
    val dateRange: DateRange = DateRange(today(), today()),
    val nutrition: NutritionTotals = NutritionTotals(),
    val waterMl: Int = 0,
)
