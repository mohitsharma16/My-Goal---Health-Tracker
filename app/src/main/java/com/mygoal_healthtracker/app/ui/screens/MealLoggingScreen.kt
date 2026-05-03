package com.mygoal_healthtracker.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mygoal_healthtracker.app.ui.DashboardState

@Composable
fun MealLoggingScreen(
    state: DashboardState,
    onComplete: (Long, Long) -> Unit,
    onSkip: (Long) -> Unit,
    onClear: (Long) -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(state.meals, key = { it.meal.id }) { meal ->
            val log = state.logs.firstOrNull { it.mealId == meal.meal.id }
            val scale by animateFloatAsState(
                if (log?.status == "COMPLETED") 1.02f else 1f,
                label = "mealComplete",
            )
            ElevatedCard(Modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale }) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(meal.meal.name, fontWeight = FontWeight.Bold)
                        Text(log?.status ?: "Pending", color = MaterialTheme.colorScheme.primary)
                    }
                    meal.options.forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = log?.selectedOptionId == option.option.id && log?.status == "COMPLETED",
                                onClick = { onComplete(meal.meal.id, option.option.id) },
                            )
                            Column {
                                Text(option.option.name)
                                Text(
                                    "${option.foodItems.sumOf { it.calories.toDouble() }.toInt()} cal",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { onSkip(meal.meal.id) }) { Text("Skipped") }
                        TextButton(onClick = { onClear(meal.meal.id) }) { Text("Clear") }
                    }
                }
            }
        }
    }
}
