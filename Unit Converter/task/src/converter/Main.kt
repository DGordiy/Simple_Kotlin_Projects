package converter

data class UnitsByCategory(val name: List<String>, val single: List<String>, val plural: List<String>, val coefficient: List<Double>)
data class UnitIndexAndCategory(val index: Int, val category: UnitCategory)

enum class UnitCategory(val unitsTable: UnitsByCategory) {
    DISTANCE(UnitsByCategory(
        listOf("m", "km", "cm", "mm", "mi", "yd", "ft", "in"),
        listOf("meter", "kilometer", "centimeter", "millimeter", "mile", "yard", "foot", "inch"),
        listOf("meters", "kilometers", "centimeters", "millimeters", "miles", "yards", "feet", "inches"),
        listOf(1.0, 1000.0, 0.01, 0.001, 1609.35, 0.9144, 0.3048, 0.0254))),
    WEIGHT(UnitsByCategory(
        listOf("g", "kg", "mg", "lb", "oz"),
        listOf("gram", "kilogram", "milligram", "pound", "ounce"),
        listOf("grams", "kilograms", "milligrams", "pounds", "ounces"),
        listOf(1.0, 1000.0, 0.001, 453.592, 28.3495))),
    TEMPERATURE(UnitsByCategory(
        listOf("c", "f", "k"),
        listOf("degree Celsius", "degree Fahrenheit", "kelvin"),
        listOf("degrees Celsius", "degrees Fahrenheit", "kelvins"),
        listOf(0.0, 0.0, 0.0)));
}

fun main() {
    makeChoice()
}

fun convertedValue(sourceValue: Double, oldIndexAndCategory: UnitIndexAndCategory, newIndexAndCategory: UnitIndexAndCategory): Double {
    val unitsTable = oldIndexAndCategory.category.unitsTable

    if (oldIndexAndCategory.category == UnitCategory.WEIGHT || oldIndexAndCategory.category == UnitCategory.DISTANCE)
        return sourceValue * unitsTable.coefficient[oldIndexAndCategory.index] / unitsTable.coefficient[newIndexAndCategory.index]
    else
        if (oldIndexAndCategory.index == 0 && newIndexAndCategory.index == 1) //From Celsius to Fahrenheit
            return sourceValue * 9 / 5 + 32
        else if (oldIndexAndCategory.index == 1 && newIndexAndCategory.index == 0) //From Fahrenheit to Celsius
            return (sourceValue - 32) * 5 / 9
        else if (oldIndexAndCategory.index == 0 && newIndexAndCategory.index == 2) //From Celsius to Kelvins
            return sourceValue + 273.15
        else if (oldIndexAndCategory.index == 2 && newIndexAndCategory.index == 0) //From Kelvins to Celsius
            return sourceValue - 273.15
        else if (oldIndexAndCategory.index == 1 && newIndexAndCategory.index == 2) //From Fahrenheit to Kelvins
            return (sourceValue + 459.67) * 5 / 9
        else if (oldIndexAndCategory.index == 2 && newIndexAndCategory.index == 1) //From Kelvins to Fahrenheit
            return sourceValue * 9 / 5 - 459.67
        else
            return sourceValue
}

fun valueAndUnit(indexAndCategory: UnitIndexAndCategory, value: Double, showValue: Boolean): String {
    val unitDesc = if (value == 1.0) indexAndCategory.category.unitsTable.single[indexAndCategory.index] else indexAndCategory.category.unitsTable.plural[indexAndCategory.index]
    return if (showValue) {
        "$value $unitDesc"
    } else {
        unitDesc
    }
}

fun getUnitIndexAndCategoryFromString(name: String): UnitIndexAndCategory? {
    var unitsTable = UnitCategory.DISTANCE.unitsTable
    for (i in 0..unitsTable.name.lastIndex) {
        if (name.equals(unitsTable.name[i], ignoreCase = true) || name.equals(unitsTable.single[i], ignoreCase = true) || name.equals(
                unitsTable.plural[i], ignoreCase = true)) {
            return UnitIndexAndCategory(i, UnitCategory.DISTANCE)
        }
    }
    unitsTable = UnitCategory.WEIGHT.unitsTable
    for (i in 0..4) {
        if (name.equals(unitsTable.name[i], ignoreCase = true) ||
            name.equals(unitsTable.single[i], ignoreCase = true) ||
            name.equals(unitsTable.plural[i], ignoreCase = true)) {
            return UnitIndexAndCategory(i, UnitCategory.WEIGHT)
        }
    }
    unitsTable = UnitCategory.TEMPERATURE.unitsTable
    val convertedName = when (name.toLowerCase()) {
        "dc" -> "c"
        "celsius" -> "c"
        "df" -> "f"
        "fahrenheit" -> "f"
        else -> name
    }
    for (i in 0..2) {
        if (convertedName.equals(unitsTable.name[i], ignoreCase = true) ||
            convertedName.equals(unitsTable.single[i], ignoreCase = true) ||
            convertedName.equals(unitsTable.plural[i], ignoreCase = true)) {
            return UnitIndexAndCategory(i, UnitCategory.TEMPERATURE)
        }
    }

    return null
}

fun makeChoice() {
    print("Enter what you want to convert (or exit): ")
    val input = readLine()!!.split(" ")
    if (input[0] == "exit") return

    if (input.size >= 2) {
        try {
            val oldValue = input[0].toDouble()

            val oldUnit = getUnitIndexAndCategoryFromString(input[1] + if (input[2].toLowerCase() in "into") "" else " " + input[2])
            val newUnit = getUnitIndexAndCategoryFromString((if (input[input.lastIndex - 1].toLowerCase() in "into") "" else input[input.lastIndex - 1] + " ") + input[input.lastIndex])

            if (oldUnit == null || newUnit == null || oldUnit.category != newUnit.category) {
                throw Exception("Conversion from ${if (oldUnit == null) "???" else valueAndUnit(oldUnit, 0.0, false)} to ${if (newUnit == null) "???" else valueAndUnit(newUnit, 0.0, false)} is impossible\n")
            }

            if (oldUnit.category != UnitCategory.TEMPERATURE && oldValue < 0) {
                throw Exception("${if (oldUnit.category == UnitCategory.DISTANCE) "Length" else "Weight"} shouldn't be negative")
            }

            val newValue = convertedValue(oldValue, oldUnit, newUnit)
            println("${valueAndUnit(oldUnit, oldValue, true)} is ${valueAndUnit(newUnit, newValue,true)}")
        } catch (e: NumberFormatException) {
            println("Parse error")
        }
        catch (e: Exception) {
            println(e.message)
        }
    } else {
        println("Invalid command")
    }

    println()
    makeChoice()
}