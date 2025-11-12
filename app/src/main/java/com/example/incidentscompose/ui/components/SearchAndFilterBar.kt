package com.example.incidentscompose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.incidentscompose.R
import com.example.incidentscompose.util.IncidentDisplayHelper
import com.example.incidentscompose.viewmodel.IncidentManagementViewModel
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SearchAndFilterBar(
    viewModel: IncidentManagementViewModel,
    onFilterClick: () -> Unit
) {
    val hasActiveFilters by remember { derivedStateOf { viewModel.hasActiveFilters } }

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
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                SearchTextField(viewModel = viewModel)
            }

            FilterIconButton(
                hasActiveFilters = hasActiveFilters,
                onFilterClick = onFilterClick
            )
        }
    }
}

@Composable
private fun SearchTextField(
    viewModel: IncidentManagementViewModel
) {
    val searchQuery by viewModel.searchQuery.collectAsState()

    OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.updateSearchQuery(it) },
        modifier = Modifier.fillMaxWidth(), // Now this works inside the Box with weight
        placeholder = { Text(stringResource(R.string.search_incidents)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun FilterIconButton(
    hasActiveFilters: Boolean,
    onFilterClick: () -> Unit
) {
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
            contentDescription = stringResource(R.string.filter),
            tint = if (hasActiveFilters) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FilterDialog(
    viewModel: IncidentManagementViewModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.filter_incidents)) },
        text = {
            FilterDialogContent(viewModel = viewModel)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.apply))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.clearAllFilters()
                onDismiss()
            }) {
                Text(stringResource(R.string.clear_all))
            }
        }
    )
}

@Composable
private fun FilterDialogContent(
    viewModel: IncidentManagementViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Priority Filter
        FilterSection(
            title = stringResource(R.string.priority_lowercase),
            options = listOf("LOW", "NORMAL", "HIGH", "CRITICAL"),
            selectedOptionsFlow = viewModel.selectedPriorityFilter,
            onOptionsSelected = { viewModel.updatePriorityFilter(it.toSet()) }
        )

        HorizontalDivider()

        FilterSection(
            title = stringResource(R.string.status_lowercase),
            options = listOf("REPORTED", "ASSIGNED", "RESOLVED"),
            selectedOptionsFlow = viewModel.selectedStatusFilter,
            onOptionsSelected = { viewModel.updateStatusFilter(it.toSet()) }
        )

        HorizontalDivider()

        FilterSection(
            title = stringResource(R.string.category_lowercase),
            options = listOf("CRIME", "ENVIRONMENT", "COMMUNAL", "TRAFFIC", "OTHER"),
            selectedOptionsFlow = viewModel.selectedCategoryFilter,
            onOptionsSelected = { viewModel.updateCategoryFilter(it.toSet()) }
        )
    }
}

@Composable
private fun FilterSection(
    title: String,
    options: List<String>,
    selectedOptionsFlow: StateFlow<Set<String>>,
    onOptionsSelected: (List<String>) -> Unit
) {
    val selectedOptions by selectedOptionsFlow.collectAsState()

    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
    )
    HorizontalScrollableFilterChipGroup(
        options = options,
        selectedOptions = selectedOptions.toList(),
        onOptionsSelected = onOptionsSelected
    )
}

@Composable
fun HorizontalScrollableFilterChipGroup(
    options: List<String>,
    selectedOptions: List<String>,
    onOptionsSelected: (List<String>) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = selectedOptions.contains(option),
                onClick = {
                    val newSelection = if (selectedOptions.contains(option)) {
                        selectedOptions - option
                    } else {
                        selectedOptions + option
                    }
                    onOptionsSelected(newSelection)
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