package com.example.incidentscompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.incidentscompose.ui.theme.IncidentsComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IncidentsComposeTheme {
                var note by remember { mutableStateOf("") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    IncidentScreen(
                        category = "Vandalisme",
                        description = "Zijruit van bushalte ingegooid",
                        status = "Reported",
                        priority = "High",
                        note = note,
                        onNoteChange = { note = it },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun IncidentScreen(
    category: String,
    description: String,
    status: String,
    priority: String,
    note: String,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.incident_details),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(20.dp))

                IncidentInfoRow(label = stringResource(R.string.category), value = category)
                Spacer(modifier = Modifier.height(12.dp))

                IncidentInfoRow(label = stringResource(R.string.description), value = description)
                Spacer(modifier = Modifier.height(12.dp))

                IncidentInfoRow(label = stringResource(R.string.status), value = status)
                Spacer(modifier = Modifier.height(12.dp))

                IncidentInfoRow(label = stringResource(R.string.priority), value = priority)
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.notes),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextField(
                    value = note,
                    onValueChange = onNoteChange,
                    placeholder = { Text(stringResource(R.string.add_notes)) },
                    modifier = Modifier.fillMaxWidth()
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
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
fun IncidentScreenPreview() {
    IncidentsComposeTheme {
        var note by remember { mutableStateOf("") }

        IncidentScreen(
            category = "Vandalisme",
            description = "Zijruit van bushalte ingegooid",
            status = "Reported",
            priority = "High",
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

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            IncidentScreen(
                category = "Afval",
                description = "Vuilniszakken achter de jumbo, stinkt als een malle",
                status = "Reported",
                priority = "Medium",
                note = note,
                onNoteChange = { note = it },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

