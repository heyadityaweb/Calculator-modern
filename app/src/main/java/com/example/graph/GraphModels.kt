package com.example.graph

import androidx.compose.ui.graphics.Color

enum class CriticalPointType {
    ROOT,
    Y_INTERCEPT,
    LOCAL_MIN,
    LOCAL_MAX,
    INTERSECTION
}

data class CriticalPoint(
    val id: String,
    val type: CriticalPointType,
    val x: Double,
    val y: Double,
    val equationLabel: String,
    val description: String
)

data class GraphEquation(
    val id: String,
    val label: String,
    val expression: String,
    val color: Color,
    val isVisible: Boolean = true,
    val error: String? = null
)

data class TangentLine(
    val x0: Double,
    val y0: Double,
    val slope: Double,
    val equationStr: String
)

data class IntegralShading(
    val equationId: String,
    val a: Double,
    val b: Double,
    val value: Double = 0.0,
    val isEnabled: Boolean = false
)

data class TableRowData(
    val x: Double,
    val values: Map<String, Double?> // equation label to evaluated y
)

data class GraphViewport(
    val xMin: Double = -10.0,
    val xMax: Double = 10.0,
    val yMin: Double = -10.0,
    val yMax: Double = 10.0
) {
    val spanX: Double get() = xMax - xMin
    val spanY: Double get() = yMax - yMin

    fun toScreenX(mathX: Double, screenWidth: Float): Float {
        return ((mathX - xMin) / spanX * screenWidth).toFloat()
    }

    fun toScreenY(mathY: Double, screenHeight: Float): Float {
        return (screenHeight - (mathY - yMin) / spanY * screenHeight).toFloat()
    }

    fun toMathX(screenX: Float, screenWidth: Float): Double {
        return xMin + (screenX / screenWidth) * spanX
    }

    fun toMathY(screenY: Float, screenHeight: Float): Double {
        return yMin + ((screenHeight - screenY) / screenHeight) * spanY
    }

    fun pan(deltaScreenX: Float, deltaScreenY: Float, screenWidth: Float, screenHeight: Float): GraphViewport {
        val dxMath = (deltaScreenX / screenWidth) * spanX
        val dyMath = (deltaScreenY / screenHeight) * spanY
        return copy(
            xMin = xMin - dxMath,
            xMax = xMax - dxMath,
            yMin = yMin + dyMath,
            yMax = yMax + dyMath
        )
    }

    fun zoom(zoomFactor: Double, centerMathX: Double, centerMathY: Double): GraphViewport {
        // zoomFactor > 1 zooms in, zoomFactor < 1 zooms out
        val newSpanX = spanX / zoomFactor
        val newSpanY = spanY / zoomFactor

        val ratioX = (centerMathX - xMin) / spanX
        val ratioY = (centerMathY - yMin) / spanY

        val newXMin = centerMathX - ratioX * newSpanX
        val newXMax = newXMin + newSpanX
        val newYMin = centerMathY - ratioY * newSpanY
        val newYMax = newYMin + newSpanY

        return copy(
            xMin = newXMin,
            xMax = newXMax,
            yMin = newYMin,
            yMax = newYMax
        )
    }

    companion object {
        val DEFAULT = GraphViewport(-10.0, 10.0, -10.0, 10.0)
        val TRIG_DEFAULT = GraphViewport(-2.0 * Math.PI, 2.0 * Math.PI, -3.0, 3.0)
    }
}
