package com.gas.city

import android.content.Context
import androidx.core.content.ContextCompat

class LogDataViewModel(dataModel: LogData, context: Context) {

    val tankLevel = "${dataModel.tankLevel}%"

    val tankLevelColor = when (dataModel.tankLevel) {
        in 0..25 -> ContextCompat.getColor(context, R.color.tank_level_25)
        in 26..50 -> ContextCompat.getColor(context, R.color.tank_level_50)
        in 51..75 -> ContextCompat.getColor(context, R.color.tank_level_75)
        else -> ContextCompat.getColor(context, R.color.tank_level_100)
    }

    val deviceId = dataModel.deviceId
}