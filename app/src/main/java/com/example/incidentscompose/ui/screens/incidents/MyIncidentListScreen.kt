package com.example.incidentscompose.ui.screens.incidents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.incidentscompose.navigation.Destinations
import com.example.incidentscompose.ui.components.LoadingOverlay
import com.example.incidentscompose.viewmodel.MyIncidentViewModel
import org.koin.compose.koinInject

@Composable
fun MyIncidentListScreen(
    navController: NavController,
    viewModel: MyIncidentViewModel = koinInject()
) {
    val user by viewModel.user.collectAsState()
    val incidents by viewModel.incidents.collectAsState()
    val logoutEvent by viewModel.logoutEvent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var isDropdownVisible by remember { mutableStateOf(false) }

    LaunchedEffect(logoutEvent) {
        if (logoutEvent) {
            navController.navigate(Destinations.Login.route) {
                popUpTo(Destinations.MyIncidentList.route) { inclusive = true }
            }
            viewModel.resetLogoutEvent()
        }
    }

    val fullName = user?.username ?: "Loading..."
    val totalIncidents = incidents.size
    val activeIncidents = incidents.count { it.status.lowercase() == "active" || it.status.lowercase() == "assigned" }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0D47A1),
                                Color(0xFF1976D2)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.2f)
                            )
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Avatar",
                            tint = Color(0xFF0D47A1),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 15.dp)
                    ) {
                        Text(
                            text = fullName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Account Dashboard",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { isDropdownVisible = !isDropdownVisible },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Incidents",
                    value = totalIncidents.toString(),
                    valueColor = Color(0xFF0D47A1)
                )

                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Active",
                    value = activeIncidents.toString(),
                    valueColor = Color(0xFFFF6B35)
                )
            }

            Text(
                text = "My incidents",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 0.dp)
            )

            if (incidents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No incidents found.",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 15.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    items(incidents) { incident ->
                        IncidentCard(
                            incident = incident,
                            onClick = {
                                navController.navigate("incidentDetail/${incident.id}")
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate(Destinations.ReportIncident.route) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(25.dp),
            containerColor = Color(0xFF0D47A1),
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Incident",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        if (isDropdownVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isDropdownVisible = false }
            )
        }

        AnimatedVisibility(
            visible = isDropdownVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -20 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -20 }),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 20.dp)
        ) {
            Surface(
                modifier = Modifier
                    .width(200.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Column {
                    DropdownMenuItem(
                        text = { Text("Details", color = Color(0xFF1976D2)) },
                        onClick = { isDropdownVisible = false },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    DropdownMenuItem(
                        text = { Text("Logout", color = Color(0xFFD32F2F)) },
                        onClick = {
                            isDropdownVisible = false
                            viewModel.logout()
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        LoadingOverlay(isLoading = isLoading)
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(15.dp)
            ),
        shape = RoundedCornerShape(15.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun IncidentCard(
    incident: com.example.incidentscompose.data.model.IncidentResponse,
    onClick: () -> Unit
) {
    val shortDescription = if (incident.description.length > 60) {
        incident.description.take(60) + "..."
    } else {
        incident.description
    }

    val formattedDate = formatDateForDisplay(incident.createdAt)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(15.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp, 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(getPriorityColor(incident.priority))
            )

            Spacer(modifier = Modifier.width(15.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = incident.category,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = shortDescription,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    maxLines = 2,
                    lineHeight = 16.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = getStatusColor(incident.status).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = incident.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = getStatusColor(incident.status),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = incident.priority,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = getPriorityColor(incident.priority)
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 12.sp,
                            color = Color(0xFF888888)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "›",
                fontSize = 24.sp,
                color = Color(0xFFCCCCCC)
            )
        }
    }
}

fun getStatusColor(status: String): Color {
    return when (status.uppercase()) {
        "ACTIVE", "ASSIGNED" -> Color(0xFFFF6B35)
        "RESOLVED", "COMPLETED" -> Color(0xFF4CAF50)
        "PENDING" -> Color(0xFFFFC107)
        "NORMAL" -> Color(0xFF2196F3)
        "HIGH" -> Color(0xFFF44336)
        else -> Color.Gray
    }
}

fun getPriorityColor(priority: String): Color {
    return when (priority.uppercase()) {
        "HIGH" -> Color(0xFFF44336)
        "NORMAL" -> Color(0xFF2196F3)
        "LOW" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
}

fun formatDateForDisplay(dateString: String): String {
    return try {
        if (dateString.contains("T")) {
            dateString.split("T").first()
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}