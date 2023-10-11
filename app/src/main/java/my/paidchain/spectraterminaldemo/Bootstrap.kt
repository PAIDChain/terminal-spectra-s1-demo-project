package my.paidchain.spectraterminaldemo

import android.app.Application
import android.content.pm.PackageManager
import android.content.pm.Signature
import com.spectratech.andext.AndExt
import com.spectratech.andext.AndExtErrors
import com.spectratech.andext.AndExtWhitelist
import com.spectratech.andext.ConnectionCallback
import com.spectratech.andext.ResultCallback
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.log
import my.paidchain.spectraterminaldemo.common.Misc.Companion.toHex
import my.paidchain.spectraterminaldemo.common.sha256ToBytes
import java.security.NoSuchAlgorithmException

class Bootstrap private constructor(private val app: Application) {
    companion object {
        private var self: Bootstrap? = null
        lateinit var app: Application

        fun init(app: Application) {
            if (null == self) {
                this.app = app
                self = Bootstrap(app)
                self!!.init()
            }
        }
    }

    fun init() {
        // Initialize systems
        setDefaultAppSettings()
    }

    private fun setDefaultAppSettings() {
        val mAndExt = AndExt(app)

        try {
            mAndExt.connect(object : ConnectionCallback {
                override fun onServiceConnected() {
                    val installer = "${app.packageName}|${getSignatureFingerprint()}"

                    AndExtWhitelist(mAndExt).setWhitelist(AndExtWhitelist.WhitelistType.PACKAGE_INSTALLER_WHITELIST, arrayOf(installer), object : ResultCallback() {
                        override fun onError(err: Int) {
                            if (err != AndExtErrors.SUCCESS) {
                                log(Level.ERROR, javaClass.simpleName) { "PACKAGE_INSTALLER_WHITELIST setWhitelist error: $err" }
                            } else {
                                log(Level.INFO, javaClass.simpleName) { "PACKAGE_INSTALLER_WHITELIST setWhitelist success: $installer" }
                            }
                        }
                    })
                }

                override fun onServiceDisconnected() {
                }
            })
        } catch (error: Throwable) {
            error.printStackTrace()
        } finally {
            mAndExt.disconnect()
        }
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