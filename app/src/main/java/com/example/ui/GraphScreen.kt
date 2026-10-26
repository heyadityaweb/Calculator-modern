package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.graph.*
import com.example.math.MathExpressionEvaluator
import com.example.ui.theme.JetBrainsMonoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(
    viewModel: CalculatorViewModel,
    evaluator: MathExpressionEvaluator,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var isEquationsSheetExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
    ) {
        // 1. Primary Graph Canvas
        GraphCanvas(
            viewport = state.viewport,
            equations = state.equations,
            criticalPoints = state.criticalPoints,
            evaluator = evaluator,
            isDegrees = state.isDegrees,
            showGrid = state.showGrid,
            showRoots = state.showRoots,
            showExtrema = state.showExtrema,
            showIntersections = state.showIntersections,
            showTangent = state.showTangent,
            tangentLine = state.tangentLine,
            integralShading = state.integralShading,
            tracePoint = state.tracePoint,
            onViewportChange = { viewModel.setViewport(it) },
            onTracePointChange = { viewModel.setTracePoint(it) },
            onPointSelected = { viewModel.setSelectedCriticalPoint(it) },
            modifier = Modifier.fillMaxSize().testTag("graph_canvas")
        )

        // 2. Top Header & Feature Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xE60F172A), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF0284C7), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ƒ", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GraphCalc",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Angle Mode Pill (RAD / DEG)
                    Surface(
                        onClick = { viewModel.toggleAngleMode() },
                        shape = RoundedCornerShape(20.dp),
                        color = if (state.isDegrees) Color(0xFF1E293B) else Color(0xFF0284C7),
                        modifier = Modifier.testTag("angle_mode_toggle")
                    ) {
                        Text(
                            text = if (state.isDegrees) "DEG" else "RAD",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    // Formula Presets Button
                    IconButton(
                        onClick = { viewModel.setScreen(AppScreen.PRESETS_LIBRARY) },
                        modifier = Modifier.size(36.dp).testTag("open_presets_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Presets Library",
                            tint = Color(0xFF38BDF8)
                        )
                    }

                    // Table of Values Button
                    IconButton(
                        onClick = { viewModel.openTableDialog() },
                        modifier = Modifier.size(36.dp).testTag("open_table_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = "Table of Values",
                            tint = Color(0xFF818CF8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Analysis Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AnalysisFilterChip(
                    label = "Roots",
                    dotColor = Color(0xFF10B981),
                    isSelected = state.showRoots,
                    onClick = { viewModel.toggleRoots() },
                    tag = "filter_roots"
                )
                AnalysisFilterChip(
                    label = "Extrema",
                    dotColor = Color(0xFFF43F5E),
                    isSelected = state.showExtrema,
                    onClick = { viewModel.toggleExtrema() },
                    tag = "filter_extrema"
                )
                AnalysisFilterChip(
                    label = "Intersects",
                    dotColor = Color(0xFFA855F7),
                    isSelected = state.showIntersections,
                    onClick = { viewModel.toggleIntersections() },
                    tag = "filter_intersections"
                )
                AnalysisFilterChip(
                    label = "Tangent",
                    dotColor = Color(0xFFF59E0B),
                    isSelected = state.showTangent,
                    onClick = { viewModel.toggleTangentLine() },
                    tag = "filter_tangent"
                )
            }
        }

        // 3. Floating Zoom & Navigation Controls (Right Side)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { viewModel.zoomIn() },
                modifier = Modifier.size(44.dp).testTag("zoom_in_button"),
                containerColor = Color(0xE60F172A),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In")
            }
            FloatingActionButton(
                onClick = { viewModel.zoomOut() },
                modifier = Modifier.size(44.dp).testTag("zoom_out_button"),
                containerColor = Color(0xE60F172A),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
            }
            FloatingActionButton(
                onClick = { viewModel.resetView() },
                modifier = Modifier.size(44.dp).testTag("reset_view_button"),
                containerColor = Color(0xE60F172A),
                contentColor = Color(0xFF38BDF8)
            ) {
                Icon(Icons.Default.CenterFocusStrong, contentDescription = "Reset View")
            }
        }

        // 4. Floating Banners (Tangent Slope / Definite Integral / Selected Point)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 110.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tangent Banner
            AnimatedVisibility(visible = state.showTangent && state.tangentLine != null) {
                state.tangentLine?.let { tl ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xE61E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                        modifier = Modifier.testTag("tangent_info_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tangent: ${tl.equationStr}  (m = ${tl.slope})",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }

            // Definite Integral Banner
            AnimatedVisibility(visible = state.integralShading != null && state.integralShading!!.isEnabled) {
                state.integralShading?.let { ish ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xE60F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                        modifier = Modifier.testTag("integral_info_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "∫ [${ish.a}, ${ish.b}] = ${ish.value}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Integral",
                                tint = Color.LightGray,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { viewModel.clearDefiniteIntegral() }
                            )
                        }
                    }
                }
            }

            // Selected Critical Point Inspector
            AnimatedVisibility(visible = state.selectedCriticalPoint != null) {
                state.selectedCriticalPoint?.let { cp ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xF00F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                        modifier = Modifier.testTag("critical_point_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${cp.equationLabel} ${cp.description}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF38BDF8)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color.LightGray,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { viewModel.setSelectedCriticalPoint(null) }
                            )
                        }
                    }
                }
            }
        }

        // 5. Bottom Equations Sheet
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color(0xF50F172A),
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header Bar with Toggle Collapse
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isEquationsSheetExpanded = !isEquationsSheetExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Equations (${state.equations.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (isEquationsSheetExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = "Toggle Sheet",
                            tint = Color.LightGray
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = { viewModel.addEquation() },
                            modifier = Modifier.testTag("add_equation_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add f(x)")
                        }
                    }
                }

                // If collapsed: show active equation; If expanded: show all
                val displayEquations = if (isEquationsSheetExpanded) state.equations else state.equations.take(2)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    displayEquations.forEach { eq ->
                        EquationItemRow(
                            equation = eq,
                            isActive = eq.id == state.activeEquationId,
                            onExpressionChange = { viewModel.updateEquationExpression(eq.id, it) },
                            onToggleVisibility = { viewModel.toggleEquationVisibility(eq.id) },
                            onSelect = { viewModel.setActiveEquation(eq.id) },
                            onOpenIntegral = { viewModel.openIntegralDialog(eq.id) },
                            onDelete = { viewModel.deleteEquation(eq.id) }
                        )
                    }
                }
            }
        }
    }

    // Table of values modal
    if (state.isTableDialogVisible) {
        TableOfValuesDialog(
            equations = state.equations,
            tableData = state.tableOfValues,
            startX = state.tableStartX,
            endX = state.tableEndX,
            step = state.tableStep,
            onUpdateParams = { s, e, st -> viewModel.setTableParams(s, e, st) },
            onDismiss = { viewModel.closeTableDialog() }
        )
    }

    // Definite integral modal
    if (state.isIntegralDialogVisible) {
        val targetEq = state.equations.firstOrNull { it.id == state.integralTargetEqId } ?: state.equations.first()
        IntegralBoundsDialog(
            equation = targetEq,
            initialA = state.integralA,
            initialB = state.integralB,
            onCalculate = { a, b -> viewModel.applyDefiniteIntegral(targetEq.id, a, b) },
            onClear = { viewModel.clearDefiniteIntegral() },
            onDismiss = { viewModel.closeIntegralDialog() }
        )
    }
}

@Composable
fun AnalysisFilterChip(
    label: String,
    dotColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF1E293B) else Color(0x800F172A),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) dotColor else Color(0xFF334155)
        ),
        modifier = Modifier.testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun EquationItemRow(
    equation: GraphEquation,
    isActive: Boolean,
    onExpressionChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    onSelect: () -> Unit,
    onOpenIntegral: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) Color(0xFF1E293B) else Color(0xFF131D2E),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) equation.color.copy(alpha = 0.8f) else Color(0xFF1E293B)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("equation_row_${equation.label}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator dot
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(equation.color)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Label
            Text(
                text = "${equation.label} =",
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = equation.color,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Expression TextField
            TextField(
                value = equation.expression,
                onValueChange = onExpressionChange,
                placeholder = { Text("e.g. sin(x)", color = Color.Gray, fontSize = 13.sp) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 14.sp,
                    color = Color.White
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f).testTag("equation_input_${equation.label}")
            )

            // Definite Integral Button
            IconButton(
                onClick = onOpenIntegral,
                modifier = Modifier.size(32.dp).testTag("integral_button_${equation.label}")
            ) {
                Text("∫", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 16.sp)
            }

            // Visibility Toggle Eye
            IconButton(
                onClick = onToggleVisibility,
                modifier = Modifier.size(32.dp).testTag("visibility_button_${equation.label}")
            ) {
                Icon(
                    imageVector = if (equation.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Toggle Visibility",
                    tint = if (equation.isVisible) Color.White else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Delete Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp).testTag("delete_button_${equation.label}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Equation",
                    tint = Color(0xFFF43F5E),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
