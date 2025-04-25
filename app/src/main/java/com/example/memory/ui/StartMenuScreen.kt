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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import com.example.memory.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


@Composable
fun StartMenu(viewModel: PlayCardViewModel, navController: NavController) {
    var showPopupMenu by remember { mutableStateOf(false) }
    var showPopupSectionMenu by remember { mutableStateOf(false) }
    var showPopupUnitMenu by remember { mutableStateOf(false) }
    var showPopupModeLanguage by remember { mutableStateOf(false) }
    var showPopupExerciseMenu by remember { mutableStateOf(false) }
    var showPopupCategories by remember { mutableStateOf(false) }
    var showPopupGrammarCategories by remember { mutableStateOf(false) }
    var settingProgress by remember { mutableStateOf(false) }
    var selectedSectionIndex by remember { mutableIntStateOf(0) }
    var selectedUnitIndex by remember { mutableIntStateOf(0)}

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val alertMessage by viewModel.newEntriesAlert.observeAsState()

    if (alertMessage != null) {
        AlertDialog(
            onDismissRequest = { /* Dismiss the dialog and reset the alert, e.g.: */ viewModel.clearNewEntriesAlert() },
            title = { Text("New Entries Detected") },
            text = { Text(alertMessage ?: "") },
            confirmButton = {
                Button(onClick = { viewModel.clearNewEntriesAlert() }) {
                    Text("OK")
                }
            }
        )
    }

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

        val isLoading by viewModel.isLoading.collectAsState()
        if (isLoading) {
            LoadingDialog(message = "Fetching course...\nPlease wait")
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd) // 👈 top-left corner of the screen
                .padding(horizontal = 12.dp, vertical = 40.dp)
//                .background(Color(0xAA000000), shape = RoundedCornerShape(6.dp)) // semi-transparent background
                .padding(horizontal = 30.dp, vertical = 20.dp)
        ) {
            Button(
                onClick = {
                    showPopupSectionMenu = true
                    settingProgress = true
                          },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF447E78),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp),
//                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
            ) {
                Text("Set progress", fontSize = 14.sp)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart) // 👈 top-left corner of the screen
                .padding(horizontal = 12.dp, vertical = 40.dp)
//                .background(Color(0xAA000000), shape = RoundedCornerShape(6.dp)) // semi-transparent background
                .padding(horizontal = 30.dp, vertical = 20.dp)
        ) {
            Button(
                onClick = {
                    showPopupModeLanguage = true
                    viewModel.preSelectionMode = false
                          },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF447E78),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp),
//                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
            ) {
                Text("Select course", fontSize = 14.sp)
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
                onClick = {
                    resetSelectedModes(viewModel)
                    showPopupMenu = true
                          },
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
                            resetSelectedModes(viewModel)
                            showPopupExerciseMenu = true
                            viewModel.preSelectionMode = false
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

        SnackbarHost(
            hostState = snackbarHostState,
            snackbar = { data -> Snackbar(snackbarData = data) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )

        if (showPopupMenu) {
            PopUpPlay(
                onDismiss = {
                    showPopupMenu = false
                    resetSelectedModes(viewModel)
                            },
                onTestUnit = { showPopupSectionMenu = true },
                onTestCategory = {
                    showPopupCategories = true
                },
                onListen = {
                    coroutineScope.launch {
                        viewModel.loadRandomFlashcard()
                        navController.navigate("listen_screen")
                    }
                },
                navController = navController,
                viewModel = viewModel
            )
        }

        if (showPopupExerciseMenu) {
            PopUpExerciseOptions(
                onDismiss = {
                    showPopupExerciseMenu = false
                    showPopupSectionMenu = false
                    settingProgress = false
                    resetSelectedModes(viewModel)
                },
                onSectionUnitModeSelected = {
                    showPopupExerciseMenu = false
                    showPopupSectionMenu = true
                },
                onCategoryModeSelected = {
                    showPopupExerciseMenu = false
                    showPopupCategories = true
                },
                onGrammarCategoryModeSelected = {
                    showPopupExerciseMenu = false
                    showPopupGrammarCategories = true
                },
                onFavouritesModeSelected = {
                    showPopupExerciseMenu = false
                    coroutineScope.launch {
                        viewModel.loadCardsToDisplay(PlayCardViewModel.CardDisplayType.FAVORITE)
                        showPopupCategories = false
                        navController.navigate("category_flashcard_screen")
                    }
                },
                onForgottenModeSelected = {
                    showPopupExerciseMenu = false
                    coroutineScope.launch {
                        viewModel.loadCardsToDisplay(PlayCardViewModel.CardDisplayType.FORGOTTEN)
                        showPopupCategories = false
                        navController.navigate("category_flashcard_screen")
                    }
                },
                navController = navController,
                viewModel = viewModel,
            )
        }

        if (showPopupCategories) {
            PopUpCategories(
                onDismiss = {
                    showPopupCategories = false
                    showPopupExerciseMenu = false
                    showPopupSectionMenu = false
                    settingProgress = false
                    resetSelectedModes(viewModel)
                },
                onCategorySelected = { categoryName ->
                    viewModel.categorySelected = categoryName
                    showPopupCategories = false
                    if(viewModel.categoryMode){
                        navController.navigate("play_mode")
                    } else {
                        coroutineScope.launch {
                            viewModel.loadCardsToDisplay(PlayCardViewModel.CardDisplayType.CATEGORY)
                            navController.navigate("category_flashcard_screen")
                        }
                    }
                },
                navController = navController,
                viewModel = viewModel,
            )
        }

        if (showPopupGrammarCategories) {
            PopUpGrammarCategories(
                onDismiss = {
                    showPopupGrammarCategories = false
                    showPopupExerciseMenu = false
                    showPopupSectionMenu = false
                    settingProgress = false
                    resetSelectedModes(viewModel)
                },
                onCategorySelected = { categoryName ->
                    viewModel.categorySelected = categoryName
                    if(viewModel.categoryMode){
                        navController.navigate("play_mode")
                    } else {
                        coroutineScope.launch {
                            viewModel.loadCardsToDisplay(PlayCardViewModel.CardDisplayType.CATEGORY)
                            showPopupCategories = false
                            navController.navigate("category_flashcard_screen")
                        }
                    }
                },
                navController = navController,
                viewModel = viewModel,
            )
        }

        if (showPopupSectionMenu) {
            PopUpSection(
                onDismiss = {
                    showPopupExerciseMenu = false
                    showPopupSectionMenu = false
                    settingProgress = false
                    resetSelectedModes(viewModel)
                            },
                onSectionSelected = { index ->
                    selectedSectionIndex = index
                    showPopupSectionMenu = false
                    showPopupUnitMenu = true
                                    },
                navController = navController,
                viewModel = viewModel,
                settingProgress = settingProgress
            )
        }

        if (showPopupUnitMenu) {
            PopUpUnit(
                onDismiss = {
                    showPopupUnitMenu = false
                    settingProgress = false
                    resetSelectedModes(viewModel)
                            },
                onUnitSelected = { index ->
                    showPopupUnitMenu = false
                    if(settingProgress){
                        coroutineScope.launch {
                            viewModel.updateProgress(newSection = selectedSectionIndex, newUnit = index)
                            snackbarHostState.showSnackbar("Progress updated!")
                        }
                        settingProgress = false
                    }
                    else if(viewModel.preSelectionMode){
                        navController.navigate("play_mode")
                    } else {
                        selectedUnitIndex = index
                        navController.navigate("flashcard_screen/$selectedUnitIndex")
                    }
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

private fun resetSelectedModes(viewModel: PlayCardViewModel){
    viewModel.preSelectionMode = false
    viewModel.categoryMode = false
    viewModel.randomWeightedMode = false
    viewModel.untilProgressedUnit = false
}

@Composable
fun LoadingDialog(message: String) {
    Dialog(
        onDismissRequest = { /* Cannot be dismissed */ },
        DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(200.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF447E78),
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}