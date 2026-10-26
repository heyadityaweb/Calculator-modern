package com.example.math

import kotlin.math.*

/**
 * Represents a Complex Number z = a + bi with comprehensive arithmetic,
 * polar form conversion, Euler form, roots, powers, and formatting.
 */
data class ComplexNumber(
    val real: Double,
    val imag: Double
) {
    val magnitude: Double
        get() = hypot(real, imag)

    val argument: Double
        get() = atan2(imag, real)

    val argumentDegrees: Double
        get() = Math.toDegrees(argument)

    operator fun plus(other: ComplexNumber): ComplexNumber =
        ComplexNumber(real + other.real, imag + other.imag)

    operator fun minus(other: ComplexNumber): ComplexNumber =
        ComplexNumber(real - other.real, imag - other.imag)

    operator fun times(other: ComplexNumber): ComplexNumber =
        ComplexNumber(
            real * other.real - imag * other.imag,
            real * other.imag + imag * other.real
        )

    operator fun times(scalar: Double): ComplexNumber =
        ComplexNumber(real * scalar, imag * scalar)

    operator fun div(other: ComplexNumber): ComplexNumber {
        val denom = other.real * other.real + other.imag * other.imag
        if (denom == 0.0) return ComplexNumber(Double.NaN, Double.NaN)
        return ComplexNumber(
            (real * other.real + imag * other.imag) / denom,
            (imag * other.real - real * other.imag) / denom
        )
    }

    fun conjugate(): ComplexNumber = ComplexNumber(real, -imag)

    fun pow(power: Double): ComplexNumber {
        val r = magnitude.pow(power)
        val theta = argument * power
        return ComplexNumber(r * cos(theta), r * sin(theta))
    }

    fun sqrt(): ComplexNumber = pow(0.5)

    fun toStandardString(precision: Int = 4): String {
        val rStr = formatDouble(real, precision)
        val iStr = formatDouble(abs(imag), precision)

        return when {
            imag == 0.0 -> rStr
            real == 0.0 -> if (imag == 1.0) "i" else if (imag == -1.0) "-i" else "${formatDouble(imag, precision)}i"
            imag > 0 -> "$rStr + ${if (imag == 1.0) "" else iStr}i"
            else -> "$rStr - ${if (imag == -1.0) "" else iStr}i"
        }
    }

    fun toPolarString(isDegrees: Boolean = false, precision: Int = 4): String {
        val rStr = formatDouble(magnitude, precision)
        val angleStr = if (isDegrees) {
            "${formatDouble(argumentDegrees, precision)}°"
        } else {
            "${formatDouble(argument, precision)} rad"
        }
        return "$rStr ∠ $angleStr (or $rStr · e^(${if (isDegrees) "${formatDouble(argumentDegrees, precision)}°" else "${formatDouble(argument, precision)} rad"} · i))"
    }

    companion object {
        val ZERO = ComplexNumber(0.0, 0.0)
        val ONE = ComplexNumber(1.0, 0.0)
        val I = ComplexNumber(0.0, 1.0)

        fun fromPolar(r: Double, theta: Double, isDegrees: Boolean = false): ComplexNumber {
            val rad = if (isDegrees) Math.toRadians(theta) else theta
            return ComplexNumber(r * cos(rad), r * sin(rad))
        }

        private fun formatDouble(v: Double, precision: Int): String {
            if (v.isNaN()) return "NaN"
            if (v.isInfinite()) return if (v > 0) "∞" else "-∞"
            val rounded = (v * 10.0.pow(precision)).roundToLong() / 10.0.pow(precision)
            return if (rounded == floor(rounded)) rounded.toLong().toString() else rounded.toString()
        }
    }
}
