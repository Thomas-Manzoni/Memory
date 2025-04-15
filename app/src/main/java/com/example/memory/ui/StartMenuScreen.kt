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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.memory.viewmodel.PlayCardViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.memory.R


@Composable
fun StartMenu(viewModel: PlayCardViewModel, navController: NavController) {
    var showPopupMenu by remember { mutableStateOf(false) }
    var showPopupSectionMenu by remember { mutableStateOf(false) }
    var showPopupUnitMenu by remember { mutableStateOf(false) }
    var showPopupModeLanguage by remember { mutableStateOf(false) }
    var selectedUnitIndex by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background2),
            contentDescription = null,
            contentScale = ContentScale.Crop, // or whatever scale you're using
            alignment = Alignment.BottomCenter,     // 👈 recenter the image
            modifier = Modifier.matchParentSize()
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart) // 👈 top-left corner of the screen
                .padding(horizontal = 12.dp, vertical = 40.dp)
//                .background(Color(0xAA000000), shape = RoundedCornerShape(6.dp)) // semi-transparent background
                .padding(horizontal = 30.dp, vertical = 20.dp)
        ) {
            Button(
                onClick = { showPopupModeLanguage = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF447E78),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp),
//                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
            ) {
                Text("Select mode", fontSize = 14.sp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height(680.dp))

            Button(
                onClick = { showPopupMenu = true },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(60.dp) // 30dp might be too tight for text
                    .shadow(
                        elevation = 8.dp, // ⬅️ the strength of the shadow
                        shape = RoundedCornerShape(8.dp), // ⬅️ match your border shape
                        clip = false // Optional: if true, content is clipped to the shape
                    ),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF447E78), // Background color
                    contentColor = Color.White          // Text color
                ),
            ) {
                Text(text = "Play")
            }


            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween // Optional: keeps them flush to edges
                ) {
                    Button(
                        onClick = {
                            showPopupSectionMenu = true
                             },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .padding(end = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF447E78),
                            contentColor = Color.White
                        ),
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
                    ) {
                        Text(text = "Statistics", fontSize = 14.sp)
                    }
                }
            }
        }

        if (showPopupMenu) {
            PopUpPlay(
                onDismiss = { showPopupMenu = false },
                navController = navController,
                viewModel = viewModel
            )
        }

        if (showPopupSectionMenu) {
            PopUpSection(
                onDismiss = { showPopupSectionMenu = false },
                onSectionSelected = {
                    showPopupSectionMenu = false
                    showPopupUnitMenu = true
                                    },
                navController = navController,
                viewModel = viewModel
            )
        }

        if (showPopupUnitMenu) {
            PopUpUnit(
                onDismiss = { showPopupUnitMenu = false },
                onUnitSelected = { index ->
                    selectedUnitIndex = index
                    showPopupUnitMenu = false
                    navController.navigate("flashcard_screen/$selectedUnitIndex")
                                 },
                navController = navController,
                viewModel = viewModel
            )
        }

        if (showPopupModeLanguage) {
            PopUpModeSet(
                onDismiss = { showPopupModeLanguage = false },
                navController = navController,
                viewModel = viewModel
            )
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