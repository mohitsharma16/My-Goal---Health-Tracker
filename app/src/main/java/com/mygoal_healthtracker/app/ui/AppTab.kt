package com.mygoal_healthtracker.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppTab(val title: String, val icon: ImageVector) {
    Home("Home", Icons.Default.Home),
    Water("Water", Icons.Default.WaterDrop),
    Meals("Meals", Icons.Default.RestaurantMenu),
    Log("Log", Icons.Default.TaskAlt),
    Progress("Progress", Icons.Default.BarChart),
}
