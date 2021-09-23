package com.gas.city

import androidx.databinding.BaseObservable

data class LogData(
    val rangingData: String,
    val productId: String,
    val frameType: FrameType,
    val deviceId: String,
    val tankLevelInit: String,
    val tankLevelEnd: String,
    val vbatt: String,
    val txSequence: String,
    val tankLevel: Int
) : BaseObservable() {

    enum class FrameType(value: String) {
        TYPE_A("A"), TYPE_B("B"), TYPE_C("C"), TYPE_D("D"), INVALID("");

        companion object {
            fun fromChar(char: Char) = when (char) {
                'A' -> TYPE_A
                'B' -> TYPE_B
                'C' -> TYPE_C
                'D' -> TYPE_D
                else -> INVALID
            }
        }
    }
}