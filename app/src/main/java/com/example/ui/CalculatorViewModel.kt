package com.example.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CalculationRecord
import com.example.data.MathPreset
import com.example.graph.*
import com.example.math.ComplexNumber
import com.example.math.MathExpressionEvaluator
import com.example.math.MatrixEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.*

enum class AppScreen {
    GRAPH_STUDIO,
    SCIENTIFIC_CALC,
    PRESETS_LIBRARY
}

enum class CalculatorTab {
    SCIENTIFIC,
    CALCULUS,
    COMPLEX,
    MATRIX
}

data class CalculatorUiState(
    val currentScreen: AppScreen = AppScreen.GRAPH_STUDIO,
    val calcTab: CalculatorTab = CalculatorTab.SCIENTIFIC,

    // Calculator inputs
    val expression: String = "",
    val liveResult: String = "",
    val lastAns: Double = 0.0,
    val isDegrees: Boolean = false, // false = RAD, true = DEG
    val history: List<CalculationRecord> = emptyList(),

    // Complex Calculator
    val complexRealA: String = "3",
    val complexImagA: String = "4",
    val complexRealB: String = "1",
    val complexImagB: String = "-2",
    val complexResult: String = "",
    val complexPolarResult: String = "",

    // Matrix Calculator (2x2)
    val m2A11: String = "2", val m2A12: String = "-1",
    val m2A21: String = "1", val m2A22: String = "3",
    val m2B11: String = "1", val m2B12: String = "0",
    val m2B21: String = "0", val m2B22: String = "1",
    val matrixResultStr: String = "",

    // Graph Studio
    val equations: List<GraphEquation> = listOf(
        GraphEquation(
            id = "eq1",
            label = "f(x)",
            expression = "sin(x)",
            color = Color(0xFF38BDF8),
            isVisible = true
        ),
        GraphEquation(
            id = "eq2",
            label = "g(x)",
            expression = "0.2*x^2 - 2",
            color = Color(0xFFF43F5E),
            isVisible = true
        )
    ),
    val activeEquationId: String = "eq1",
    val viewport: GraphViewport = GraphViewport.DEFAULT,
    val showGrid: Boolean = true,
    val showRoots: Boolean = true,
    val showExtrema: Boolean = true,
    val showIntersections: Boolean = true,
    val showTangent: Boolean = false,
    val tracePoint: Point2D? = null,
    val selectedCriticalPoint: CriticalPoint? = null,
    val tangentLine: TangentLine? = null,
    val integralShading: IntegralShading? = null,
    val criticalPoints: List<CriticalPoint> = emptyList(),

    // Table of values
    val tableOfValues: List<TableRowData> = emptyList(),
    val tableStartX: Double = -5.0,
    val tableEndX: Double = 5.0,
    val tableStep: Double = 1.0,
    val isTableDialogVisible: Boolean = false,

    // Integral dialog
    val isIntegralDialogVisible: Boolean = false,
    val integralTargetEqId: String = "eq1",
    val integralA: String = "-2",
    val integralB: String = "2"
)

class CalculatorViewModel : ViewModel() {

    private val evaluator = MathExpressionEvaluator()
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        recomputeGraphAnalysis()
        evaluateComplexDefault()
        evaluateMatrixDefault()
    }

    fun setScreen(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun setCalcTab(tab: CalculatorTab) {
        _uiState.update { it.copy(calcTab = tab) }
    }

    fun toggleAngleMode() {
        _uiState.update { it.copy(isDegrees = !it.isDegrees) }
        recomputeCalculatorLive()
        recomputeGraphAnalysis()
    }

    // --- Calculator Methods ---
    fun onCalculatorKeyPress(key: String) {
        _uiState.update { state ->
            val newExpr = when (key) {
                "CLEAR" -> ""
                "DEL" -> if (state.expression.isNotEmpty()) state.expression.dropLast(1) else ""
                "ANS" -> state.expression + "ans"
                "π" -> state.expression + "pi"
                "e" -> state.expression + "e"
                "√" -> state.expression + "sqrt("
                "sin", "cos", "tan", "asin", "acos", "atan", "sinh", "cosh", "tanh", "ln", "log", "exp", "abs" -> state.expression + "$key("
                "x²" -> state.expression + "^2"
                "xʸ" -> state.expression + "^"
                "1/x" -> state.expression + "^(-1)"
                "nCr" -> state.expression + "nCr("
                "nPr" -> state.expression + "nPr("
                "d/dx" -> state.expression + "derivative"
                "∫" -> state.expression + "integrate"
                else -> state.expression + key
            }
            state.copy(expression = newExpr)
        }
        recomputeCalculatorLive()
    }

    fun onCalculatorSubmit() {
        val state = _uiState.value
        val expr = state.expression.trim()
        if (expr.isEmpty()) return

        try {
            val res = evaluator.evaluate(expr, isDegrees = state.isDegrees, ans = state.lastAns)
            val resStr = formatResult(res)
            val newRecord = CalculationRecord(
                id = UUID.randomUUID().toString(),
                expression = expr,
                result = resStr,
                mode = "REAL"
            )
            _uiState.update {
                it.copy(
                    liveResult = "= $resStr",
                    lastAns = if (!res.isNaN() && !res.isInfinite()) res else it.lastAns,
                    history = listOf(newRecord) + it.history
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(liveResult = "Error: ${e.message ?: "Invalid syntax"}") }
        }
    }

    fun recallHistory(record: CalculationRecord) {
        _uiState.update { it.copy(expression = record.expression) }
        recomputeCalculatorLive()
    }

    fun clearHistory() {
        _uiState.update { it.copy(history = emptyList()) }
    }

    private fun recomputeCalculatorLive() {
        val state = _uiState.value
        val expr = state.expression.trim()
        if (expr.isEmpty()) {
            _uiState.update { it.copy(liveResult = "") }
            return
        }

        try {
            val res = evaluator.evaluate(expr, isDegrees = state.isDegrees, ans = state.lastAns)
            if (!res.isNaN() && !res.isInfinite()) {
                _uiState.update { it.copy(liveResult = "= ${formatResult(res)}") }
            } else {
                _uiState.update { it.copy(liveResult = "") }
            }
        } catch (_: Exception) {
            // Incomplete expression while typing
        }
    }

    // --- Complex Numbers ---
    fun setComplexField(aReal: String? = null, aImag: String? = null, bReal: String? = null, bImag: String? = null) {
        _uiState.update {
            it.copy(
                complexRealA = aReal ?: it.complexRealA,
                complexImagA = aImag ?: it.complexImagA,
                complexRealB = bReal ?: it.complexRealB,
                complexImagB = bImag ?: it.complexImagB
            )
        }
    }

    fun calculateComplexOperation(op: String) {
        val s = _uiState.value
        val z1 = ComplexNumber(s.complexRealA.toDoubleOrNull() ?: 0.0, s.complexImagA.toDoubleOrNull() ?: 0.0)
        val z2 = ComplexNumber(s.complexRealB.toDoubleOrNull() ?: 0.0, s.complexImagB.toDoubleOrNull() ?: 0.0)

        val resultZ = when (op) {
            "+" -> z1 + z2
            "-" -> z1 - z2
            "*" -> z1 * z2
            "/" -> z1 / z2
            "pow2" -> z1.pow(2.0)
            "sqrt" -> z1.sqrt()
            "conj" -> z1.conjugate()
            else -> z1
        }

        _uiState.update {
            it.copy(
                complexResult = "z = ${resultZ.toStandardString()}",
                complexPolarResult = "Polar: ${resultZ.toPolarString(s.isDegrees)}"
            )
        }
    }

    private fun evaluateComplexDefault() {
        calculateComplexOperation("+")
    }

    // --- Matrix Linear Algebra ---
    fun setMatrix2x2Value(field: String, value: String) {
        _uiState.update {
            when (field) {
                "A11" -> it.copy(m2A11 = value)
                "A12" -> it.copy(m2A12 = value)
                "A21" -> it.copy(m2A21 = value)
                "A22" -> it.copy(m2A22 = value)
                "B11" -> it.copy(m2B11 = value)
                "B12" -> it.copy(m2B12 = value)
                "B21" -> it.copy(m2B21 = value)
                "B22" -> it.copy(m2B22 = value)
                else -> it
            }
        }
    }

    fun calculateMatrixOp(op: String) {
        val s = _uiState.value
        val a = MatrixEngine.Matrix2x2(
            s.m2A11.toDoubleOrNull() ?: 0.0, s.m2A12.toDoubleOrNull() ?: 0.0,
            s.m2A21.toDoubleOrNull() ?: 0.0, s.m2A22.toDoubleOrNull() ?: 0.0
        )
        val b = MatrixEngine.Matrix2x2(
            s.m2B11.toDoubleOrNull() ?: 0.0, s.m2B12.toDoubleOrNull() ?: 0.0,
            s.m2B21.toDoubleOrNull() ?: 0.0, s.m2B22.toDoubleOrNull() ?: 0.0
        )

        val resStr = when (op) {
            "DET_A" -> "det(A) = ${formatDouble(a.determinant)}"
            "INV_A" -> {
                val inv = a.inverse()
                if (inv != null) {
                    "A⁻¹ =\n[ ${formatDouble(inv.a11)}, ${formatDouble(inv.a12)} ]\n[ ${formatDouble(inv.a21)}, ${formatDouble(inv.a22)} ]"
                } else "Matrix A is singular (det = 0, no inverse)"
            }
            "TRANS_A" -> {
                val t = a.transpose()
                "Aᵀ =\n[ ${formatDouble(t.a11)}, ${formatDouble(t.a12)} ]\n[ ${formatDouble(t.a21)}, ${formatDouble(t.a22)} ]"
            }
            "TRACE_A" -> "tr(A) = ${formatDouble(a.trace)}"
            "EIGEN_A" -> {
                val eig = a.eigenvalues()
                if (eig != null) "Eigenvalues: λ₁ = ${formatDouble(eig.first)}, λ₂ = ${formatDouble(eig.second)}"
                else "Eigenvalues are complex"
            }
            "A_PLUS_B" -> {
                val c = a + b
                "A + B =\n[ ${formatDouble(c.a11)}, ${formatDouble(c.a12)} ]\n[ ${formatDouble(c.a21)}, ${formatDouble(c.a22)} ]"
            }
            "A_MINUS_B" -> {
                val c = a - b
                "A - B =\n[ ${formatDouble(c.a11)}, ${formatDouble(c.a12)} ]\n[ ${formatDouble(c.a21)}, ${formatDouble(c.a22)} ]"
            }
            "A_TIMES_B" -> {
                val c = a * b
                "A × B =\n[ ${formatDouble(c.a11)}, ${formatDouble(c.a12)} ]\n[ ${formatDouble(c.a21)}, ${formatDouble(c.a22)} ]"
            }
            else -> ""
        }
        _uiState.update { it.copy(matrixResultStr = resStr) }
    }

    private fun evaluateMatrixDefault() {
        calculateMatrixOp("DET_A")
    }

    // --- Graph Studio Methods ---
    fun setViewport(vp: GraphViewport) {
        _uiState.update { it.copy(viewport = vp) }
        recomputeGraphAnalysis()
    }

    fun zoomIn() {
        val s = _uiState.value
        val centerX = (s.viewport.xMin + s.viewport.xMax) / 2.0
        val centerY = (s.viewport.yMin + s.viewport.yMax) / 2.0
        setViewport(s.viewport.zoom(1.4, centerX, centerY))
    }

    fun zoomOut() {
        val s = _uiState.value
        val centerX = (s.viewport.xMin + s.viewport.xMax) / 2.0
        val centerY = (s.viewport.yMin + s.viewport.yMax) / 2.0
        setViewport(s.viewport.zoom(0.714, centerX, centerY))
    }

    fun resetView() {
        setViewport(GraphViewport.DEFAULT)
    }

    fun updateEquationExpression(id: String, newExpression: String) {
        _uiState.update { state ->
            val updated = state.equations.map { eq ->
                if (eq.id == id) eq.copy(expression = newExpression, error = null) else eq
            }
            state.copy(equations = updated)
        }
        recomputeGraphAnalysis()
    }

    fun toggleEquationVisibility(id: String) {
        _uiState.update { state ->
            val updated = state.equations.map { eq ->
                if (eq.id == id) eq.copy(isVisible = !eq.isVisible) else eq
            }
            state.copy(equations = updated)
        }
        recomputeGraphAnalysis()
    }

    fun addEquation() {
        val colors = listOf(
            Color(0xFF38BDF8), Color(0xFFF43F5E), Color(0xFF10B981),
            Color(0xFFA855F7), Color(0xFFF59E0B), Color(0xFFEC4899)
        )
        _uiState.update { state ->
            val index = state.equations.size + 1
            val label = "f$index(x)"
            val newColor = colors[(index - 1) % colors.size]
            val newEq = GraphEquation(
                id = UUID.randomUUID().toString(),
                label = label,
                expression = "",
                color = newColor,
                isVisible = true
            )
            state.copy(equations = state.equations + newEq, activeEquationId = newEq.id)
        }
    }

    fun deleteEquation(id: String) {
        _uiState.update { state ->
            if (state.equations.size <= 1) return@update state // keep at least one
            val updated = state.equations.filter { it.id != id }
            state.copy(
                equations = updated,
                activeEquationId = updated.firstOrNull()?.id ?: ""
            )
        }
        recomputeGraphAnalysis()
    }

    fun setActiveEquation(id: String) {
        _uiState.update { it.copy(activeEquationId = id) }
        updateTangentAndTrace(itState = _uiState.value)
    }

    fun setTracePoint(point: Point2D?) {
        _uiState.update { it.copy(tracePoint = point) }
        updateTangentAndTrace(itState = _uiState.value)
    }

    fun setSelectedCriticalPoint(cp: CriticalPoint?) {
        _uiState.update { it.copy(selectedCriticalPoint = cp) }
    }

    fun toggleTangentLine() {
        val nextShow = !_uiState.value.showTangent
        _uiState.update { it.copy(showTangent = nextShow) }
        if (nextShow && _uiState.value.tracePoint == null) {
            // Default trace point at x = 1.0 on active eq
            val activeEq = _uiState.value.equations.firstOrNull { it.id == _uiState.value.activeEquationId }
            if (activeEq != null) {
                try {
                    val y = evaluator.evaluate(activeEq.expression, 1.0, _uiState.value.isDegrees)
                    setTracePoint(Point2D(1.0, y))
                } catch (_: Exception) {}
            }
        } else {
            updateTangentAndTrace(_uiState.value)
        }
    }

    fun toggleRoots() {
        _uiState.update { it.copy(showRoots = !it.showRoots) }
    }

    fun toggleExtrema() {
        _uiState.update { it.copy(showExtrema = !it.showExtrema) }
    }

    fun toggleIntersections() {
        _uiState.update { it.copy(showIntersections = !it.showIntersections) }
    }

    fun applyPreset(preset: MathPreset) {
        val s = _uiState.value
        val activeId = s.activeEquationId
        val updated = s.equations.map { eq ->
            if (eq.id == activeId) {
                eq.copy(expression = preset.formula, color = preset.accentColor, isVisible = true)
            } else eq
        }
        val newVp = GraphViewport(
            xMin = preset.suggestedXRange.first,
            xMax = preset.suggestedXRange.second,
            yMin = preset.suggestedYRange.first,
            yMax = preset.suggestedYRange.second
        )
        _uiState.update {
            it.copy(
                equations = updated,
                viewport = newVp,
                currentScreen = AppScreen.GRAPH_STUDIO
            )
        }
        recomputeGraphAnalysis()
    }

    fun openTableDialog() {
        generateTable()
        _uiState.update { it.copy(isTableDialogVisible = true) }
    }

    fun closeTableDialog() {
        _uiState.update { it.copy(isTableDialogVisible = false) }
    }

    fun setTableParams(start: Double, end: Double, step: Double) {
        _uiState.update {
            it.copy(tableStartX = start, tableEndX = end, tableStep = step)
        }
        generateTable()
    }

    private fun generateTable() {
        val s = _uiState.value
        val visibleEqs = s.equations.filter { it.isVisible && it.expression.isNotBlank() }
        val table = GraphAnalyzer.generateTable(
            equations = visibleEqs,
            startX = s.tableStartX,
            endX = s.tableEndX,
            step = s.tableStep,
            evaluator = evaluator,
            isDegrees = s.isDegrees
        )
        _uiState.update { it.copy(tableOfValues = table) }
    }

    fun openIntegralDialog(equationId: String) {
        _uiState.update {
            it.copy(
                isIntegralDialogVisible = true,
                integralTargetEqId = equationId
            )
        }
    }

    fun closeIntegralDialog() {
        _uiState.update { it.copy(isIntegralDialogVisible = false) }
    }

    fun applyDefiniteIntegral(equationId: String, aStr: String, bStr: String) {
        val a = aStr.toDoubleOrNull() ?: 0.0
        val b = bStr.toDoubleOrNull() ?: 1.0
        val targetEq = _uiState.value.equations.firstOrNull { it.id == equationId }

        if (targetEq != null) {
            val areaVal = evaluator.numericalIntegrate(targetEq.expression, a, b, _uiState.value.isDegrees)
            _uiState.update {
                it.copy(
                    integralShading = IntegralShading(
                        equationId = equationId,
                        a = a,
                        b = b,
                        value = roundTo(areaVal, 4),
                        isEnabled = true
                    ),
                    isIntegralDialogVisible = false
                )
            }
        }
    }

    fun clearDefiniteIntegral() {
        _uiState.update { it.copy(integralShading = null) }
    }

    private fun recomputeGraphAnalysis() {
        val state = _uiState.value
        val visibleEqs = state.equations.filter { it.isVisible && it.expression.isNotBlank() }
        val allPoints = mutableListOf<CriticalPoint>()

        // 1. Critical points per equation (Roots, Y-intercept, Extrema)
        visibleEqs.forEach { eq ->
            val cpList = GraphAnalyzer.findCriticalPoints(eq, state.viewport, evaluator, state.isDegrees)
            allPoints.addAll(cpList)
        }

        // 2. Intersections between pairs of equations
        for (i in visibleEqs.indices) {
            for (j in i + 1 until visibleEqs.size) {
                val inters = GraphAnalyzer.findIntersections(
                    visibleEqs[i], visibleEqs[j], state.viewport, evaluator, state.isDegrees
                )
                allPoints.addAll(inters)
            }
        }

        _uiState.update { it.copy(criticalPoints = allPoints) }
        updateTangentAndTrace(_uiState.value)
    }

    private fun updateTangentAndTrace(itState: CalculatorUiState) {
        if (!itState.showTangent || itState.tracePoint == null) {
            _uiState.update { it.copy(tangentLine = null) }
            return
        }
        val activeEq = itState.equations.firstOrNull { it.id == itState.activeEquationId && it.isVisible }
        if (activeEq != null && activeEq.expression.isNotBlank()) {
            val tangent = GraphAnalyzer.computeTangent(
                equation = activeEq,
                x0 = itState.tracePoint.x,
                evaluator = evaluator,
                isDegrees = itState.isDegrees
            )
            _uiState.update { it.copy(tangentLine = tangent) }
        } else {
            _uiState.update { it.copy(tangentLine = null) }
        }
    }

    private fun formatResult(v: Double): String {
        if (v.isNaN()) return "Undefined"
        if (v.isInfinite()) return if (v > 0) "∞" else "-∞"
        val rounded = (v * 1000000.0).roundToLong() / 1000000.0
        return if (rounded == floor(rounded) && abs(rounded) < 1e12) rounded.toLong().toString() else rounded.toString()
    }

    private fun formatDouble(v: Double): String {
        if (v.isNaN()) return "NaN"
        if (v.isInfinite()) return if (v > 0) "∞" else "-∞"
        val rounded = (v * 1000.0).roundToLong() / 1000.0
        return if (rounded == floor(rounded)) rounded.toLong().toString() else rounded.toString()
    }

    private fun roundTo(v: Double, places: Int): Double {
        if (v.isNaN() || v.isInfinite()) return v
        val factor = 10.0.pow(places)
        return (v * factor).roundToLong() / factor
    }
}
