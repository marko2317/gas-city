package com.gas.city

import android.util.Log
import java.io.FileOutputStream
import java.util.*
import kotlin.math.abs
import kotlin.math.atan2

const val FILE_NAME = "log.csv"
private const val TAG = "LogData"

private val data = listOf(
    SensorData(200F, 207.92F, 0F, 5F),
    SensorData(207.92F, 253.13F, 5F, 10F),
    SensorData(253.13F, 273.19F, 10F, 15F),
    SensorData(273.19F, 286.57F, 15F, 20F),
    SensorData(286.57F, 296.43F, 20F, 25F),
    SensorData(296.43F, 305.44F, 25F, 30F),
    SensorData(305.44F, 313.98F, 30F, 35F),
    SensorData(313.98F, 324.44F, 35F, 40F),
    SensorData(324.44F, 333.23F, 40F, 45F),
    SensorData(333.23F, 341.91F, 45F, 50F),
    SensorData(341.91F, 352.50F, 50F, 55F),
    SensorData(352.50F, 360F, 55F, 57.17F),
    SensorData(0F, 9.75F, 57.18F, 60F),
    SensorData(9.75F, 24.36F, 60F, 65F),
    SensorData(24.36F, 37.38F, 65F, 70F),
    SensorData(37.38F, 55.78F, 70F, 75F),
    SensorData(55.78F, 70.74F, 75F, 80F),
    SensorData(70.74F, 91.43F, 80F, 85F),
    SensorData(91.43F, 116.90F, 85F, 90F),
    SensorData(116.90F, 143.91F, 90F, 95F),
    SensorData(143.91F, 150F, 95F, 95F),
    SensorData(150F, 200F, 0F, 0F)
)

@ExperimentalUnsignedTypes
fun ByteArray.toHex() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }

fun extractCoordinate(id: String, index1: Int, index2: Int, index3: Int): String {
    val builder = StringBuilder()
    if (id.length == 32) {
        val characters = id.toCharArray()
        val coordinates = CharArray(3)
        coordinates[0] = characters[index1]
        coordinates[1] = characters[index2]
        coordinates[2] = characters[index3]
        for (char in coordinates) {
            builder.append(char)
        }
    }
    return builder.toString()
}

fun convertRawCoordinateToFinal(rawCoordinateX: String, rawCoordinateY: String): Double {

    val decimalX = hexToDecimal(rawCoordinateX)
    val decimalY = hexToDecimal(rawCoordinateY)

    val atan2 = atan2(decimalX, decimalY)
    val degrees = Math.toDegrees(atan2)

    return if (degrees > 0) {
        360 - degrees
    } else {
        abs(degrees)
    }
}

fun convertAngleToPercentage(angle: Double): Double {
    val sensorData = getSensorDataForAngle(angle)
    return sensorData?.let { data ->
        val percentageDifference = data.percentageEnd - data.percentageStart
        val angleVariation = angle - data.angleStart
        val angleDifference = data.angleEnd - data.angleStart
        val percentageVariation = (angleVariation * percentageDifference) / angleDifference
        data.percentageStart + percentageVariation
    } ?: 0.0
}

@ExperimentalUnsignedTypes
fun createLogDataFromDeviceId(data: ByteArray): LogData {
    val id = data.copyOfRange(2, 18).toHex()
    val x0 = extractCoordinate(id, 20, 22, 23)
    val x1 = extractCoordinate(id, 26, 28, 29)
    val y0 = extractCoordinate(id, 21, 24, 25)
    val y1 = extractCoordinate(id, 27, 30, 31)
    val start = convertRawCoordinateToFinal(x0, y0)
    val end = convertRawCoordinateToFinal(x1, y1)
    return LogData(
        deviceId = id,
        dateTime = Date().toString(),
        battery = 0F,
        startX = x0,
        startY = y0,
        endX = x1,
        endY = y1,
        angleStart = start,
        angleEnd = end,
        percentageStart = convertAngleToPercentage(start),
        percentageEnd = convertAngleToPercentage(end),
        showEndAngle = x1.toInt() != 888 && y1.toInt() != 888
    )
}

fun LogData.saveDataIntoCSV(out: FileOutputStream) {
    val builder = StringBuilder()
    with(builder) {
        append("$deviceId, $dateTime, $battery, $startX, $startY, $endX, $endY, $angleStart, $angleEnd, $percentageStart, $percentageEnd")
            .append("\n")
        Log.d(TAG, toString())
        try {
            out.write(toString().toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun getSensorDataForAngle(angle: Double): SensorData? {
    data.forEach { element ->
        if (angle in element.angleStart..element.angleEnd) {
            return element
        }
    }

    return null
}

private fun hexToDecimal(value: String): Double {
    val decimal = value.toInt(16)
    return if (decimal > 2048) {
        (decimal - 4096).toDouble()
    } else {
        decimal.toDouble()
    }
}