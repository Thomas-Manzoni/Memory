package com.example.memory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.memory.viewmodel.PlayCardViewModel
import kotlinx.coroutines.launch

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import androidx.compose.ui.viewinterop.AndroidView


@Composable
fun StatisticsMenu(viewModel: PlayCardViewModel, navController: NavController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val totalSwipes = remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        totalSwipes.value = viewModel.getTotalSwipes()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Swipes per day", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(4.dp))

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(bottom = 16.dp),
                factory = { context ->
                    BarChart(context).apply {
                        val entries = listOf(
                            BarEntry(0f, 20f),
                            BarEntry(1f, 40f),
                            BarEntry(2f, 60f)
                        )

                        val dataSet = BarDataSet(entries, "Review Stats")
                        dataSet.setColors(
                            android.graphics.Color.rgb(128, 185, 134),
                            android.graphics.Color.rgb(68, 126, 120),
                            android.graphics.Color.rgb(248, 199, 177)
                        )

                        val barData = BarData(dataSet)
                        this.data = barData

                        description.isEnabled = false
                        legend.isEnabled = false
                        setFitBars(true)
                        invalidate()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Total cards swiped: ${totalSwipes.value}", style = MaterialTheme.typography.labelMedium)

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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Reset swipes record")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween // Optional: keeps them flush to edges
                ) {
                    Text(text = "Recently unknown cards:", style = MaterialTheme.typography.titleSmall)

                    Text(text = "Recently unknown cards2:", style = MaterialTheme.typography.titleSmall)
                }
            }




        }
    }
}
