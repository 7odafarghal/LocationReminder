package com.udacity.project4.locationreminders

import android.Manifest.permission.*
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import kotlinx.android.synthetic.main.activity_reminders.*
import org.koin.android.ext.android.inject

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    val auth: FirebaseAuth by inject()
    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    private val runningTOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    private val backgroundLocationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()
    ) { if (!it){showPermissionErrorSnack()} }

    @RequiresApi(Build.VERSION_CODES.Q)
    val locationPermissionLauncher =  registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (!it) showPermissionErrorSnack()
        else if (runningQOrLater) backgroundLocationPermissionLauncher.launch(ACCESS_BACKGROUND_LOCATION)
    }
    // ask for notification permission then fine location then background location
    @RequiresApi(Build.VERSION_CODES.Q)
    val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (!it) Toast.makeText(applicationContext, getString(R.string.enable_notifications), Toast.LENGTH_SHORT).show()
        locationPermissionLauncher.launch(ACCESS_FINE_LOCATION)
    }



    private fun showPermissionErrorSnack() {
        Snackbar.make(
            nav_host_fragment,
            R.string.location_required_error,
            Snackbar.LENGTH_INDEFINITE
        ).setAction(R.string.settings) {
            startActivity(Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }.show()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            val intent = Intent(this, AuthenticationActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
        if (runningTOrLater) notificationPermissionLauncher.launch(POST_NOTIFICATIONS)
        else locationPermissionLauncher.launch(ACCESS_FINE_LOCATION)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                findNavController(R.id.nav_host_fragment).popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
