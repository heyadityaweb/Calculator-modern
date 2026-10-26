package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlin.math.*

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showHistoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp)
    ) {
        // 1. Top Bar: Title, Angle Mode, History Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Scientific Studio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Angle Mode Pill
                Surface(
                    onClick = { viewModel.toggleAngleMode() },
                    shape = RoundedCornerShape(20.dp),
                    color = if (state.isDegrees) Color(0xFF1E293B) else Color(0xFF0284C7),
                    modifier = Modifier.testTag("calc_angle_mode_toggle")
                ) {
                    Text(
                        text = if (state.isDegrees) "DEG" else "RAD",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                // History Button
                IconButton(
                    onClick = { showHistoryDialog = true },
                    modifier = Modifier.size(40.dp).testTag("calc_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Calculation History",
                        tint = Color(0xFF38BDF8)
                    )
                }
            }
        }

        // 2. Tabs Row: Scientific, Calculus, Complex, Matrices
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcModeTab(
                title = "Scientific",
                isSelected = state.calcTab == CalculatorTab.SCIENTIFIC,
                onClick = { viewModel.setCalcTab(CalculatorTab.SCIENTIFIC) },
                modifier = Modifier.weight(1f),
                tag = "tab_scientific"
            )
            CalcModeTab(
                title = "Calculus",
                isSelected = state.calcTab == CalculatorTab.CALCULUS,
                onClick = { viewModel.setCalcTab(CalculatorTab.CALCULUS) },
                modifier = Modifier.weight(1f),
                tag = "tab_calculus"
            )
            CalcModeTab(
                title = "Complex (z)",
                isSelected = state.calcTab == CalculatorTab.COMPLEX,
                onClick = { viewModel.setCalcTab(CalculatorTab.COMPLEX) },
                modifier = Modifier.weight(1f),
                tag = "tab_complex"
            )
            CalcModeTab(
                title = "Matrix",
                isSelected = state.calcTab == CalculatorTab.MATRIX,
                onClick = { viewModel.setCalcTab(CalculatorTab.MATRIX) },
                modifier = Modifier.weight(1f),
                tag = "tab_matrix"
            )
        }

        // 3. Tab Content
        when (state.calcTab) {
            CalculatorTab.SCIENTIFIC -> ScientificTabContent(state, viewModel)
            CalculatorTab.CALCULUS -> CalculusTabContent(state, viewModel)
            CalculatorTab.COMPLEX -> ComplexTabContent(state, viewModel)
            CalculatorTab.MATRIX -> MatrixTabContent(state, viewModel)
        }
    }

    // History Modal Dialog
    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Calculation History", fontWeight = FontWeight.Bold)
                    if (state.history.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearHistory() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear History", tint = Color(0xFFF43F5E))
                        }
                    }
                }
            },
            text = {
                if (state.history.isEmpty()) {
                    Text("No past calculations yet.", color = Color.Gray)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)) {
                        items(state.history) { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.recallHistory(item)
                                        showHistoryDialog = false
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = item.expression,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "= ${item.result}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 16.sp
                                )
                                HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color(0xFF1E293B))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
fun ColumnScope.ScientificTabContent(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel
) {
    // Expression Display & Result Preview
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .weight(0.7f)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            // Scrollable Expression Line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState(), reverseScrolling = true),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = if (state.expression.isEmpty()) "0" else state.expression,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 28.sp,
                    color = if (state.expression.isEmpty()) Color.Gray else Color.White,
                    modifier = Modifier.testTag("calc_expression_text")
                )
            }

            // Live Computed Result
            Text(
                text = state.liveResult,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF38BDF8),
                modifier = Modifier.testTag("calc_result_text")
            )
        }
    }

    // Keypad Rows
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1.8f)
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: Advanced functions
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcButton(text = "sin", type = CalcButtonType.FUNCTION, onClick = { viewModel.onCalculatorKeyPress("sin") }, modifier = Modifier.weight(1f))
            CalcButton(text = "cos", type = CalcButtonType.FUNCTION, onClick = { viewModel.onCalculatorKeyPress("cos") }, modifier = Modifier.weight(1f))
            CalcButton(text = "tan", type = CalcButtonType.FUNCTION, onClick = { viewModel.onCalculatorKeyPress("tan") }, modifier = Modifier.weight(1f))
            CalcButton(text = "ln", type = CalcButtonType.FUNCTION, onClick = { viewModel.onCalculatorKeyPress("ln") }, modifier = Modifier.weight(1f))
            CalcButton(text = "log", type = CalcButtonType.FUNCTION, onClick = { viewModel.onCalculatorKeyPress("log") }, modifier = Modifier.weight(1f))
        }

        // Row 2: Powers, Roots, Constants
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcButton(text = "x²", type = CalcButtonType.FUNCTION, onClick = { viewModel.onCalculatorKeyPress("x²") }, modifier = Modifier.weight(1f))
            CalcButton(text = "xʸ", type = CalcButtonType.FUNCTION, onClick = { viewModel.onCalculatorKeyPress("xʸ") }, modifier = Modifier.weight(1f))
            CalcButton(text = "√", type = CalcButtonType.FUNCTION, onClick = { viewModel.onCalculatorKeyPress("√") }, modifier = Modifier.weight(1f))
            CalcButton(text = "π", type = CalcButtonType.FUNCTION, onClick = { viewModel.onCalculatorKeyPress("π") }, modifier = Modifier.weight(1f))
            CalcButton(text = "e", type = CalcButtonType.FUNCTION, onClick = { viewModel.onCalculatorKeyPress("e") }, modifier = Modifier.weight(1f))
        }

        // Row 3: Clear, Brackets, Division
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcButton(text = "C", type = CalcButtonType.CLEAR, onClick = { viewModel.onCalculatorKeyPress("CLEAR") }, modifier = Modifier.weight(1f))
            CalcButton(text = "(", type = CalcButtonType.FUNCTION, onClick = { viewModel.onCalculatorKeyPress("(") }, modifier = Modifier.weight(1f))
            CalcButton(text = ")", type = CalcButtonType.FUNCTION, onClick = { viewModel.onCalculatorKeyPress(")") }, modifier = Modifier.weight(1f))
            CalcButton(text = "DEL", type = CalcButtonType.ACTION, onClick = { viewModel.onCalculatorKeyPress("DEL") }, modifier = Modifier.weight(1f))
            CalcButton(text = "÷", type = CalcButtonType.OPERATOR, onClick = { viewModel.onCalculatorKeyPress("÷") }, modifier = Modifier.weight(1f))
        }

        // Row 4: 7, 8, 9, Multiplication
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcButton(text = "7", type = CalcButtonType.NUMBER, onClick = { viewModel.onCalculatorKeyPress("7") }, modifier = Modifier.weight(1f))
            CalcButton(text = "8", type = CalcButtonType.NUMBER, onClick = { viewModel.onCalculatorKeyPress("8") }, modifier = Modifier.weight(1f))
            CalcButton(text = "9", type = CalcButtonType.NUMBER, onClick = { viewModel.onCalculatorKeyPress("9") }, modifier = Modifier.weight(1f))
            CalcButton(text = "%", type = CalcButtonType.FUNCTION, onClick = { viewModel.onCalculatorKeyPress("%") }, modifier = Modifier.weight(1f))
            CalcButton(text = "×", type = CalcButtonType.OPERATOR, onClick = { viewModel.onCalculatorKeyPress("×") }, modifier = Modifier.weight(1f))
        }

        // Row 5: 4, 5, 6, Subtraction
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcButton(text = "4", type = CalcButtonType.NUMBER, onClick = { viewModel.onCalculatorKeyPress("4") }, modifier = Modifier.weight(1f))
            CalcButton(text = "5", type = CalcButtonType.NUMBER, onClick = { viewModel.onCalculatorKeyPress("5") }, modifier = Modifier.weight(1f))
            CalcButton(text = "6", type = CalcButtonType.NUMBER, onClick = { viewModel.onCalculatorKeyPress("6") }, modifier = Modifier.weight(1f))
            CalcButton(text = "!", type = CalcButtonType.FUNCTION, onClick = { viewModel.onCalculatorKeyPress("!") }, modifier = Modifier.weight(1f))
            CalcButton(text = "−", type = CalcButtonType.OPERATOR, onClick = { viewModel.onCalculatorKeyPress("−") }, modifier = Modifier.weight(1f))
        }

        // Row 6: 1, 2, 3, Addition
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcButton(text = "1", type = CalcButtonType.NUMBER, onClick = { viewModel.onCalculatorKeyPress("1") }, modifier = Modifier.weight(1f))
            CalcButton(text = "2", type = CalcButtonType.NUMBER, onClick = { viewModel.onCalculatorKeyPress("2") }, modifier = Modifier.weight(1f))
            CalcButton(text = "3", type = CalcButtonType.NUMBER, onClick = { viewModel.onCalculatorKeyPress("3") }, modifier = Modifier.weight(1f))
            CalcButton(text = "ANS", type = CalcButtonType.FUNCTION, onClick = { viewModel.onCalculatorKeyPress("ANS") }, modifier = Modifier.weight(1f))
            CalcButton(text = "+", type = CalcButtonType.OPERATOR, onClick = { viewModel.onCalculatorKeyPress("+") }, modifier = Modifier.weight(1f))
        }

        // Row 7: 0, Dot, Equals
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcButton(text = "0", type = CalcButtonType.NUMBER, onClick = { viewModel.onCalculatorKeyPress("0") }, modifier = Modifier.weight(2f))
            CalcButton(text = ".", type = CalcButtonType.NUMBER, onClick = { viewModel.onCalculatorKeyPress(".") }, modifier = Modifier.weight(1f))
            CalcButton(text = "=", type = CalcButtonType.EQUALS, onClick = { viewModel.onCalculatorSubmit() }, modifier = Modifier.weight(2f))
        }
    }
}

@Composable
fun ColumnScope.CalculusTabContent(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel
) {
    var diffExpr by remember { mutableStateOf("x^3 - 4*x") }
    var diffX0 by remember { mutableStateOf("2") }
    var diffResult by remember { mutableStateOf("") }

    var intExpr by remember { mutableStateOf("sin(x)") }
    var intA by remember { mutableStateOf("0") }
    var intB by remember { mutableStateOf("pi") }
    var intResult by remember { mutableStateOf("") }

    val eval = remember { com.example.math.MathExpressionEvaluator() }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Derivative Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Numerical Derivative  d/dx [ f(x) ]",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = diffExpr,
                        onValueChange = { diffExpr = it },
                        label = { Text("f(x)") },
                        modifier = Modifier.fillMaxWidth().testTag("diff_expr_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = diffX0,
                            onValueChange = { diffX0 = it },
                            label = { Text("at x₀ =") },
                            modifier = Modifier.weight(1f).testTag("diff_x0_input")
                        )
                        Button(
                            onClick = {
                                val x0 = diffX0.toDoubleOrNull() ?: 0.0
                                try {
                                    val slope = eval.numericalDerivative(diffExpr, x0, state.isDegrees)
                                    diffResult = "f'($x0) = ${roundTo(slope, 5)}"
                                } catch (e: Exception) {
                                    diffResult = "Error: ${e.message}"
                                }
                            },
                            modifier = Modifier.testTag("calc_diff_button")
                        ) {
                            Text("Compute d/dx")
                        }
                    }
                    if (diffResult.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = diffResult,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF34D399)
                        )
                    }
                }
            }
        }

        item {
            // Definite Integral Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Definite Integral  ∫ [a to b] f(x) dx",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF818CF8)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = intExpr,
                        onValueChange = { intExpr = it },
                        label = { Text("f(x)") },
                        modifier = Modifier.fillMaxWidth().testTag("int_expr_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = intA,
                            onValueChange = { intA = it },
                            label = { Text("a (lower)") },
                            modifier = Modifier.weight(1f).testTag("int_a_input")
                        )
                        OutlinedTextField(
                            value = intB,
                            onValueChange = { intB = it },
                            label = { Text("b (upper)") },
                            modifier = Modifier.weight(1f).testTag("int_b_input")
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val a = if (intA.lowercase() == "pi") Math.PI else (intA.toDoubleOrNull() ?: 0.0)
                            val b = if (intB.lowercase() == "pi") Math.PI else (intB.toDoubleOrNull() ?: 1.0)
                            try {
                                val area = eval.numericalIntegrate(intExpr, a, b, state.isDegrees)
                                intResult = "∫ = ${roundTo(area, 5)}"
                            } catch (e: Exception) {
                                intResult = "Error: ${e.message}"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("calc_integral_button")
                    ) {
                        Text("Compute Integral")
                    }
                    if (intResult.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = intResult,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF34D399)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColumnScope.ComplexTabContent(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Complex Number Arithmetic (z = a + bi)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("z₁ (First Number):", fontWeight = FontWeight.SemiBold, color = Color.White)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = state.complexRealA,
                            onValueChange = { viewModel.setComplexField(aReal = it) },
                            label = { Text("Real (a)") },
                            modifier = Modifier.weight(1f).testTag("z1_real_input")
                        )
                        OutlinedTextField(
                            value = state.complexImagA,
                            onValueChange = { viewModel.setComplexField(aImag = it) },
                            label = { Text("Imag (b)") },
                            modifier = Modifier.weight(1f).testTag("z1_imag_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("z₂ (Second Number):", fontWeight = FontWeight.SemiBold, color = Color.White)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = state.complexRealB,
                            onValueChange = { viewModel.setComplexField(bReal = it) },
                            label = { Text("Real (c)") },
                            modifier = Modifier.weight(1f).testTag("z2_real_input")
                        )
                        OutlinedTextField(
                            value = state.complexImagB,
                            onValueChange = { viewModel.setComplexField(bImag = it) },
                            label = { Text("Imag (d)") },
                            modifier = Modifier.weight(1f).testTag("z2_imag_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Operation Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(onClick = { viewModel.calculateComplexOperation("+") }, modifier = Modifier.weight(1f)) { Text("+") }
                        Button(onClick = { viewModel.calculateComplexOperation("-") }, modifier = Modifier.weight(1f)) { Text("−") }
                        Button(onClick = { viewModel.calculateComplexOperation("*") }, modifier = Modifier.weight(1f)) { Text("×") }
                        Button(onClick = { viewModel.calculateComplexOperation("/") }, modifier = Modifier.weight(1f)) { Text("÷") }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledTonalButton(onClick = { viewModel.calculateComplexOperation("pow2") }, modifier = Modifier.weight(1f)) { Text("z₁²") }
                        FilledTonalButton(onClick = { viewModel.calculateComplexOperation("sqrt") }, modifier = Modifier.weight(1f)) { Text("√z₁") }
                        FilledTonalButton(onClick = { viewModel.calculateComplexOperation("conj") }, modifier = Modifier.weight(1f)) { Text("z₁*") }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Results
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = state.complexResult,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF38BDF8)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.complexPolarResult,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 14.sp,
                                color = Color(0xFF34D399)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColumnScope.MatrixTabContent(
    state: CalculatorUiState,
    viewModel: CalculatorViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Matrix A (2x2):",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.m2A11,
                            onValueChange = { viewModel.setMatrix2x2Value("A11", it) },
                            label = { Text("a₁₁") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.m2A12,
                            onValueChange = { viewModel.setMatrix2x2Value("A12", it) },
                            label = { Text("a₁₂") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.m2A21,
                            onValueChange = { viewModel.setMatrix2x2Value("A21", it) },
                            label = { Text("a₂₁") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.m2A22,
                            onValueChange = { viewModel.setMatrix2x2Value("A22", it) },
                            label = { Text("a₂₂") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Operations on Matrix A:", fontWeight = FontWeight.SemiBold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(onClick = { viewModel.calculateMatrixOp("DET_A") }, modifier = Modifier.weight(1f)) { Text("det(A)") }
                        Button(onClick = { viewModel.calculateMatrixOp("INV_A") }, modifier = Modifier.weight(1f)) { Text("A⁻¹") }
                        Button(onClick = { viewModel.calculateMatrixOp("TRANS_A") }, modifier = Modifier.weight(1f)) { Text("Aᵀ") }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledTonalButton(onClick = { viewModel.calculateMatrixOp("TRACE_A") }, modifier = Modifier.weight(1f)) { Text("tr(A)") }
                        FilledTonalButton(onClick = { viewModel.calculateMatrixOp("EIGEN_A") }, modifier = Modifier.weight(1f)) { Text("Eigenvalues") }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Result Display
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = state.matrixResultStr,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalcModeTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF0284C7) else Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B)),
        modifier = modifier.testTag(tag)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFF94A3B8)
            )
        }
    }
}

enum class CalcButtonType {
    NUMBER,
    OPERATOR,
    FUNCTION,
    ACTION,
    CLEAR,
    EQUALS
}

@Composable
fun CalcButton(
    text: String,
    type: CalcButtonType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when (type) {
        CalcButtonType.NUMBER -> Color(0xFF1E293B)
        CalcButtonType.OPERATOR -> Color(0xFF0284C7)
        CalcButtonType.FUNCTION -> Color(0xFF0F172A)
        CalcButtonType.ACTION -> Color(0xFF334155)
        CalcButtonType.CLEAR -> Color(0xFFE11D48)
        CalcButtonType.EQUALS -> Color(0xFF10B981)
    }

    val textColor = when (type) {
        CalcButtonType.OPERATOR, CalcButtonType.EQUALS, CalcButtonType.CLEAR -> Color.White
        CalcButtonType.FUNCTION -> Color(0xFF38BDF8)
        else -> Color.White
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier
            .height(48.dp)
            .testTag("calc_key_$text")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = textColor
            )
        }
    }
}

private fun roundTo(v: Double, places: Int): Double {
    if (v.isNaN() || v.isInfinite()) return v
    val factor = 10.0.pow(places)
    return (v * factor).roundToLong() / factor
}
