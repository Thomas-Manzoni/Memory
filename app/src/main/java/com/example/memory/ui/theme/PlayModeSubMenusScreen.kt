package com.example.memory.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.memory.viewmodel.FlashcardViewModel
import com.example.memory.viewmodel.PlayCardViewModel

@Composable
fun SectionSelectionPlayMenu(viewModel: PlayCardViewModel, onSectionSelected: (Int) -> Unit){
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

@Composable
fun UnitSelectionPlayMenu(viewModel: PlayCardViewModel, onUnitSelected: (Int) -> Unit) {
    val flashcardUnits by viewModel.flashcardUnits.observeAsState(emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Select a Unit", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {

            item {
                Button(
                    onClick = { onUnitSelected(4) },
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Text(text = "Manual Entry")
                }
            }

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