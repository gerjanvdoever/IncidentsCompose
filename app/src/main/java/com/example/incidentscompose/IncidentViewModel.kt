package com.example.incidentscompose

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime

class IncidentsViewModel : ViewModel() {

    // The _incidents property is private mutable state flow, and the property incidents is
    // exposed as a public immutable StateFlow, to be called upon by the screen.
    private val _incidents = MutableStateFlow<List<Incident>>(emptyList())
    val incidents: StateFlow<List<Incident>> = _incidents.asStateFlow()

    init {
        loadIncidents()
    }

    fun removeIncident(id: Int) {
        _incidents.update { currentList ->
            currentList.filter { it.id != id }
        }
    }

    fun updateIncidentStatus(id: Int, newStatus: String) {
        _incidents.update { currentList ->
            currentList.map { incident ->
                if (incident.id == id) {
                    incident.copy(status = newStatus)
                } else {
                    incident
                }
            }
        }
    }

    private fun loadIncidents() {
        _incidents.update { currentList ->
            currentList + listOf(
                Incident(
                    id = 1,
                    category = "Vandalisme",
                    description = "Zijruit van bushalte ingegooid",
                    priority = Priority.HIGH,
                    status = "In Progress",
                    reportedAt = LocalDateTime.now().minusHours(2)
                ),
                Incident(
                    id = 2,
                    category = "Afval",
                    description = "Vuilniszakken op straat gedumpt",
                    priority = Priority.MEDIUM,
                    status = "Reported",
                    reportedAt = LocalDateTime.now().minusHours(5)
                ),
                Incident(
                    id = 3,
                    category = "Gladheid",
                    description = "IJsplaat op fietspad Hoofdstraat",
                    priority = Priority.HIGH,
                    status = "Reported",
                    reportedAt = LocalDateTime.now().minusMinutes(45)
                ),
                Incident(
                    id = 4,
                    category = "Verlichting",
                    description = "Straatlantaarn defect bij park",
                    priority = Priority.LOW,
                    status = "Resolved",
                    reportedAt = LocalDateTime.now().minusDays(1)
                ),
                Incident(
                    id = 5,
                    category = "Geluidsoverlast",
                    description = "Bouwwerkzaamheden na 20:00",
                    priority = Priority.MEDIUM,
                    status = "In Progress",
                    reportedAt = LocalDateTime.now().minusHours(8)
                ),
                Incident(
                    id = 6,
                    category = "Graffiti",
                    description = "Tags op gemeentelijk gebouw",
                    priority = Priority.LOW,
                    status = "Reported",
                    reportedAt = LocalDateTime.now().minusDays(2)
                )
            )
        }
    }
}
