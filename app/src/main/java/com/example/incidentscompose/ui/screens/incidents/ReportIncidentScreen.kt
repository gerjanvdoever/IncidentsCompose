package com.example.incidentscompose.ui.screens.incidents

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.incidentscompose.R
import com.example.incidentscompose.data.model.IncidentCategory
import com.example.incidentscompose.navigation.Destinations
import com.example.incidentscompose.ui.components.LoadingOverlay
import com.example.incidentscompose.ui.components.TopNavBar
import com.example.incidentscompose.util.PhotoUtils
import com.example.incidentscompose.viewmodel.ReportIncidentViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.core.net.toUri

@Composable
fun ReportIncidentScreen(
    navController: NavController,
    viewModel: ReportIncidentViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isBusy.collectAsState()
    val context = LocalContext.current

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allRequiredGranted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val hasCamera = permissions[Manifest.permission.CAMERA] == true
            val hasFullImageAccess = permissions[Manifest.permission.READ_MEDIA_IMAGES] == true
            val hasSelectedAccess = permissions[Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] == true

            hasCamera && (hasFullImageAccess || hasSelectedAccess)
        } else {
            permissions.values.all { it }
        }

        viewModel.updatePermissions(allRequiredGranted)
    }

    // Request permissions on first composition
    LaunchedEffect(Unit) {
        val hasPermissions = PhotoUtils.hasAllPermissions(context)
        viewModel.updatePermissions(hasPermissions)

        if (!hasPermissions) {
            permissionLauncher.launch(PhotoUtils.getRequiredPermissions())
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.addPhoto(it.toString()) }
    }

    var currentCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentCameraUri?.let { viewModel.addPhoto(it.toString()) }
        }
        currentCameraUri = null
    }

    LaunchedEffect(uiState.showImageSourceDialog) {
        if (uiState.showImageSourceDialog) {
            if (uiState.hasPermissions) {
                // Dialog will be shown — wait for user selection
            } else {
                permissionLauncher.launch(PhotoUtils.getRequiredPermissions())
                viewModel.dismissImageSourceDialog()
            }
        }
    }

    if (uiState.showImageSourceDialog) {
        ImageSourceDialog(
            onDismiss = { viewModel.dismissImageSourceDialog() },
            onCameraClick = {
                viewModel.dismissImageSourceDialog()
                if (uiState.hasPermissions) {
                    val imageUri = PhotoUtils.createImageUri(context)
                    currentCameraUri = imageUri // Store the URI
                    cameraLauncher.launch(imageUri)
                } else {
                    permissionLauncher.launch(PhotoUtils.getRequiredPermissions())
                }
            },
            onGalleryClick = {
                viewModel.dismissImageSourceDialog()
                if (uiState.hasPermissions) {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                } else {
                    permissionLauncher.launch(PhotoUtils.getRequiredPermissions())
                }
            }
        )
    }

    if (uiState.showPermissionDeniedWarning) {
        PermissionDeniedDialog(
            onDismiss = { viewModel.dismissPermissionWarning() }
        )
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopNavBar(
                    title = stringResource(R.string.report_incident),
                    showBackButton = true,
                    onBackClick = { navController.popBackStack() },
                )
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
                    photos = uiState.photos.map { it.toUri() },
                    onAddPhoto = {
                        if (uiState.hasPermissions) {
                            viewModel.showImageSourceDialog()
                        } else {
                            permissionLauncher.launch(PhotoUtils.getRequiredPermissions())
                        }
                    },
                    onRemovePhoto = { viewModel.removePhoto(it.toString()) }
                )

                MapLocationCard(
                    latitude = uiState.latitude,
                    longitude = uiState.longitude,
                    onUseCurrentLocation = { viewModel.useCurrentLocation() }
                )

                if (!uiState.errorMessage.isNullOrBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .shadow(4.dp, RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = BorderStroke(1.dp, Color(0xFFFECACA))
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            color = Color(0xFFDC2626),
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.submitReport(context)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626),
                        contentColor = Color.White
                    ),
                    enabled = !isLoading
                ) {
                    Text(
                        stringResource(R.string.submit_report),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        LoadingOverlay(isLoading = isLoading)
    }
}

@Composable
fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Photo",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Button(
                    onClick = onCameraClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0969DA),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "Take Photo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onGalleryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0969DA),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "Choose from Gallery",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun PermissionDeniedDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Warning",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFF59E0B)
                )

                Text(
                    text = "Permission Required",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "To add photos, please grant camera and storage permissions in your device settings.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0969DA),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "OK",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
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
                    text = stringResource(R.string.where_did_you_observe_this),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.tap_on_the_map_to_mark_the_exact_location),
                    fontSize = 13.sp,
                    color = Color(0xFF656D76)
                )
            }

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
                                text = stringResource(R.string.selected_location),
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .border(1.dp, Color(0xFFD0D7DE), RoundedCornerShape(12.dp))
                    .background(Color(0xFFF6F8FA), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.map_goes_here),
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
                    stringResource(R.string.use_sample_location),
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
                    text = stringResource(R.string.suspicious_activity_or_emergency),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF92400E)
                )
                Text(
                    text = stringResource(R.string.for_immediate_danger_call_emergency_services),
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
                text = stringResource(R.string.what_type_of_incident_is_this),
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
                    IncidentCategory.CRIME -> stringResource(R.string.illegal_activities_and_safety_threats)
                    IncidentCategory.ENVIRONMENT -> stringResource(R.string.nature_pollution_and_conservation_issues)
                    IncidentCategory.COMMUNAL -> stringResource(R.string.shared_spaces_and_neighborhood_quality_of_life)
                    IncidentCategory.TRAFFIC -> stringResource(R.string.roads_vehicles_and_transportation_safety)
                    IncidentCategory.OTHER -> stringResource(R.string.any_issue_that_doesn_t_fit_the_other_categories)
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
    photos: List<Uri>,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (Uri) -> Unit
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
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Success",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF10B981)
                )

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