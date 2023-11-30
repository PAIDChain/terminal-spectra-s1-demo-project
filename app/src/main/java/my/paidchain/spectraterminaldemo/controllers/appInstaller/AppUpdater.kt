package my.paidchain.spectraterminaldemo.controllers.appInstaller

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import my.paidchain.spectraterminaldemo.Bootstrap
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.log

class AppUpdater {
    private val app = Bootstrap.app
    private val dm: DownloadManager = Bootstrap.app.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    private var processIsRequired = false
    private var processInProgress = false

    companion object {
        private var self: AppUpdater? = null

        val instance: AppUpdater
            get() {
                if (null == self) {
                    self = AppUpdater()
                }
                return self!!
            }
    }

    fun download(url: String) {
        val request = DownloadManager.Request(Uri.parse(url))

        request.setTitle("Test")
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Test")

        // DownloadManager to handle download
        val downloadId = dm.enqueue(request)

        log(Level.INFO, javaClass.simpleName) { "APP_UPDATER: FILE $downloadId - STARTED - $url" }
    }

    fun process(downloadId: Long) {
        if (requestToProcess()) {
            log(Level.INFO, javaClass.simpleName) { "APP_UPDATER: Process lock is granted" }

            var isCompleted: Boolean

            do {
                isCompleted = doProcess(downloadId)
            } while (isProcessRequired(isCompleted))

            log(Level.INFO, javaClass.simpleName) { "APP_UPDATER: Process lock has been released" }
        }
    }

    private fun requestToProcess(): Boolean {
        return synchronized(this) {
            if (processInProgress) {
                processIsRequired = true
                false
            } else {
                processInProgress = true
                true
            }
        }
    }

    private fun isProcessRequired(isCompleted: Boolean): Boolean {
        return synchronized(this) {
            if (!processIsRequired || isCompleted) {
                processInProgress = false
                processIsRequired = false
                false
            } else {
                processIsRequired = false
                true
            }
        }
    }

    private fun doProcess(downloadId: Long): Boolean {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = dm.query(query)

        if (cursor.moveToFirst()) {
            val localUri = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))

            if (null != localUri) {
                var packageName = "App"

                log(Level.WARN, javaClass.simpleName) { "APP_UPDATER: INSTALL $packageName" }

                packageName = AppInstaller.instance.install("App", "", localUri)

                dm.remove(downloadId)

                log(Level.WARN, javaClass.simpleName) { "APP_UPDATER: LAUNCH $packageName" }

                if (AppInstaller.instance.launch(packageName)) {
                    log(Level.WARN, javaClass.simpleName) { "APP_UPDATER: INSTALLATION has been COMPLETED - $packageName" }
                } else {
                    log(Level.ERROR, javaClass.simpleName) { "APP_UPDATER: INSTALLATION has FAILED - $packageName" }
                }
            }
        }

        return true
    }
}