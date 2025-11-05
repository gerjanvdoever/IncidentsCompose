package com.example.incidentscompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.incidentscompose.ui.theme.IncidentsComposeTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class Priority(val label: String, val deadlineDays: Long) {
    LOW("Low", 7),
    MEDIUM("Medium", 3),
    HIGH("High", 1)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IncidentsComposeTheme {
                var note by remember { mutableStateOf("") }
                var priority by remember { mutableStateOf(Priority.MEDIUM) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    IncidentScreen(
                        category = "Vandalisme",
                        description = "Zijruit van bushalte ingegooid",
                        status = "Reported",
                        priority = priority,
                        onPriorityChange = { priority = it },
                        note = note,
                        onNoteChange = { note = it },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// State (note, priority) is passed down as parameters
// Events (onNoteChange, onPriorityChange) are passed up as callbacks
@Composable
fun IncidentScreen(
    category: String,
    description: String,
    status: String,
    priority: Priority,
    onPriorityChange: (Priority) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // We're "subscribing" to priority, changing deadline each time priority updates.
    val deadline = remember(priority) {
        val date = LocalDate.now().plusDays(priority.deadlineDays)
        date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Incident Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )

                IncidentInfoRow(label = "Category", value = category)
                Spacer(modifier = Modifier.height(16.dp))

                IncidentInfoRow(label = "Description", value = description)
                Spacer(modifier = Modifier.height(16.dp))

                IncidentInfoRow(label = "Status", value = status)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Priority.entries.forEachIndexed { index, p ->
                        SegmentedButton(
                            selected = p == priority,
                            onClick = { onPriorityChange(p) },
                            shape = SegmentedButtonDefaults.itemShape(index, Priority.entries.size)
                        ) {
                            Text(p.label)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                ) {
                    IncidentInfoRow(
                        label = "Deadline",
                        value = deadline,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = note,
                    onValueChange = onNoteChange,
                    placeholder = { Text("Add notes...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Composable
fun IncidentInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview
@Composable
fun IncidentScreenPreview() {
    IncidentsComposeTheme {
        var note by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf(Priority.MEDIUM) }

        IncidentScreen(
            category = "Vandalisme",
            description = "Zijruit van bushalte ingegooid",
            status = "Reported",
            priority = priority,
            onPriorityChange = { priority = it },
            note = note,
            onNoteChange = { note = it }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun IncidentScreenFullPreview() {
    IncidentsComposeTheme {
        var note by remember { mutableStateOf("Vuilniswagen rijdt er morgen langs") }
        var priority by remember { mutableStateOf(Priority.LOW) }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            IncidentScreen(
                category = "Afval",
                description = "Vuilniszakken achter de jumbo, stinkt als een malle",
                status = "Reported",
                priority = priority,
                onPriorityChange = { priority = it },
                note = note,
                onNoteChange = { note = it },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}