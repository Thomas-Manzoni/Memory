package com.example.memory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.memory.ui.CategoryListScreen
import com.example.memory.ui.FlashcardListScreen
import com.example.memory.ui.PlayScreen
import com.example.memory.ui.StartMenu
import com.example.memory.ui.StatisticsMenu
import com.example.memory.viewmodel.PlayCardViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val viewModelPlay: PlayCardViewModel = viewModel()

            NavHost(navController = navController, startDestination = "start_menu") {

                // Start Menu Screen
                composable("start_menu") {
                    StartMenu(viewModelPlay, navController)
                }

                composable("play_mode") {
                    PlayScreen(viewModelPlay)
                }

                composable("statistics_menu") {
                    StatisticsMenu(viewModelPlay, navController)
                }

                composable("flashcard_screen/{unit}") { backStackEntry ->
                    val unitIndex = backStackEntry.arguments?.getString("unit")?.toIntOrNull() ?: 0
                    viewModelPlay.selectUnitExercise(unitIndex)
                    FlashcardListScreen(viewModelPlay)
                }

                composable("category_flashcard_screen") {
                    CategoryListScreen(viewModelPlay)
                }
            }
        }
    }
}
