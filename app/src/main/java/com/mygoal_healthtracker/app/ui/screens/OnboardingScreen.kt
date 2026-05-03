package com.mygoal_healthtracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mygoal_healthtracker.app.ui.theme.MyGoalHealthTrackerTheme

@Composable
fun OnboardingScreen(onSave: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    val valid = name.isNotBlank() && height.toFloatOrNull() != null && weight.toFloatOrNull() != null

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("My Goals", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text("Health Tracker", color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Button(
            enabled = valid,
            onClick = { onSave(name, height, weight) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Start tracking")
        }
    }
}

@Preview
@Composable
private fun OnboardingPreview() {
    MyGoalHealthTrackerTheme(darkTheme = true) {
        OnboardingScreen(onSave = { _, _, _ -> })
    }
}
