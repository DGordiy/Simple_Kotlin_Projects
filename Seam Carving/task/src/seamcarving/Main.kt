package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.sqrt

const val COMMAND = "reduce"

class ImageMaker(private var img: BufferedImage) {

    // BufferedImage extensions
    private fun BufferedImage.transpose(): BufferedImage {
        val newImg = BufferedImage(height, width, BufferedImage.TYPE_INT_RGB)
        for (x in 0 until height) {
            for (y in 0 until width) {
                newImg.setRGB(x, y, getRGB(y, x))
            }
        }
        return newImg
    }

    private fun BufferedImage.getPixelEnergy(x: Int, y: Int): Double {
        return sqrt((doubleGradientX(x, y) + doubleGradientY(x, y)).toDouble())
    }

    private fun BufferedImage.doubleGradientX(x: Int, y: Int): Int {
        if (x == 0) return doubleGradientX(x + 1, y)
        if (x == width - 1) return doubleGradientX(x - 1, y)

        val color1 = Color(getRGB(x - 1, y), true)
        val color2 = Color(getRGB(x + 1, y), true)

        val dRed = color1.red - color2.red
        val dGreen = color1.green - color2.green
        val dBlue = color1.blue - color2.blue

        return dRed * dRed + dGreen * dGreen + dBlue * dBlue
    }

    private fun BufferedImage.doubleGradientY(x: Int, y: Int): Int {
        if (y == 0) return doubleGradientY(x, y + 1)
        if (y == height - 1) return doubleGradientY(x, y - 1)

        val color1 = Color(getRGB(x, y - 1), true)
        val color2 = Color(getRGB(x, y + 1), true)

        val dRed = color1.red - color2.red
        val dGreen = color1.green - color2.green
        val dBlue = color1.blue - color2.blue

        return dRed * dRed + dGreen * dGreen + dBlue * dBlue
    }

    private fun BufferedImage.getEnergyMatrix(): Array<DoubleArray> {
        val result = Array(height) { DoubleArray(width) }

        for (y in 0 until height) {
            for (x in 0 until width) {
                result[y][x] = getPixelEnergy(x, y)
            }
        }

        return result
    }

    //

    fun getNegative(): BufferedImage {
        val newImg = img
        for (x in 0 until newImg.width) {
            for (y in 0 until newImg.height) {
                val color = Color(newImg.getRGB(x, y), true)
                newImg.setRGB(x, y, Color(255 - color.red, 255 - color.green, 255 - color.blue).rgb)
            }
        }

        return newImg
    }

    fun getBlackAndWhite(): BufferedImage {
        val energyMatrix: Array<DoubleArray> = img.getEnergyMatrix()
        val maxPixelEnergy: Double = energyMatrix.flatMap { it.toList() }.maxOrNull() ?: 1.0

        val newImg = img
        for (y in 0 until newImg.height) {
            for (x in 0 until newImg.width) {
                val intensity = (255 * energyMatrix[y][x] / maxPixelEnergy).toInt()
                newImg.setRGB(x, y, Color(intensity, intensity, intensity).rgb)
            }
        }

        return newImg
    }

    private fun getSeam(horizontal: Boolean = false, image: BufferedImage = img): List<Pair<Int, Int>> {
        val newImg = if (horizontal) image.transpose() else image

        val energyMatrix: Array<DoubleArray> = newImg.getEnergyMatrix()
        val tmpArray: Array<DoubleArray> = Array(newImg.height) {DoubleArray(newImg.width)}

        for (x in 0 until newImg.width) {
            tmpArray[0][x] = energyMatrix[0][x]
        }

        for (y in 1 until newImg.height) {
            for (x in 0 until newImg.width) {
                tmpArray[y][x] = energyMatrix[y][x] +
                        when (x) {
                            0 -> minOf(tmpArray[y - 1][x], tmpArray[y - 1][x + 1])
                            newImg.width - 1 -> minOf(tmpArray[y - 1][x], tmpArray[y - 1][x - 1])
                            else -> minOf(tmpArray[y - 1][x - 1], tmpArray[y - 1][x], tmpArray[y - 1][x + 1])
                        }
            }
        }

        val result = mutableListOf<Pair<Int, Int>>()
        var minimumEnergy = tmpArray.last().minOf { it }
        var x = tmpArray.last().indexOfFirst { it == minimumEnergy }

        for (y in newImg.height - 1 downTo 1) {
            result.add(Pair(x, y))

            minimumEnergy = when (x) {
                0 -> minOf(tmpArray[y - 1][x], tmpArray[y - 1][x + 1])
                newImg.width - 1 -> minOf(tmpArray[y - 1][x], tmpArray[y - 1][x - 1])
                else -> minOf(tmpArray[y - 1][x - 1], tmpArray[y - 1][x], tmpArray[y - 1][x + 1])
            }
            x = when {
                x > 0 && tmpArray[y - 1][x - 1] == minimumEnergy -> x - 1
                x < newImg.width - 1 && tmpArray[y - 1][x + 1] == minimumEnergy -> x + 1
                else -> x
            }
        }
        result.add(Pair(x, 0))

        return if (horizontal) result.map { Pair(it.second, it.first) } else result
    }

    fun getReduced(widthCount: Int, heightCount: Int, image: BufferedImage = img): BufferedImage {
        if (widthCount > 0) {
            val seam = getSeam(image = image)
            val newImg = BufferedImage(image.width - 1, image.height, BufferedImage.TYPE_INT_RGB)

            for (y in 0 until image.height) {
                val xSeam = seam.find { it.second == y }?.first ?: 0
                for (x in 0 until xSeam) {
                    newImg.setRGB(x, y, image.getRGB(x, y))
                }
                for (x in xSeam + 1 until image.width) {
                    newImg.setRGB(x - 1, y, image.getRGB(x, y))
                }
            }

            return getReduced(widthCount - 1, heightCount, newImg)
        }
        if (heightCount > 0) {
            val seam = getSeam(image = image)
            val newImg = BufferedImage(image.width, image.height - 1, BufferedImage.TYPE_INT_RGB)

            for (x in 0 until image.width) {
                val ySeam = seam.find { it.first == x }?.second ?: 0
                for (y in 0 until ySeam) {
                    newImg.setRGB(x, y, image.getRGB(x, y))
                }
                for (y in ySeam + 1 until image.height) {
                    newImg.setRGB(x, y - 1, image.getRGB(x, y))
                }
            }

            return getReduced(widthCount, heightCount - 1, newImg)
        }

        return image
    }
}

fun main(args: Array<String>) {
    val inputFileName = args[args.indexOf("-in") + 1]
    val outputFileName = args[args.indexOf("-out") + 1]
    val reduceWidth = args[args.indexOf("-width") + 1].toInt()
    val reduceHeight = args[args.indexOf("-height") + 1].toInt()

    val imageMaker = ImageMaker(ImageIO.read(File(inputFileName)))

    val outputFile = File(outputFileName)

    val outputBufferedImage = when (COMMAND) {
        "negative" -> imageMaker.getNegative()
        "blackWhite" -> imageMaker.getBlackAndWhite()
        else -> imageMaker.getReduced(reduceWidth, reduceHeight)
    }

    ImageIO.write(outputBufferedImage, outputFile.extension, outputFile)
}