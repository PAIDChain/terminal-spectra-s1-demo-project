package my.paidchain.spectraterminaldemo.common.secureElement

import android.app.Application
import com.spectratech.controllers.KKeyDllController
import com.spectratech.controllers.KeyDllController
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.getKeyValue
import my.paidchain.spectraterminaldemo.common.getKeyValueOrNull

abstract class KeyCipherDukptAes(private val app: Application, private val controller: KeyDllController) {
    internal fun encrypt(keySlot: KeySlot, params: KeyParam): ByteArray {
        val ksn: ByteArray = getKeyValue(params, KeyParamKey.KSN.name, 12)
        val data: ByteArray = run {
            val value: ByteArray = getKeyValue(params, KeyParamKey.DATA.name)

            // Perform PKCS5 padding
            KeyCipher.padDataToBlockSize(value, keySlot.spec.blockSize)
        }

        val keyUsage = keySlot.usages?.map { it.value }?.reduce { acc, keyUsage -> (acc.toInt() or keyUsage.toInt()).toByte() } ?: throw ContextAwareError(
            Errors.NotConfigured.name, "Key usage is not configured", mapOf("slotCode" to keySlot.code)
        )

        val result = when (getKeyValueOrNull(params, KeyParamKey.CIPHER_MODE.name) as CipherMode?) {
            null -> {
                controller.dukptAesCipher(app, keySlot.index, ksn, KKeyDllController.CipherModeConstant.K_Encrypt, keySlot.spec.keyType, keyUsage, data, null)
            }

            CipherMode.CBC -> {
                val iv: ByteArray = getKeyValue(params, KeyParamKey.IV.name)
                controller.dukptAesCipher(
                    app, keySlot.index, ksn, (KKeyDllController.CipherModeConstant.K_Encrypt.toInt() or CipherMode.CBC.value).toShort(), keySlot.spec.keyType, keyUsage, data, iv
                )
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
        val ksn: ByteArray = getKeyValue(params, KeyParamKey.KSN.name, 12)
        val data: ByteArray = getKeyValue(params, KeyParamKey.DATA.name)

        val keyUsage = keySlot.usages?.map { it.value }?.reduce { acc, keyUsage -> (acc.toInt() or keyUsage.toInt()).toByte() } ?: throw ContextAwareError(
            Errors.NotConfigured.name, "Key usage is not configured", mapOf("slotCode" to keySlot.code)
        )

        val result = when (getKeyValueOrNull(params, KeyParamKey.CIPHER_MODE.name) as CipherMode?) {
            null -> controller.dukptAesCipher(app, keySlot.index, ksn, KKeyDllController.CipherModeConstant.K_Decrypt, keySlot.spec.keyType, keyUsage, data, null)
            CipherMode.CBC -> {
                val iv: ByteArray = getKeyValue(params, KeyParamKey.IV.name)
                controller.dukptAesCipher(
                    app, keySlot.index, ksn, (KKeyDllController.CipherModeConstant.K_Decrypt.toInt() or CipherMode.CBC.value).toShort(), keySlot.spec.keyType, keyUsage, data, iv
                )
            }

            CipherMode.CBC_MASK -> TODO()
        }

        if (-1 != result.resultCode) {
            throw ContextAwareError(
                Errors.Failed.name, "Decryption failed", mapOf("resultCode" to result.resultCode)
            )
        }

        return KeyCipher.unpadDataFromBlockSize(result.responseBytes, keySlot.spec.blockSize)
    }
}