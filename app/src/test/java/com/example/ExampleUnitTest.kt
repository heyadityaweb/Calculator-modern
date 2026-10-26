package com.example

import com.example.math.ComplexNumber
import com.example.math.MathExpressionEvaluator
import com.example.math.MatrixEngine
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class ExampleUnitTest {

    private val evaluator = MathExpressionEvaluator()

    @Test
    fun testBasicArithmeticAndPrecedence() {
        val result = evaluator.evaluate("2 + 3 * 4")
        assertEquals(14.0, result, 1e-6)

        val resultParens = evaluator.evaluate("(2 + 3) * 4")
        assertEquals(20.0, resultParens, 1e-6)
    }

    @Test
    fun testImplicitMultiplication() {
        val res1 = evaluator.evaluate("2(3 + 4)")
        assertEquals(14.0, res1, 1e-6)

        val res2 = evaluator.evaluate("2x", x = 5.0)
        assertEquals(10.0, res2, 1e-6)

        val res3 = evaluator.evaluate("(x+1)(x-1)", x = 3.0)
        assertEquals(8.0, res3, 1e-6)
    }

    @Test
    fun testPowersAndAssociativity() {
        val res = evaluator.evaluate("2^3")
        assertEquals(8.0, res, 1e-6)

        val resPoly = evaluator.evaluate("x^2 - 4*x + 4", x = 2.0)
        assertEquals(0.0, resPoly, 1e-6)
    }

    @Test
    fun testTrigAndConstants() {
        val sinPi2 = evaluator.evaluate("sin(pi / 2)", isDegrees = false)
        assertEquals(1.0, sinPi2, 1e-6)

        val sin90Deg = evaluator.evaluate("sin(90)", isDegrees = true)
        assertEquals(1.0, sin90Deg, 1e-6)

        val exp0 = evaluator.evaluate("exp(0)")
        assertEquals(1.0, exp0, 1e-6)
    }

    @Test
    fun testNumericalDerivative() {
        // d/dx [ x^3 ] at x = 2 is 3*(2^2) = 12
        val deriv = evaluator.numericalDerivative("x^3", x = 2.0)
        assertEquals(12.0, deriv, 1e-3)
    }

    @Test
    fun testNumericalIntegrate() {
        // \int_0^\pi sin(x) dx = 2.0
        val integral = evaluator.numericalIntegrate("sin(x)", 0.0, Math.PI, isDegrees = false)
        assertEquals(2.0, integral, 1e-3)
    }

    @Test
    fun testComplexNumbers() {
        val z1 = ComplexNumber(3.0, 4.0)
        assertEquals(5.0, z1.magnitude, 1e-6)

        val z2 = ComplexNumber(1.0, -2.0)
        val sum = z1 + z2
        assertEquals(4.0, sum.real, 1e-6)
        assertEquals(2.0, sum.imag, 1e-6)

        // (3+4i)*(1-2i) = 3 - 6i + 4i - 8i^2 = 3 - 2i + 8 = 11 - 2i
        val prod = z1 * z2
        assertEquals(11.0, prod.real, 1e-6)
        assertEquals(-2.0, prod.imag, 1e-6)
    }

    @Test
    fun testMatrix2x2() {
        val m = MatrixEngine.Matrix2x2(2.0, 1.0, 1.0, 2.0)
        assertEquals(3.0, m.determinant, 1e-6)
        assertEquals(4.0, m.trace, 1e-6)

        val inv = m.inverse()
        assertNotNull(inv)
        val identity = m * inv!!
        assertEquals(1.0, identity.a11, 1e-6)
        assertEquals(0.0, identity.a12, 1e-6)
        assertEquals(0.0, identity.a21, 1e-6)
        assertEquals(1.0, identity.a22, 1e-6)

        val eig = m.eigenvalues()
        assertNotNull(eig)
        assertEquals(3.0, eig!!.first, 1e-6)
        assertEquals(1.0, eig.second, 1e-6)
    }
}
