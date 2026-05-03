package com.mygoal_healthtracker.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mygoal_healthtracker.app.ui.DashboardState
import com.mygoal_healthtracker.app.ui.components.EmptyState
import com.mygoal_healthtracker.app.ui.components.MacroCard
import com.mygoal_healthtracker.app.ui.components.SectionHeader
import com.mygoal_healthtracker.app.ui.components.WaterCard

@Composable
fun HomeScreen(state: DashboardState) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { GreetingHeader(state) }
        item { MacroCard(state) }
        item { WaterCard(state) }
        item {
            SectionHeader(
                title = "Today's plan",
                subtitle = if (state.meals.isEmpty()) "No meals yet" else "${state.meals.size} meals scheduled",
            )
        }
        if (state.meals.isEmpty()) {
            item {
                EmptyState(
                    title = "No meals scheduled",
                    description = "Add meals from the Meals tab to start tracking.",
                )
            }
        } else {
            items(state.meals, key = { it.meal.id }) { meal ->
                val log = state.logs.firstOrNull { it.mealId == meal.meal.id }
                MealRowCard(
                    name = meal.meal.name,
                    time = meal.meal.time,
                    options = meal.options.size,
                    status = log?.status,
                )
            }
        }
    }
}

@Composable
private fun GreetingHeader(state: DashboardState) {
    val name = state.profile?.name?.takeIf { it.isNotBlank() } ?: "there"
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                Text(
                    "Hi, $name",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Let's hit your goals today.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ClockChip(state.clock)
        }
    }
}

@Composable
private fun ClockChip(time: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Icon(
            Icons.Default.AccessTime,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            time,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun MealRowCard(
    name: String,
    time: String,
    options: Int,
    status: String?,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MealIcon(time)
            Column(Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    "$time · $options option${if (options == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusBadge(status)
        }
    }
}

@Composable
private fun MealIcon(time: String) {
    val hour = time.split(":").firstOrNull()?.toIntOrNull() ?: 12
    val (icon, accent) = when {
        hour < 11 -> Icons.Default.LocalCafe to MaterialTheme.colorScheme.primary
        hour < 16 -> Icons.Default.LightMode to MaterialTheme.colorScheme.secondary
        hour < 20 -> Icons.Default.Restaurant to MaterialTheme.colorScheme.tertiary
        else -> Icons.Default.Bedtime to MaterialTheme.colorScheme.tertiary
    }
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(accent.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = accent)
    }
}

@Composable
private fun StatusBadge(status: String?) {
    val (label, color) = when (status) {
        "COMPLETED" -> "Done" to MaterialTheme.colorScheme.primary
        "SKIPPED" -> "Skipped" to MaterialTheme.colorScheme.tertiary
        else -> "Pending" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .background(color.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        if (status == "COMPLETED") {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

