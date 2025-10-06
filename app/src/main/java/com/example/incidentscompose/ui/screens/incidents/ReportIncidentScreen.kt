package com.example.incidentscompose.ui.screens.incidents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.incidentscompose.data.model.IncidentCategory
import com.example.incidentscompose.navigation.Destinations
import com.example.incidentscompose.viewmodel.ReportIncidentViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReportIncidentScreen(
    navController: NavController,
    viewModel: ReportIncidentViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showSuccessDialog) {
        ReportSuccessDialog(
            onDismiss = { viewModel.dismissSuccessDialog() },
            onContinue = {
                viewModel.dismissSuccessDialog()
                if (uiState.createdIncident?.reportedBy != null) {
                    navController.navigate(Destinations.MyIncidentList.route) {
                        popUpTo(Destinations.ReportIncident.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Destinations.Login.route) {
                        popUpTo(Destinations.ReportIncident.route) { inclusive = true }
                    }
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    uiState.errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = Color(0xFFDC2626),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.submitReport() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 2.dp
                        ),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Submit Report",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            WarningBanner()

            CategorySelectionCard(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.updateCategory(it) }
            )

            DescriptionInputCard(
                description = uiState.description,
                onDescriptionChange = { viewModel.updateDescription(it) }
            )

            PhotoUploadCard(
                photos = uiState.photos,
                onAddPhoto = { viewModel.addPhoto() },
                onRemovePhoto = { viewModel.removePhoto(it) }
            )

            MapLocationCard(
                latitude = uiState.latitude,
                longitude = uiState.longitude,
                onUseCurrentLocation = { viewModel.useCurrentLocation() }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MapLocationCard(
    latitude: Double?,
    longitude: Double?,
    onUseCurrentLocation: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp, 16.dp, 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, Color(0xFFD0D7DE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp, 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Where did you observe this?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap on the map to mark the exact location",
                    fontSize = 13.sp,
                    color = Color(0xFF656D76)
                )
            }

            // Show selected coordinates if available
            if (latitude != null && longitude != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF0F9FF)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFBAE6FD))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Selected Location",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0369A1)
                            )
                            Text(
                                text = "Lat: ${"%.6f".format(latitude)}, Lng: ${"%.6f".format(longitude)}",
                                fontSize = 12.sp,
                                color = Color(0xFF0C4A6E)
                            )
                        }
                    }
                }
            }

            // Placeholder for map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .border(1.dp, Color(0xFFD0D7DE), RoundedCornerShape(12.dp))
                    .background(Color(0xFFF6F8FA), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Map goes here",
                    fontSize = 16.sp,
                    color = Color(0xFF656D76)
                )
            }

            Button(
                onClick = onUseCurrentLocation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDDF4FF),
                    contentColor = Color(0xFF0969DA)
                ),
                border = BorderStroke(1.dp, Color(0xFF54AEFF)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Use Sample Location",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun WarningBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 16.dp, 16.dp, 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF8DC)
        ),
        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp, 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Suspicious Activity or Emergency?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF92400E)
                )
                Text(
                    text = "For immediate danger, call emergency services!",
                    fontSize = 13.sp,
                    color = Color(0xFFA16207)
                )
            }
        }
    }
}

@Composable
fun CategorySelectionCard(
    selectedCategory: IncidentCategory,
    onCategorySelected: (IncidentCategory) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, Color(0xFFD0D7DE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp, 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "What type of incident is this?",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IncidentCategory.entries.forEach { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelected(category) },
                        label = {
                            Text(
                                text = category.name.lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Text(
                text = when (selectedCategory) {
                    IncidentCategory.CRIME -> "Illegal activities and safety threats"
                    IncidentCategory.ENVIRONMENT -> "Nature, pollution and conservation issues"
                    IncidentCategory.COMMUNAL -> "Shared spaces and neighborhood quality of life"
                    IncidentCategory.TRAFFIC -> "Roads, vehicles and transportation safety"
                    IncidentCategory.OTHER -> "Any issue that doesn't fit the other categories"
                },
                fontSize = 13.sp,
                color = Color(0xFF656D76)
            )
        }
    }
}


@Composable
fun DescriptionInputCard(
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, Color(0xFFD0D7DE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp, 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Provide a short but detailed description",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                placeholder = { Text("What exactly did you observe?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF6F8FA),
                    focusedContainerColor = Color(0xFFF6F8FA),
                    unfocusedBorderColor = Color(0xFFD0D7DE),
                    focusedBorderColor = Color(0xFF0969DA)
                ),
                minLines = 4
            )
        }
    }
}

@Composable
fun PhotoUploadCard(
    photos: List<String>,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, Color(0xFFD0D7DE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp, 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Can you please add some photos?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Visual evidence helps us respond more effectively",
                    fontSize = 13.sp,
                    color = Color(0xFF656D76)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Add photo button
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .border(
                            width = 2.dp,
                            color = Color(0xFFD0D7DE),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(Color(0xFFF6F8FA), RoundedCornerShape(12.dp))
                        .clickable { onAddPhoto() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF0969DA), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "Add Photo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF656D76)
                        )
                    }
                }

                // Photo list
                photos.forEach { photoUri ->
                    Box(modifier = Modifier.size(120.dp)) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Photo",
                            modifier = Modifier
                                .size(120.dp)
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFD0D7DE), RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // Delete button
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.TopEnd)
                                .offset((-8).dp, (-8).dp)
                                .shadow(4.dp, RoundedCornerShape(12.dp))
                                .background(Color(0xFFDC2626), RoundedCornerShape(12.dp))
                                .clickable { onRemovePhoto(photoUri) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportSuccessDialog(
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Success Icon
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Success",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF10B981)
                )

                // Title and Message
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Thank You!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Your incident report has been successfully submitted. Our team will review it shortly.",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }

                // Continue Button
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Text(
                        "Continue",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}