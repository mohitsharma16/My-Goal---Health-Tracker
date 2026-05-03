package com.mygoal_healthtracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mygoal_healthtracker.app.ui.MyGoalApp
import com.mygoal_healthtracker.app.ui.theme.MyGoalHealthTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyGoalHealthTrackerTheme(darkTheme = true) {
                MyGoalApp()
            }
        }
    }
}
