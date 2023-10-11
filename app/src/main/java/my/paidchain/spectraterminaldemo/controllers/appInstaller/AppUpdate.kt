package my.paidchain.spectraterminaldemo.controllers.appInstaller

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import my.paidchain.spectraterminaldemo.Bootstrap
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.log

class AppUpdate : BroadcastReceiver() {
    private val app = Bootstrap.app
    private val dm: DownloadManager = Bootstrap.app.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    init {
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        app.registerReceiver(this, filter)
    }

    companion object {
        private var self: AppUpdate? = null

        val instance: AppUpdate
            get() {
                if (null == self) {
                    self = AppUpdate()
                }
                return self!!
            }
    }

    override fun onReceive(context: Context, intent: Intent) {
        log(Level.INFO, javaClass.simpleName) { "GET UPDATES: Event ${intent.action}" }

        CoroutineScope(Dispatchers.Default).launch {
            try {
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent.action) {
                    val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = dm.query(query)

                    if (cursor.moveToFirst()) {
                        val localUri = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))

                        log(Level.WARN, javaClass.simpleName) { "APP UPDATES: Prepare to install $localUri" }

                        AppInstaller.instance.install(localUri)
                    }
                }
            } catch (error: Throwable) {
                log(Level.ERROR, javaClass.simpleName) { "GET UPDATES: ERROR $error" }
            }
        }
    }

    fun download(url: String) {
        val request = DownloadManager.Request(Uri.parse(url))

        // DownloadManager to handle download
        val downloadId = dm.enqueue(request)

        log(Level.INFO, javaClass.simpleName) { "APP UPDATES: FILE $downloadId - STARTED - $url" }
    }
}