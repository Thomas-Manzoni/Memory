package com.example.memory.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memory.viewmodel.FlashcardViewModel
import androidx.compose.runtime.livedata.observeAsState


@Composable
fun FlashcardScreen(viewModel: FlashcardViewModel = viewModel()) {
    val currentFlashcard by viewModel.currentFlashcard.observeAsState()

    var flipped by remember { mutableStateOf(false) }
    var showTranslationBox by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        // Switch to toggle translation box visibility
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Show Translation Box")
        }

        Spacer(modifier = Modifier.height(20.dp))
        // Switch to toggle translation box visibility
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Switch(
                checked = showTranslationBox,
                onCheckedChange = { showTranslationBox = it }
            )
        }

        Spacer(modifier = Modifier.height(120.dp))
        // Flashcard Box
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f) // 90% of the screen width
                .height(200.dp) // Adjust height as needed
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Rotate only the front and back based on flipped state
            if (!flipped) {
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

        Spacer(modifier = Modifier.height(20.dp))

        // Button to flip the card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = { flipped = !flipped }) {
                Text(if (flipped) "Show Word" else "Show Translations")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Button to show the next flashcard
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { viewModel.showPrevFlashcard() }) {
                Text("Prev")
            }
            Button(onClick = { viewModel.showNextFlashcard() }) {
                Text("Next")
            }
        }

        if (showTranslationBox) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f) // 90% of the screen width
                    .height(100.dp) // Adjust height as needed
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentFlashcard?.translations?.joinToString(", ") ?: "No translations",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Visible
                )
            }
        }
    }
}

