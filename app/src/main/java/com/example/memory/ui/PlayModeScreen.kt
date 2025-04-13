package com.example.memory.ui

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memory.viewmodel.PlayCardViewModel
import kotlinx.coroutines.launch

@Composable
fun PlayScreen(viewModel: PlayCardViewModel = viewModel()) {
    val currentFlashcard by viewModel.currentFlashcard.observeAsState()
    val currentSection by viewModel.currentSectionVal.observeAsState()
    val currentUnit by viewModel.currentUnitVal.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRandomFlashcard()
    }

    val swipeThreshold = 250f // Distance required for a swipe to register
    var swipeOffset by remember { mutableStateOf(0f) }
    var isSwiping by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }

    var cardRotation by remember { mutableStateOf(0f) }
    val cardWidth = 300.dp
    val cardHeight = 500.dp
    val cardCornerRadius = 16.dp
    val cardBorderThickness = 3.dp

    var cardId by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    // Assume your ViewModel exposes a LiveData (or State) for the description
    cardId = currentFlashcard?.wordId.toString()

    var flipped by remember { mutableStateOf(false) }

    var fetchedReviewedTimes by remember { mutableStateOf<Int?>(0) }
    var fetchedCorrectTimes by remember { mutableStateOf<Int?>(0) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Animate swipe back to center when released
    val animatedSwipeOffset by animateFloatAsState(
        targetValue = if (isDragging) swipeOffset else 0f, // Instantly follows drag, but animates back
        animationSpec = tween(50)
    )

    // Reset the swipeOffset variable after animation completes
    LaunchedEffect(animatedSwipeOffset) {
        if (!isSwiping && animatedSwipeOffset == 0f) {
            swipeOffset = 0f
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFAD8661)) // <-- Your background color here
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(100.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cardHeight), // same height as card to anchor them
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(cardHeight)
                            .width(cardWidth)
                            .graphicsLayer(
                                rotationZ = -5f
                            )
                            .border(cardBorderThickness, Color(0xFFAD2929), RoundedCornerShape(cardCornerRadius))
                            .padding(2.dp)
                            .background(Color(0xFFFFF1E0), shape = RoundedCornerShape(cardCornerRadius))
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(cardHeight)
                            .width(cardWidth)
                            .graphicsLayer(
                                rotationZ = 4f
                            )
                            .border(cardBorderThickness, Color(0xFFAD2929), RoundedCornerShape(cardCornerRadius))
                            .padding(2.dp)
                            .background(Color(0xFFFFF1E0), shape = RoundedCornerShape(cardCornerRadius))
                    )

                    // Flashcard Box with Swipe Gesture Detection and Clickable Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(cardHeight)
                            .width(cardWidth)
                            .graphicsLayer(
                                translationX = animatedSwipeOffset,
                                rotationZ = cardRotation
                            )
                            .border(cardBorderThickness, Color(0xFFAD2929), RoundedCornerShape(cardCornerRadius))
                            .padding(2.dp)
                            .shadow(
                                elevation = 8.dp, // ⬅️ the strength of the shadow
                                shape = RoundedCornerShape(cardCornerRadius), // ⬅️ match your border shape
                                clip = false // Optional: if true, content is clipped to the shape
                            )
                            .background(Color(0xFFFFF1E0), shape = RoundedCornerShape(cardCornerRadius))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { isDragging = true },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        swipeOffset += dragAmount.x // Instant movement while dragging
                                    },
                                    onDragEnd = {
                                        cardId = currentFlashcard?.wordId.toString()
                                        coroutineScope.launch {
                                            if (swipeOffset > swipeThreshold) {
                                                cardRotation = (-3..3).random().toFloat()
                                                viewModel.updateCorrectAnswer(cardId)
                                                viewModel.loadRandomFlashcard()
                                            } else if (swipeOffset < -swipeThreshold) {
                                                cardRotation = (-3..3).random().toFloat()
                                                viewModel.updateWrongAnswer(cardId)
                                                viewModel.loadRandomFlashcard()
                                            }

                                            // Fetch review stats *after* update has been applied
                                            if (cardId.isNotBlank() && cardId != "null") {
                                                val (reviewCount, correctCount) = viewModel.fetchReviewStats(
                                                    cardId
                                                )
                                                fetchedReviewedTimes = reviewCount
                                                fetchedCorrectTimes = correctCount
                                            }

                                            isDragging = false
                                        }
                                    }

                                )
                            }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() }, // Custom interaction source
                                indication = null // Remove the default indication (no ripple effect)
                            ) {
                                flipped = !flipped
                            },
                        contentAlignment = Alignment.Center
                    ) {

                        if (!flipped) {
                            Text(
                                text = currentFlashcard?.text ?: "Loading...",
                                modifier = Modifier.offset(y = (-80).dp),
                                style = MaterialTheme.typography.headlineMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Visible
                            )
                        } else {
                            Text(
                                text = currentFlashcard?.translations?.joinToString(", ")
                                    ?: "No translations",
                                modifier = Modifier.offset(y = (-80).dp),
                                style = MaterialTheme.typography.headlineMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Visible
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(90.dp)
                                .width(150.dp)
                                .offset(y = (100).dp)
                                .background(Color(0xFFB98080), shape = RoundedCornerShape(8.dp))
                                .clickable { showDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Description")
                        }

                        if (showDialog) {
                            // Guard: only fetch if we have a valid cardId
                            if (cardId.isNotBlank() && cardId != "null") {
                                // State to track when the description has been fetched
                                var fetchedDescription by remember { mutableStateOf<String?>(null) }
                                // State to hold the current (editable) description
                                var editedDescription by remember { mutableStateOf("") }

                                LaunchedEffect(cardId) {
                                    Log.d("PlayScreen", "Fetching description for cardId: $cardId")
                                    val desc = viewModel.fetchDescription(cardId)
                                    fetchedDescription = desc
                                    // Initialize the editable value with the fetched value.
                                    editedDescription = desc
                                }

                                AlertDialog(
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .height(300.dp), // Adjust height as needed
                                    onDismissRequest = { showDialog = false },
                                    title = { Text("Flashcard Description") },
                                    text = {
                                        if (fetchedDescription == null) {
                                            // Show a loading text until the description is fetched
                                            Text("Loading...")
                                        } else {
                                            // Display the fetched description in an editable field
                                            OutlinedTextField(
                                                value = editedDescription,
                                                onValueChange = { editedDescription = it },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(200.dp)
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                viewModel.updateDescription(
                                                    cardId,
                                                    editedDescription
                                                )
                                                showDialog = false
                                            }
                                        ) {
                                            Text("Save")
                                        }
                                    },
                                    dismissButton = {
                                        Button(
                                            onClick = { showDialog = false }
                                        ) {
                                            Text("Close")
                                        }
                                    }
                                )
                            } else {
                                Log.d("PlayScreen", "Invalid cardId: $cardId")
                            }
                        }
                    }

                }


                //            Spacer(modifier = Modifier.height(60.dp))
                //
                //            Text(
                //                text = "Section: ${currentSection?.plus(1)}  Unit: ${currentUnit?.plus(1)}",
                //                style = MaterialTheme.typography.headlineMedium,
                //                textAlign = TextAlign.Center,
                //                maxLines = 1,
                //                overflow = TextOverflow.Visible
                //            )
                //
                //            Spacer(modifier = Modifier.height(100.dp))


                //
                //            Box(
                //                modifier = Modifier
                //                    .offset(x = (-80).dp)
                //                    .height(90.dp)
                //                    .width(150.dp)
                //                    .background(Color(0xFF5B7356), shape = RoundedCornerShape(8.dp)),
                //                contentAlignment = Alignment.Center
                //            ) {
                //                Text(
                //                    text = if (fetchedReviewedTimes != null && fetchedCorrectTimes != null) {
                //                        "Reviewed $fetchedReviewedTimes times\nCorrect $fetchedCorrectTimes times"
                //                    } else {
                //                        "Loading..."
                //                    },
                //                    color = Color.White, // Optional: ensures visibility
                //                    textAlign = TextAlign.Center // Optional: centers multi-line text
                //                )
                //            }
            }
        }
    }
}
