package com.example.incidentscompose.ui.components

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.incidentscompose.data.model.IncidentResponse
import org.maplibre.compose.map.MaplibreMap
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.incidentscompose.data.model.Priority
import com.example.incidentscompose.data.model.Status
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

@Composable
fun IncidentMap(
    modifier: Modifier = Modifier,
    incidents: List<IncidentResponse>,
    isLocationSelectionEnabled: Boolean = false,
    allowDetailNavigation: Boolean = false,
    onIncidentClick: (IncidentResponse) -> Unit = {},
    onLocationSelected: (Double, Double) -> Unit = { _, _ -> },
    userLocation: Pair<Double, Double>? = null
) {
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    var selectedIncident by remember { mutableStateOf<IncidentResponse?>(null) }
    var selectedLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    val camera = rememberCameraState(
        firstPosition = calculateInitialCamera(incidents, userLocation)
    )

    Box(modifier = modifier) {
        if (!hasLocationPermission && userLocation != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Location permission is needed to show your current location",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(onClick = {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }) {
                    Text("Grant Permission")
                }
            }
        } else {
            MaplibreMap(
                modifier = modifier.fillMaxSize(),
                baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
                cameraState = camera,
                options = MapOptions(
                    gestureOptions = GestureOptions(
                        isTiltEnabled = true,
                        isZoomEnabled = true,
                        isRotateEnabled = true,
                        isScrollEnabled = true,)
                )
            )
        }
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
                zoom = 15.0
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





