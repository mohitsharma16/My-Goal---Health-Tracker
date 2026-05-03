package com.mygoal_healthtracker.app.ui.screens

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mygoal_healthtracker.app.ui.DashboardState
import com.mygoal_healthtracker.app.ui.components.MacroCard
import com.mygoal_healthtracker.app.ui.components.WaterCard

@Composable
fun HomeScreen(state: DashboardState) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(
                "Hi, ${state.profile?.name.orEmpty()}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(state.clock, color = MaterialTheme.colorScheme.secondary)
        }
        item { MacroCard(state) }
        item { WaterCard(state) }
        items(state.meals, key = { it.meal.id }) { meal ->
            val log = state.logs.firstOrNull { it.mealId == meal.meal.id }
            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(meal.meal.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${meal.meal.time} - ${meal.options.size} options",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        log?.status ?: "Pending",
                        color = if (log?.status == "COMPLETED") {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        },
                    )
                }
            }
        }
    }
}
