package com.example.incidentscompose.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.util.LocationPermissionHandler
import com.example.incidentscompose.util.rememberPermissionLauncher
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.ClickResult
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import kotlin.collections.listOf
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.spatialk.geojson.Feature.Companion.getStringProperty

@Composable
fun IncidentMap(
    modifier: Modifier = Modifier,
    incidents: List<IncidentResponse>,
    isLocationSelectionEnabled: Boolean = false,
    allowDetailNavigation: Boolean = false,
    onIncidentClick: (IncidentResponse) -> Unit = {},
    onLocationSelected: (Double, Double) -> Unit = { _, _ -> },
    userLocation: Pair<Double, Double>? = null,
    onMapTouch: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current

    // Track permission state - now required to show map at all
    var hasLocationPermission by remember { mutableStateOf(false) }

    val locationPermissionHandler = remember {
        LocationPermissionHandler(
            context = context,
            onPermissionResult = { granted ->
                hasLocationPermission = granted
            },
            onLocationFetched = { _, _ -> },
            onError = { _ -> }
        )
    }

    val permissionLauncher = rememberPermissionLauncher { isGranted ->
        hasLocationPermission = isGranted
    }

    // Initialize permission state
    LaunchedEffect(Unit) {
        hasLocationPermission = locationPermissionHandler.hasPermission()
    }

    var selectedIncident by remember { mutableStateOf<IncidentResponse?>(null) }
    var clickedIncident by remember { mutableStateOf<IncidentResponse?>(null) }
    var selectedLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    val camera = rememberCameraState(
        firstPosition = calculateInitialCamera(incidents, userLocation)
    )

    Box(
        modifier = modifier
    ) {
        // CHANGED: Only show map when we have location permission
        if (hasLocationPermission) {
            MaplibreMap(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                when {
                                    event.changes.any { it.pressed } -> onMapTouch(true)   // touching map -> disable parent scroll
                                    event.changes.all { !it.pressed } -> onMapTouch(false) // released -> enable parent scroll
                                }
                            }
                        }
                    },
                baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
                cameraState = camera,
                options = MapOptions(
                    gestureOptions = GestureOptions(
                        isTiltEnabled = true,
                        isZoomEnabled = true,
                        isRotateEnabled = true,
                        isScrollEnabled = true
                    )
                ),
                onMapClick = { position, _ ->
                    if (isLocationSelectionEnabled) {
                        selectedLocation = position.latitude to position.longitude
                        onLocationSelected(position.latitude, position.longitude)
                        ClickResult.Consume
                    } else {
                        selectedIncident = null
                        clickedIncident = null
                        ClickResult.Pass
                    }
                }
            ) {
                // incidents source
                val incidentsSource = rememberGeoJsonSource(
                    GeoJsonData.Features(
                        createIncidentsGeoJson(incidents).takeIf { it.features.isNotEmpty() }
                            ?: FeatureCollection(features = listOf(
                                Feature(
                                    geometry = Point(Position(0.0, 0.0)),
                                    properties = buildJsonObject { }
                                )
                            ))
                    )
                )

                // selected location source
                val selectedLocationSource = selectedLocation?.let { location ->
                    val feature = Feature(
                        geometry = Point(Position(location.second, location.first)),
                        properties = buildJsonObject {} // empty JSON instead of null
                    )
                    val featureCollection = FeatureCollection(features = listOf(feature))
                    rememberGeoJsonSource(GeoJsonData.Features(featureCollection))
                }

                // user location source - now always show since we have permission
                val userLocationSource = userLocation?.let { location ->
                    val feature = Feature(
                        geometry = Point(Position(location.second, location.first)),
                        properties = buildJsonObject { put("type", "user-location") }
                    )
                    val featureCollection = FeatureCollection(features = listOf(feature))
                    rememberGeoJsonSource(GeoJsonData.Features(featureCollection))
                }

                // incidents layer
                CircleLayer(
                    id = "incidents-outer",
                    source = incidentsSource,
                    radius = const(10.dp),
                    color = const(Color.Red),
                    onClick = { features ->
                        val feature = features.firstOrNull()
                        val idString = feature?.getStringProperty("id")
                        val id = idString?.toLongOrNull()

                        if (id != null) {
                            incidents.find { it.id == id }?.let { incident ->
                                if (clickedIncident?.id == incident.id && allowDetailNavigation) {
                                    onIncidentClick(incident)
                                    clickedIncident = null
                                    selectedIncident = null
                                } else {
                                    selectedIncident = incident
                                    clickedIncident = incident
                                }
                            }
                        }
                        ClickResult.Consume
                    }
                )

                CircleLayer(
                    id = "incidents-inner",
                    source = incidentsSource,
                    radius = const(4.dp),
                    color = const(Color.White)
                )

                // Selected Location Layer
                selectedLocationSource?.let { source ->
                    CircleLayer(
                        id = "selected-location-outer",
                        source = source,
                        radius = const(12.dp),
                        color = const(Color(0xFF2196F3))
                    )
                    CircleLayer(
                        id = "selected-location-inner",
                        source = source,
                        radius = const(6.dp),
                        color = const(Color.White)
                    )
                }

                // user location layer - now always show since we have permission
                userLocationSource?.let { source ->
                    CircleLayer(
                        id = "user-location-outer",
                        source = source,
                        radius = const(10.dp),
                        color = const(Color(0xFF4CAF50)),
                        onClick = {
                            userLocation?.let { location ->
                                selectedLocation = location
                                onLocationSelected(location.first, location.second)
                            }
                            ClickResult.Consume
                        }
                    )
                    CircleLayer(
                        id = "user-location-inner",
                        source = source,
                        radius = const(4.dp),
                        color = const(Color.White)
                    )
                }
            }
        } else {
            // Show permission request UI when we don't have permission
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Location permission is required to show the map",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(onClick = {
                    permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                }) {
                    Text("Grant Permission")
                }
            }
        }

        selectedIncident?.let { incident ->
            IncidentInfoCard(
                incident = incident,
                allowDetailNavigation = allowDetailNavigation,
                onClose = {
                    selectedIncident = null
                    clickedIncident = null
                }
            )
        }
    }
}

// ---------------------- INCIDENT CARD & CHIP HELPERS ----------------------

@Composable
fun IncidentInfoCard(
    incident: IncidentResponse,
    allowDetailNavigation: Boolean,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatCategory(incident.category),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onClose) {
                        Text("✕", style = MaterialTheme.typography.titleLarge)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusChip(status = incident.status)
                    PriorityChip(priority = incident.priority)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Due: ${formatDate(incident.dueAt)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                if (allowDetailNavigation) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap again to view details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (backgroundColor, textColor) = when (status.uppercase()) {
        "REPORTED" -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        "ASSIGNED" -> Color(0xFFFFF3E0) to Color(0xFFF57C00)
        "RESOLVED" -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
        else -> Color(0xFFF5F5F5) to Color.Gray
    }

    Surface(shape = RoundedCornerShape(16.dp), color = backgroundColor) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PriorityChip(priority: String) {
    val (backgroundColor, textColor) = when (priority.uppercase()) {
        "LOW" -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
        "NORMAL" -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        "HIGH" -> Color(0xFFFFF3E0) to Color(0xFFF57C00)
        "CRITICAL" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        else -> Color(0xFFF5F5F5) to Color.Gray
    }

    Surface(shape = RoundedCornerShape(16.dp), color = backgroundColor) {
        Text(
            text = priority.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---------------------- UTILITY FUNCTIONS ----------------------

private fun createIncidentsGeoJson(incidents: List<IncidentResponse>): FeatureCollection<Point, JsonObject?> {
    val features = incidents.mapNotNull { incident ->
        try {
            Feature(
                geometry = Point(Position(incident.longitude, incident.latitude)),
                properties = buildJsonObject {
                    put("id", incident.id.toString())
                    put("category", incident.category)
                    put("priority", incident.priority)
                    put("status", incident.status)
                    put("dueAt", incident.dueAt)
                },
                id = JsonPrimitive(incident.id.toString())
            )
        } catch (_: Exception) {
            null
        }
    }
    return FeatureCollection(features = features)
}

private fun formatCategory(category: String): String {
    return category.lowercase().replaceFirstChar { it.uppercase() }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (_: Exception) {
        dateString
    }
}

private fun calculateInitialCamera(
    incidents: List<IncidentResponse>,
    userLocation: Pair<Double, Double>?
): CameraPosition {
    return when {
        incidents.isEmpty() && userLocation != null -> {
            CameraPosition(
                target = Position(
                    latitude = userLocation.first,
                    longitude = userLocation.second
                ),
                zoom = 15.0
            )
        }
        incidents.isEmpty() -> {
            CameraPosition(
                target = Position(latitude = 52.0, longitude = 5.0),
                zoom = 7.0
            )
        }
        else -> {
            val latitudes = incidents.map { it.latitude }
            val longitudes = incidents.map { it.longitude }

            val minLat = latitudes.minOrNull() ?: 52.0
            val maxLat = latitudes.maxOrNull() ?: 52.0
            val minLon = longitudes.minOrNull() ?: 5.0
            val maxLon = longitudes.maxOrNull() ?: 5.0

            val finalMinLat = userLocation?.let { min(minLat, it.first) } ?: minLat
            val finalMaxLat = userLocation?.let { max(maxLat, it.first) } ?: maxLat
            val finalMinLon = userLocation?.let { min(minLon, it.second) } ?: minLon
            val finalMaxLon = userLocation?.let { max(maxLon, it.second) } ?: maxLon

            val centerLat = (finalMinLat + finalMaxLat) / 2
            val centerLon = (finalMinLon + finalMaxLon) / 2

            val latSpan = finalMaxLat - finalMinLat
            val lonSpan = finalMaxLon - finalMinLon
            val maxSpan = max(latSpan, lonSpan)

            val zoom = when {
                maxSpan > 10 -> 5.0
                maxSpan > 5 -> 7.0
                maxSpan > 1 -> 9.0
                maxSpan > 0.5 -> 11.0
                maxSpan > 0.1 -> 13.0
                else -> 15.0
            }

            CameraPosition(
                target = Position(latitude = centerLat, longitude = centerLon),
                zoom = zoom
            )
        }
    }
}