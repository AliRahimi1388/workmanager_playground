package com.example.workmanager_jetpackcompose_playground

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import coil.compose.rememberAsyncImagePainter
import com.example.workmanager_jetpackcompose_playground.ui.theme.WorkManagerJetpackComposeplayGroundTheme

class MainActivity : ComponentActivity() {

    private val REQUEST_CODE_NOTIFICATION_PERMISSION = 101

    private val viewModel by viewModels<PhotoViewModel>()

    private var downloadUrl: String? = null // Use a nullable String for download URL

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.POST_NOTIFICATIONS] ?: false
        if (granted) {
            // Permission granted, enable notifications
            // (Your notification logic here)
        } else {
            // Permission denied, inform user
            Toast.makeText(this, "Notifications might be limited", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.imageUri.observe(this) { newUrl ->
            downloadUrl = newUrl?.toString()
        }

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workDataOf(WorkerKeys.IMAGE_URL to downloadUrl))
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            ).build()


        val colorFilterRequest = OneTimeWorkRequestBuilder<ColorFilterWorker>().build()
        val workManager = WorkManager.getInstance(context = applicationContext)

        setContent {
            WorkManagerJetpackComposeplayGroundTheme {


                val workInfos = workManager.getWorkInfosForUniqueWorkLiveData(
                    "download"
                ).observeAsState().value


                val downloadInfo = remember(key1 = workInfos) {
                    workInfos?.find { it.id == downloadRequest.id }
                }

                val filterInfo = remember(key1 = workInfos) {
                    workInfos?.find { it.id == colorFilterRequest.id }
                }

                val imageUri by derivedStateOf {
                    val downloadUri =
                        downloadInfo?.outputData?.getString(WorkerKeys.IMAGE_URL)?.toUri()

                    val filterUri =
                        filterInfo?.outputData?.getString(WorkerKeys.FILTER_URL)?.toUri()

                    filterUri ?: downloadUri
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    imageUri?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(model = uri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Text(text = imageUri.toString())
                    Spacer(modifier = Modifier.height(16.dp))



                    Button(
                        onClick = {
                            requestNotificationPermission()
                            workManager
                                .beginUniqueWork(
                                    "download",
                                    ExistingWorkPolicy.KEEP,
                                    downloadRequest
                                ).then(colorFilterRequest)
                                .enqueue()
                        },
                        enabled = downloadInfo?.state != WorkInfo.State.RUNNING
                    ) {
                        Text("Start Download")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    when (downloadInfo?.state) {
                        WorkInfo.State.RUNNING -> Text("Downloading ...")
                        WorkInfo.State.SUCCEEDED -> Text("Download Succeeded")
                        WorkInfo.State.FAILED -> Text("Download failed")
                        WorkInfo.State.CANCELLED -> Text("Download cancelled")
                        WorkInfo.State.ENQUEUED -> Text("Download enqueued")
                        WorkInfo.State.BLOCKED -> Text("Download blocked")
                        null -> Text("Download Not started.")
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    when (filterInfo?.state) {
                        WorkInfo.State.RUNNING -> Text("applying Filter ...")
                        WorkInfo.State.SUCCEEDED -> Text("Filter Succeeded")
                        WorkInfo.State.FAILED -> Text("Filter failed")
                        WorkInfo.State.CANCELLED -> Text("Filter cancelled")
                        WorkInfo.State.ENQUEUED -> Text("Filter enqueued")
                        WorkInfo.State.BLOCKED -> Text("Filter blocked")
                        null -> Text("Filter not applied")
                    }

                }

            }
        }
    }


    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Explain why and request permission
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Show explanation dialog before requesting permission
                // (Your custom explanation logic here)
                AlertDialog.Builder(this)
                    .setTitle("Notification Permission")
                    .setMessage("Our app needs notification permission to keep you updated. Would you like to grant it?")
                    .setPositiveButton("Grant") { _, _ ->
                        requestNotificationPermissionLauncher.launch(
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                        )
                    }
                    .setNegativeButton("Deny") { dialog, _ ->
                        dialog.dismiss()
                        // Inform user about potential limitations
                        Toast.makeText(this, "Notifications might be limited", Toast.LENGTH_SHORT)
                            .show()
                    }
                    .create()
                    .show()
            } else {
                // No explanation needed, directly request permission
                requestNotificationPermissionLauncher.launch(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                Intent.EXTRA_STREAM, Uri::class.java
            )
        } else {
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        } ?: return

        viewModel.updateUncompressedUri(uri)

    }
}