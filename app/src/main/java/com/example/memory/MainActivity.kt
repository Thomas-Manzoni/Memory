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
import com.example.memory.ui.SectionSelectionMenu
import com.example.memory.viewmodel.FlashcardViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val viewModel: FlashcardViewModel = viewModel()

            NavHost(navController = navController, startDestination = "section_selection") {
                composable("section_selection") {
                    SectionSelectionMenu(viewModel) { selectedSection ->
                        navController.navigate("unit_selection/$selectedSection")
                    }
                }

                composable("unit_selection/{section}") { backStackEntry ->
                    val sectionIndex = backStackEntry.arguments?.getString("section")?.toIntOrNull() ?: 0
                    viewModel.selectSection(sectionIndex)
                    UnitSelectionMenu(viewModel) { selectedUnit -> // I interpret this as if on the onUnitSelected is triggered we perform this
                        navController.navigate("flashcard_screen/$selectedUnit")
                    }
                }

                composable("flashcard_screen/{unit}") { backStackEntry ->
                    // It reads the string of the unit passed by the previous call and uses the viewmodel to load the unit
                    val unitIndex = backStackEntry.arguments?.getString("unit")?.toIntOrNull() ?: 0
                    viewModel.selectUnit(unitIndex) // Load the selected unit
                    FlashcardScreen(viewModel)
                }
            }
        }
    }
}
