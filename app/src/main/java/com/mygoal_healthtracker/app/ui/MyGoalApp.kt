package com.mygoal_healthtracker.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mygoal_healthtracker.app.data.DraftMealOption
import com.mygoal_healthtracker.app.data.MealEntity
import com.mygoal_healthtracker.app.ui.screens.HomeScreen
import com.mygoal_healthtracker.app.ui.screens.MealLoggingScreen
import com.mygoal_healthtracker.app.ui.screens.MealPlannerScreen
import com.mygoal_healthtracker.app.ui.screens.OnboardingScreen
import com.mygoal_healthtracker.app.ui.screens.ProgressScreen
import com.mygoal_healthtracker.app.ui.screens.WaterScreen
import java.io.File

@Composable
fun MyGoalApp(
    viewModel: HealthTrackerViewModel = viewModel(
        factory = HealthTrackerFactory(LocalContext.current.applicationContext as android.app.Application),
    ),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val json = context.contentResolver.openInputStream(uri)
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()
            viewModel.importJson(json)
        }
    }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (state.profile == null) {
        OnboardingScreen(onSave = viewModel::saveProfile)
    } else {
        TrackerShell(
            state = state,
            onAddWater = viewModel::addWater,
            onSetGoal = viewModel::setWaterGoal,
            onAddMeal = viewModel::addMeal,
            onUpdateMeal = viewModel::updateMeal,
            onDeleteMeal = viewModel::deleteMeal,
            onCompleteMeal = viewModel::completeMeal,
            onSkipMeal = viewModel::skipMeal,
            onClearMeal = viewModel::clearMeal,
            onImportJson = { importLauncher.launch("application/json") },
            onSelectDate = viewModel::selectDate,
            onSetProgressRange = viewModel::setProgressRange,
            onExportJson = {
                viewModel.exportJson { shareJson(context, it) }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerShell(
    state: DashboardState,
    onAddWater: (Int) -> Unit,
    onSetGoal: (String) -> Unit,
    onAddMeal: (String, String, Boolean, List<DraftMealOption>) -> Unit,
    onUpdateMeal: (Long, String, String, Boolean, List<DraftMealOption>) -> Unit,
    onDeleteMeal: (MealEntity) -> Unit,
    onCompleteMeal: (Long, Long) -> Unit,
    onSkipMeal: (Long) -> Unit,
    onClearMeal: (Long) -> Unit,
    onImportJson: () -> Unit,
    onSelectDate: (Long?) -> Unit,
    onSetProgressRange: (com.mygoal_healthtracker.app.data.ProgressRange) -> Unit,
    onExportJson: () -> Unit,
) {
    var tab by remember { mutableStateOf(AppTab.Home) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tab.title) },
                actions = {
                    TextButton(onClick = onImportJson) { Text("Import") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onExportJson) {
                Icon(Icons.Default.UploadFile, contentDescription = "Export JSON")
            }
        },
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(Color.Black)) {
            when (tab) {
                AppTab.Home -> HomeScreen(state)
                AppTab.Water -> WaterScreen(state, onAddWater, onSetGoal)
                AppTab.Meals -> MealPlannerScreen(state, onAddMeal, onUpdateMeal, onDeleteMeal)
                AppTab.Log -> MealLoggingScreen(state, onCompleteMeal, onSkipMeal, onClearMeal)
                AppTab.Progress -> ProgressScreen(state, onSelectDate, onSetProgressRange)
            }
        }
    }
}

private fun shareJson(context: android.content.Context, json: String) {
    val file = File(context.cacheDir, "my-goals-health-tracker-backup.json")
    file.writeText(json)
    val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Export health data"))
}
