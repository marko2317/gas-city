package com.gas.city

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.gas.city.databinding.ActivityMainBinding
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

private const val ACCESS_FINE_LOCATION_REQUEST = 1

class GasCityActivity @Inject constructor() : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var disposable: Disposable

    private var dialog: AlertDialog? = null

    @Inject
    lateinit var logDataObservable: PublishSubject<LogData>

    @Inject
    lateinit var serviceIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
        } else {
            startService(serviceIntent)
        }

        disposable = logDataObservable.observeOn(Schedulers.io())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe { logData ->
                binding.viewModel = LogDataViewModel(logData, this)
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            ACCESS_FINE_LOCATION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    startService(serviceIntent)
                } else if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_DENIED) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        dialog = createDialog(
                            "We need this permission to be able to run",
                            "Retry",
                            DialogInterface.OnClickListener { _, _ -> requestPermission() }
                        )
                        dialog?.show()
                    } else {
                        showSettingsDialog()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startService(serviceIntent)
    }

    override fun onStop() {
        super.onStop()
        stopService(serviceIntent)
        dialog?.dismiss()
    }

    private fun showSettingsDialog() {
        dialog = createDialog(
            "Please grant permission in the setting menu to be able to open the app",
            "Settings",
            DialogInterface.OnClickListener { _, _ -> goToSettings() }
        )
        dialog?.show()
    }

    private fun createDialog(
        message: String,
        buttonText: String,
        listener: DialogInterface.OnClickListener
    ) = AlertDialog.Builder(this)
        .setMessage(message)
        .setPositiveButton(buttonText, listener)
        .setCancelable(false)
        .create()

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            ACCESS_FINE_LOCATION_REQUEST
        )
    }

    private fun goToSettings() {
        startActivity(
            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
        )
        finish()
    }
}
