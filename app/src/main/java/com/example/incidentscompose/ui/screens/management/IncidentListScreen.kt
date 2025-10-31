package com.example.incidentscompose.ui.screens.management

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.navigation.Destinations
import com.example.incidentscompose.ui.components.BottomNavBar
import com.example.incidentscompose.ui.components.LoadingOverlay
import com.example.incidentscompose.util.IncidentDisplayHelper
import com.example.incidentscompose.viewmodel.IncidentManagementViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentListScreen(
    navController: NavController,
    viewModel: IncidentManagementViewModel = koinViewModel()
) {
    val unauthorizedState by viewModel.unauthorizedState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isLoading by viewModel.isBusy.collectAsState()
    val displayedIncidents by viewModel.displayedIncidents.collectAsState()
    val showLoadMore by viewModel.showLoadMore.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showFilterMenu by remember { mutableStateOf(false) }
    var selectedPriorityFilter by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(unauthorizedState) {
        if (unauthorizedState) {
            navController.navigate(Destinations.MyIncidentList.route) {
                popUpTo(Destinations.UserManagement.route) { inclusive = true }
            }
        }
    }

    val filteredIncidents = remember(displayedIncidents, searchQuery, selectedPriorityFilter, selectedStatusFilter, selectedCategoryFilter) {
        displayedIncidents.filter { incident ->
            val matchesSearch = searchQuery.isEmpty() ||
                    incident.category.contains(searchQuery, ignoreCase = true) ||
                    incident.description.contains(searchQuery, ignoreCase = true) ||
                    incident.priority.contains(searchQuery, ignoreCase = true) ||
                    incident.status.contains(searchQuery, ignoreCase = true)

            val matchesPriority = selectedPriorityFilter == null ||
                    incident.priority.equals(selectedPriorityFilter, ignoreCase = true)

            val matchesStatus = selectedStatusFilter == null ||
                    incident.status.equals(selectedStatusFilter, ignoreCase = true)

            val matchesCategory = selectedCategoryFilter == null ||
                    incident.category.equals(selectedCategoryFilter, ignoreCase = true)

            matchesSearch && matchesPriority && matchesStatus && matchesCategory
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(
                currentRoute = Destinations.IncidentList.route,
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Search and Filter Section
                SearchAndFilterBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onFilterClick = { showFilterMenu = true },
                    hasActiveFilters = selectedPriorityFilter != null ||
                            selectedStatusFilter != null ||
                            selectedCategoryFilter != null
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
                                    viewModel.onIncidentTap(incident)
                                    TODO()
                                }
                            )
                        }

                        if (showLoadMore && selectedPriorityFilter == null &&
                            selectedStatusFilter == null && selectedCategoryFilter == null &&
                            searchQuery.isEmpty()) {
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

    // Filter Dialog
    if (showFilterMenu) {
        FilterDialog(
            selectedPriority = selectedPriorityFilter,
            selectedStatus = selectedStatusFilter,
            selectedCategory = selectedCategoryFilter,
            onPrioritySelected = { selectedPriorityFilter = it },
            onStatusSelected = { selectedStatusFilter = it },
            onCategorySelected = { selectedCategoryFilter = it },
            onDismiss = { showFilterMenu = false },
            onClearFilters = {
                selectedPriorityFilter = null
                selectedStatusFilter = null
                selectedCategoryFilter = null
            }
        )
    }
}

@Composable
fun SearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    hasActiveFilters: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search incidents...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            IconButton(
                onClick = onFilterClick,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (hasActiveFilters) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Filter",
                    tint = if (hasActiveFilters) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
        "MEDIUM" -> Color(0xFFFDD835) to Color.Black
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

@Composable
fun FilterDialog(
    selectedPriority: String?,
    selectedStatus: String?,
    selectedCategory: String?,
    onPrioritySelected: (String?) -> Unit,
    onStatusSelected: (String?) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    onClearFilters: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Incidents") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Priority Filter
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                FilterChipGroup(
                    options = listOf("LOW", "MEDIUM", "HIGH", "CRITICAL"),
                    selectedOption = selectedPriority,
                    onOptionSelected = onPrioritySelected
                )

                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                // Status Filter
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                FilterChipGroup(
                    options = listOf("REPORTED", "ASSIGNED", "RESOLVED"),
                    selectedOption = selectedStatus,
                    onOptionSelected = onStatusSelected
                )

                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                // Category Filter
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                FilterChipGroup(
                    options = listOf("CRIME", "ENVIRONMENT", "COMMUNAL", "TRAFFIC", "OTHER"),
                    selectedOption = selectedCategory,
                    onOptionSelected = onCategorySelected
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onClearFilters()
                onDismiss()
            }) {
                Text("Clear All")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipGroup(
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = selectedOption == option,
                onClick = {
                    onOptionSelected(if (selectedOption == option) null else option)
                },
                label = {
                    Text(
                        text = IncidentDisplayHelper.formatCategoryText(option),
                        fontSize = 12.sp
                    )
                }
            )
        }
    }
}