package com.example.memory.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import com.example.memory.viewmodel.PlayCardViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.memory.R
import com.example.memory.ui.components.readCard
import com.example.memory.ui.components.rememberTts
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun ListenScreen(viewModel: PlayCardViewModel = viewModel()) {
    val tts = rememberTts()
    val scope = rememberCoroutineScope()

    // dynamically chosen source‐text language
    val sourceLang by viewModel.sourceLang.collectAsState()
    val currentFlashcard by viewModel.currentFlashcard.observeAsState()

    var isActive by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Image(
            painter = painterResource(id = R.drawable.woodenbackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter,
            modifier = Modifier.matchParentSize()
        )

        // Current word display in the middle of the screen
        currentFlashcard?.let { flashcard ->
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
                    .fillMaxWidth(0.8f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = flashcard.text,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    if (flashcard.translations.isNotEmpty()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.Gray.copy(alpha = 0.5f)
                        )
                        Text(
                            text = flashcard.translations.joinToString(", "),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        // Play button
        IconButton(
            onClick = {
                isActive = true
                currentFlashcard?.let { _ ->
                    scope.launch {
                        while (isActive) {
                            viewModel.loadRandomFlashcard()
                            // Wait for the current flashcard to update
                            delay(300)
                            // Get the latest flashcard
                            viewModel.currentFlashcard.value?.let { currentCard ->
                                tts.readCard(currentCard, sourceLang)
                            }
                            // Add a longer delay between repetitions
                            delay(1000)
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(bottom = 64.dp)
                .size(100.dp)
                .align(Alignment.BottomStart)
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Read card",
                modifier = Modifier.size(64.dp),
                tint = Color.White
            )
        }

        // Stop button
        IconButton(
            onClick = {
                isActive = false
            },
            modifier = Modifier
                .padding(bottom = 64.dp)
                .size(100.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(
                Icons.Default.Clear,
                contentDescription = "Stop reading",
                modifier = Modifier.size(64.dp),
                tint = Color.White
            )
        }
    }
}