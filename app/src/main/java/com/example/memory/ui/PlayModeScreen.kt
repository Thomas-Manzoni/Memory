package com.example.memory.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memory.viewmodel.PlayCardViewModel
import kotlin.math.abs

@Composable
fun PlayScreen(viewModel: PlayCardViewModel = viewModel()) {
    val currentFlashcard by viewModel.currentFlashcard.observeAsState()
    val currentSection by viewModel.currentSectionVal.observeAsState()
    val currentUnit by viewModel.currentUnitVal.observeAsState()

    val swipeThreshold = 400f // Distance required for a swipe to register
    var swipeOffset by remember { mutableStateOf(0f) }
    var isSwiping by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }

    var flipped by remember { mutableStateOf(false) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(120.dp))

        // Flashcard Box with Swipe Gesture Detection and Clickable Box
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(200.dp)
                .graphicsLayer(translationX = animatedSwipeOffset) // Use animated offset only for reset
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            swipeOffset += dragAmount.x // Instant movement while dragging
                        },
                        onDragEnd = {
                            if (abs(swipeOffset) > swipeThreshold) {
                                viewModel.loadRandomFlashcard() // Get the returned values
                            }
                            isDragging = false // Triggers animation back to 0f
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

            if (flipped) {
                Text(
                    text = currentFlashcard?.text ?: "Loading...",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Visible
                )
            } else {
                Text(
                    text = currentFlashcard?.translations?.joinToString(", ") ?: "No translations",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Visible
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "Section: ${currentSection?.plus(1)}  Unit: ${currentUnit?.plus(1)}",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Visible
        )
    }
}
