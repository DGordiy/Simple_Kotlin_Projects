package calculator

import java.math.BigInteger

data class CalculatorException(override val message: String) : Exception(message)

val VAR_PATTERN = Regex("[A-Za-z]+")
val EXPRESSION_PATTERN = Regex("\\d+|[-]+|[+]+|[*/]|\\(|\\)|[a-zA-Z]+")
const val OPERATORS = "*/-+"

class Calculator {
    private val variables = mutableMapOf<String, BigInteger>()

    @Throws(NumberFormatException::class, CalculatorException::class)
    fun calculate(input: String): BigInteger {
        //Make assignment to variable if possible
        if (input.contains("=")) {
            val varName = input.substringBefore("=")
            val expression = input.substringAfter("=")

            if (varName.matches(VAR_PATTERN)) {
                try {
                    variables[varName] = calculate(expression)
                    return BigInteger.ZERO
                } catch (e: Exception) {
                    throw CalculatorException("Invalid assignment")
                }
            } else {
                throw CalculatorException("Invalid identifier")
            }
        }

        if (input.count { it == '(' } != input.count { it == ')' }) throw CalculatorException("Invalid expression")

        //Replace - or + before digit or variable to 0 -/+ ...
        var transformedInput = if (input.startsWith("-")) "0$input" else input
        for (i in "(*/+") transformedInput = transformedInput.replace("${i}-", "${i}0-")

        val polish = getPolishNotation(transformedInput)
        val stack = ArrayDeque<Any>()
        while (polish.isNotEmpty()) {
            val m = polish.removeFirst()
            when {
                m.matches("\\d+".toRegex()) -> stack.addLast(m.toBigInteger())
                m.matches(VAR_PATTERN) -> {
                    val num = variables[m] ?: throw CalculatorException("Unknown variable")
                    stack.addLast(num)
                }
                m in "*/-+" -> {
                    val n1: BigInteger
                    val n2: BigInteger
                    try {
                        n2 = stack.removeLast() as BigInteger
                        n1 = stack.removeLast() as BigInteger
                    } catch (e: Exception) {
                        throw CalculatorException("Invalid expression")
                    }

                    when (m) {
                        "*" -> stack.addLast(n1 * n2)
                        "/" -> stack.addLast(n1 / n2)
                        "-" -> stack.addLast(n1 - n2)
                        "+" -> stack.addLast(n1 + n2)
                    }
                }
            }
        }

        return stack.last() as BigInteger
    }

    private fun reduceOperator(operator: String): String {
        if (operator.startsWith("-")) return if (operator.length % 2 == 0) "+" else "-"
        if (operator.startsWith("+")) return "+"
        return operator
    }

    private fun getPolishNotation(expression: String): ArrayDeque<String> {
        val result = ArrayDeque<String>()
        val stack: ArrayDeque<String> = ArrayDeque()

        val matchResult = EXPRESSION_PATTERN.findAll(expression)

        for (m in matchResult.map { it.value }.toList()) {
            when {
                m.matches("\\d+".toRegex()) || m.matches(VAR_PATTERN) -> result.add(m)
                m == "(" -> stack.addLast(m)
                m == ")" -> {
                    do {
                        val value = stack.removeLast()
                        if (value != "(") {
                            result.add(value)
                        }
                    } while (stack.isNotEmpty() && value != "(")
                }
                m[0] in OPERATORS -> {
                    val reducedOperator = reduceOperator(m)

                    if (stack.isEmpty() || OPERATORS.indexOf(reducedOperator) < OPERATORS.indexOf(stack.last())) {
                        stack.addLast(reducedOperator)
                    } else {
                        do {
                            val value = stack.last()
                            if (value != "(" && OPERATORS.indexOf(value) <= OPERATORS.indexOf(reducedOperator)) {
                                result.add(stack.removeLast())
                            }
                        } while (stack.isNotEmpty() && value != "(" && OPERATORS.indexOf(value) <= OPERATORS.indexOf(reducedOperator))
                        stack.addLast(reducedOperator)
                    }
                }
                else -> if (stack.isEmpty() || stack.last() == "(") stack.addLast(m)
            }
        }

        while (stack.isNotEmpty()) {
            if (stack.last() in OPERATORS) {
                result.add(stack.removeLast())
            } else {
                throw CalculatorException("Invalid expression")
            }
        }

        return result
    }
}