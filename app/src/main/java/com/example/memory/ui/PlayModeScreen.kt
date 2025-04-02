package com.example.memory.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memory.viewmodel.PlayCardViewModel

@Composable
fun PlayScreen(viewModel: PlayCardViewModel = viewModel()) {
    val currentFlashcard by viewModel.currentFlashcard.observeAsState()

    // Swipe threshold
    val swipeThreshold = 500f // The threshold to trigger the action

    // Track the horizontal offset (position) of the flashcard box
    val swipeOffset = remember { mutableStateOf(0f) }

    // Handle swipe gestures
    val onSwipe: (Float) -> Unit = { dragAmount ->
        swipeOffset.value += dragAmount // Update the swipe offset

        // If swipe exceeds the threshold, trigger the corresponding action
        if (swipeOffset.value > swipeThreshold) {
            viewModel.loadRandomFlashcard() // Trigger next flashcard action
            swipeOffset.value = 0f // Reset offset after action
        } else if (swipeOffset.value < -swipeThreshold) {
            viewModel.loadRandomFlashcard() // Trigger previous flashcard action
            swipeOffset.value = 0f // Reset offset after action
        }
    }

    // Animate swipeOffset back to 0 when released or completed
    val animatedSwipeOffset by animateFloatAsState(
        targetValue = swipeOffset.value,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 200) // Smooth animation for 200ms
    )

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
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        // Update position and detect swipe gesture
                        onSwipe(dragAmount)
                    }
                }
                .graphicsLayer(
                    // Move the box along with the swipe offset
                    translationX = animatedSwipeOffset
                ),
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
    }
}
