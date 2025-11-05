package com.example.incidentscompose.ui.screens.management

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.incidentscompose.R
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.data.model.Priority
import com.example.incidentscompose.data.model.Status
import com.example.incidentscompose.ui.components.BottomNavBar
import com.example.incidentscompose.ui.components.LoadingOverlay
import com.example.incidentscompose.ui.components.TopNavBar
import com.example.incidentscompose.util.ImageUrlHelper
import com.example.incidentscompose.util.IncidentDisplayHelper.formatCategoryText
import com.example.incidentscompose.util.IncidentDisplayHelper.formatDateForDisplay
import com.example.incidentscompose.util.IncidentDisplayHelper.getStatusColor
import com.example.incidentscompose.viewmodel.IncidentManagementViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun IncidentDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMyIncidentList: () -> Unit,
    incidentId: Long?,
    viewModel: IncidentManagementViewModel = koinViewModel()
) {

    val incident by viewModel.currentIncident.collectAsState()
    val isBusy by viewModel.isBusy.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val reportedUser by viewModel.reportedUser.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val unauthorizedState by viewModel.unauthorizedState.collectAsState()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Load incident when screen opens or incidentId changes
    LaunchedEffect(incidentId) {
        if (incidentId != null) {
            viewModel.getIncidentById(incidentId)
        }
    }

    LaunchedEffect(unauthorizedState) {
        if (unauthorizedState) {
            onNavigateToMyIncidentList()
        }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearToastMessage()
        }
    }

    // Clear data when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearCurrentIncident()
        }
    }

    // Delete Confirmation Dialog (keep your existing dialog code)
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.delete),
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.delete_incident),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.are_you_sure_you_want_to_delete_this_incident_this_action_cannot_be_undone),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        incident?.let { inc ->
                            viewModel.deleteIncident(inc.id)
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text(
                        text = "Delete",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false }
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = stringResource(R.string.incident_details),
                showBackButton = true,
                onBackClick = { onNavigateBack() },
                backgroundColor = MaterialTheme.colorScheme.surface,
                textColor = MaterialTheme.colorScheme.onSurface
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (isBusy && incident == null) {
                LoadingOverlay(isLoading = true)
            } else if (incident == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.incident_details_not_available),
                        color = Color(0xFF6B7280),
                        fontSize = 16.sp
                    )
                }
            } else {
                IncidentManagementContent(
                    incident = incident!!,
                    reportedUser = reportedUser,
                    onPriorityChange = { priority ->
                        viewModel.updatePriority(incident!!.id, priority)
                    },
                    onStatusChange = { status ->
                        viewModel.updateStatus(incident!!.id, status)
                    },
                    onDelete = {
                        showDeleteConfirmDialog = true
                    }
                )
            }

            LoadingOverlay(isLoading = isBusy)
        }
    }
}

@Composable
private fun IncidentManagementContent(
    incident: IncidentResponse,
    reportedUser: com.example.incidentscompose.data.model.UserResponse?,
    onPriorityChange: (Priority) -> Unit,
    onStatusChange: (Status) -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IncidentManagementHeaderCard(
            incident = incident,
            onPriorityChange = onPriorityChange,
            onStatusChange = onStatusChange
        )

        ReporterInfoCard(incident, reportedUser)

        IncidentDescriptionCard(incident.description)

        IncidentImagesCard(incident)

        IncidentLocationCard(incident)

        DeleteButton(onDelete = onDelete)

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun DeleteButton(onDelete: () -> Unit) {
    OutlinedButton(
        onClick = onDelete,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFFD32F2F)
        ),
        border = BorderStroke(1.5.dp, Color(0xFFD32F2F))
    ) {
        Icon(
            painter = painterResource(id = R.drawable.delete),
            contentDescription = stringResource(R.string.delete_incident),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.delete_incident_button),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun IncidentManagementHeaderCard(
    incident: IncidentResponse,
    onPriorityChange: (Priority) -> Unit,
    onStatusChange: (Status) -> Unit
) {
    var priorityExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // CATEGORY (Read-only)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.category),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    letterSpacing = 0.8.sp
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF9FAFB),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Text(
                        text = formatCategoryText(incident.category),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // PRIORITY DROPDOWN
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "PRIORITY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    letterSpacing = 0.8.sp
                )

                Box {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { priorityExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        color = getPriorityColor(incident.priority).copy(alpha = 0.12f),
                        border = BorderStroke(1.5.dp, getPriorityColor(incident.priority))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = incident.priority.uppercase(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = getPriorityColor(incident.priority)
                            )
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = "Expand",
                                tint = getPriorityColor(incident.priority)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Priority.entries.forEach { priority ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = priority.name,
                                        color = getPriorityColor(priority.name)
                                    )
                                },
                                onClick = {
                                    onPriorityChange(priority)
                                    priorityExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // STATUS DROPDOWN
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "STATUS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    letterSpacing = 0.8.sp
                )

                Box {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { statusExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        color = getStatusColor(incident.status).copy(alpha = 0.12f),
                        border = BorderStroke(1.5.dp, getStatusColor(incident.status))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            getStatusColor(incident.status),
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                                Text(
                                    text = incident.status.uppercase(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = getStatusColor(incident.status)
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = "Expand",
                                tint = getStatusColor(incident.status)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Status.entries.forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = status.name,
                                        color = getStatusColor(status.name)
                                    )
                                },
                                onClick = {
                                    onStatusChange(status)
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = Color(0xFFE5E7EB))

            // UPDATED DATE/TIME INFORMATION LAYOUT
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // CREATED DATE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "CREATED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280),
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = formatDateForDisplay(incident.createdAt),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF111827)
                    )
                }

                // UPDATED DATE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "UPDATED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280),
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = formatDateForDisplay(incident.updatedAt),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF111827)
                    )
                }

                // DUE DATE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "DUE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280),
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = formatDateForDisplay(incident.dueAt),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF111827)
                    )
                }

                // COMPLETED DATE (only show if exists)
                if (incident.completedAt != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "COMPLETED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280),
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = formatDateForDisplay(incident.completedAt),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF111827)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReporterInfoCard(
    incident: IncidentResponse,
    reportedUser: com.example.incidentscompose.data.model.UserResponse?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "REPORTED BY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    letterSpacing = 0.8.sp
                )
            }

            // FIXED LOGIC: Only show anonymous if explicitly marked as anonymous
            // OR if there's no reportedBy ID at all
            if (incident.isAnonymous || incident.reportedBy == null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Anonymous Report",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            } else if (reportedUser != null) {
                // Show user info when we have it
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF9FAFB),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Username",
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = reportedUser.username,
                                fontSize = 14.sp,
                                color = Color(0xFF111827),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE5E7EB))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Email",
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = reportedUser.email,
                                fontSize = 14.sp,
                                color = Color(0xFF111827),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                // Show loading state while fetching user data
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF9FAFB)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IncidentDescriptionCard(description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.description),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280),
                letterSpacing = 0.8.sp
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF9FAFB),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Text(
                    text = description,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF374151),
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun IncidentImagesCard(incident: IncidentResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.photos),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    letterSpacing = 0.8.sp
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF3F4F6)
                ) {
                    Text(
                        text = if (incident.images.isNotEmpty()) "${incident.images.size}" else "0",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (incident.images.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(incident.images.count()) { index ->
                        val imageUrl = ImageUrlHelper.getFullImageUrl(incident.images[index])
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.size(140.dp),
                            color = Color(0xFFF3F4F6)
                        ) {
                            if (!imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Incident image ${index + 1}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFF3F4F6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No image",
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            Color(0xFFF9FAFB),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_photos_available),
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun IncidentLocationCard(incident: IncidentResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.location),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    letterSpacing = 0.8.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF9FAFB)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Latitude",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = incident.latitude.toString(),
                            fontSize = 14.sp,
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE5E7EB))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Longitude",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = incident.longitude.toString(),
                            fontSize = 14.sp,
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getPriorityColor(priority: String): Color {
    return when (priority.uppercase()) {
        "LOW" -> Color(0xFF10B981)
        "MEDIUM" -> Color(0xFFF59E0B)
        "HIGH" -> Color(0xFFFF6B35)
        "CRITICAL" -> Color(0xFFDC2626)
        else -> Color.Gray
    }
}