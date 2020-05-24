package com.gas.city

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GasCityViewModel @Inject constructor(private val dataModel: BLEDataModel) {

    fun createBLEScanner() {
        dataModel.createBLEScanner()
    }

    @ExperimentalUnsignedTypes
    fun startScan() {
        dataModel.startScan()
    }
}