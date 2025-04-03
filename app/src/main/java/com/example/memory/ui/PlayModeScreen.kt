package com.example.memory.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
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

    val swipeThreshold = 600f // Distance required for a swipe to register
    var swipeOffset by remember { mutableStateOf(0f) }
    var isSwiping by remember { mutableStateOf(false) }
    var randomSectionIndex by remember { mutableStateOf(-1) }
    var randomUnitIndex by remember { mutableStateOf(-1) }

    // Animate swipe back to center when released
    val animatedSwipeOffset by animateFloatAsState(
        targetValue = if (isSwiping) swipeOffset else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "Swipe Animation"
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

        // Flashcard Box with Swipe Gesture Detection
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f) // 90% of the screen width
                .height(200.dp) // Adjust height as needed
                .graphicsLayer(translationX = animatedSwipeOffset) // Move entire Box
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isSwiping = true },
                        onDrag = { change, dragAmount ->
                            change.consume() // Consume gesture to prevent interference
                            swipeOffset = if (swipeOffset * dragAmount.x < 0) {
                                // If direction changes, reset the offset to the current delta.
                                dragAmount.x
                            } else {
                                swipeOffset + dragAmount.x
                            }
                        },
                        onDragEnd = {
                            if (abs(swipeOffset) > swipeThreshold) {
                                val result = viewModel.loadRandomFlashcard() // Get the returned values
                                result?.let { (sectionIndex, unitIndex) ->
                                    randomSectionIndex = sectionIndex + 1
                                    randomUnitIndex = unitIndex + 1
                                }
                                swipeOffset = 0f // Reset immediately for new card
                            }
                            isSwiping = false // Allow animation to reset position
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentFlashcard?.text ?: "Loading...",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Visible
            )
        }

        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "Section: $randomSectionIndex  Unit: $randomUnitIndex",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Visible
        )
    }
}
