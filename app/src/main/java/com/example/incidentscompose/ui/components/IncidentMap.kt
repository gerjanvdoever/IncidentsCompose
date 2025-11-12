package com.example.incidentscompose.ui.components

import androidx.compose.runtime.Composable
import com.example.incidentscompose.data.model.IncidentResponse
import org.maplibre.compose.map.MaplibreMap

@Composable
fun IncidentMap(
    incidents: List<IncidentResponse>,
    isLocationSelectionEnabled: Boolean = false,
    allowDetailNavigation: Boolean = false,
    onIncidentClick: (IncidentResponse) -> Unit){
    MaplibreMap()
}