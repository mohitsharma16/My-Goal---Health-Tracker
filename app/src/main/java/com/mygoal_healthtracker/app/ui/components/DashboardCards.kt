package com.mygoal_healthtracker.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mygoal_healthtracker.app.ui.DashboardState

@Composable
fun MacroCard(state: DashboardState) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Today nutrition", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatPill("Cal", state.calories.toInt().toString(), Modifier.weight(1f))
                StatPill("Protein", "${state.protein.toInt()}g", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatPill("Carbs", "${state.carbs.toInt()}g", Modifier.weight(1f))
                StatPill("Fat", "${state.fat.toInt()}g", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun WaterCard(state: DashboardState) {
    val animated by animateFloatAsState(state.waterProgress, label = "waterProgress")
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Water", fontWeight = FontWeight.Bold)
                Text("${state.waterTotalMl}/${state.waterGoalMl} ml")
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(animated)
                        .height(84.dp)
                        .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp)),
                )
                if (state.waterProgress >= 1f) {
                    Text("Goal complete", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatPill(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}
