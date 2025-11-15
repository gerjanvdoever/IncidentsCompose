package com.example.incidentscompose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.incidentscompose.ui.theme.IncidentsComposeTheme
import java.time.format.DateTimeFormatter

@Composable
fun IncidentsListScreen(
    viewModel: IncidentsViewModel = viewModel()
) {
    val incidents by viewModel.incidents.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Incidents",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${incidents.size} actieve meldingen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(incidents, key = { it.id }) { incident ->
                    IncidentListItem(
                        incident = incident,
                        onRemove = { id -> viewModel.removeIncident(id) }
                    )
                }
            }
        }
    }
}

@Composable
fun IncidentListItem(incident: Incident, onRemove: (Int) -> Unit, viewModel: IncidentsViewModel = viewModel()) {
    var expanded by remember { mutableStateOf(false) }
    var showDropdown by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(300),
        label = "rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (expanded) 12.dp else 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    PriorityBadge(priority = incident.priority)

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = incident.category,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = incident.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (expanded) Int.MAX_VALUE else 1
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Expanded content
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DetailChip(
                        label = "Status",
                        value = incident.status,
                        color = getStatusColor(incident.status)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = incident.reportedAt.format(DateTimeFormatter.ofPattern("dd-MM HH:mm")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Box(
                            modifier = Modifier.wrapContentSize(Alignment.TopEnd),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            IconButton(onClick = { showDropdown = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            DropdownMenu(
                                expanded = showDropdown,
                                onDismissRequest = { showDropdown = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Remove Incident") },
                                    onClick = {
                                        onRemove(incident.id)
                                        showDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Edit Status") },
                                    onClick = {
                                        showStatusDialog = true
                                        showDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Change Status") },
            text = {
                Column {
                    listOf("Reported", "In Progress", "Resolved").forEach { status ->
                        Text(
                            text = status,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateIncidentStatus(incident.id, status)
                                    showStatusDialog = false
                                }
                                .padding(vertical = 8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }
}
@Composable
fun PriorityBadge(priority: Priority) {
    val backgroundColor by animateColorAsState(
        targetValue = when (priority) {
            Priority.HIGH -> Color(0xFFFF5252)
            Priority.MEDIUM -> Color(0xFFFFB74D)
            Priority.LOW -> Color(0xFF66BB6A)
        },
        animationSpec = tween(300),
        label = "background"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = priority.label[0].toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun DetailChip(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$label: $value",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun getStatusColor(status: String): Color {
    return when (status) {
        "Reported" -> Color(0xFF2196F3)
        "In Progress" -> Color(0xFFFF9800)
        "Resolved" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
}

@Preview(showBackground = true)
@Composable
fun IncidentsListScreenPreview() {
    IncidentsComposeTheme {
        IncidentsListScreen()
    }
}