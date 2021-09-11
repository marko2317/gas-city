package com.gas.city

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.android.AndroidInjection
import javax.inject.Inject

class GasCityService : Service() {

    @Inject
    lateinit var viewModel: GasCityViewModel

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    @ExperimentalUnsignedTypes
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startScanning()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
//        sendRestartBroadcast()
    }

    @ExperimentalUnsignedTypes
    private fun startScanning() {
        viewModel.createBLEScanner()
        viewModel.startScan()
    }

    private fun sendRestartBroadcast() {
        sendBroadcast(Intent(this, GasCityBroadcastReceiver::class.java))
    }
}
