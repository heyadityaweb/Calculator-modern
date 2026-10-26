package com.example.math

import kotlin.math.*

/**
 * High-performance, robust expression tokenizer and evaluator for scientific,
 * graphing, and calculus operations.
 * Supports implicit multiplication, standard functions, variable x, constants,
 * and high-order operators.
 */
class MathExpressionEvaluator {

    sealed class Token {
        data class Number(val value: Double) : Token()
        data class Identifier(val name: String) : Token()
        data class Operator(val symbol: Char, val precedence: Int, val rightAssociative: Boolean = false) : Token()
        object LeftParen : Token()
        object RightParen : Token()
        object Comma : Token()
    }

    sealed class Node {
        data class Number(val value: Double) : Node()
        data class Variable(val name: String) : Node()
        data class UnaryOp(val op: Char, val operand: Node) : Node()
        data class BinaryOp(val op: Char, val left: Node, val right: Node) : Node()
        data class FunctionCall(val name: String, val args: List<Node>) : Node()
    }

    /**
     * Parses an expression string into an AST Node.
     * Throws IllegalArgumentException on invalid syntax.
     */
    fun parse(expression: String): Node {
        val sanitized = sanitize(expression)
        val tokens = tokenize(sanitized)
        val rpn = shuntingYard(tokens)
        return buildAst(rpn)
    }

    /**
     * Evaluates the expression with a given variable x and degree/radian toggle.
     */
    fun evaluate(expression: String, x: Double = 0.0, isDegrees: Boolean = false, ans: Double = 0.0): Double {
        val ast = parse(expression)
        return evaluateNode(ast, x, isDegrees, ans)
    }

    /**
     * Evaluates numerical derivative f'(x) using symmetric difference quotient.
     */
    fun numericalDerivative(expression: String, x: Double, isDegrees: Boolean = false, h: Double = 1e-5): Double {
        val ast = parse(expression)
        val fPlus = evaluateNode(ast, x + h, isDegrees, 0.0)
        val fMinus = evaluateNode(ast, x - h, isDegrees, 0.0)
        return (fPlus - fMinus) / (2.0 * h)
    }

    /**
     * Evaluates numerical definite integral \int_a^b f(x) dx using Simpson's 1/3 Rule.
     */
    fun numericalIntegrate(expression: String, a: Double, b: Double, isDegrees: Boolean = false, nSubdivisions: Int = 100): Double {
        if (a == b) return 0.0
        val ast = parse(expression)
        val n = if (nSubdivisions % 2 != 0) nSubdivisions + 1 else nSubdivisions
        val h = (b - a) / n
        var sum = evaluateNode(ast, a, isDegrees, 0.0) + evaluateNode(ast, b, isDegrees, 0.0)

        for (i in 1 until n) {
            val xi = a + i * h
            val yi = evaluateNode(ast, xi, isDegrees, 0.0)
            if (yi.isNaN() || yi.isInfinite()) continue
            sum += if (i % 2 == 1) 4.0 * yi else 2.0 * yi
        }
        return (h / 3.0) * sum
    }

    fun evaluateNode(node: Node, x: Double, isDegrees: Boolean, ans: Double): Double {
        return when (node) {
            is Node.Number -> node.value
            is Node.Variable -> {
                when (node.name.lowercase()) {
                    "x" -> x
                    "pi", "π" -> Math.PI
                    "e" -> Math.E
                    "phi", "ϕ" -> 1.618033988749895
                    "ans" -> ans
                    else -> throw IllegalArgumentException("Unknown variable: ${node.name}")
                }
            }
            is Node.UnaryOp -> {
                val operand = evaluateNode(node.operand, x, isDegrees, ans)
                when (node.op) {
                    '-' -> -operand
                    '+' -> operand
                    '!' -> factorial(operand)
                    else -> throw IllegalArgumentException("Unknown unary operator: ${node.op}")
                }
            }
            is Node.BinaryOp -> {
                val left = evaluateNode(node.left, x, isDegrees, ans)
                val right = evaluateNode(node.right, x, isDegrees, ans)
                when (node.op) {
                    '+' -> left + right
                    '-' -> left - right
                    '*' -> left * right
                    '/' -> {
                        if (right == 0.0) Double.NaN else left / right
                    }
                    '%' -> left % right
                    '^' -> left.pow(right)
                    else -> throw IllegalArgumentException("Unknown binary operator: ${node.op}")
                }
            }
            is Node.FunctionCall -> {
                val evalArgs = node.args.map { evaluateNode(it, x, isDegrees, ans) }
                evaluateFunction(node.name.lowercase(), evalArgs, isDegrees)
            }
        }
    }

    private fun evaluateFunction(name: String, args: List<Double>, isDegrees: Boolean): Double {
        val a1 = args.getOrNull(0) ?: throw IllegalArgumentException("Missing argument for $name")
        return when (name) {
            "sin" -> {
                val rad = if (isDegrees) Math.toRadians(a1) else a1
                sin(rad)
            }
            "cos" -> {
                val rad = if (isDegrees) Math.toRadians(a1) else a1
                cos(rad)
            }
            "tan" -> {
                val rad = if (isDegrees) Math.toRadians(a1) else a1
                tan(rad)
            }
            "asin", "arcsin" -> {
                val res = asin(a1)
                if (isDegrees) Math.toDegrees(res) else res
            }
            "acos", "arccos" -> {
                val res = acos(a1)
                if (isDegrees) Math.toDegrees(res) else res
            }
            "atan", "arctan" -> {
                val res = atan(a1)
                if (isDegrees) Math.toDegrees(res) else res
            }
            "atan2" -> {
                val a2 = args.getOrNull(1) ?: throw IllegalArgumentException("atan2 requires 2 arguments")
                val res = atan2(a1, a2)
                if (isDegrees) Math.toDegrees(res) else res
            }
            "sinh" -> sinh(a1)
            "cosh" -> cosh(a1)
            "tanh" -> tanh(a1)
            "asinh" -> ln(a1 + sqrt(a1 * a1 + 1.0))
            "acosh" -> ln(a1 + sqrt(a1 * a1 - 1.0))
            "atanh" -> 0.5 * ln((1.0 + a1) / (1.0 - a1))
            "sqrt", "√" -> sqrt(a1)
            "cbrt" -> cbrt(a1)
            "ln" -> ln(a1)
            "log", "log10" -> log10(a1)
            "log2" -> log2(a1)
            "exp" -> exp(a1)
            "abs" -> abs(a1)
            "floor" -> floor(a1)
            "ceil" -> ceil(a1)
            "round" -> round(a1)
            "sign", "sgn" -> sign(a1)
            "rad" -> Math.toRadians(a1)
            "deg" -> Math.toDegrees(a1)
            "fact", "factorial" -> factorial(a1)
            "ncr", "comb" -> {
                val a2 = args.getOrNull(1) ?: throw IllegalArgumentException("nCr requires 2 arguments")
                nCr(a1, a2)
            }
            "npr", "perm" -> {
                val a2 = args.getOrNull(1) ?: throw IllegalArgumentException("nPr requires 2 arguments")
                nPr(a1, a2)
            }
            "mod" -> {
                val a2 = args.getOrNull(1) ?: throw IllegalArgumentException("mod requires 2 arguments")
                a1 % a2
            }
            "pow" -> {
                val a2 = args.getOrNull(1) ?: throw IllegalArgumentException("pow requires 2 arguments")
                a1.pow(a2)
            }
            else -> throw IllegalArgumentException("Unknown function: $name")
        }
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n)) return Double.NaN
        if (n > 170) return Double.POSITIVE_INFINITY
        var result = 1.0
        val intN = n.toLong()
        for (i in 2..intN) {
            result *= i
        }
        return result
    }

    private fun nCr(n: Double, r: Double): Double {
        if (r < 0 || r > n) return 0.0
        val k = min(r, n - r)
        var result = 1.0
        for (i in 1..k.toInt()) {
            result = result * (n - k + i) / i
        }
        return result
    }

    private fun nPr(n: Double, r: Double): Double {
        if (r < 0 || r > n) return 0.0
        var result = 1.0
        for (i in 0 until r.toInt()) {
            result *= (n - i)
        }
        return result
    }

    private fun sanitize(input: String): String {
        return input
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "pi")
            .replace("√", "sqrt")
            .replace("²", "^2")
            .replace("³", "^3")
            .trim()
    }

    private fun tokenize(text: String): List<Token> {
        val rawTokens = mutableListOf<Token>()
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || c == '.' -> {
                    val start = i
                    var hasDot = c == '.'
                    i++
                    while (i < text.length && (text[i].isDigit() || (!hasDot && text[i] == '.'))) {
                        if (text[i] == '.') hasDot = true
                        i++
                    }
                    // Handle scientific notation e.g. 1e-4 or 2.5E3
                    if (i < text.length && (text[i] == 'e' || text[i] == 'E')) {
                        val nextIdx = i + 1
                        if (nextIdx < text.length && (text[nextIdx].isDigit() || text[nextIdx] == '+' || text[nextIdx] == '-')) {
                            i++ // consume e/E
                            if (text[i] == '+' || text[i] == '-') i++
                            while (i < text.length && text[i].isDigit()) {
                                i++
                            }
                        }
                    }
                    val numStr = text.substring(start, i)
                    rawTokens.add(Token.Number(numStr.toDoubleOrNull() ?: 0.0))
                }
                c.isLetter() -> {
                    val start = i
                    while (i < text.length && (text[i].isLetterOrDigit() || text[i] == '_')) {
                        i++
                    }
                    rawTokens.add(Token.Identifier(text.substring(start, i)))
                }
                c == '(' -> {
                    rawTokens.add(Token.LeftParen)
                    i++
                }
                c == ')' -> {
                    rawTokens.add(Token.RightParen)
                    i++
                }
                c == ',' -> {
                    rawTokens.add(Token.Comma)
                    i++
                }
                c == '+' || c == '-' -> {
                    rawTokens.add(Token.Operator(c, precedence = 1))
                    i++
                }
                c == '*' || c == '/' || c == '%' -> {
                    rawTokens.add(Token.Operator(c, precedence = 2))
                    i++
                }
                c == '^' -> {
                    rawTokens.add(Token.Operator(c, precedence = 3, rightAssociative = true))
                    i++
                }
                c == '!' -> {
                    rawTokens.add(Token.Operator(c, precedence = 4))
                    i++
                }
                else -> i++
            }
        }

        // Insert implicit multiplication tokens where needed: e.g. 2x, 2(3), (a)(b), x sin(x)
        val processedTokens = mutableListOf<Token>()
        for (k in rawTokens.indices) {
            val curr = rawTokens[k]
            if (k > 0) {
                val prev = rawTokens[k - 1]
                val prevCanEnd = prev is Token.Number || prev is Token.RightParen ||
                        (prev is Token.Identifier && isConstantOrVariable(prev.name)) ||
                        (prev is Token.Operator && prev.symbol == '!')

                val currCanStart = curr is Token.Number || curr is Token.LeftParen ||
                        (curr is Token.Identifier && !isBinaryFunctionName(curr.name))

                if (prevCanEnd && currCanStart) {
                    processedTokens.add(Token.Operator('*', precedence = 2))
                }
            }
            processedTokens.add(curr)
        }
        return processedTokens
    }

    private fun isConstantOrVariable(name: String): Boolean {
        val lower = name.lowercase()
        return lower == "x" || lower == "pi" || lower == "e" || lower == "phi" || lower == "ans"
    }

    private fun isBinaryFunctionName(name: String): Boolean = false

    private fun shuntingYard(tokens: List<Token>): List<Any> {
        val output = mutableListOf<Any>()
        val operatorStack = ArrayDeque<Token>()

        var expectUnary = true

        for (token in tokens) {
            when (token) {
                is Token.Number -> {
                    output.add(token)
                    expectUnary = false
                }
                is Token.Identifier -> {
                    if (isConstantOrVariable(token.name)) {
                        output.add(token)
                        expectUnary = false
                    } else {
                        // Function call
                        operatorStack.addLast(token)
                    }
                }
                is Token.Comma -> {
                    while (operatorStack.isNotEmpty() && operatorStack.last() !is Token.LeftParen) {
                        output.add(operatorStack.removeLast())
                    }
                    expectUnary = true
                }
                is Token.Operator -> {
                    if (expectUnary && (token.symbol == '-' || token.symbol == '+')) {
                        // Unary operator has high precedence
                        val unaryOp = Token.Operator(if (token.symbol == '-') '~' else '#', precedence = 4, rightAssociative = true)
                        operatorStack.addLast(unaryOp)
                    } else {
                        val p1 = token.precedence
                        while (operatorStack.isNotEmpty()) {
                            val top = operatorStack.last()
                            if (top is Token.Operator) {
                                val p2 = top.precedence
                                if ((!token.rightAssociative && p1 <= p2) || (token.rightAssociative && p1 < p2)) {
                                    output.add(operatorStack.removeLast())
                                } else break
                            } else break
                        }
                        operatorStack.addLast(token)
                        expectUnary = token.symbol != '!'
                    }
                }
                is Token.LeftParen -> {
                    operatorStack.addLast(token)
                    expectUnary = true
                }
                is Token.RightParen -> {
                    while (operatorStack.isNotEmpty() && operatorStack.last() !is Token.LeftParen) {
                        output.add(operatorStack.removeLast())
                    }
                    if (operatorStack.isNotEmpty() && operatorStack.last() is Token.LeftParen) {
                        operatorStack.removeLast() // pop left paren
                    }
                    if (operatorStack.isNotEmpty() && operatorStack.last() is Token.Identifier) {
                        output.add(operatorStack.removeLast()) // pop function
                    }
                    expectUnary = false
                }
            }
        }

        while (operatorStack.isNotEmpty()) {
            output.add(operatorStack.removeLast())
        }

        return output
    }

    private fun buildAst(rpn: List<Any>): Node {
        val stack = ArrayDeque<Node>()

        for (item in rpn) {
            when (item) {
                is Token.Number -> stack.addLast(Node.Number(item.value))
                is Token.Identifier -> {
                    if (isConstantOrVariable(item.name)) {
                        stack.addLast(Node.Variable(item.name))
                    } else {
                        // Function call: determine arity
                        val arity = when (item.name.lowercase()) {
                            "atan2", "ncr", "comb", "npr", "perm", "mod", "pow" -> 2
                            else -> 1
                        }
                        val args = mutableListOf<Node>()
                        for (i in 0 until arity) {
                            if (stack.isNotEmpty()) {
                                args.add(0, stack.removeLast())
                            }
                        }
                        stack.addLast(Node.FunctionCall(item.name, args))
                    }
                }
                is Token.Operator -> {
                    when (item.symbol) {
                        '~' -> {
                            val operand = stack.removeLastOrNull() ?: Node.Number(0.0)
                            stack.addLast(Node.UnaryOp('-', operand))
                        }
                        '#' -> {
                            val operand = stack.removeLastOrNull() ?: Node.Number(0.0)
                            stack.addLast(Node.UnaryOp('+', operand))
                        }
                        '!' -> {
                            val operand = stack.removeLastOrNull() ?: Node.Number(0.0)
                            stack.addLast(Node.UnaryOp('!', operand))
                        }
                        else -> {
                            val right = stack.removeLastOrNull() ?: Node.Number(0.0)
                            val left = stack.removeLastOrNull() ?: Node.Number(0.0)
                            stack.addLast(Node.BinaryOp(item.symbol, left, right))
                        }
                    }
                }
            }
        }

        return stack.lastOrNull() ?: Node.Number(0.0)
    }
}
