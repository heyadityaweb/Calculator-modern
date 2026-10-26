package com.example.math

import kotlin.math.*

/**
 * Linear algebra engine for university and high school mathematics.
 * Handles 2x2 and 3x3 matrices with determinant, inverse, multiplication,
 * transpose, trace, and eigenvalue decomposition.
 */
object MatrixEngine {

    data class Matrix2x2(
        val a11: Double, val a12: Double,
        val a21: Double, val a22: Double
    ) {
        val determinant: Double
            get() = a11 * a22 - a12 * a21

        val trace: Double
            get() = a11 + a22

        val isInvertible: Boolean
            get() = abs(determinant) > 1e-12

        fun transpose(): Matrix2x2 = Matrix2x2(a11, a21, a12, a22)

        fun inverse(): Matrix2x2? {
            val det = determinant
            if (abs(det) < 1e-12) return null
            val invDet = 1.0 / det
            return Matrix2x2(
                a22 * invDet, -a12 * invDet,
                -a21 * invDet, a11 * invDet
            )
        }

        operator fun plus(other: Matrix2x2): Matrix2x2 =
            Matrix2x2(
                a11 + other.a11, a12 + other.a12,
                a21 + other.a21, a22 + other.a22
            )

        operator fun minus(other: Matrix2x2): Matrix2x2 =
            Matrix2x2(
                a11 - other.a11, a12 - other.a12,
                a21 - other.a21, a22 - other.a22
            )

        operator fun times(scalar: Double): Matrix2x2 =
            Matrix2x2(a11 * scalar, a12 * scalar, a21 * scalar, a22 * scalar)

        operator fun times(other: Matrix2x2): Matrix2x2 =
            Matrix2x2(
                a11 * other.a11 + a12 * other.a21, a11 * other.a12 + a12 * other.a22,
                a21 * other.a11 + a22 * other.a21, a21 * other.a12 + a22 * other.a22
            )

        fun eigenvalues(): Pair<Double, Double>? {
            val tr = trace
            val det = determinant
            val discriminant = tr * tr - 4 * det
            if (discriminant < 0) return null // Complex eigenvalues
            val sqrtD = sqrt(discriminant)
            return Pair((tr + sqrtD) / 2.0, (tr - sqrtD) / 2.0)
        }
    }

    data class Matrix3x3(
        val a11: Double, val a12: Double, val a13: Double,
        val a21: Double, val a22: Double, val a23: Double,
        val a31: Double, val a32: Double, val a33: Double
    ) {
        val determinant: Double
            get() = a11 * (a22 * a33 - a23 * a32) -
                    a12 * (a21 * a33 - a23 * a31) +
                    a13 * (a21 * a32 - a22 * a31)

        val trace: Double
            get() = a11 + a22 + a33

        val isInvertible: Boolean
            get() = abs(determinant) > 1e-12

        fun transpose(): Matrix3x3 = Matrix3x3(
            a11, a21, a31,
            a12, a22, a32,
            a13, a23, a33
        )

        fun inverse(): Matrix3x3? {
            val det = determinant
            if (abs(det) < 1e-12) return null
            val invDet = 1.0 / det

            // Matrix of cofactors transposed (adjugate)
            val c11 = (a22 * a33 - a23 * a32) * invDet
            val c12 = -(a12 * a33 - a13 * a32) * invDet
            val c13 = (a12 * a23 - a13 * a22) * invDet

            val c21 = -(a21 * a33 - a23 * a31) * invDet
            val c22 = (a11 * a33 - a13 * a31) * invDet
            val c23 = -(a11 * a23 - a13 * a21) * invDet

            val c31 = (a21 * a32 - a22 * a31) * invDet
            val c32 = -(a11 * a32 - a12 * a31) * invDet
            val c33 = (a11 * a22 - a12 * a21) * invDet

            return Matrix3x3(
                c11, c12, c13,
                c21, c22, c23,
                c31, c32, c33
            )
        }

        operator fun times(other: Matrix3x3): Matrix3x3 =
            Matrix3x3(
                a11 * other.a11 + a12 * other.a21 + a13 * other.a31,
                a11 * other.a12 + a12 * other.a22 + a13 * other.a32,
                a11 * other.a13 + a12 * other.a23 + a13 * other.a33,

                a21 * other.a11 + a22 * other.a21 + a23 * other.a31,
                a21 * other.a12 + a22 * other.a22 + a23 * other.a32,
                a21 * other.a13 + a22 * other.a23 + a23 * other.a33,

                a31 * other.a11 + a32 * other.a21 + a33 * other.a31,
                a31 * other.a12 + a32 * other.a22 + a33 * other.a32,
                a31 * other.a13 + a32 * other.a23 + a33 * other.a33
            )

        operator fun times(scalar: Double): Matrix3x3 =
            Matrix3x3(
                a11 * scalar, a12 * scalar, a13 * scalar,
                a21 * scalar, a22 * scalar, a23 * scalar,
                a31 * scalar, a32 * scalar, a33 * scalar
            )
    }
}
