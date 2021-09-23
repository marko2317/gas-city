package com.gas.city

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.os.ParcelUuid
import android.util.Log
import io.reactivex.subjects.PublishSubject
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GasCityViewModel @Inject constructor(
    private val outputStream: FileOutputStream,
    private val btManager: BluetoothManager,
    private val publisher: PublishSubject<LogData>
) {
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var scanner: BluetoothLeScanner

    private var lastLogData: LogData? = null

    private var isScanning = false

    private val eddystoneServiceUuid = ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB")
    private val scanFilters = mutableListOf<ScanFilter>(
        ScanFilter.Builder().setServiceUuid(eddystoneServiceUuid).build()
    )
    private val scanSettings =
        ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            fetchAndSaveScanResult(result)
        }

        fun fetchAndSaveScanResult(result: ScanResult?) {
            val scanRecord = result?.scanRecord
            val serviceData = scanRecord?.getServiceData(eddystoneServiceUuid)
            serviceData?.let { data ->
                if (data.size == 20 && data[0] == EDDYSTONE_UID_FRAME_TYPE) {
                    val currentLogData = createLogData(data)
                    if (currentLogData.txSequence != lastLogData?.txSequence) {
                        saveDataIntoCSV(outputStream, currentLogData)
                        publisher.onNext(currentLogData)
                        lastLogData = currentLogData
                    }
                }
            }
        }
    }

    fun createBLEScanner() {
        btAdapter = btManager.adapter
        btAdapter.let { adapter ->
            if (!adapter.isEnabled) {
                Log.d(TAG, "Bluetooth is not ready to connect")
            }

            scanner = adapter.bluetoothLeScanner
        }
    }

    fun startScan() {
        if (!isScanning) {
            scanner.startScan(scanFilters, scanSettings, scanCallback)
            isScanning = true
        }
    }

    private fun createLogData(data: ByteArray): LogData {
        val rangingData = data.copyOfRange(1, 2).toHex()
        val productId = data.copyOfRange(2, 4).toHex()
        val productionMonth = data.copyOfRange(4, 5).toHex()
        val byte5 = data.copyOfRange(5, 6).toHex()
        val frameType = LogData.FrameType.fromChar(byte5[0])
        val tankLevelInit = data.copyOfRange(13, 14).toHex()
        val tankLevelEnd = data.copyOfRange(14, 15).toHex()
        val vbatt = data.copyOfRange(15, 16).toHex()
        val txSequence = data.copyOfRange(16, 18).toHex()
        val deviceIdB = data.copyOfRange(18, 20).toHex()
        val deviceId = "$productionMonth${byte5[1]}${data.copyOfRange(6, 13).toHex()}$deviceIdB"

        return LogData(
            rangingData = rangingData,
            productId = productId,
            frameType = frameType,
            deviceId = deviceId,
            tankLevelInit = tankLevelInit,
            tankLevelEnd = tankLevelEnd,
            vbatt = vbatt,
            txSequence = hexToDecimal(txSequence).toString(),
            tankLevel = (100 * hexToDecimal(tankLevelInit).toInt()) / 255
        )
    }

    private fun hexToDecimal(value: String): Double {
        val decimal = value.toInt(16)
        return if (decimal > 2048) {
            (decimal - 4096).toDouble()
        } else {
            decimal.toDouble()
        }
    }

    fun saveDataIntoCSV(out: FileOutputStream, data: LogData) {
        val builder = StringBuilder()
        with(builder) {
            append("${data.deviceId}, ${data.txSequence}")
                .append("\n")
            Log.d(TAG, toString())
            try {
                out.write(toString().toByteArray())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private companion object {
        const val TAG = "GasCityViewModel"
        const val EDDYSTONE_UID_FRAME_TYPE: Byte = 0x00
    }
}