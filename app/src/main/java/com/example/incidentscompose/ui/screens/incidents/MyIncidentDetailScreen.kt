package com.example.incidentscompose.ui.screens.incidents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.ui.components.LoadingOverlay
import com.example.incidentscompose.ui.components.TopNavBar
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

    val selectedIncidentFlow = viewModel.getSelectedIncident()

    LaunchedEffect(selectedIncidentFlow) {
        selectedIncidentFlow.collect { selectedIncident ->
            incident = selectedIncident
            isLoading = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSelectedIncident()
        }
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = "Incident Details",
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
                        text = "Incident details not available",
                        color = Color(0xFF6B7280),
                        fontSize = 16.sp
                    )
                }
            } else {
                IncidentDetailContent(incident = incident!!, navController = navController)
            }
        }
    }
}

@Composable
private fun IncidentDetailContent(incident: IncidentResponse, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IncidentHeaderCard(incident)
        IncidentDescriptionCard(incident)
        IncidentImagesCard(incident)
        IncidentLocationCard(incident)

        // Action buttons placed naturally at the bottom of content
        ActionButtons(navController = navController, incident = incident)

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ActionButtons(navController: NavController, incident: IncidentResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = {
                // TODO: Add save logic
            },
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
                text = "Save Changes",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        OutlinedButton(
            onClick = {
                // TODO: Add delete logic
            },
            modifier = Modifier
                .size(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFD32F2F)
            ),
            border = BorderStroke(1.5.dp, Color(0xFFD32F2F))
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete incident",
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// The rest of your composable functions remain the same...
@Composable
private fun IncidentHeaderCard(incident: IncidentResponse) {
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
            // Category
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "CATEGORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = formatCategoryText(incident.category),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    lineHeight = 32.sp
                )
            }

            // Status Badge
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
                            text = if (incident.completedAt != null) "COMPLETED" else "PENDING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280),
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = incident.completedAt?.let { formatDateForDisplay(it) } ?: "Not completed",
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
private fun IncidentDescriptionCard(incident: IncidentResponse) {
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
                text = "DESCRIPTION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280),
                letterSpacing = 0.8.sp
            )

            Text(
                text = incident.description,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF374151),
                lineHeight = 24.sp
            )
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
                    text = "PHOTOS",
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
                        val imageUrl = incident.images[index]
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.size(140.dp),
                            color = Color(0xFFF3F4F6)
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Incident image ${index + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
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
                        text = "No photos available",
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
                    text = "LOCATION",
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