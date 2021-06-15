package calculator

import kotlin.NumberFormatException

fun main() {
    val calculator = Calculator()

    do {
        val input = readLine()!!.replace(" ", "")
        if (input.isBlank()) continue

        when (input) {
            "/exit" -> break
            "/help" -> println("The program calculates the sum of numbers.\n" +
                    "You can use +, -, *, / operators.\n" +
                    "So you can use variables assignments.\n" +
                    "Parentheses are used for determining a priority of arithmetics operations.\n")
            else -> {
                if (input.startsWith("/")) {
                    println("Unknown command")
                } else {
                    try {
                        val result = calculator.calculate(input)

                        //If statement is assignment we must skip printing the result
                        if (!input.contains("=")) {
                            println(result)
                        }
                    } catch (e: NumberFormatException) {
                        println("Invalid expression")
                    } catch (e: CalculatorException) {
                        println(e.message)
                    }
                }
            }
        }
    } while (true)

    println("Bye!")
}