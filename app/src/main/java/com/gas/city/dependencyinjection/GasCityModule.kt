package com.gas.city.dependencyinjection

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import com.gas.city.*
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.io.FileOutputStream
import javax.inject.Singleton

@Module
abstract class GasCityModule {

    @ContributesAndroidInjector
    abstract fun activityInjector(): GasCityActivity

    @ContributesAndroidInjector
    abstract fun serviceInjector(): GasCityService

    @ContributesAndroidInjector
    abstract fun broadcastReceiverInjector(): GasCityBroadcastReceiver

    @Module
    companion object {

        @Provides
        @Singleton
        fun provideIntentService(application: GasCityApplication) =
            Intent(application.applicationContext, GasCityService::class.java)

        @Provides
        @Singleton
        fun provideFileOutput(application: GasCityApplication): FileOutputStream {
            val file = File(application.baseContext.getExternalFilesDir(null), "log.csv")
            return FileOutputStream(file)
        }

        @Provides
        @Singleton
        fun provideBluetoothManager(application: GasCityApplication) =
            application.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        @Provides
        @Singleton
        fun provideLogDataObservable(): PublishSubject<LogData> = PublishSubject.create()
    }
}