package sorting

import java.io.File
import java.util.*
import kotlin.system.exitProcess

enum class SortedType {
    NATURAL,
    BY_COUNT
}

abstract class SortingTool<T: Comparable<T>>(outputFile: String?) {
    val data: MutableList<T> = mutableListOf()
    private val output = if (outputFile != null) File(outputFile) else null

    init {
        output?.writeText("")
    }

    abstract fun inputData(scanner: Scanner)
    abstract fun showStatistics(sortedType: SortedType)

    fun showSortedData(sortedType: SortedType, separator: String = " ") {
        if (sortedType == SortedType.NATURAL) {
            printInfo("Sorted data: ${data.sorted().joinToString(separator = separator)}")
        } else {
            val mapByFreq = data.groupingBy { it }.eachCount()

            for (c in mapByFreq.values.distinct().sorted()) {
                mapByFreq
                    .filterValues { it == c }.entries
                    .sortedBy { it.key }
                    .forEach { printInfo("${it.key}: ${it.value} time(s), ${if (data.isEmpty()) 0 else (it.value * 100 / data.size)}%") }
            }
        }
    }

    protected fun printInfo(info: String) {
        if (output == null) {
            println(info)
        } else {
            output.appendText("$info\n")
        }
    }
}

class LongSortingTool(outputFile: String?): SortingTool<Long>(outputFile) {
    override fun inputData(scanner: Scanner) {
        while (scanner.hasNext()) {
            val dat = scanner.next()
            try {
                data.add(dat.toLong())
            } catch (e: NumberFormatException) {
                println("$dat is not a long. It will we skipped.")
            }
        }
    }

    override fun showStatistics(sortedType: SortedType) {
        printInfo("Total numbers: ${data.size}.")

        showSortedData(sortedType)
    }
}

class LineSortingTool(outputFile: String?): SortingTool<String>(outputFile) {
    override fun inputData(scanner: Scanner) {
        while (scanner.hasNextLine()) {
            data.add(scanner.nextLine())
        }
    }

    override fun showStatistics(sortedType: SortedType) {
        printInfo("Total lines: ${data.size}.")

        showSortedData(sortedType, "\n")
    }
}

class WordSortingTool(outputFile: String?): SortingTool<String>(outputFile) {
    override fun inputData(scanner: Scanner) {
        while (scanner.hasNext()) {
            data.add(scanner.next())
        }
    }

    override fun showStatistics(sortedType: SortedType) {
        printInfo("Total words: ${data.size}.")

        showSortedData(sortedType)
    }
}


fun main(args: Array<String>) {
    var dataType: String? = null
    var sortingType: String? = null
    var inputFile: String? = null
    var outputFile: String? = null

    for (i in 0 until args.lastIndex) {
        when (args[i]) {
            "-dataType" -> if (i < args.lastIndex) {
                dataType = args[i + 1].toUpperCase()
            } else {
                println("No data type defined!")
                exitProcess(0)
            }
            "-sortingType" -> if (i < args.lastIndex) {
                sortingType = args[i + 1].toUpperCase()
            } else {
                println("No sorting type defined!")
                exitProcess(0)
            }
            "-inputFile" -> if (i < args.lastIndex) {
                inputFile = args[i + 1]
            } else {
                println("No input file defined!")
                exitProcess(0)
            }
            "-outputFile" -> if (i < args.lastIndex) {
                outputFile = args[i + 1]
            } else {
                println("No output file defined!")
                exitProcess(0)
            }
            else -> if (args[i].startsWith("-")) {
                println("${args[i]} is not a valid parameter. It will be skipped.")
            }
        }
    }

    val sortingTool = when (dataType) {
        "LONG" -> LongSortingTool(outputFile)
        "LINE" -> LineSortingTool(outputFile)
        else -> WordSortingTool(outputFile)
    }

    sortingTool.inputData(Scanner(if (inputFile != null) File(inputFile).inputStream() else System.`in`))
    sortingTool.showStatistics(if (sortingType == "BYCOUNT") SortedType.BY_COUNT else SortedType.NATURAL)
}
