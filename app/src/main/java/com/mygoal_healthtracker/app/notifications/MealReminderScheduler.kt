package com.mygoal_healthtracker.app.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object MealReminderScheduler {
    fun schedule(context: Context, mealId: Long, mealName: String, time: String, enabled: Boolean) {
        val workName = workName(mealId)
        val workManager = WorkManager.getInstance(context)
        if (!enabled) {
            workManager.cancelUniqueWork(workName)
            return
        }
        val delay = delayUntilReminder(time)
        val request = PeriodicWorkRequestBuilder<MealReminderWorker>(1, TimeUnit.DAYS)
            .setInputData(Data.Builder().putString(MealReminderWorker.KEY_MEAL_NAME, mealName).build())
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(workName, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun cancel(context: Context, mealId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(mealId))
    }

    fun cancelAll(context: Context, mealIds: List<Long>) {
        mealIds.forEach { cancel(context, it) }
    }

    private fun delayUntilReminder(time: String): Long {
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, -10)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }

    private fun workName(mealId: Long): String = "meal_reminder_$mealId"
}
