package com.example.graph

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.example.math.MathExpressionEvaluator
import kotlin.math.*

@Composable
fun GraphCanvas(
    viewport: GraphViewport,
    equations: List<GraphEquation>,
    criticalPoints: List<CriticalPoint>,
    evaluator: MathExpressionEvaluator,
    isDegrees: Boolean,
    showGrid: Boolean = true,
    showRoots: Boolean = true,
    showExtrema: Boolean = true,
    showIntersections: Boolean = true,
    showTangent: Boolean = false,
    tangentLine: TangentLine? = null,
    integralShading: IntegralShading? = null,
    tracePoint: Point2D? = null,
    onViewportChange: (GraphViewport) -> Unit,
    onTracePointChange: (Point2D?) -> Unit,
    onPointSelected: (CriticalPoint) -> Unit,
    modifier: Modifier = Modifier
) {
    var sizePx by remember { mutableStateOf(Pair(1000f, 1000f)) }

    // Text paint for axis numbers
    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.argb(190, 160, 175, 200)
            textSize = 28f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }

    val tooltipPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 32f
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .pointerInput(viewport) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val centerMathX = viewport.toMathX(centroid.x, sizePx.first)
                    val centerMathY = viewport.toMathY(centroid.y, sizePx.second)

                    var newVp = viewport
                    if (zoom != 1.0f) {
                        newVp = newVp.zoom(zoom.toDouble(), centerMathX, centerMathY)
                    }
                    if (pan.x != 0f || pan.y != 0f) {
                        newVp = newVp.pan(pan.x, pan.y, sizePx.first, sizePx.second)
                    }
                    onViewportChange(newVp)
                }
            }
            .pointerInput(viewport, equations) {
                detectTapGestures(
                    onTap = { offset ->
                        val mathX = viewport.toMathX(offset.x, sizePx.first)
                        val mathY = viewport.toMathY(offset.y, sizePx.second)

                        // Check if tapped near any critical point
                        val tappedPoint = criticalPoints.firstOrNull { cp ->
                            val sx = viewport.toScreenX(cp.x, sizePx.first)
                            val sy = viewport.toScreenY(cp.y, sizePx.second)
                            hypot(offset.x - sx, offset.y - sy) < 40f
                        }

                        if (tappedPoint != null) {
                            onPointSelected(tappedPoint)
                            onTracePointChange(Point2D(tappedPoint.x, tappedPoint.y))
                        } else {
                            // Find nearest y on active visible equation
                            val activeEq = equations.firstOrNull { it.isVisible && it.expression.isNotBlank() }
                            if (activeEq != null) {
                                try {
                                    val y = evaluator.evaluate(activeEq.expression, mathX, isDegrees)
                                    if (!y.isNaN() && !y.isInfinite()) {
                                        onTracePointChange(Point2D(mathX, y))
                                    } else {
                                        onTracePointChange(null)
                                    }
                                } catch (_: Exception) {
                                    onTracePointChange(null)
                                }
                            } else {
                                onTracePointChange(null)
                            }
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            if (width > 0f && height > 0f) {
                sizePx = Pair(width, height)
            }

            // 1. Draw Grid Lines and Axes
            if (showGrid) {
                drawGridAndAxes(
                    viewport = viewport,
                    width = width,
                    height = height,
                    textPaint = textPaint
                )
            }

            // 2. Draw Shaded Definite Integral Area if enabled
            if (integralShading != null && integralShading.isEnabled) {
                val targetEq = equations.firstOrNull { it.id == integralShading.equationId && it.isVisible }
                if (targetEq != null) {
                    drawIntegralShading(
                        equation = targetEq,
                        a = integralShading.a,
                        b = integralShading.b,
                        viewport = viewport,
                        evaluator = evaluator,
                        isDegrees = isDegrees,
                        width = width,
                        height = height
                    )
                }
            }

            // 3. Draw All Visible Functions
            equations.filter { it.isVisible && it.expression.isNotBlank() }.forEach { eq ->
                drawFunctionCurve(
                    equation = eq,
                    viewport = viewport,
                    evaluator = evaluator,
                    isDegrees = isDegrees,
                    width = width,
                    height = height
                )
            }

            // 4. Draw Tangent Line if enabled
            if (showTangent && tangentLine != null) {
                val x1Math = viewport.xMin
                val y1Math = tangentLine.slope * (x1Math - tangentLine.x0) + tangentLine.y0
                val x2Math = viewport.xMax
                val y2Math = tangentLine.slope * (x2Math - tangentLine.x0) + tangentLine.y0

                val p1 = Offset(viewport.toScreenX(x1Math, width), viewport.toScreenY(y1Math, height))
                val p2 = Offset(viewport.toScreenX(x2Math, width), viewport.toScreenY(y2Math, height))

                drawLine(
                    color = Color(0xFFF59E0B),
                    start = p1,
                    end = p2,
                    strokeWidth = 3f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 10f))
                )

                // Point of tangency
                val ptX = viewport.toScreenX(tangentLine.x0, width)
                val ptY = viewport.toScreenY(tangentLine.y0, height)
                drawCircle(Color(0xFFF59E0B), radius = 6f, center = Offset(ptX, ptY))
            }

            // 5. Draw Critical Points (Roots, Extrema, Intersections)
            criticalPoints.forEach { cp ->
                val shouldDraw = when (cp.type) {
                    CriticalPointType.ROOT -> showRoots
                    CriticalPointType.LOCAL_MIN, CriticalPointType.LOCAL_MAX -> showExtrema
                    CriticalPointType.INTERSECTION -> showIntersections
                    CriticalPointType.Y_INTERCEPT -> true
                }
                if (shouldDraw) {
                    val sx = viewport.toScreenX(cp.x, width)
                    val sy = viewport.toScreenY(cp.y, height)

                    if (sx in 0f..width && sy in 0f..height) {
                        val color = when (cp.type) {
                            CriticalPointType.ROOT -> Color(0xFF10B981) // Green
                            CriticalPointType.Y_INTERCEPT -> Color(0xFF0284C7) // Blue
                            CriticalPointType.LOCAL_MAX -> Color(0xFFF43F5E) // Red/Rose
                            CriticalPointType.LOCAL_MIN -> Color(0xFFF97316) // Orange
                            CriticalPointType.INTERSECTION -> Color(0xFFA855F7) // Purple
                        }

                        // Outer halo
                        drawCircle(color.copy(alpha = 0.3f), radius = 11f, center = Offset(sx, sy))
                        // Inner solid dot
                        drawCircle(color, radius = 6f, center = Offset(sx, sy))
                        // Border ring
                        drawCircle(Color.White, radius = 6f, center = Offset(sx, sy), style = Stroke(width = 1.5f))
                    }
                }
            }

            // 6. Draw Interactive Trace Scrubber Point & Tooltip
            if (tracePoint != null) {
                val sx = viewport.toScreenX(tracePoint.x, width)
                val sy = viewport.toScreenY(tracePoint.y, height)

                if (sx in 0f..width && sy in 0f..height) {
                    // Pulsing tracer dot
                    drawCircle(Color(0xFF38BDF8).copy(alpha = 0.35f), radius = 14f, center = Offset(sx, sy))
                    drawCircle(Color(0xFF38BDF8), radius = 7f, center = Offset(sx, sy))
                    drawCircle(Color.White, radius = 7f, center = Offset(sx, sy), style = Stroke(width = 2f))

                    // Draw floating coordinate pill
                    val coordText = "(${roundTo(tracePoint.x, 3)}, ${roundTo(tracePoint.y, 3)})"
                    val pillY = if (sy > 80f) sy - 30f else sy + 50f

                    drawContext.canvas.nativeCanvas.apply {
                        // Background pill
                        val textWidth = tooltipPaint.measureText(coordText)
                        val padX = 20f
                        val padY = 16f
                        val rectPaint = Paint().apply {
                            color = android.graphics.Color.argb(220, 15, 23, 42)
                            style = Paint.Style.FILL
                        }
                        val strokePaint = Paint().apply {
                            color = android.graphics.Color.argb(200, 56, 189, 248)
                            style = Paint.Style.STROKE
                            strokeWidth = 2f
                        }
                        val left = sx - textWidth / 2f - padX
                        val top = pillY - padY - 24f
                        val right = sx + textWidth / 2f + padX
                        val bottom = pillY + padY

                        drawRoundRect(left, top, right, bottom, 16f, 16f, rectPaint)
                        drawRoundRect(left, top, right, bottom, 16f, 16f, strokePaint)
                        drawText(coordText, sx, pillY - 4f, tooltipPaint)
                    }
                }
            }
        }
    }
}

data class Point2D(val x: Double, val y: Double)

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGridAndAxes(
    viewport: GraphViewport,
    width: Float,
    height: Float,
    textPaint: Paint
) {
    val step = calculateGridStep(viewport.spanX)
    val minorStep = step / 5.0

    // Minor Grid Lines
    var xMinor = ceil(viewport.xMin / minorStep) * minorStep
    while (xMinor <= viewport.xMax) {
        val sx = viewport.toScreenX(xMinor, width)
        drawLine(
            color = Color(0xFF131D2E),
            start = Offset(sx, 0f),
            end = Offset(sx, height),
            strokeWidth = 1f
        )
        xMinor += minorStep
    }

    var yMinor = ceil(viewport.yMin / minorStep) * minorStep
    while (yMinor <= viewport.yMax) {
        val sy = viewport.toScreenY(yMinor, height)
        drawLine(
            color = Color(0xFF131D2E),
            start = Offset(0f, sy),
            end = Offset(width, sy),
            strokeWidth = 1f
        )
        yMinor += minorStep
    }

    // Major Grid Lines & Numbers
    var xMajor = ceil(viewport.xMin / step) * step
    while (xMajor <= viewport.xMax) {
        val sx = viewport.toScreenX(xMajor, width)
        drawLine(
            color = Color(0xFF1E293B),
            start = Offset(sx, 0f),
            end = Offset(sx, height),
            strokeWidth = 1.5f
        )

        // Draw X-axis label
        if (abs(xMajor) > 1e-9) {
            val labelY = (viewport.toScreenY(0.0, height) + 36f).coerceIn(40f, height - 20f)
            val numStr = formatNiceNumber(xMajor)
            drawContext.canvas.nativeCanvas.drawText(numStr, sx, labelY, textPaint)
        }
        xMajor += step
    }

    var yMajor = ceil(viewport.yMin / step) * step
    while (yMajor <= viewport.yMax) {
        val sy = viewport.toScreenY(yMajor, height)
        drawLine(
            color = Color(0xFF1E293B),
            start = Offset(0f, sy),
            end = Offset(width, sy),
            strokeWidth = 1.5f
        )

        // Draw Y-axis label
        if (abs(yMajor) > 1e-9) {
            val labelX = (viewport.toScreenX(0.0, width) + 30f).coerceIn(40f, width - 40f)
            val numStr = formatNiceNumber(yMajor)
            drawContext.canvas.nativeCanvas.drawText(numStr, labelX, sy + 10f, textPaint)
        }
        yMajor += step
    }

    // Primary X-Axis (y = 0)
    val originY = viewport.toScreenY(0.0, height)
    if (originY in 0f..height) {
        drawLine(
            color = Color(0xFF475569),
            start = Offset(0f, originY),
            end = Offset(width, originY),
            strokeWidth = 2.5f
        )
    }

    // Primary Y-Axis (x = 0)
    val originX = viewport.toScreenX(0.0, width)
    if (originX in 0f..width) {
        drawLine(
            color = Color(0xFF475569),
            start = Offset(originX, 0f),
            end = Offset(originX, height),
            strokeWidth = 2.5f
        )
    }

    // Origin Label (0,0)
    if (originX in 0f..width && originY in 0f..height) {
        drawContext.canvas.nativeCanvas.drawText("0", originX - 16f, originY + 28f, textPaint)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFunctionCurve(
    equation: GraphEquation,
    viewport: GraphViewport,
    evaluator: MathExpressionEvaluator,
    isDegrees: Boolean,
    width: Float,
    height: Float
) {
    val node = try {
        evaluator.parse(equation.expression)
    } catch (_: Exception) {
        return
    }

    val path = Path()
    var isPathOpen = false
    val pixelStep = 2 // Evaluate every 2 screen pixels for optimal performance and smooth rendering
    var lastYScreen = 0f

    for (pixelX in 0..width.toInt() step pixelStep) {
        val mathX = viewport.toMathX(pixelX.toFloat(), width)
        val mathY = try {
            evaluator.evaluateNode(node, mathX, isDegrees, 0.0)
        } catch (_: Exception) {
            Double.NaN
        }

        if (mathY.isNaN() || mathY.isInfinite()) {
            isPathOpen = false
            continue
        }

        val screenY = viewport.toScreenY(mathY, height)

        // Asymptote discontinuity check (e.g. tan(x), 1/x)
        if (isPathOpen && abs(screenY - lastYScreen) > height * 1.5f) {
            isPathOpen = false
        }

        if (!isPathOpen) {
            path.moveTo(pixelX.toFloat(), screenY)
            isPathOpen = true
        } else {
            path.lineTo(pixelX.toFloat(), screenY)
        }
        lastYScreen = screenY
    }

    drawPath(
        path = path,
        color = equation.color,
        style = Stroke(width = 3.5f)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawIntegralShading(
    equation: GraphEquation,
    a: Double,
    b: Double,
    viewport: GraphViewport,
    evaluator: MathExpressionEvaluator,
    isDegrees: Boolean,
    width: Float,
    height: Float
) {
    val node = try { evaluator.parse(equation.expression) } catch (_: Exception) { return }

    val startX = min(a, b)
    val endX = max(a, b)
    val startPx = viewport.toScreenX(startX, width).coerceIn(0f, width)
    val endPx = viewport.toScreenX(endX, width).coerceIn(0f, width)

    if (startPx >= endPx) return

    val path = Path()
    val axisY = viewport.toScreenY(0.0, height)
    path.moveTo(startPx, axisY)

    var px = startPx
    while (px <= endPx) {
        val mathX = viewport.toMathX(px, width)
        val mathY = try { evaluator.evaluateNode(node, mathX, isDegrees, 0.0) } catch (_: Exception) { 0.0 }
        val screenY = if (mathY.isNaN() || mathY.isInfinite()) axisY else viewport.toScreenY(mathY, height)
        path.lineTo(px, screenY)
        px += 3f
    }
    path.lineTo(endPx, axisY)
    path.close()

    drawPath(
        path = path,
        color = equation.color.copy(alpha = 0.25f)
    )
}

private fun calculateGridStep(span: Double): Double {
    val targetSteps = 6.0
    val rawStep = span / targetSteps
    val magnitude = 10.0.pow(floor(log10(rawStep)))
    val normalized = rawStep / magnitude

    val stepMultiplier = when {
        normalized < 1.5 -> 1.0
        normalized < 3.5 -> 2.0
        normalized < 7.5 -> 5.0
        else -> 10.0
    }
    return stepMultiplier * magnitude
}

private fun formatNiceNumber(v: Double): String {
    val rounded = roundTo(v, 4)
    return if (rounded == floor(rounded)) rounded.toLong().toString() else rounded.toString()
}

private fun roundTo(v: Double, places: Int): Double {
    if (v.isNaN() || v.isInfinite()) return v
    val factor = 10.0.pow(places)
    return (v * factor).roundToLong() / factor
}
