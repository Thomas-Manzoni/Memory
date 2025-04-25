package com.example.memory.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memory.R
import com.example.memory.viewmodel.PlayCardViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(viewModel: PlayCardViewModel = viewModel()) {
    // 1) UI state for tabs & search query
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    // 2) Collect your full list from the VM
    val allCards by viewModel.displayCards.collectAsState()
    val favoriteStatusMap = remember { mutableStateMapOf<String, Boolean>() }

// Launch a coroutine to load favorites when the screen is first displayed
    LaunchedEffect(Unit) {
        allCards.forEach { card ->
            favoriteStatusMap[card.wordId] = viewModel.fetchIsFavorite(card.wordId)
        }
    }

    // 3) Filtered list
    val results = remember(allCards, searchQuery) {
        if (searchQuery.isBlank()) allCards
        else allCards.filter { card ->
            card.text.contains(searchQuery, ignoreCase = true) ||
                    card.translations.any { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.woodenbackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter,
            modifier = Modifier.matchParentSize()
        )

        Column(Modifier.fillMaxSize()) {

            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFFADD2D2),
                contentColor = Color(0xFF090C0C)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("All") },
                    selectedContentColor = Color(0xFFE50A0A),
                    unselectedContentColor = Color(0xFF000000)
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Search") },
                    selectedContentColor = Color(0xFFE50A0A),
                    unselectedContentColor = Color(0xFF000909)
                )
            }

            // 5) Only show the search field when “Search” is selected
            if (selectedTab == 1) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search cards…") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor      = Color(0xFFFFFFFF),
                        unfocusedBorderColor    = Color(0xFFFFFFFF),
                        focusedLabelColor       = Color(0xFFFFFFFF),
                        unfocusedLabelColor     = Color(0xFFFCFCFC),
                        cursorColor             = Color(0xFFFFFFFF),
                        focusedTextColor        = Color(0xFFFFFFFF),
                        unfocusedTextColor      = Color(0xFFFFFFFF),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

            // 6) Show either allCards or results based on tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                val listToShow = if (selectedTab == 0) allCards else results
                items(listToShow) { card ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF447E78)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = card.text,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .align(Alignment.CenterStart),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )

                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Favorite",
                                    tint = if (favoriteStatusMap[card.wordId] == true) Color(0xFFFFD700) else Color(0xFFF1EFEF),
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 16.dp)
                                        .size(24.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null  // Removes the ripple effect
                                        ) {
                                            coroutineScope.launch {
                                                val newFavoriteState = !favoriteStatusMap.getOrDefault(card.wordId, false)
                                                viewModel.updateFavoriteStatus(card.wordId, newFavoriteState)
                                                favoriteStatusMap[card.wordId] = newFavoriteState
                                            }
                                        }
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF92D3C6)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = card.translations.joinToString(", "),
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}


