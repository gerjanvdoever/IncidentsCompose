package com.example.incidentscompose.ui.screens.management

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.navigation.IncidentListKey
import com.example.incidentscompose.navigation.IncidentMapKey
import com.example.incidentscompose.navigation.MyIncidentListKey
import com.example.incidentscompose.navigation.UserManagementKey
import com.example.incidentscompose.ui.components.BottomNavBar
import com.example.incidentscompose.ui.components.FilterDialog
import com.example.incidentscompose.ui.components.LoadingOverlay
import com.example.incidentscompose.ui.components.SearchAndFilterBar
import com.example.incidentscompose.util.IncidentDisplayHelper
import com.example.incidentscompose.viewmodel.IncidentManagementViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun IncidentListScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToIncidentMap: () -> Unit,
    onNavigateToUserManagement: () -> Unit,
    onNavigateToMyIncidentList: () -> Unit,
    viewModel: IncidentManagementViewModel = koinViewModel()
) {
    val unauthorizedState by viewModel.unauthorizedState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isLoading by viewModel.isBusy.collectAsState()
    val filteredIncidents by viewModel.filteredIncidents.collectAsState()
    val showLoadMore by viewModel.showLoadMore.collectAsState()

    var showFilterMenu by remember { mutableStateOf(false) }

    LaunchedEffect(unauthorizedState) {
        if (unauthorizedState) {
            onNavigateToMyIncidentList()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshIncidents()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(
                currentKey = IncidentListKey,
                userRole = userRole,
                onNavigateTo = { route ->
                    when (route) {
                        IncidentMapKey -> onNavigateToIncidentMap()
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                SearchAndFilterBar(
                    viewModel = viewModel,
                    onFilterClick = { showFilterMenu = true }
                )

                if (filteredIncidents.isEmpty() && !isLoading) {
                    EmptyState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredIncidents, key = { it.id }) { incident ->
                            IncidentCard(
                                incident = incident,
                                onClick = {
                                    onNavigateToDetail(incident.id)
                                }
                            )
                        }
                        if (showLoadMore) {
                            item {
                                Button(
                                    onClick = { viewModel.loadMoreIncidents() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Load More")
                                }
                            }
                        }
                    }
                }
            }

            LoadingOverlay(isLoading = isLoading)
        }
    }

    if (showFilterMenu) {
        FilterDialog(
            viewModel = viewModel,
            onDismiss = { showFilterMenu = false }
        )
    }
}

@Composable
fun IncidentCard(
    incident: IncidentResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = IncidentDisplayHelper.formatCategoryText(incident.category),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                StatusBadge(status = incident.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = incident.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PriorityChip(priority = incident.priority)

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Due: ${IncidentDisplayHelper.formatDateForDisplay(incident.dueAt)}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val statusColor = IncidentDisplayHelper.getStatusColor(status)

    Surface(
        color = statusColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = statusColor,
            fontSize = 11.sp
        )
    }
}

@Composable
fun PriorityChip(priority: String) {
    val (backgroundColor, textColor) = when (priority.uppercase()) {
        "CRITICAL" -> Color(0xFFD32F2F) to Color.White
        "HIGH" -> Color(0xFFF57C00) to Color.White
        "NORMAL" -> Color(0xFFFDD835) to Color.Black
        "LOW" -> Color(0xFF66BB6A) to Color.White
        else -> Color.Gray to Color.White
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = priority.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 11.sp
        )
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No incidents found",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Try adjusting your search or filters",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}