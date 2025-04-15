package com.example.memory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.memory.viewmodel.PlayCardViewModel

@Composable
fun PopUpSection (
    onDismiss: () -> Unit,
    onSectionSelected: (index: Int) -> Unit,
    navController: NavController,
    viewModel: PlayCardViewModel
) {
    val flashcardSections by viewModel.flashcardSections.observeAsState(emptyList())

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Dimmed clickable background
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { onDismiss() }
            )

            // Your popup menu content

            Column(
                modifier = Modifier
                    .height(700.dp)
                    .width(350.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFAD8661))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(flashcardSections) { index, unit ->
                        Button(
                            onClick = {
                                viewModel.selectSection(index)
                                onSectionSelected(index) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(text = unit.sectionName)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PopUpUnit (
    onDismiss: () -> Unit,
    onUnitSelected: (index: Int) -> Unit,
    navController: NavController,
    viewModel: PlayCardViewModel
) {
    val flashcardUnits by viewModel.flashcardUnits.observeAsState(emptyList())

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Dimmed clickable background
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { onDismiss() }
            )

            // Your popup menu content

            Column(
                modifier = Modifier
                    .height(700.dp)
                    .width(350.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFAD8661))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(flashcardUnits) { index, unit ->
                        Button(
                            onClick = { onUnitSelected(index) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(text = "Unit ${index + 1}: ${unit.unitName}")
                        }
                    }
                }
            }
        }
    }
}
