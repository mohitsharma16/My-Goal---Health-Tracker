package com.mygoal_healthtracker.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mygoal_healthtracker.app.ui.DashboardState
import com.mygoal_healthtracker.app.ui.components.WaterCard

@Composable
fun WaterScreen(state: DashboardState, onAddWater: (Int) -> Unit, onSetGoal: (String) -> Unit) {
    var goal by remember(state.waterGoalMl) { mutableStateOf(state.waterGoalMl.toString()) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { WaterCard(state) }
        item {
            ElevatedCard {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = goal,
                        onValueChange = { goal = it },
                        label = { Text("Daily goal (ml)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(onClick = { onSetGoal(goal) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Save goal")
                    }
                }
            }
        }
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(180.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(listOf(250, 500, 750, 1000)) { amount ->
                    Button(onClick = { onAddWater(amount) }, modifier = Modifier.fillMaxWidth().height(72.dp)) {
                        Text("+$amount ml")
                    }
                }
            }
        }
    }
}
