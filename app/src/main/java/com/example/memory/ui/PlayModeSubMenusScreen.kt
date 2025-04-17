package com.example.memory.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.memory.viewmodel.PlayCardViewModel
import kotlinx.coroutines.launch

@Composable
fun SectionSelectionPlayMenu(viewModel: PlayCardViewModel, onSectionSelected: (Int) -> Unit){
    val flashcardSections by viewModel.flashcardSections.observeAsState(emptyList())

    val alertMessage by viewModel.newEntriesAlert.observeAsState()

    // This is to show if there is some inconsistency in the database
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

    LaunchedEffect(Unit) {
        viewModel.resetSectionSelection()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Select a Section", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(flashcardSections) { index, unit ->
                Button(
                    onClick = { onSectionSelected(index) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(text = unit.sectionName)
                }
            }
        }
    }
}

@Composable
fun UnitSelectionPlayMenu(viewModel: PlayCardViewModel, navController: NavController) {
    val flashcardUnits by viewModel.flashcardUnits.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.resetUnitSelection()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Select a Unit", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {

            item {
                Button(
                    onClick = { navController.navigate("play_mode") },
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Text(text = "Play with all units")
                }
            }

            itemsIndexed(flashcardUnits) { index, unit ->
                Button(
                    onClick = {
                        viewModel.selectUnit(index)
                        navController.navigate("play_mode") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(text = "Unit ${index + 1}: ${unit.unitName}") // Add 1 to index
                }
            }
        }
    }
}

@Composable
fun ProgressSectionSelectionPlayMenu(viewModel: PlayCardViewModel, onSectionSelected: (Int) -> Unit){
    val flashcardSections by viewModel.flashcardSections.observeAsState(emptyList())

    val alertMessage by viewModel.newEntriesAlert.observeAsState()

    // This is to show if there is some inconsistency in the database
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Select a Section", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(flashcardSections) { index, unit ->
                Button(
                    onClick = { onSectionSelected(index) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(text = unit.sectionName)
                }
            }
        }
    }
}

@Composable
fun ProgressUnitSelectionPlayMenu(viewModel: PlayCardViewModel, progressSectionIndex: Int) {
    val flashcardUnits by viewModel.flashcardUnits.observeAsState(emptyList())
    // Create a SnackbarHostState manually using remember
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding)
        ) {
            Text(text = "Select a Unit", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(flashcardUnits) { index, unit ->
                    Button(
                        onClick = {
                            coroutineScope.launch {
                            viewModel.updateProgress(newSection = progressSectionIndex, newUnit = index)

                            snackbarHostState.showSnackbar(
                                message = "Progress updated",
                                duration = SnackbarDuration.Short
                            )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(text = "Unit ${index + 1}: ${unit.unitName}")
                    }
                }
            }
        }
    }
}

