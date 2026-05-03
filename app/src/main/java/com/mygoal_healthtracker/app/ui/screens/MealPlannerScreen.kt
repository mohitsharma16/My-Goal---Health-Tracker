package com.mygoal_healthtracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mygoal_healthtracker.app.data.DraftFoodItem
import com.mygoal_healthtracker.app.data.DraftMealOption
import com.mygoal_healthtracker.app.data.MealEntity
import com.mygoal_healthtracker.app.data.MealWithOptions
import com.mygoal_healthtracker.app.data.OptionWithFood
import com.mygoal_healthtracker.app.ui.DashboardState
import com.mygoal_healthtracker.app.ui.components.MealTimePickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlannerScreen(
    state: DashboardState,
    onAddMeal: (String, String, Boolean, List<DraftMealOption>) -> Unit,
    onUpdateMeal: (Long, String, String, Boolean, List<DraftMealOption>) -> Unit,
    onDeleteMeal: (MealEntity) -> Unit,
) {
    var editingMeal by remember { mutableStateOf<MealWithOptions?>(null) }
    var addingMeal by remember { mutableStateOf(false) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Button(onClick = { addingMeal = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Add meal")
            }
        }
        items(state.meals, key = { it.meal.id }) { meal ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(meal.meal.name, fontWeight = FontWeight.Bold)
                            Text(
                                "${meal.meal.time} - reminders ${if (meal.meal.reminderEnabled) "on" else "off"}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(onClick = { editingMeal = meal }) { Text("Edit") }
                        TextButton(onClick = { onDeleteMeal(meal.meal) }) { Text("Delete") }
                    }
                    meal.options.forEach { option ->
                        OptionSummary(option)
                    }
                }
            }
        }
    }

    if (addingMeal) {
        MealEditorSheet(
            title = "Add meal",
            initialMeal = null,
            onDismiss = { addingMeal = false },
            onSave = { name, time, reminder, options ->
                onAddMeal(name, time, reminder, options)
                addingMeal = false
            },
        )
    }

    editingMeal?.let { meal ->
        MealEditorSheet(
            title = "Edit meal",
            initialMeal = meal,
            onDismiss = { editingMeal = null },
            onSave = { name, time, reminder, options ->
                onUpdateMeal(meal.meal.id, name, time, reminder, options)
                editingMeal = null
            },
        )
    }
}

@Composable
private fun OptionSummary(option: OptionWithFood) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
    ) {
        Text(option.option.name, fontWeight = FontWeight.SemiBold)
        option.foodItems.forEach {
            Text(
                "${it.name} - ${it.quantity} - ${it.calories.toInt()} cal",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealEditorSheet(
    title: String,
    initialMeal: MealWithOptions?,
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean, List<DraftMealOption>) -> Unit,
) {
    var name by remember(initialMeal) { mutableStateOf(initialMeal?.meal?.name.orEmpty()) }
    var time by remember(initialMeal) { mutableStateOf(initialMeal?.meal?.time ?: "08:30") }
    var reminder by remember(initialMeal) { mutableStateOf(initialMeal?.meal?.reminderEnabled ?: true) }
    var optionName by remember { mutableStateOf("") }
    var foodName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    val pendingFood = remember { mutableStateListOf<DraftFoodItem>() }
    val options = remember(initialMeal) {
        mutableStateListOf<DraftMealOption>().apply {
            initialMeal?.options?.mapTo(this) { option ->
                DraftMealOption(
                    name = option.option.name,
                    foodItems = option.foodItems.map { DraftFoodItem(it.name, it.quantity) },
                )
            }
        }
    }
    var showTimePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Meal name") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(onClick = { showTimePicker = true }) { Text(time) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Reminder")
                        Switch(checked = reminder, onCheckedChange = { reminder = it })
                    }
                }
            }
            item {
                ElevatedCard {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = optionName,
                            onValueChange = { optionName = it },
                            label = { Text("Option name") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = foodName,
                            onValueChange = { foodName = it },
                            label = { Text("Food item") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (foodName.isNotBlank() && quantity.isNotBlank()) {
                                        pendingFood.add(DraftFoodItem(foodName.trim(), quantity.trim()))
                                        foodName = ""
                                        quantity = ""
                                    }
                                },
                            ) {
                                Text("Add food")
                            }
                            Button(
                                enabled = optionName.isNotBlank() && pendingFood.isNotEmpty(),
                                onClick = {
                                    options.add(DraftMealOption(optionName.trim(), pendingFood.toList()))
                                    optionName = ""
                                    pendingFood.clear()
                                },
                            ) {
                                Text("Add option")
                            }
                        }
                        pendingFood.forEach { food ->
                            Text("${food.name} (${food.quantity})", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            items(options) { option ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(option.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            option.foodItems.joinToString { "${it.name} (${it.quantity})" },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = { options.remove(option) }) { Text("Remove") }
                }
            }
            item {
                Button(
                    enabled = name.isNotBlank() && options.isNotEmpty(),
                    onClick = { onSave(name, time, reminder, options.toList()) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save meal")
                }
            }
        }
    }
    if (showTimePicker) {
        MealTimePickerDialog(
            current = time,
            onDismiss = { showTimePicker = false },
            onSelected = {
                time = it
                showTimePicker = false
            },
        )
    }
}
