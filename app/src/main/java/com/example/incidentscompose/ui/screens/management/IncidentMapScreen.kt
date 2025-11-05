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
import com.example.incidentscompose.navigation.IncidentListKey
import com.example.incidentscompose.navigation.IncidentMapKey
import com.example.incidentscompose.navigation.MyIncidentListKey
import com.example.incidentscompose.navigation.UserManagementKey
import com.example.incidentscompose.ui.components.BottomNavBar
import com.example.incidentscompose.ui.components.LoadingOverlay
import com.example.incidentscompose.viewmodel.IncidentManagementViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun IncidentMapScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToMyIncidentList: () -> Unit,
    onNavigateToIncidentList: () -> Unit,
    onNavigateToUserManagement: () -> Unit,
    viewModel: IncidentManagementViewModel = koinViewModel()
){
    val unauthorizedState by viewModel.unauthorizedState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isLoading by viewModel.isBusy.collectAsState()

    LaunchedEffect(unauthorizedState) {
        if (unauthorizedState) {
            onNavigateToMyIncidentList()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(
                currentKey = IncidentMapKey,
                userRole = userRole,
                onNavigateTo = { route ->
                    when (route) {
                        IncidentListKey -> onNavigateToIncidentList()
                        UserManagementKey -> onNavigateToUserManagement()
                        MyIncidentListKey -> onNavigateToMyIncidentList()
                        else -> {}
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