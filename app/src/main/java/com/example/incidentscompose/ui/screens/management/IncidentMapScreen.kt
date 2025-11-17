package com.example.incidentscompose.ui.screens.management

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.incidentscompose.navigation.IncidentListKey
import com.example.incidentscompose.navigation.IncidentMapKey
import com.example.incidentscompose.navigation.MyIncidentListKey
import com.example.incidentscompose.navigation.UserManagementKey
import com.example.incidentscompose.ui.components.*
import com.example.incidentscompose.viewmodel.IncidentManagementViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun IncidentMapScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToMyIncidentList: () -> Unit,
    onNavigateToIncidentList: () -> Unit,
    onNavigateToUserManagement: () -> Unit,
    viewModel: IncidentManagementViewModel = koinViewModel()
) {
    val unauthorizedState by viewModel.unauthorizedState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isLoading by viewModel.isBusy.collectAsState()
    val filteredIncidents by viewModel.filteredIncidents.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchAndFilterBar(
                viewModel = viewModel,
                onFilterClick = { showFilterDialog = true }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                IncidentMap(
                    modifier = Modifier.fillMaxSize(),
                    incidents = filteredIncidents,
                    isLocationSelectionEnabled = false,
                    allowDetailNavigation = true,
                    onIncidentClick = { incident ->
                        onNavigateToDetail(incident.id)
                    },
                    onLocationSelected = { _, _ -> },
                    userLocation = null,
                    onMapTouch = {  }
                )
            }

            LoadingOverlay(isLoading = isLoading)
        }

        if (showFilterDialog) {
            FilterDialog(
                viewModel = viewModel,
                onDismiss = { showFilterDialog = false }
            )
        }
    }
}