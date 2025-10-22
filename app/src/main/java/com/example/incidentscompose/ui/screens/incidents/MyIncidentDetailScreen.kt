package com.example.incidentscompose.ui.screens.incidents

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.incidentscompose.R
import com.example.incidentscompose.data.model.IncidentCategory
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.ui.components.LoadingOverlay
import com.example.incidentscompose.ui.components.TopNavBar
import com.example.incidentscompose.util.ImageUrlHelper
import com.example.incidentscompose.util.IncidentCategoryUtils
import com.example.incidentscompose.util.IncidentDisplayHelper.formatCategoryText
import com.example.incidentscompose.util.IncidentDisplayHelper.formatDateForDisplay
import com.example.incidentscompose.util.IncidentDisplayHelper.getStatusColor
import com.example.incidentscompose.viewmodel.MyIncidentViewModel
import org.koin.compose.koinInject

@Composable
fun MyIncidentDetailScreen(
    navController: NavController,
    viewModel: MyIncidentViewModel = koinInject()
) {
    var incident by remember { mutableStateOf<IncidentResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedCategory by remember { mutableStateOf<IncidentCategory?>(null) }
    var editableDescription by remember { mutableStateOf("") }

    var showResolvedDialog by remember { mutableStateOf(false) }
    var showCannotDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val selectedIncidentFlow = viewModel.getSelectedIncident()
    val isBusy by viewModel.isBusy.collectAsState()
    val updateResult by viewModel.updateResult.collectAsState()
    val deleteResult by viewModel.deleteResult.collectAsState()

    LaunchedEffect(selectedIncidentFlow) {
        selectedIncidentFlow.collect { selectedIncident ->
            incident = selectedIncident
            selectedIncident?.let {
                selectedCategory = IncidentCategoryUtils.safeValueOf(it.category)
                editableDescription = it.description
            }
            isLoading = false
        }
    }

    LaunchedEffect(updateResult) {
        updateResult?.let { result ->
            if (result.isSuccess) {
                Toast.makeText(
                    context,
                    context.getString(R.string.incident_updated_successfully),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.failed_to_update_incident) + " ${result.exceptionOrNull()?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
            viewModel.resetUpdateResult()
        }
    }

    LaunchedEffect(deleteResult) {
        deleteResult?.let { result ->
            if (result.isSuccess) {
                Toast.makeText(
                    context,
                    context.getString(R.string.incident_deleted_successfully),
                    Toast.LENGTH_LONG
                ).show()
                viewModel.refreshIncidents()
                navController.popBackStack()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.failed_to_delete_incident) + " ${result.exceptionOrNull()?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
            viewModel.resetDeleteResult()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSelectedIncident()
        }
    }

    // Resolved Incident Dialog
    if (showResolvedDialog) {
        AlertDialog(
            onDismissRequest = { showResolvedDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.incident_already_resolved),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.this_incident_has_been_marked_as_resolved_and_can_no_longer_be_modified),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showResolvedDialog = false }
                ) {
                    Text(
                        text = "OK",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Cannot Delete Dialog
    if (showCannotDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showCannotDeleteDialog = false },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.delete),
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.cannot_delete_incident),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.incidents_that_are_assigned_or_resolved_cannot_be_deleted),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showCannotDeleteDialog = false }
                ) {
                    Text(
                        text = "OK",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Delete Confirmation Dialog
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
                onBackClick = { navController.popBackStack() },
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
            if (isLoading) {
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
                IncidentDetailContent(
                    incident = incident!!,
                    selectedCategory = selectedCategory,
                    editableDescription = editableDescription,
                    onCategoryChange = { selectedCategory = it },
                    onDescriptionChange = { editableDescription = it },
                    onSave = {
                        incident?.let { inc ->
                            if (inc.status.uppercase() == "RESOLVED") {
                                showResolvedDialog = true
                            } else {
                                viewModel.updateIncident(
                                    incidentId = inc.id,
                                    category = selectedCategory,
                                    description = editableDescription.takeIf { it.isNotBlank() }
                                )
                            }
                        }
                    },
                    onDelete = {
                        incident?.let { inc ->
                            val status = inc.status.uppercase()
                            if (status == "ASSIGNED" || status == "RESOLVED") {
                                showCannotDeleteDialog = true
                            } else {
                                showDeleteConfirmDialog = true
                            }
                        }
                    }
                )
            }

            LoadingOverlay(isLoading = isBusy)
        }
    }
}

@Composable
private fun IncidentDetailContent(
    incident: IncidentResponse,
    selectedCategory: IncidentCategory?,
    editableDescription: String,
    onCategoryChange: (IncidentCategory) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IncidentHeaderCard(
            incident = incident,
            selectedCategory = selectedCategory,
            onCategoryChange = onCategoryChange
        )
        IncidentDescriptionCard(
            description = editableDescription,
            onDescriptionChange = onDescriptionChange
        )
        IncidentImagesCard(incident)
        IncidentLocationCard(incident)

        ActionButtons(
            onSave = onSave,
            onDelete = onDelete
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ActionButtons(
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onSave,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0D47A1)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Text(
                text = stringResource(R.string.save_changes),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        OutlinedButton(
            onClick = onDelete,
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFD32F2F)
            ),
            border = BorderStroke(1.5.dp, Color(0xFFD32F2F)),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.delete),
                contentDescription = stringResource(R.string.delete_incident),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun IncidentHeaderCard(
    incident: IncidentResponse,
    selectedCategory: IncidentCategory?,
    onCategoryChange: (IncidentCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

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
            // CATEGORY DROPDOWN
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.category),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    letterSpacing = 0.8.sp
                )

                Box {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF9FAFB),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedCategory?.let { formatCategoryText(it.name) }
                                    ?: stringResource(R.string.select_category),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = "Expand",
                                tint = Color(0xFF6B7280)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IncidentCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(formatCategoryText(category.name)) },
                                onClick = {
                                    onCategoryChange(category)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // STATUS
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = getStatusColor(incident.status).copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = getStatusColor(incident.status),
                        letterSpacing = 0.5.sp
                    )
                }
            }

            HorizontalDivider(thickness = 1.dp, color = Color(0xFFE5E7EB))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            text = if (incident.completedAt != null) stringResource(R.string.completed) else stringResource(
                                R.string.pending
                            ),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280),
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = incident.completedAt?.let { formatDateForDisplay(it) } ?: stringResource(
                            R.string.not_completed),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF111827)
                    )
                }
            }
        }
    }
}

@Composable
private fun IncidentDescriptionCard(
    description: String,
    onDescriptionChange: (String) -> Unit
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
                BasicTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                        .padding(16.dp),
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF374151),
                        lineHeight = 24.sp
                    ),
                    cursorBrush = SolidColor(Color(0xFF0D47A1))
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
                                    contentDescription = stringResource(R.string.incident_image) + " ${index + 1}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Fallback when no valid URL
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