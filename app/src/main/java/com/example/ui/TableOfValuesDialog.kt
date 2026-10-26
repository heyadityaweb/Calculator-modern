package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.graph.GraphEquation
import com.example.graph.TableRowData
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun TableOfValuesDialog(
    equations: List<GraphEquation>,
    tableData: List<TableRowData>,
    startX: Double,
    endX: Double,
    step: Double,
    onUpdateParams: (Double, Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var startInput by remember { mutableStateOf(startX.toString()) }
    var endInput by remember { mutableStateOf(endX.toString()) }
    var stepInput by remember { mutableStateOf(step.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .testTag("table_of_values_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Table of Values",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Evaluate functions over interval",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_table_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Table",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Parameter Controls (Start, End, Step)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = startInput,
                        onValueChange = { startInput = it },
                        label = { Text("Start") },
                        modifier = Modifier.weight(1f).testTag("table_start_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endInput,
                        onValueChange = { endInput = it },
                        label = { Text("End") },
                        modifier = Modifier.weight(1f).testTag("table_end_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = stepInput,
                        onValueChange = { stepInput = it },
                        label = { Text("Step") },
                        modifier = Modifier.weight(1f).testTag("table_step_input"),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            val s = startInput.toDoubleOrNull() ?: startX
                            val e = endInput.toDoubleOrNull() ?: endX
                            val st = stepInput.toDoubleOrNull() ?: step
                            onUpdateParams(s, e, st)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                            .testTag("refresh_table_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Update Table",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Table Header
                val visibleEqs = equations.filter { it.isVisible && it.expression.isNotBlank() }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "x",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    visibleEqs.forEach { eq ->
                        Text(
                            text = eq.label,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = eq.color,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Table Rows
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                ) {
                    items(tableData) { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = row.x.toString(),
                                fontFamily = JetBrainsMonoFontFamily,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            visibleEqs.forEach { eq ->
                                val valY = row.values[eq.label]
                                Text(
                                    text = valY?.toString() ?: "undef",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = if (valY != null) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFF43F5E),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    }
                }
            }
        }
    }
}
