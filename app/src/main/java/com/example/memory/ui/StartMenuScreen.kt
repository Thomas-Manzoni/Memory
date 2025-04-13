package com.example.memory.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.memory.viewmodel.PlayCardViewModel

@Composable
fun StartMenu(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFAD8661)) // <-- Your background color here
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(600.dp))

            Button(
                onClick = { navController.navigate("play_options_menu") },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(60.dp), // 30dp might be too tight for text
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF447E78), // Background color
                    contentColor = Color.White          // Text color
                ),
                border = BorderStroke(2.dp, Color.Black)
            ) {
                Text(text = "Play")
            }


            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f) // 👈 Same width as the Play button
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween // Optional: keeps them flush to edges
                ) {
                    Button(
                        onClick = { navController.navigate("section_selection") },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .padding(end = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF447E78),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(2.dp, Color.Black)
                    ) {
                        Text(text = "Exercise", fontSize = 14.sp)
                    }

                    Button(
                        onClick = { navController.navigate("statistics_menu") },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .padding(start = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF447E78),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(2.dp, Color.Black)
                    ) {
                        Text(text = "Statistics", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PlayOptionsMenu(viewModel: PlayCardViewModel, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Do you want to select a specific section?", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.untilProgressedUnit = false
                navController.navigate("section_selection_play")
                      },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(text = "Select section")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.untilProgressedUnit = true
                navController.navigate("play_mode")
                      },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(text = "Play with all learned cards")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.untilProgressedUnit = false
                navController.navigate("play_mode")
                      },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(text = "Play with all cards")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.randomWeightedMode = true
                navController.navigate("play_mode")
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(text = "Weighted random selection mode")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("progress_section_selection_play")
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(text = "Set progress")
        }
    }
}