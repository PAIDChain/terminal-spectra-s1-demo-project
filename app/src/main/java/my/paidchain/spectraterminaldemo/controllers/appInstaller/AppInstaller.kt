package my.paidchain.spectraterminaldemo.controllers.appInstaller

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import my.paidchain.spectraterminaldemo.Bootstrap
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.log
import org.fdroid.fdroid.privileged.IPrivilegedCallback
import org.fdroid.fdroid.privileged.IPrivilegedService
import java.util.concurrent.CompletableFuture

const val PRIVILEGED_EXTENSION_PACKAGE_NAME = "org.fdroid.fdroid.privileged"
const val PRIVILEGED_EXTENSION_SERVICE_INTENT = "IPrivilegedService"
const val ACTION_INSTALL_REPLACE_EXISTING = 2
const val INSTALL_SUCCEEDED = 1

internal class PrivilegedServiceDelegator(
    private val fnConnected: (name: ComponentName, service: IPrivilegedService) -> Unit,
    private val fnError: (error: Throwable) -> Unit
) : ServiceConnection {
    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                fnConnected(name, IPrivilegedService.Stub.asInterface(service))
            } catch (error: Throwable) {
                fnError(error)
            }
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
    }
}

class AppInstaller {
    companion object {
        private var self: AppInstaller? = null

        val instance: AppInstaller
            get() {
                if (null == self) {
                    self = AppInstaller()
                }
                return self!!
            }
    }

    fun install(url: String): String {
        val uri = Uri.parse(url)
        val result = CompletableFuture<String>()

        try {
            val privilegedServiceDelegator = PrivilegedServiceDelegator(fnConnected = { _, privilegedService ->
                val callback: IPrivilegedCallback = object : IPrivilegedCallback.Stub() {
                    override fun handleResult(packageName: String, returnCode: Int) {
                        log(Level.WARN, javaClass.simpleName) { "APP UPDATES: CALLBACK - $packageName" }

                        if (INSTALL_SUCCEEDED == returnCode) {
                            log(Level.WARN, javaClass.simpleName) { "APP UPDATES: CALLBACK completed - $packageName" }
                            result.complete(packageName)
                        } else {
                            result.completeExceptionally(
                                ContextAwareError(
                                    Errors.Failed.name, "Package installation failed", mapOf("returnCode" to returnCode, "packageName" to packageName)
                                )
                            )
                        }
                    }
                }

                try {
                    log(Level.WARN, javaClass.simpleName) { "APP UPDATES: PREPARING TO INSTALL ${uri.encodedPath}" }

                    if (!privilegedService.hasPrivilegedPermissions()) {
                        throw ContextAwareError(
                            Errors.Forbidden.name, "Install package permission is not granted", mapOf("url" to url)
                        )
                    }

                    log(Level.WARN, javaClass.simpleName) { "APP UPDATES: INSTALL ${uri.encodedPath}" }

                    privilegedService.installPackage(uri, ACTION_INSTALL_REPLACE_EXISTING, null, callback)

                    log(Level.WARN, javaClass.simpleName) { "APP UPDATES: INSTALLING ${uri.encodedPath}" }
                } catch (error: Throwable) {
                    log(Level.ERROR, javaClass.simpleName) { "APP UPDATES: INSTALL ERROR $error" }
                    result.completeExceptionally(error)
                }
            }, fnError = { error -> result.completeExceptionally(error) })

            val serviceIntent = Intent("$PRIVILEGED_EXTENSION_PACKAGE_NAME.$PRIVILEGED_EXTENSION_SERVICE_INTENT")

            serviceIntent.setPackage(PRIVILEGED_EXTENSION_PACKAGE_NAME)

            Bootstrap.app.bindService(serviceIntent, privilegedServiceDelegator, Context.BIND_AUTO_CREATE)
        } catch (error: Throwable) {
            log(Level.ERROR, javaClass.simpleName) { "APP UPDATES: INSTALL ERROR $error" }
            result.completeExceptionally(error)
        }

        val installedPackageName = result.get()

        log(Level.WARN, javaClass.simpleName) { "APP UPDATES: INSTALLED $installedPackageName: ${uri.encodedPath}" }

        return installedPackageName
    }
}