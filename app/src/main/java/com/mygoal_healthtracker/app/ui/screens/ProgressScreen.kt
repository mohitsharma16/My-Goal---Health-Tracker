package com.mygoal_healthtracker.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import com.mygoal_healthtracker.app.data.ProgressRange
import com.mygoal_healthtracker.app.ui.DashboardState
import com.mygoal_healthtracker.app.ui.components.ChartCard

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

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = { showDatePicker = true }) {
                    Text("${progress.dateRange.start} - ${progress.dateRange.end}")
                }
                SingleChoiceSegmentedButtonRow {
                    ProgressRange.entries.forEachIndexed { index, value ->
                        SegmentedButton(
                            selected = progress.range == value,
                            onClick = { onSetRange(value) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = ProgressRange.entries.size),
                        ) {
                            Text(value.label)
                        }
                    }
                }
            }
        }
        item { ChartCard("Calories and macros", bars, listOf("Cal", "Pro", "Carb", "Fat", "Water")) }
        item {
            ChartCard(
                "Water line",
                listOf(
                    0f,
                    progress.waterMl * .25f,
                    progress.waterMl * .5f,
                    progress.waterMl * .75f,
                    progress.waterMl.toFloat(),
                ),
                listOf("6", "9", "12", "15", "Now"),
                line = true,
            )
        }
    }
    if (showDatePicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSelectDate(pickerState.selectedDateMillis)
                        showDatePicker = false
                    },
                ) {
                    Text("Done")
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}
