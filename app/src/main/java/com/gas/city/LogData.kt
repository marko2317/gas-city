package com.gas.city

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

data class LogData(
    val deviceId: String,
    val dateTime: String,
    val battery: Float,
    val startX: String,
    val startY: String,
    val endX: String,
    val endY: String,
    val angleStart: Double,
    val angleEnd: Double,
    val percentageStart: Double,
    val percentageEnd: Double
) : BaseObservable() {

    @Bindable
    var progressBarPercentage: Int = 0
        set(value) {
            field = value
            percentageText = value.toString()
            notifyPropertyChanged(BR.progressBarPercentage)
        }

    @Bindable
    var percentageText: String = ""
        set(value) {
            field = "$value%"
            notifyPropertyChanged(BR.percentageText)
        }
}