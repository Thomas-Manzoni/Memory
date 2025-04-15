package com.example.memory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.memory.viewmodel.PlayCardViewModel
import kotlinx.coroutines.launch

@Composable
fun StatisticsMenu(viewModel: PlayCardViewModel, navController: NavController) {

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.White)
                .padding(innerPadding)
                .padding(16.dp)
        ){
//            BarChart(
//                barChartData = BarChartData(
//                    bars = listOf(
//                        BarChartData.Bar("A", 20f, Color(0xFF80B986)),
//                        BarChartData.Bar("B", 40f, Color(0xFF447E78)),
//                        BarChartData.Bar("C", 60f, Color(0xFFF8C7B1))
//                    )
//                ),
//                labelDrawer = SimpleValueDrawer(drawLocation = SimpleValueDrawer.DrawLocation.Inside)
//            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.resetAllFlashcardSwipes()
                        snackbarHostState.showSnackbar(
                            message = "Cards swipes reset",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text(text = "Reset swipes record")
            }
        }
    }
}