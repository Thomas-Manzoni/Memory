package com.example.memory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.memory.ui.FlashcardScreen
import com.example.memory.ui.PlayScreen
import com.example.memory.ui.PlayOptionsMenu
import com.example.memory.ui.UnitSelectionMenu
import com.example.memory.ui.SectionSelectionMenu
import com.example.memory.ui.StartMenu
import com.example.memory.ui.ProgressSectionSelectionPlayMenu
import com.example.memory.ui.ProgressUnitSelectionPlayMenu
import com.example.memory.ui.SectionSelectionPlayMenu
import com.example.memory.ui.UnitSelectionPlayMenu
import com.example.memory.ui.StatisticsMenu
import com.example.memory.viewmodel.PlayCardViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val viewModelPlay: PlayCardViewModel = viewModel()

            NavHost(navController = navController, startDestination = "start_menu") {

                // Start Menu Screen
                composable("start_menu") {
                    StartMenu(navController)
                }

                composable("play_options_menu") {
                    PlayOptionsMenu(viewModelPlay, navController)
                }

                composable("play_mode") {
                    PlayScreen(viewModelPlay)
                }

                composable("statistics_menu") {
                    StatisticsMenu(viewModelPlay, navController)
                }

                composable("section_selection_play") {
                    SectionSelectionPlayMenu(viewModelPlay) { selectedSection ->
                        navController.navigate("unit_selection_play/$selectedSection")
                    }
                }

                composable("unit_selection_play/{section}") { backStackEntry ->
                    val sectionIndex = backStackEntry.arguments?.getString("section")?.toIntOrNull() ?: 0
                    viewModelPlay.selectSection(sectionIndex)
                    UnitSelectionPlayMenu(viewModelPlay, navController)
                }

                composable("progress_section_selection_play") {
                    ProgressSectionSelectionPlayMenu(viewModelPlay) { selectedSection ->
                        navController.navigate("progress_unit_selection_play/$selectedSection")
                    }
                }

                composable("progress_unit_selection_play/{section}") { backStackEntry ->
                    val sectionIndex = backStackEntry.arguments?.getString("section")?.toIntOrNull() ?: 0
                    viewModelPlay.selectSection(sectionIndex)
                    ProgressUnitSelectionPlayMenu(viewModelPlay, sectionIndex)
                }

                // Existing Flashcard Navigation
                composable("section_selection") {
                    SectionSelectionMenu(viewModelPlay) { selectedSection ->
                        navController.navigate("unit_selection/$selectedSection")
                    }
                }

                composable("unit_selection/{section}") { backStackEntry ->
                    val sectionIndex = backStackEntry.arguments?.getString("section")?.toIntOrNull() ?: 0
                    viewModelPlay.selectSectionExercise(sectionIndex)
                    UnitSelectionMenu(viewModelPlay) { selectedUnit ->
                        navController.navigate("flashcard_screen/$selectedUnit")
                    }
                }

                composable("flashcard_screen/{unit}") { backStackEntry ->
                    val unitIndex = backStackEntry.arguments?.getString("unit")?.toIntOrNull() ?: 0
                    viewModelPlay.selectUnitExercise(unitIndex)
                    FlashcardScreen(viewModelPlay)
                }
            }
        }
    }
}
