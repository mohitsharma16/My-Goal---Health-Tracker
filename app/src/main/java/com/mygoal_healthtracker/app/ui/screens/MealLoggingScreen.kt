package com.mygoal_healthtracker.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mygoal_healthtracker.app.data.OptionWithFood
import com.mygoal_healthtracker.app.ui.DashboardState
import com.mygoal_healthtracker.app.ui.components.EmptyState
import com.mygoal_healthtracker.app.ui.components.SectionHeader

@Composable
fun MealLoggingScreen(
    state: DashboardState,
    onComplete: (Long, Long) -> Unit,
    onSkip: (Long) -> Unit,
    onClear: (Long) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SectionHeader(
                title = "Log today",
                subtitle = "${state.completedMeals} of ${state.meals.size} meals logged",
            )
        }
        if (state.meals.isEmpty()) {
            item {
                EmptyState(
                    title = "Nothing to log",
                    description = "Add meals from the Meals tab to start logging.",
                )
            }
        }
        items(state.meals, key = { it.meal.id }) { meal ->
            val log = state.logs.firstOrNull { it.mealId == meal.meal.id }
            val scale by animateFloatAsState(
                if (log?.status == "COMPLETED") 1.015f else 1f,
                animationSpec = tween(220),
                label = "mealComplete",
            )
            val alpha by animateFloatAsState(
                if (log?.status == "SKIPPED") 0.6f else 1f,
                animationSpec = tween(220),
                label = "mealAlpha",
            )
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    },
                shape = RoundedCornerShape(22.dp),
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                meal.meal.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                            )
                            Text(
                                meal.meal.time,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        StatusChip(log?.status)
                    }
                    meal.options.forEach { option ->
                        OptionRow(
                            option = option,
                            selected = log?.selectedOptionId == option.option.id && log.status == "COMPLETED",
                            onSelect = { onComplete(meal.meal.id, option.option.id) },
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        TextButton(
                            onClick = { onSkip(meal.meal.id) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.tertiary,
                            )
                            Text("  Skip", color = MaterialTheme.colorScheme.tertiary)
                        }
                        TextButton(
                            onClick = { onClear(meal.meal.id) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Clear", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionRow(
    option: OptionWithFood,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val cardColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            if (selected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(Modifier.weight(1f)) {
            Text(
                option.option.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Text(
                "${option.foodItems.sumOf { it.calories.toDouble() }.toInt()} cal · ${option.foodItems.size} items",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        // suppress unused warning for borderColor — intentional design hook for future border accent
        if (borderColor != Color.Transparent) Box(Modifier.size(0.dp))
    }
}

@Composable
private fun StatusChip(status: String?) {
    val (label, color, icon) = when (status) {
        "COMPLETED" -> Triple("Done", MaterialTheme.colorScheme.primary, Icons.Default.CheckCircle)
        "SKIPPED" -> Triple("Skipped", MaterialTheme.colorScheme.tertiary, Icons.Default.Cancel)
        else -> Triple("Pending", MaterialTheme.colorScheme.onSurfaceVariant, null)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .background(color.copy(alpha = 0.16f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
