package my.paidchain.spectraterminaldemo.controllers.appInstaller

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import my.paidchain.spectraterminaldemo.Bootstrap
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.log

class AppDownloader : BroadcastReceiver() {
    private val app = Bootstrap.app

    init {
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        app.registerReceiver(this, filter)

        log(Level.WARN, javaClass.simpleName) { "Registered ACTION_DOWNLOAD_COMPLETE event" }
    }

    override fun onReceive(context: Context, intent: Intent) {
        log(Level.INFO, javaClass.simpleName) { "APP_DOWNLOADER: Event ${intent.action}" }

        CoroutineScope(Dispatchers.Default).launch {
            try {
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent.action) {
                    val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                    if (0L >= downloadId) {
                        throw ContextAwareError(Errors.InvalidParameter.name, "Invalid download id", mapOf("downloadId" to downloadId))
                    }

                    AppUpdater.instance.process(downloadId)
                }
            } catch (error: Throwable) {
                log(Level.ERROR, javaClass.simpleName) { "APP_DOWNLOADER: ERROR $error" }
            }
        }
    }
}