package com.example.incidentscompose.ui.screens.management

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.incidentscompose.navigation.Destinations
import com.example.incidentscompose.ui.components.BottomNavBar
import com.example.incidentscompose.ui.components.LoadingOverlay
import com.example.incidentscompose.viewmodel.IncidentManagementViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun IncidentMapScreen(
    navController: NavController,
    viewModel: IncidentManagementViewModel = koinViewModel()
){
    val unauthorizedState by viewModel.unauthorizedState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isLoading by viewModel.isBusy.collectAsState()

    LaunchedEffect(unauthorizedState) {
        if (unauthorizedState) {
            navController.navigate(Destinations.MyIncidentList.route) {
                popUpTo(Destinations.UserManagement.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(
                currentRoute = Destinations.IncidentMap.route,
                userRole = userRole,
                onItemClick = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Here goes map"
            )
            LoadingOverlay(isLoading = isLoading)
        }
    }
}