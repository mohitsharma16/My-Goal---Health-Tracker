package com.mygoal_healthtracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mygoal_healthtracker.app.data.DraftFoodItem
import com.mygoal_healthtracker.app.data.DraftMealOption
import com.mygoal_healthtracker.app.data.MealEntity
import com.mygoal_healthtracker.app.data.MealWithOptions
import com.mygoal_healthtracker.app.data.OptionWithFood
import com.mygoal_healthtracker.app.ui.DashboardState
import com.mygoal_healthtracker.app.ui.components.EmptyState
import com.mygoal_healthtracker.app.ui.components.MealTimePickerDialog
import com.mygoal_healthtracker.app.ui.components.SectionHeader

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

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SectionHeader(
                title = "Your meal plan",
                subtitle = "${state.meals.size} meals · macros cached locally",
            )
        }
        item {
            Button(
                onClick = { addingMeal = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(
                    "  Add a new meal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        if (state.meals.isEmpty()) {
            item {
                EmptyState(
                    title = "No meals yet",
                    description = "Tap Add to build your first meal with options and food items.",
                )
            }
        }
        items(state.meals, key = { it.meal.id }) { meal ->
            MealPlanCard(
                meal = meal,
                onEdit = { editingMeal = meal },
                onDelete = { onDeleteMeal(meal.meal) },
            )
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
private fun MealPlanCard(
    meal: MealWithOptions,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val (icon, accent) = mealIconFor(meal.meal.time)
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(accent.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = accent)
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        meal.meal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            "${meal.meal.time} · reminders ${if (meal.meal.reminderEnabled) "on" else "off"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.tertiary)
                }
            }
            meal.options.forEach { option ->
                OptionSummary(option)
            }
        }
    }
}

@Composable
private fun OptionSummary(option: OptionWithFood) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            option.option.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        option.foodItems.forEach { food ->
            Text(
                "${food.name} · ${food.quantity} · ${food.calories.toInt()} cal",
                style = MaterialTheme.typography.bodySmall,
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
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Meal name") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                ElevatedCard(shape = RoundedCornerShape(18.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        FilledTonalButton(
                            onClick = { showTimePicker = true },
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                            Text("  $time", fontWeight = FontWeight.SemiBold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Reminder", style = MaterialTheme.typography.labelLarge)
                            Switch(checked = reminder, onCheckedChange = { reminder = it })
                        }
                    }
                }
            }
            item {
                ElevatedCard(shape = RoundedCornerShape(18.dp)) {
                    Column(
                        Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            "Add an option",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        OutlinedTextField(
                            value = optionName,
                            onValueChange = { optionName = it },
                            label = { Text("Option name (e.g. Paneer Sandwich)") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = foodName,
                                onValueChange = { foodName = it },
                                label = { Text("Food") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1.4f),
                            )
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { quantity = it },
                                label = { Text("Qty") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (pendingFood.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                pendingFood.forEach { food ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colorScheme.surfaceContainerHighest,
                                                RoundedCornerShape(12.dp),
                                            )
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                    ) {
                                        Text(
                                            "${food.name} (${food.quantity})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f),
                                        )
                                        IconButton(onClick = { pendingFood.remove(food) }) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove",
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    if (foodName.isNotBlank() && quantity.isNotBlank()) {
                                        pendingFood.add(DraftFoodItem(foodName.trim(), quantity.trim()))
                                        foodName = ""
                                        quantity = ""
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                            ) { Text("Add food") }
                            Button(
                                enabled = optionName.isNotBlank() && pendingFood.isNotEmpty(),
                                onClick = {
                                    options.add(DraftMealOption(optionName.trim(), pendingFood.toList()))
                                    optionName = ""
                                    pendingFood.clear()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                            ) { Text("Add option") }
                        }
                    }
                }
            }
            if (options.isNotEmpty()) {
                item {
                    Text(
                        "Options (${options.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            items(options) { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceContainer,
                            RoundedCornerShape(14.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            option.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            option.foodItems.joinToString { "${it.name} (${it.quantity})" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { options.remove(option) }) {
                        Icon(Icons.Default.Close, contentDescription = "Remove option")
                    }
                }
            }
            item {
                Button(
                    enabled = name.isNotBlank() && options.isNotEmpty(),
                    onClick = { onSave(name, time, reminder, options.toList()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text("Save meal", fontWeight = FontWeight.SemiBold)
                }
            }
            item { Box(Modifier.height(16.dp)) }
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

@Composable
private fun mealIconFor(time: String): Pair<ImageVector, Color> {
    val hour = time.split(":").firstOrNull()?.toIntOrNull() ?: 12
    return when {
        hour < 11 -> Icons.Default.LocalCafe to MaterialTheme.colorScheme.primary
        hour < 16 -> Icons.Default.LightMode to MaterialTheme.colorScheme.secondary
        hour < 20 -> Icons.Default.Restaurant to MaterialTheme.colorScheme.tertiary
        else -> Icons.Default.Bedtime to MaterialTheme.colorScheme.tertiary
    }
}

