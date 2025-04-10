package com.example.memory.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.memory.viewmodel.PlayCardViewModel

@Composable
fun UnitSelectionMenu(viewModel: PlayCardViewModel, onUnitSelected: (Int) -> Unit) {
    val flashcardUnits by viewModel.flashcardUnits.observeAsState(emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Select a Unit", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(flashcardUnits) { index, unit ->
                Button(
                    onClick = { onUnitSelected(index) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(text = "Unit ${index + 1}: ${unit.unitName}") // Add 1 to index
                }
            }
        }
    }
}

@Composable
fun SectionSelectionMenu(viewModel: PlayCardViewModel, onSectionSelected: (Int) -> Unit){
    val flashcardSections by viewModel.flashcardSections.observeAsState(emptyList())

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
