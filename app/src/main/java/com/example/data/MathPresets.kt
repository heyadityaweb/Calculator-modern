package com.example.data

import androidx.compose.ui.graphics.Color

data class MathPreset(
    val title: String,
    val category: String,
    val level: String, // "High School" or "University"
    val description: String,
    val formula: String,
    val suggestedXRange: Pair<Double, Double> = Pair(-10.0, 10.0),
    val suggestedYRange: Pair<Double, Double> = Pair(-10.0, 10.0),
    val accentColor: Color = Color(0xFF38BDF8)
)

object MathPresetsRepository {
    val PRESETS = listOf(
        // High School - Algebra
        MathPreset(
            title = "Cubic Polynomial (3 Real Roots)",
            category = "Algebra",
            level = "High School",
            description = "Classic polynomial f(x) = (x+3)(x-1)(x-3)/4 illustrating roots, local maximum, and local minimum.",
            formula = "0.25*(x+3)*(x-1)*(x-3)",
            suggestedXRange = Pair(-5.0, 5.0),
            suggestedYRange = Pair(-6.0, 6.0),
            accentColor = Color(0xFF38BDF8)
        ),
        MathPreset(
            title = "Quadratic Parabola",
            category = "Algebra",
            level = "High School",
            description = "Standard parabola f(x) = x² - 4x + 3 with vertex at (2, -1) and roots at x = 1, 3.",
            formula = "x^2 - 4*x + 3",
            suggestedXRange = Pair(-2.0, 6.0),
            suggestedYRange = Pair(-3.0, 8.0),
            accentColor = Color(0xFF10B981)
        ),
        MathPreset(
            title = "Rational Function & Asymptotes",
            category = "Algebra",
            level = "High School",
            description = "f(x) = (x² - 1) / (x² + 1) with horizontal asymptote at y = 1 and minimum at (0, -1).",
            formula = "(x^2 - 1) / (x^2 + 1)",
            suggestedXRange = Pair(-5.0, 5.0),
            suggestedYRange = Pair(-2.0, 3.0),
            accentColor = Color(0xFFF59E0B)
        ),
        MathPreset(
            title = "Semi-Circle",
            category = "Geometry",
            level = "High School",
            description = "Upper half of radius 4 circle: y = √(16 - x²).",
            formula = "sqrt(16 - x^2)",
            suggestedXRange = Pair(-6.0, 6.0),
            suggestedYRange = Pair(-1.0, 6.0),
            accentColor = Color(0xFFA855F7)
        ),

        // High School - Trigonometry
        MathPreset(
            title = "Sine Function",
            category = "Trigonometry",
            level = "High School",
            description = "Fundamental periodic wave f(x) = sin(x) with period 2π and amplitude 1.",
            formula = "sin(x)",
            suggestedXRange = Pair(-7.0, 7.0),
            suggestedYRange = Pair(-2.0, 2.0),
            accentColor = Color(0xFF38BDF8)
        ),
        MathPreset(
            title = "Wave Beats & Superposition",
            category = "Trigonometry",
            level = "High School",
            description = "Acoustic beat frequencies from two slightly detuned oscillators f(x) = sin(x) + sin(1.2x).",
            formula = "sin(x) + sin(1.2*x)",
            suggestedXRange = Pair(-25.0, 25.0),
            suggestedYRange = Pair(-3.0, 3.0),
            accentColor = Color(0xFFEC4899)
        ),
        MathPreset(
            title = "Amplitude Modulation (AM)",
            category = "Trigonometry",
            level = "High School",
            description = "Carrier wave modulated by an envelope: f(x) = sin(x) · sin(8x).",
            formula = "sin(x) * sin(8*x)",
            suggestedXRange = Pair(-10.0, 10.0),
            suggestedYRange = Pair(-2.0, 2.0),
            accentColor = Color(0xFF8B5CF6)
        ),

        // University - Calculus
        MathPreset(
            title = "Damped Harmonic Oscillator",
            category = "Calculus & ODEs",
            level = "University",
            description = "Mechanical & electrical vibrations with exponential damping: f(x) = e^(-0.25x) · cos(3x).",
            formula = "exp(-0.25*x) * cos(3*x)",
            suggestedXRange = Pair(0.0, 15.0),
            suggestedYRange = Pair(-1.5, 1.5),
            accentColor = Color(0xFF06B6D4)
        ),
        MathPreset(
            title = "Gaussian Bell Curve (Normal Dist)",
            category = "Statistics & Calculus",
            level = "University",
            description = "Probability density function of normal distribution with μ = 0, σ = 1: f(x) = (1/√(2π)) · e^(-x²/2).",
            formula = "(1/sqrt(2*pi)) * exp(-(x^2)/2)",
            suggestedXRange = Pair(-4.0, 4.0),
            suggestedYRange = Pair(-0.1, 0.6),
            accentColor = Color(0xFF10B981)
        ),
        MathPreset(
            title = "Logistic Growth / Sigmoid",
            category = "Calculus & Biology",
            level = "University",
            description = "Population dynamics and neural network activation function: f(x) = 1 / (1 + e^(-x)).",
            formula = "1 / (1 + exp(-x))",
            suggestedXRange = Pair(-6.0, 6.0),
            suggestedYRange = Pair(-0.2, 1.2),
            accentColor = Color(0xFFF97316)
        ),
        MathPreset(
            title = "Taylor Series Approximation of sin(x)",
            category = "Calculus & Analysis",
            level = "University",
            description = "5th order Maclaurin polynomial f(x) = x - x³/6 + x⁵/120 approximating sin(x) near origin.",
            formula = "x - (x^3)/6 + (x^5)/120",
            suggestedXRange = Pair(-4.0, 4.0),
            suggestedYRange = Pair(-3.0, 3.0),
            accentColor = Color(0xFF6366F1)
        ),
        MathPreset(
            title = "Runge's Phenomenon",
            category = "Numerical Analysis",
            level = "University",
            description = "Classic demonstration of oscillation problem in high-degree polynomial interpolation: f(x) = 1 / (1 + 25x²).",
            formula = "1 / (1 + 25*x^2)",
            suggestedXRange = Pair(-2.0, 2.0),
            suggestedYRange = Pair(-0.2, 1.2),
            accentColor = Color(0xFFEF4444)
        ),

        // University - Physics & Signal Processing
        MathPreset(
            title = "Normalized Sinc Function",
            category = "Signal Processing",
            level = "University",
            description = "Fourier transform of rectangular pulse: f(x) = sin(πx)/(πx). Essential in Nyquist-Shannon sampling.",
            formula = "sin(pi*x) / (pi*x)",
            suggestedXRange = Pair(-6.0, 6.0),
            suggestedYRange = Pair(-0.4, 1.2),
            accentColor = Color(0xFF14B8A6)
        ),
        MathPreset(
            title = "RLC Bandpass Resonance",
            category = "Electrical Engineering",
            level = "University",
            description = "Frequency response magnitude of resonant RLC circuit with peak at ω₀ = 1: Q-factor resonance.",
            formula = "1 / sqrt((1 - x^2)^2 + (0.25*x)^2)",
            suggestedXRange = Pair(0.0, 3.0),
            suggestedYRange = Pair(0.0, 5.0),
            accentColor = Color(0xFFEAB308)
        )
    )
}
