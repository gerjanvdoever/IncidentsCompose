package com.example.incidentscompose.ui.screens.incidents

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.incidentscompose.viewmodel.MyIncidentListViewModel
import org.koin.compose.koinInject

@Composable
fun MyIncidentListScreen(
    navController: NavController,
    viewModel: MyIncidentListViewModel = koinInject()
) {
    val isLoggingOut by viewModel.isLoggingOut.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("My Incidents Screen", style = MaterialTheme.typography.headlineMedium)

            Button(
                onClick = {
                    viewModel.logout {
                        // This callback runs after logout completes
                        navController.navigate("login") {
                            // Clear the back stack so user can't go back to incidents
                            popUpTo("incidentList") { inclusive = true }
                        }
                    }
                },
                enabled = !isLoggingOut,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                if (isLoggingOut) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logging out...")
                } else {
                    Text("Logout")
                }
            }
        }
    }
}