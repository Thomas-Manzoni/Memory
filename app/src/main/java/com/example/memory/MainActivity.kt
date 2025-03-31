package com.example.memory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.memory.ui.FlashcardScreen
import com.example.memory.ui.UnitSelectionMenu
import com.example.memory.viewmodel.FlashcardViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val viewModel: FlashcardViewModel = viewModel()

            NavHost(navController = navController, startDestination = "unit_selection") {
                composable("unit_selection") {
                    UnitSelectionMenu(viewModel) { selectedUnit ->
                        navController.navigate("flashcard_screen/$selectedUnit")
                    }
                }

                composable("flashcard_screen/{unit}") { backStackEntry ->
                    val unitIndex = backStackEntry.arguments?.getString("unit")?.toIntOrNull() ?: 0
                    viewModel.selectUnit(unitIndex) // Load the selected unit
                    FlashcardScreen(viewModel)
                }
            }
        }
    }
}
