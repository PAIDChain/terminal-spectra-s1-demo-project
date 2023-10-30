package my.paidchain.spectraterminaldemo.controllers.appInstaller

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import com.spectratech.andext.AndExt
import com.spectratech.andext.AndExtLaunchApp
import com.spectratech.andext.ConnectionCallback
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

enum class InstallCode(val value: Int) {
    SUCCEEDED(1),
    FAILED_ALREADY_EXISTS(-1),
    FAILED_INVALID_APK(-2),
    FAILED_INVALID_URI(-3),
    FAILED_INSUFFICIENT_STORAGE(-4),
    FAILED_DUPLICATE_PACKAGE(-5),
    FAILED_NO_SHARED_USER(-6),
    FAILED_UPDATE_INCOMPATIBLE(-7),
    FAILED_SHARED_USER_INCOMPATIBLE(-8),
    FAILED_MISSING_SHARED_LIBRARY(-9),
    FAILED_REPLACE_COULDNT_DELETE(-10),
    FAILED_DEXOPT(-11),
    FAILED_OLDER_SDK(-12),
    FAILED_CONFLICTING_PROVIDER(-13),
    FAILED_NEWER_SDK(-14),
    FAILED_TEST_ONLY(-15),
    FAILED_CPU_ABI_INCOMPATIBLE(-16),
    FAILED_MISSING_FEATURE(-17),
    FAILED_CONTAINER_ERROR(-18),
    FAILED_INVALID_INSTALL_LOCATION(-19),
    FAILED_MEDIA_UNAVAILABLE(-20),
    FAILED_VERIFICATION_TIMEOUT(-21),
    FAILED_VERIFICATION_FAILURE(-22),
    FAILED_PACKAGE_CHANGED(-23),
    FAILED_UID_CHANGED(-24),
    FAILED_VERSION_DOWNGRADE(-25),
    PARSE_FAILED_NOT_APK(-100),
    PARSE_FAILED_BAD_MANIFEST(-101),
    PARSE_FAILED_UNEXPECTED_EXCEPTION(-102),
    PARSE_FAILED_NO_CERTIFICATES(-103),
    PARSE_FAILED_INCONSISTENT_CERTIFICATES(-104),
    PARSE_FAILED_CERTIFICATE_ENCODING(-105),
    PARSE_FAILED_BAD_PACKAGE_NAME(-106),
    PARSE_FAILED_BAD_SHARED_USER_ID(-107),
    PARSE_FAILED_MANIFEST_MALFORMED(-108),
    PARSE_FAILED_MANIFEST_EMPTY(-109),
    FAILED_INTERNAL_ERROR(-110),
    FAILED_USER_RESTRICTED(-111),
    FAILED_DUPLICATE_PERMISSION(-112),
    FAILED_NO_MATCHING_ABIS(-113)
}

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

class AppInstaller private constructor() {
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

    fun launch(packageName: String): Boolean {
        log(Level.WARN, javaClass.simpleName) { "APP_INSTALLER: APP LAUNCHING $packageName" }

        val intent = Bootstrap.app.packageManager.getLaunchIntentForPackage(packageName)

        if (null == intent) {
            log(Level.ERROR, javaClass.simpleName) { "APP_INSTALLER: APP LAUNCH NOT FOUND $packageName" }
            return false
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val andExt = AndExt(Bootstrap.app)
        val callResult = CompletableFuture<Unit>()

        return try {
            andExt.connect(object : ConnectionCallback {
                override fun onServiceConnected() {
                    AndExtLaunchApp(andExt).launchApp(intent, object : AndExtLaunchApp.LaunchAppCallback() {
                        override fun onResult(result: String) {
                            log(Level.WARN, javaClass.simpleName) { "APP_INSTALLER: APP LAUNCH CALLBACK $packageName - $result" }

                            if ("1" == result) {
                                callResult.complete(Unit)
                                return
                            }

                            callResult.completeExceptionally(
                                ContextAwareError(
                                    Errors.Failed.name, "Package launch failed", mapOf("returnCode" to result, "packageName" to packageName)
                                )
                            )
                        }
                    })
                }

                override fun onServiceDisconnected() {

                }
            })

            // Wait for call result
            callResult.get()

            log(Level.WARN, javaClass.simpleName) { "APP_INSTALLER: APP LAUNCHED $packageName" }

            true
        } catch (error: Throwable) {
            log(Level.ERROR, javaClass.simpleName) { "APP_INSTALLER: APP LAUNCH ERROR $error" }
            false
        } finally {
            andExt.disconnect()
        }
    }

    fun install(packageName: String, fileUri: String): String {
        val uri = Uri.parse(fileUri)
        val result = CompletableFuture<String>()

        try {
            val privilegedServiceDelegator = PrivilegedServiceDelegator(fnConnected = { _, privilegedService ->
                log(Level.WARN, javaClass.simpleName) { "APP_INSTALLER: BOUND to privileged service" }

                val callback: IPrivilegedCallback = object : IPrivilegedCallback.Stub() {
                    override fun handleResult(packageName: String, returnCode: Int) {
                        val codeName = InstallCode.values().find { it.value == returnCode }?.name ?: returnCode.toString()

                        log(Level.WARN, javaClass.simpleName) { "APP_INSTALLER: CALLBACK $packageName - $codeName" }

                        if (InstallCode.SUCCEEDED.value == returnCode) {
                            result.complete(packageName)
                            return
                        }

                        // Recovery handling for exceptional cases
                        // TODO: What kind of exceptional cases should be handled?
//                        when(returnCode){
//                            InstallCode.FAILED_UPDATE_INCOMPATIBLE.value -> {
//
//                            }
//                        }

                        result.completeExceptionally(
                            ContextAwareError(
                                Errors.Failed.name, "Package installation failed", mapOf("returnCode" to codeName, "packageName" to packageName)
                            )
                        )
                    }
                }

                try {
                    log(Level.WARN, javaClass.simpleName) { "APP_INSTALLER: INSTALLING $packageName: ${uri.encodedPath}" }

                    if (!privilegedService.hasPrivilegedPermissions()) {
                        throw ContextAwareError(
                            Errors.Forbidden.name, "Install package permission is not granted", mapOf("packageName" to packageName)
                        )
                    }

                    privilegedService.installPackage(uri, ACTION_INSTALL_REPLACE_EXISTING, null, callback)
                } catch (error: Throwable) {
                    log(Level.ERROR, javaClass.simpleName) { "APP_INSTALLER: INSTALL ERROR $error" }
                    result.completeExceptionally(error)
                }
            }, fnError = { error -> result.completeExceptionally(error) })

            val serviceIntent = Intent("$PRIVILEGED_EXTENSION_PACKAGE_NAME.$PRIVILEGED_EXTENSION_SERVICE_INTENT").apply {
                setPackage(PRIVILEGED_EXTENSION_PACKAGE_NAME)
            }

            if (!Bootstrap.app.bindService(serviceIntent, privilegedServiceDelegator, Context.BIND_AUTO_CREATE)) {
                throw ContextAwareError(
                    Errors.NotAvailable.name, "Privileged service is not available", mapOf(
                        "packageName" to "$PRIVILEGED_EXTENSION_PACKAGE_NAME.$PRIVILEGED_EXTENSION_SERVICE_INTENT"
                    )
                )
            }
        } catch (error: Throwable) {
            log(Level.ERROR, javaClass.simpleName) { "APP_INSTALLER: INSTALL ERROR $error" }
            result.completeExceptionally(error)
        }

        val installedPackageName = result.get()

        log(Level.WARN, javaClass.simpleName) { "APP_INSTALLER: INSTALLED $installedPackageName: ${uri.encodedPath}" }

        return installedPackageName
    }
}