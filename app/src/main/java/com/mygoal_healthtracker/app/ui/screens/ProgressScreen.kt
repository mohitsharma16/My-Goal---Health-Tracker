package com.mygoal_healthtracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mygoal_healthtracker.app.data.ProgressRange
import com.mygoal_healthtracker.app.ui.DashboardState
import com.mygoal_healthtracker.app.ui.components.ChartCard
import com.mygoal_healthtracker.app.ui.components.SectionHeader
import com.mygoal_healthtracker.app.ui.components.StatPill
import com.mygoal_healthtracker.app.ui.components.WavyLinearProgress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    state: DashboardState,
    onSelectDate: (Long?) -> Unit,
    onSetRange: (ProgressRange) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val progress = state.progressStats
    val bars = listOf(
        progress.nutrition.calories,
        progress.nutrition.protein * 8,
        progress.nutrition.carbs * 4,
        progress.nutrition.fat * 9,
        progress.waterMl / 5f,
    )
    val calorieGoal = 2200f
    val waterGoal = state.waterGoalMl.toFloat().coerceAtLeast(1f)
    val periodCalorieGoal = when (progress.range) {
        ProgressRange.Daily -> calorieGoal
        ProgressRange.Weekly -> calorieGoal * 7
        ProgressRange.Monthly -> calorieGoal * 30
    }
    val periodWaterGoal = when (progress.range) {
        ProgressRange.Daily -> waterGoal
        ProgressRange.Weekly -> waterGoal * 7
        ProgressRange.Monthly -> waterGoal * 30
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SectionHeader(
                title = "Progress",
                subtitle = "${progress.dateRange.start} → ${progress.dateRange.end}",
            )
        }
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(
                    Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        FilledTonalButton(
                            onClick = { showDatePicker = true },
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            Text("  Pick date", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        ProgressRange.entries.forEachIndexed { index, value ->
                            SegmentedButton(
                                selected = progress.range == value,
                                onClick = { onSetRange(value) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = ProgressRange.entries.size,
                                ),
                            ) {
                                Text(value.label)
                            }
                        }
                    }
                }
            }
        }
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        "Goal progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    GoalRow(
                        label = "Calories",
                        value = "${progress.nutrition.calories.toInt()} / ${periodCalorieGoal.toInt()} kcal",
                        progress = (progress.nutrition.calories / periodCalorieGoal).coerceIn(0f, 1f),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    GoalRow(
                        label = "Water",
                        value = "${progress.waterMl} / ${periodWaterGoal.toInt()} ml",
                        progress = (progress.waterMl / periodWaterGoal).coerceIn(0f, 1f),
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                StatPill(
                    label = "Protein",
                    value = "${progress.nutrition.protein.toInt()}g",
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.primary,
                )
                StatPill(
                    label = "Carbs",
                    value = "${progress.nutrition.carbs.toInt()}g",
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.secondary,
                )
                StatPill(
                    label = "Fat",
                    value = "${progress.nutrition.fat.toInt()}g",
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
        item { ChartCard("Calories and macros", bars, listOf("Cal", "Pro", "Carb", "Fat", "Water")) }
        item {
            ChartCard(
                title = "Water trend",
                values = listOf(
                    0f,
                    progress.waterMl * .25f,
                    progress.waterMl * .5f,
                    progress.waterMl * .75f,
                    progress.waterMl.toFloat(),
                ),
                labels = listOf("Start", "25%", "50%", "75%", "Now"),
                line = true,
            )
        }
    }
    if (showDatePicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onSelectDate(pickerState.selectedDateMillis)
                    showDatePicker = false
                }) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun GoalRow(label: String, value: String, progress: Float, color: androidx.compose.ui.graphics.Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                value,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        WavyLinearProgress(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            height = 12.dp,
            progressColor = color,
        )
    }
}
