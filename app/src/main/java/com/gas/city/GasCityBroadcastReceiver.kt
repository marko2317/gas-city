package com.gas.city

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.android.AndroidInjection
import javax.inject.Inject

class GasCityBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var serviceIntent: Intent

    override fun onReceive(context: Context?, intent: Intent?) {
        println("Receiver onReceive")
        AndroidInjection.inject(this, context)
        context?.startService(serviceIntent)
    }
}
