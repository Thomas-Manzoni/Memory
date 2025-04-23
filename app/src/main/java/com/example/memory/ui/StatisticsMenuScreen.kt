package com.example.memory.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import com.example.memory.R
import com.github.mikephil.charting.components.XAxis


@Composable
fun StatisticsMenu(viewModel: PlayCardViewModel, navController: NavController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val totalSwipes = remember { mutableIntStateOf(0) }
    val swipesPerDay = remember {
        mutableStateListOf<Int>().apply {
            repeat(7) { add(0) }
        }
    }
    val recentSwiped = remember { mutableStateListOf<String>() }
    val recentMissSwiped = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        totalSwipes.value = viewModel.getTotalSwipes()
        val weekSwipes: List<Int> = viewModel.getWeekSwipes()
        swipesPerDay.clear()
        swipesPerDay.addAll(weekSwipes)

        recentSwiped.apply {
            clear()
            addAll(viewModel.getRecentlySwipedCards())
        }

        recentMissSwiped.apply {
            clear()
//            addAll(viewModel.())
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ){
            Image(
                painter = painterResource(id = R.drawable.woodenbackground),
                contentDescription = null,
                contentScale = ContentScale.Crop, // or whatever scale you're using
                alignment = Alignment.BottomCenter,     // 👈 recenter the image
                modifier = Modifier.matchParentSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {

                Spacer(modifier = Modifier.height(4.dp))

                Text(text = "Swipes per day", style = MaterialTheme.typography.titleMedium, color = Color.White)

                Spacer(modifier = Modifier.height(4.dp))

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(bottom = 16.dp),
                    factory = { ctx ->
                        BarChart(ctx).apply {
                            // 1) One‑time styling
                            description.isEnabled = false
                            legend.isEnabled = false
                            setFitBars(true)

                            // LEFT axis (vertical)
                            axisLeft.apply {
                                axisMinimum = 0f
                                axisLineWidth = 2f               // make the left axis line thicker
                                axisLineColor = android.graphics.Color.WHITE
                                textColor = android.graphics.Color.WHITE  // y‑axis numbers in white
                                setDrawGridLines(false)
                            }
                            axisRight.isEnabled = false

                            // BOTTOM axis (horizontal)
                            xAxis.apply {
                                position = XAxis.XAxisPosition.BOTTOM
                                granularity = 1f
                                setDrawGridLines(false)
                                axisLineWidth = 2f               // thicker bottom axis line
                                axisLineColor = android.graphics.Color.WHITE
                                textColor = android.graphics.Color.WHITE  // x‑axis labels in white
                            }
                        }
                    },
                    update = { chart ->
                        // 2) Runs on every recomposition if swipesPerDay changed
                        val entries = swipesPerDay.mapIndexed { idx, swipeCount ->
                            // reverse so idx=0 (D1) → x=6, idx=6 (D7) → x=0
                            BarEntry((6 - idx).toFloat(), swipeCount.toFloat())
                        }

                        val set = BarDataSet(entries, "Swipes per day").apply {
                            color = android.graphics.Color.rgb(128, 185, 134)
                            valueTextSize = 12f
                            setValueTextColor(android.graphics.Color.WHITE)
                        }

                        chart.data = BarData(set).apply { barWidth = 0.9f }

                        // optional: relabel x‑axis to ["D7","D6",…,"D1"]
//                        chart.xAxis.valueFormatter = IndexAxisValueFormatter(
//                            listOf("D7","D6","D5","D4","D3","D2","D1")
//                        )
                        chart.xAxis.setLabelCount(7, true)

                        chart.invalidate()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Total cards swiped: ${totalSwipes.value}", style = MaterialTheme.typography.titleLarge, color = Color.White)

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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF447E78),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Reset swipes record")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.resetAllFlashcard()
                            snackbarHostState.showSnackbar(
                                message = "Cards reset",
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF447E78),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Reset cards")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),       // make row height wrap the taller child
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // optional gap
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)                     // take half the available width
                            .fillMaxHeight()                // match the row’s height
                            .background(Color.Black.copy(alpha = 0.3f))
                            .padding(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            Text(
                                text = "Recent cards:",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            recentSwiped.forEach { cardText ->
                                Text(
                                    text = cardText,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                            }


                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)                     // take the other half
                            .fillMaxHeight()
                            .background(Color.Black.copy(alpha = 0.3f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Recent cards2:",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                    }
                }

            }
        }
    }
}
