package my.paidchain.spectraterminaldemo.common.secureElement

import android.app.Application
import com.spectratech.controllers.KKeyDllController
import com.spectratech.controllers.KeyDllController
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.getKeyValue
import my.paidchain.spectraterminaldemo.common.getKeyValueOrNull

abstract class KeyCipherTdea(private val app: Application, private val controller: KeyDllController) {
    internal fun encrypt(keySlot: KeySlot, params: KeyParam): ByteArray {
        val data: ByteArray = getKeyValue(params, KeyParamKey.DATA.name)

        val result = when (getKeyValueOrNull(params, KeyParamKey.CIPHER_MODE.name) as CipherMode?) {
            null -> controller.cipher(app, keySlot.index, KKeyDllController.CipherModeConstant.K_Encrypt, data)
            CipherMode.CBC -> {
                val iv: ByteArray = getKeyValue(params, KeyParamKey.IV.name)
                controller.cipher(app, keySlot.index, (KKeyDllController.CipherModeConstant.K_Encrypt.toInt() or CipherMode.CBC.value).toShort(), data, iv)
            }

            CipherMode.CBC_MASK -> TODO()
        }

        if (-1 != result.resultCode) {
            throw ContextAwareError(
                Errors.Failed.name, "Encryption failed", mapOf("resultCode" to result.resultCode)
            )
        }

        return result.responseBytes
    }

    internal fun decrypt(keySlot: KeySlot, params: KeyParam): ByteArray {
        val data: ByteArray = getKeyValue(params, KeyParamKey.DATA.name)

        val result = when (getKeyValueOrNull(params, KeyParamKey.CIPHER_MODE.name) as CipherMode?) {
            null -> controller.cipher(app, keySlot.index, KKeyDllController.CipherModeConstant.K_Decrypt, data)
            CipherMode.CBC -> {
                val iv: ByteArray = getKeyValue(params, KeyParamKey.IV.name)
                controller.cipher(app, keySlot.index, (KKeyDllController.CipherModeConstant.K_Decrypt.toInt() or CipherMode.CBC.value).toShort(), data, iv)
            }

            CipherMode.CBC_MASK -> TODO()
        }

        if (-1 != result.resultCode) {
            throw ContextAwareError(
                Errors.Failed.name, "Decryption failed", mapOf("resultCode" to result.resultCode)
            )
        }

        return result.responseBytes
    }
}