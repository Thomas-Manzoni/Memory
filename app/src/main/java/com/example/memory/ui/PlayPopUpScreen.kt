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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun PopUpPlay (
    onDismiss: () -> Unit,
    onTestUnit: () -> Unit,
    navController: NavController,
    viewModel: PlayCardViewModel
) {
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
                    .height(500.dp)
                    .width(350.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0f))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(80.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF447E78),
                        contentColor = Color.White
                    ),
                    onClick = {
                        onDismiss()
                        viewModel.randomWeightedMode = true
                        navController.navigate("play_mode")
                    }) {
                    Text("Weighted random selection mode")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(80.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF447E78),
                        contentColor = Color.White
                    ),
                    onClick = {
                        onDismiss()
                        viewModel.untilProgressedUnit = true
                        navController.navigate("play_mode")
                    }) {
                    Text("Play with all learned cards")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(80.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF447E78),
                        contentColor = Color.White
                    ),
                    onClick = {
                        onDismiss()
                        viewModel.preSelectionMode = true
                        onTestUnit()
                    }) {
                    Text("Test unit")
                }
            }
        }
    }
}
