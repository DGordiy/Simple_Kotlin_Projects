package parking

data class Car(val number: String, val color: String)

class Parking(size: Int) {
    private var spots: Array<Car?> = Array(size) {null}

    companion object {
        fun createWithSize(size: Int): Parking {
            println("Created a parking lot with $size spots.")
            return Parking(size)
        }
    }

    private fun freeSpotIndex(): Int = spots.indexOfFirst { c -> c == null }

    fun park(car: Car) {
        val index = freeSpotIndex()
        if (index == -1) {
            println("Sorry, the parking lot is full.")
            return
        }

        spots[index] = car
        println("${car.color} car parked in spot ${index + 1}.")
    }

    fun leave(index: Int) {
        if (spots[index - 1] == null) {
            println("There is no car in spot $index.")
        } else {
            spots[index - 1] = null
            println("Spot $index is free.")
        }
    }

    fun printStatus() {
        if (spots.any {it != null}) {
            for (i in 0..spots.lastIndex) {
                val car = spots[i]
                if (car != null) {
                    println("${i + 1} ${car.number} ${car.color}")
                }
            }
        } else {
            println("Parking lot is empty.")
        }
    }

    fun printCarNumbersWithColor(color: String) {
        val numbers = spots
            .filter { c -> c?.color.equals(color, ignoreCase = true) }
            .map { it?.number }
        if (numbers.isEmpty()) {
            println("No cars with color $color were found.")
        } else {
            println(numbers.joinToString { "$it" })
        }
    }

    fun printSpotsWithColor(color: String) {
        val index = spots
            .withIndex()
            .filter { it.value?.color.equals(color, ignoreCase = true) }
            .map { it.index + 1 }

        if (index.isEmpty()) {
            println("No cars with color $color were found.")
        } else {
            println(index.joinToString { "$it" })
        }
    }

    fun printSpotByCarNumber(number: String) {
        val index = spots.indexOfFirst { c -> c?.number == number }
        if (index != -1) {
            println(index + 1)
        } else {
            println("No cars with registration number $number were found.")
        }
    }
}

fun main() {
    var parking: Parking? = null

    while (true) {
        val input = readLine()!!.split(' ')

        if (input[0].toLowerCase() == "exit") {
            break
        }

        if (input[0].toLowerCase() == "create") {
            parking = Parking.createWithSize(input[1].toInt())
            continue
        } else if (parking == null) {
            println("Sorry, a parking lot has not been created.")
            continue
        }

        when (input[0].toLowerCase()) {
            "park" -> parking.park(Car(number = input[1], color = input[2]))
            "leave" -> parking.leave(input[1].toInt())
            "status" -> parking.printStatus()
            "reg_by_color" -> parking.printCarNumbersWithColor(input[1])
            "spot_by_color" -> parking.printSpotsWithColor(input[1])
            "spot_by_reg" -> parking.printSpotByCarNumber(input[1])
            else -> println("Incorrect command")
        }
    }
}
