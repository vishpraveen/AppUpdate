package com.testingapp.appupdatedemo

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), InstallStateUpdatedListener {
    private val TAG = MainActivity::class.java.simpleName
    private lateinit var appUpdateManager: AppUpdateManager

    private var localUpdateFlag:Int = 0

    companion object {
        const val DAYS_FOR_FLEXIBLE_UPDATE = 1
        const val HIGH_PRIORITY_UPDATE = 3
//        const val HIGH_PRIORITY_UPDATE = 1
        const val LOW_PRIORITY_UPDATE = 2
        const val REQUEST_APP_UPDATE_FLEXIBLE = 1
        const val REQUEST_APP_UPDATE_IMMEDIATE = 2

        const val MEGABYTE = 1024L * 1024L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkAppVersion()

//        Create instance of AppUpdateManager
        appUpdateManager = AppUpdateManagerFactory.create(this@MainActivity)

        appUpdateManager.registerListener(this@MainActivity)

        localUpdateFlag = (0..5).random()

        setUpAppUpdate()
    }

    private fun checkAppVersion() {
        tvAppVersion.text = "Current App Version ${BuildConfig.VERSION_CODE}"
    }

    private fun setUpAppUpdate() {

// Returns an intent object that you use to check for an update.
        val appUpdateTaskInfo: Task<AppUpdateInfo> = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateTaskInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE){
                tvStatus.text = "Update Available 👍😊"
                tvPriorityValue.text = "Priority Value: $localUpdateFlag"
                // Check app update priority
//                if (appUpdateInfo.updatePriority() >= HIGH_PRIORITY_UPDATE){
                if (localUpdateFlag >= HIGH_PRIORITY_UPDATE){

                    Snackbar.make(mainConstraint, "App Update Available", Snackbar.LENGTH_INDEFINITE).show()
                    triggerImmediateAppUpdate(appUpdateInfo)

                }else {
                    // check if flexible update
                    if (//check how much time has passed since the user was notified of an update through the Google Play Store
//                        appUpdateInfo.clientVersionStalenessDays() !=null &&
//                        appUpdateInfo.clientVersionStalenessDays() >= DAYS_FOR_FLEXIBLE_UPDATE
//                        appUpdateInfo.updatePriority() <= LOW_PRIORITY_UPDATE
                        localUpdateFlag <= LOW_PRIORITY_UPDATE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                    ){
                        val snackBar = Snackbar.make(mainConstraint, "A newer version of Available", Snackbar.LENGTH_INDEFINITE).apply {
                            setAction("Ignore") {this.dismiss()}
                            setAction("Update") {triggerFlexibleAppUpdate(appUpdateInfo)}
                        }
                        snackBar.show()

                    }else{
                        Snackbar.make(mainConstraint, "App Update Available", Snackbar.LENGTH_INDEFINITE).show()
                        triggerImmediateAppUpdate(appUpdateInfo)
                    }
                }
            }else{
                tvStatus.text = "App Latest Version Installed \uD83D\uDE22"
                tvUpdateType.text = "No Update Available.."
            }
        }
    }

    private fun triggerFlexibleAppUpdate(appUpdateInfo: AppUpdateInfo) {
        tvUpdateType.text = "LOW FLEXIBLE Update Available 👍😊"
        // Request the update.
        appUpdateManager.startUpdateFlowForResult(
            // Pass the intent that is returned by 'getAppUpdateInfo()'.
            appUpdateInfo,
            // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
            AppUpdateType.FLEXIBLE,
            // The current activity making the update request.
            this,
            // Include a request code to later monitor this update request.
            REQUEST_APP_UPDATE_FLEXIBLE
        )
    }

    private fun triggerImmediateAppUpdate(appUpdateInfo: AppUpdateInfo) {
        tvUpdateType.text = "IMMEDIATE Update Available 👍😊"
        if(appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)){
            // Request an immediate update.
            appUpdateManager.startUpdateFlowForResult(
                // Pass the intent that is returned by 'getAppUpdateInfo()'.
                appUpdateInfo,
                // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                AppUpdateType.IMMEDIATE,
                // The current activity making the update request.
                this,
                // Include a request code to later monitor this update request.
                REQUEST_APP_UPDATE_IMMEDIATE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_APP_UPDATE_IMMEDIATE){
            if (resultCode != Activity.RESULT_OK) {
                Log.i(TAG, "Update flow failed! Result code: $resultCode")
                // If the update is cancelled or fails,
                // you can request to start the update again.
                tvStatus.text = "IMMEDIATE Update flow failed! Result code: $resultCode"
                Toast.makeText(this@MainActivity, "IMMEDIATE Update flow failed! Result code: $resultCode Updating Again", Toast.LENGTH_LONG).show()
                setUpAppUpdate()
            }else {
                Log.i(TAG, "Some other error occurred Result code: $resultCode")
                tvStatus.text = "IMMEDIATE Some other error occurred Result code: $resultCode"
                Toast.makeText(this@MainActivity, "IMMEDIATE Some other error occurred Result code: $resultCode Updating Again", Toast.LENGTH_LONG).show()
                setUpAppUpdate()
            }
        }
        else if (requestCode == REQUEST_APP_UPDATE_FLEXIBLE){
                if(resultCode != Activity.RESULT_OK) {
                    Log.i(TAG, "Update flow failed! Result code: $resultCode")
                    tvStatus.text = "FLEXIBLE Update flow failed! Result code: $resultCode"
                    // If the update is cancelled or fails,
                    // you can request to start the update again.
//                    setUpAppUpdate()
                    Toast.makeText(this@MainActivity, "Update Request Not Approved", Toast.LENGTH_LONG).show()
                }
                else if( resultCode == Activity.RESULT_CANCELED ) {
                    Log.i(TAG, "User cancelled the app update, Result code: $resultCode")
                    tvStatus.text = "FLEXIBLE Update User cancelled the app update, Result code: $resultCode"
                    // If the update is cancelled or fails,
                }
                else if (resultCode == ActivityResult.RESULT_IN_APP_UPDATE_FAILED ) {
                    Log.i(TAG, "Some other error occurred Result code: $resultCode")
                    tvStatus.text = "FLEXIBLE Update Some other error occurred Result code: $resultCode"
                }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::appUpdateManager.isInitialized){
            appUpdateManager
                .appUpdateInfo
                .addOnSuccessListener{ appUpdateInfo ->

                    // If the update is downloaded but not installed,
                    // notify the user to complete the update.
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        popUpSnackbarForCompleteUpdate()
                    }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(this@MainActivity)
    }

    /* Displays the snackbar notification and call to action. */
    private fun popUpSnackbarForCompleteUpdate() {
        Snackbar.make(
            mainConstraint,
            "An update has just been downloaded.",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RESTART") {
                appUpdateManager.completeUpdate()
                appUpdateManager.unregisterListener(this@MainActivity)
            }
            show()
        }
    }

    override fun onStateUpdate(state: InstallState?) {
        when(state?.installStatus()){
            InstallStatus.DOWNLOADING -> {
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytesToDownload = state.totalBytesToDownload()
                tvUpdateType.text = "Updating the app downloaded ${ ((bytesDownloaded * 100) / totalBytesToDownload).toInt() }%"
                // Show update progress bar.
                updateProgressBar(bytesDownloaded, totalBytesToDownload)
            }
            InstallStatus.DOWNLOADED -> {
                // After the update is downloaded, show a notification
                // and request user confirmation to restart the app.
                popUpSnackbarForCompleteUpdate()
            }
            InstallStatus.FAILED -> {
                setUpAppUpdate()
            }
            InstallStatus.INSTALLED -> {
                tvStatus.text = "Latest App version is installed"
                tvUpdateType.text = "App up to date"
            }
        }
    }

    private fun updateProgressBar(bytesDownloaded: Long, totalBytesToDownload: Long) {
        appUpdateProgressBar.progress = ((bytesDownloaded * 100) / totalBytesToDownload).toInt()
    }

}
