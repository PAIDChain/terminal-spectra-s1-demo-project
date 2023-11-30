package my.paidchain.spectraterminaldemo

import android.app.Application
import android.content.pm.PackageManager
import android.content.pm.Signature
import com.spectratech.andext.AndExt
import com.spectratech.andext.AndExtErrors
import com.spectratech.andext.AndExtWhitelist
import com.spectratech.andext.ConnectionCallback
import com.spectratech.andext.ResultCallback
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.Misc.Companion.toHex
import my.paidchain.spectraterminaldemo.common.doUntilComplete
import my.paidchain.spectraterminaldemo.common.log
import my.paidchain.spectraterminaldemo.common.sha256ToBytes
import java.security.NoSuchAlgorithmException

class Bootstrap private constructor() {
    companion object {
        private var self: Bootstrap? = null
        val app: Application = App.instance

        val instance: Bootstrap
            get() {
                if (null == self) {
                    self = Bootstrap()
                }
                return self!!
            }

        var hadError: Throwable? = null
            set(value) {
                field = value
            }
    }

    suspend fun init() {
        // Initialize systems
        setDefaultAppSettings()
    }

    private suspend fun setDefaultAppSettings() {
        doUntilComplete(sleepIntervalInMs = 5000L, isFatal = { error ->
            hadError = ContextAwareError(Errors.Failed.name, "setDefaultAppSettings ERROR: $error")

            log(Level.ERROR, javaClass.simpleName) { "Bootstrap ERROR: $hadError" }
            false
        }, isInterrupted = { false }, process = {
            val andExt = AndExt(app)

            andExt.connect(object : ConnectionCallback {
                override fun onServiceConnected() {
                    try {
                        // Register to auto start
                        whitelist(andExt, AndExtWhitelist.WhitelistType.AUTOSTART_WHITELIST, arrayOf(app.packageName))

                        // Register to have install package ability
                        whitelist(andExt, AndExtWhitelist.WhitelistType.PACKAGE_INSTALLER_WHITELIST, arrayOf("${app.packageName}|${getSignatureFingerprint()}"))
                    } catch (error: Throwable) {
                        hadError = ContextAwareError(Errors.Failed.name, "setDefaultAppSettings ERROR: $error")
                    } finally {
                        andExt.disconnect()
                    }
                }

                override fun onServiceDisconnected() {
                    //
                }
            })
        })
    }

    private fun whitelist(andExt: AndExt, type: AndExtWhitelist.WhitelistType, strings: Array<String>) {
        AndExtWhitelist(andExt).setWhitelist(type, strings, object : ResultCallback() {
            override fun onError(errorNo: Int) {
                if (errorNo != AndExtErrors.SUCCESS) {
                    hadError = ContextAwareError(
                        Errors.Failed.name, "Set whitelist ${type.name} ERROR: $errorNo", mapOf("errorNo" to errorNo)
                    )
                } else {
                    log(Level.INFO, javaClass.simpleName) { "Set whitelist ${type.name}" }
                }
            }
        })
    }


    private fun getSignatureFingerprint(): String {
        try {
            val signs: Array<Signature>? = getRawSignature()

            if (!signs.isNullOrEmpty()) {
                return sha256ToBytes(signs[0].toByteArray()).toHex()
            }
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun getRawSignature(): Array<Signature>? {
        val pkgMgr: PackageManager = app.packageManager

        return try {
            pkgMgr.getPackageInfo(app.packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo.apkContentsSigners
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}