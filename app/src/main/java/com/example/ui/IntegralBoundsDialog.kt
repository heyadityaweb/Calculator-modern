package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.graph.GraphEquation
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun IntegralBoundsDialog(
    equation: GraphEquation,
    initialA: String,
    initialB: String,
    onCalculate: (String, String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var aInput by remember { mutableStateOf(initialA) }
    var bInput by remember { mutableStateOf(initialB) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("integral_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Definite Integral ∫ f(x) dx",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Calculate numerical area under ${equation.label} = ${equation.expression}",
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.bodyMedium,
                    color = equation.color
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = aInput,
                        onValueChange = { aInput = it },
                        label = { Text("Lower bound (a)") },
                        modifier = Modifier.weight(1f).testTag("integral_a_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = bInput,
                        onValueChange = { bInput = it },
                        label = { Text("Upper bound (b)") },
                        modifier = Modifier.weight(1f).testTag("integral_b_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            onClear()
                            onDismiss()
                        },
                        modifier = Modifier.testTag("clear_integral_button")
                    ) {
                        Text("Clear Shading")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onCalculate(aInput, bInput)
                        },
                        modifier = Modifier.testTag("apply_integral_button")
                    ) {
                        Text("Plot & Shade")
                    }
                }
            }
        }
    }
}
