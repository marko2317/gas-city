package com.gas.city

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.os.ParcelUuid
import android.util.Log
import io.reactivex.subjects.PublishSubject
import java.io.FileOutputStream
import javax.inject.Inject

private const val EDDYSTONE_UID_FRAME_TYPE: Byte = 0x00
private const val TAG = "BLEDataModel"

class BLEDataModel @Inject constructor(
    private val outputStream: FileOutputStream,
    private val btManager: BluetoothManager,
    private val publisher: PublishSubject<LogData>
) {

    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var scanner: BluetoothLeScanner
    private lateinit var logData: LogData

    private var isScanning = false

    private val eddystoneServiceUuid = ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB")
    private val scanFilters = mutableListOf<ScanFilter>(
        ScanFilter.Builder().setServiceUuid(eddystoneServiceUuid).build()
    )
    private val scanSettings =
        ScanSettings.Builder().setScanMode(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setReportDelay(0)
            .build()

    @ExperimentalUnsignedTypes
    private val scanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            Log.i(TAG, "Scan failed errorCode: $errorCode")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            val scanRecord = result?.scanRecord
            val serviceData = scanRecord?.getServiceData(eddystoneServiceUuid)
            serviceData?.let { data ->
                if (data[0] == EDDYSTONE_UID_FRAME_TYPE) {
                    logData = createLogDataFromDeviceId(data)
                    logData.saveDataIntoCSV(outputStream)
                    with(logData) {
                        when {
                            percentageEnd > 0 -> progressBarPercentage = percentageEnd.toInt()
                            percentageStart > 0 -> progressBarPercentage =
                                percentageStart.toInt()
                        }
                    }
                    publisher.onNext(logData)
                }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            Log.i(TAG, "onBatchScanResults")
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

    @ExperimentalUnsignedTypes
    fun startScan() {
        if (!isScanning) {
            scanner.startScan(scanFilters, scanSettings, scanCallback)
            isScanning = true
        }
    }
}