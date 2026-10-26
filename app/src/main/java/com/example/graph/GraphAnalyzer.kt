package com.example.graph

import com.example.math.MathExpressionEvaluator
import java.util.UUID
import kotlin.math.*

object GraphAnalyzer {

    fun findCriticalPoints(
        equation: GraphEquation,
        viewport: GraphViewport,
        evaluator: MathExpressionEvaluator,
        isDegrees: Boolean = false
    ): List<CriticalPoint> {
        val points = mutableListOf<CriticalPoint>()
        if (equation.expression.isBlank()) return points

        val parsedNode = try {
            evaluator.parse(equation.expression)
        } catch (_: Exception) {
            return points
        }

        fun eval(x: Double): Double {
            return try {
                evaluator.evaluateNode(parsedNode, x, isDegrees, 0.0)
            } catch (_: Exception) {
                Double.NaN
            }
        }

        fun deriv(x: Double, h: Double = 1e-5): Double {
            val y1 = eval(x + h)
            val y0 = eval(x - h)
            return if (y1.isNaN() || y0.isNaN()) Double.NaN else (y1 - y0) / (2 * h)
        }

        // 1. Y-intercept (x = 0)
        if (0.0 >= viewport.xMin && 0.0 <= viewport.xMax) {
            val yAt0 = eval(0.0)
            if (!yAt0.isNaN() && !yAt0.isInfinite() && yAt0 >= viewport.yMin && yAt0 <= viewport.yMax) {
                points.add(
                    CriticalPoint(
                        id = UUID.randomUUID().toString(),
                        type = CriticalPointType.Y_INTERCEPT,
                        x = 0.0,
                        y = roundTo(yAt0, 4),
                        equationLabel = equation.label,
                        description = "y-intercept: (0, ${roundTo(yAt0, 3)})"
                    )
                )
            }
        }

        // 2. Sampling across viewport for Roots and Extrema
        val samples = 300
        val step = viewport.spanX / samples
        var prevX = viewport.xMin
        var prevY = eval(prevX)
        var prevD = deriv(prevX)

        for (i in 1..samples) {
            val currX = viewport.xMin + i * step
            val currY = eval(currX)
            val currD = deriv(currX)

            if (!prevY.isNaN() && !currY.isNaN() && !prevY.isInfinite() && !currY.isInfinite()) {
                // Check Root: sign change in y
                if ((prevY <= 0.0 && currY >= 0.0) || (prevY >= 0.0 && currY <= 0.0)) {
                    // Refine root via bisection
                    var low = prevX
                    var high = currX
                    var rootX = (low + high) / 2.0
                    for (b in 0 until 16) {
                        val mid = (low + high) / 2.0
                        val midY = eval(mid)
                        if (abs(midY) < 1e-7) {
                            rootX = mid
                            break
                        }
                        if ((eval(low) <= 0 && midY >= 0) || (eval(low) >= 0 && midY <= 0)) {
                            high = mid
                        } else {
                            low = mid
                        }
                        rootX = mid
                    }
                    val rootY = eval(rootX)
                    if (abs(rootY) < 0.1 && points.none { it.type == CriticalPointType.ROOT && abs(it.x - rootX) < step * 0.8 }) {
                        points.add(
                            CriticalPoint(
                                id = UUID.randomUUID().toString(),
                                type = CriticalPointType.ROOT,
                                x = roundTo(rootX, 4),
                                y = 0.0,
                                equationLabel = equation.label,
                                description = "Root: x ≈ ${roundTo(rootX, 3)}"
                            )
                        )
                    }
                }

                // Check Local Extrema: sign change in derivative
                if (!prevD.isNaN() && !currD.isNaN() && !prevD.isInfinite() && !currD.isInfinite()) {
                    if ((prevD <= 0.0 && currD >= 0.0) || (prevD >= 0.0 && currD <= 0.0)) {
                        // Refine extremum
                        var low = prevX
                        var high = currX
                        var extX = (low + high) / 2.0
                        for (b in 0 until 12) {
                            val mid = (low + high) / 2.0
                            val midD = deriv(mid)
                            if (abs(midD) < 1e-6) {
                                extX = mid
                                break
                            }
                            if ((deriv(low) <= 0 && midD >= 0) || (deriv(low) >= 0 && midD <= 0)) {
                                high = mid
                            } else {
                                low = mid
                            }
                            extX = mid
                        }
                        val extY = eval(extX)
                        if (!extY.isNaN() && !extY.isInfinite() && extY >= viewport.yMin && extY <= viewport.yMax) {
                            val isMax = prevD > 0 && currD < 0
                            val type = if (isMax) CriticalPointType.LOCAL_MAX else CriticalPointType.LOCAL_MIN
                            if (points.none { it.type == type && abs(it.x - extX) < step * 0.8 }) {
                                points.add(
                                    CriticalPoint(
                                        id = UUID.randomUUID().toString(),
                                        type = type,
                                        x = roundTo(extX, 4),
                                        y = roundTo(extY, 4),
                                        equationLabel = equation.label,
                                        description = "${if (isMax) "Max" else "Min"}: (${roundTo(extX, 3)}, ${roundTo(extY, 3)})"
                                    )
                                )
                            }
                        }
                    }
                }
            }

            prevX = currX
            prevY = currY
            prevD = currD
        }

        return points
    }

    fun findIntersections(
        eq1: GraphEquation,
        eq2: GraphEquation,
        viewport: GraphViewport,
        evaluator: MathExpressionEvaluator,
        isDegrees: Boolean = false
    ): List<CriticalPoint> {
        val points = mutableListOf<CriticalPoint>()
        if (eq1.expression.isBlank() || eq2.expression.isBlank()) return points

        val node1 = try { evaluator.parse(eq1.expression) } catch (_: Exception) { return points }
        val node2 = try { evaluator.parse(eq2.expression) } catch (_: Exception) { return points }

        fun diff(x: Double): Double {
            val y1 = try { evaluator.evaluateNode(node1, x, isDegrees, 0.0) } catch (_: Exception) { Double.NaN }
            val y2 = try { evaluator.evaluateNode(node2, x, isDegrees, 0.0) } catch (_: Exception) { Double.NaN }
            return if (y1.isNaN() || y2.isNaN()) Double.NaN else y1 - y2
        }

        val samples = 200
        val step = viewport.spanX / samples
        var prevX = viewport.xMin
        var prevDiff = diff(prevX)

        for (i in 1..samples) {
            val currX = viewport.xMin + i * step
            val currDiff = diff(currX)

            if (!prevDiff.isNaN() && !currDiff.isNaN() && !prevDiff.isInfinite() && !currDiff.isInfinite()) {
                if ((prevDiff <= 0.0 && currDiff >= 0.0) || (prevDiff >= 0.0 && currDiff <= 0.0)) {
                    var low = prevX
                    var high = currX
                    var intX = (low + high) / 2.0
                    for (b in 0 until 14) {
                        val mid = (low + high) / 2.0
                        val midDiff = diff(mid)
                        if (abs(midDiff) < 1e-6) {
                            intX = mid
                            break
                        }
                        if ((diff(low) <= 0 && midDiff >= 0) || (diff(low) >= 0 && midDiff <= 0)) {
                            high = mid
                        } else {
                            low = mid
                        }
                        intX = mid
                    }
                    val intY = try { evaluator.evaluateNode(node1, intX, isDegrees, 0.0) } catch (_: Exception) { Double.NaN }
                    if (!intY.isNaN() && !intY.isInfinite() && intY >= viewport.yMin && intY <= viewport.yMax) {
                        if (points.none { abs(it.x - intX) < step * 0.8 }) {
                            points.add(
                                CriticalPoint(
                                    id = UUID.randomUUID().toString(),
                                    type = CriticalPointType.INTERSECTION,
                                    x = roundTo(intX, 4),
                                    y = roundTo(intY, 4),
                                    equationLabel = "${eq1.label} ∩ ${eq2.label}",
                                    description = "Intersection: (${roundTo(intX, 3)}, ${roundTo(intY, 3)})"
                                )
                            )
                        }
                    }
                }
            }

            prevX = currX
            prevDiff = currDiff
        }

        return points
    }

    fun computeTangent(
        equation: GraphEquation,
        x0: Double,
        evaluator: MathExpressionEvaluator,
        isDegrees: Boolean = false
    ): TangentLine? {
        return try {
            val ast = evaluator.parse(equation.expression)
            val y0 = evaluator.evaluateNode(ast, x0, isDegrees, 0.0)
            if (y0.isNaN() || y0.isInfinite()) return null
            val slope = evaluator.numericalDerivative(equation.expression, x0, isDegrees)
            if (slope.isNaN() || slope.isInfinite()) return null

            val c = y0 - slope * x0
            val slopeStr = roundTo(slope, 3).toString()
            val cStr = if (c >= 0) "+ ${roundTo(c, 3)}" else "- ${roundTo(abs(c), 3)}"
            val eqStr = "y = ${slopeStr}x $cStr"

            TangentLine(x0, y0, slope, eqStr)
        } catch (_: Exception) {
            null
        }
    }

    fun generateTable(
        equations: List<GraphEquation>,
        startX: Double,
        endX: Double,
        step: Double,
        evaluator: MathExpressionEvaluator,
        isDegrees: Boolean = false
    ): List<TableRowData> {
        val rows = mutableListOf<TableRowData>()
        val validStep = if (step <= 0.0) 1.0 else min(step, 10.0)
        val count = min(((endX - startX) / validStep).toInt() + 1, 100)

        val parsedAsts = equations.map { eq ->
            try { evaluator.parse(eq.expression) } catch (_: Exception) { null }
        }

        for (i in 0 until count) {
            val x = startX + i * validStep
            val map = mutableMapOf<String, Double?>()
            equations.forEachIndexed { idx, eq ->
                val ast = parsedAsts[idx]
                if (ast != null) {
                    val y = try { evaluator.evaluateNode(ast, x, isDegrees, 0.0) } catch (_: Exception) { null }
                    map[eq.label] = if (y != null && !y.isNaN() && !y.isInfinite()) roundTo(y, 4) else null
                } else {
                    map[eq.label] = null
                }
            }
            rows.add(TableRowData(x = roundTo(x, 4), values = map))
        }

        return rows
    }

    private fun roundTo(v: Double, places: Int): Double {
        if (v.isNaN() || v.isInfinite()) return v
        val factor = 10.0.pow(places)
        return (v * factor).roundToLong() / factor
    }
}
