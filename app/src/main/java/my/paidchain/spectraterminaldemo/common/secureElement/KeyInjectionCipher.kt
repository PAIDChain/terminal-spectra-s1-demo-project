package my.paidchain.spectraterminaldemo.common.secureElement

import com.spectratech.controllers.PEDDLL
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.Misc.Companion.toHex
import my.paidchain.spectraterminaldemo.common.getKeyValue
import my.paidchain.spectraterminaldemo.common.getKeyValueOrNull
import my.paidchain.spectraterminaldemo.common.log

abstract class KeyInjectionCipher(private val controller: KeyInjection) {
    private lateinit var keySlot: KeySlot

    internal fun injectKey(keySlot: KeySlot, params: KeyParam): ByteArray? {
        this.keySlot = keySlot

        val tmkSlotIndex = keySlot.tmkKeySlot?.index ?: -1

        val keyUsage = keySlot.usages?.map { it.value }?.reduce { acc, keyUsage -> (acc.toInt() or keyUsage.toInt()).toByte() } ?: throw ContextAwareError(
            Errors.NotConfigured.name, "Key usage is not configured", mapOf("slotCode" to keySlot.code)
        )

        return when (keySlot.spec) {
            KeySpec.AES_128,
            KeySpec.AES_192,
            KeySpec.AES_256 -> keyInjectionCipherAes(params, tmkSlotIndex, keyUsage)

            KeySpec.TDEA -> keyInjectionCipherTdea(params, tmkSlotIndex, keyUsage)
            KeySpec.DUKPT -> keyInjectionCipherDukpt(params, tmkSlotIndex, keyUsage)

            KeySpec.DUKPT_AES_128,
            KeySpec.DUKPT_AES_192,
            KeySpec.DUKPT_AES_256 -> keyInjectionCipherDukptAes(params, tmkSlotIndex, keyUsage)

            KeySpec.RAW -> throw ContextAwareError(
                Errors.NotSupported.name, "Key spec is not supported by Cipher type", mapOf("spec" to keySlot.spec, "keySlot" to keySlot)
            )
        }
    }

    private fun keyInjectionCipherAes(params: KeyParam, tmkSlotIndex: Short, keyUsage: Byte): ByteArray? {
        val key: ByteArray = getKeyValue(params, KeyParamKey.KEY.name, keySlot.spec.keySize)

        val kcv = if (0 > tmkSlotIndex) {
            controller.generateKcv("AES/ECB/NoPadding", "AES", key, getKeyValueOrNull(params, KeyParamKey.KCV.name, 3))
        } else {
            getKeyValueOrNull(params, KeyParamKey.KCV.name, 3)
        }

        log(Level.WARN, javaClass.simpleName) { "Inject Cipher AES ${keySlot.index}: ${keySlot.code} (KCV: ${kcv?.sliceArray(0..2)?.toHex()})" }

        // KCV must be null when the key is encrypted by TMK
        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_AES, keyUsage, key, if (0 > tmkSlotIndex) kcv else null, tmkSlotIndex)

        return kcv
    }

    private fun keyInjectionCipherTdea(params: KeyParam, tmkSlotIndex: Short, keyUsage: Byte): ByteArray? {
        val key: ByteArray = getKeyValue(params, KeyParamKey.KEY.name, keySlot.spec.keySize)

        val kcv = if (0 > tmkSlotIndex) {
            controller.generateKcv("DESede/ECB/NoPadding", "DESede", key, getKeyValueOrNull(params, KeyParamKey.KCV.name, 3))
        } else {
            getKeyValueOrNull(params, KeyParamKey.KCV.name, 3)
        }

        log(Level.WARN, javaClass.simpleName) { "Inject Cipher TDEA ${keySlot.index}: ${keySlot.code} (KCV: ${kcv?.sliceArray(0..2)?.toHex()})" }

        // KCV must be null when the key is encrypted by TMK
        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_TDEA, keyUsage, key, if (0 > tmkSlotIndex) kcv else null, tmkSlotIndex)

        return kcv
    }

    private fun keyInjectionCipherDukpt(params: KeyParam, tmkSlotIndex: Short, keyUsage: Byte): ByteArray? {
        val ksn: ByteArray = getKeyValue(params, KeyParamKey.KSN.name, 10)
        val key: ByteArray = getKeyValue(params, KeyParamKey.KEY.name, keySlot.spec.keySize)

        val kcv = if (0 > tmkSlotIndex) {
            controller.generateKcv("DESede/ECB/NoPadding", "DESede", key, getKeyValueOrNull(params, KeyParamKey.KCV.name, 3))
        } else {
            getKeyValueOrNull(params, KeyParamKey.KCV.name, 3)
        }

        log(Level.WARN, javaClass.simpleName) { "Inject Cipher DUKPT ${keySlot.index}: ${keySlot.code} (KCV: ${kcv?.sliceArray(0..2)?.toHex()})" }

        // KCV must be null when the key is encrypted by TMK
        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_DUKPT, keyUsage, key, if (0 > tmkSlotIndex) kcv else null, tmkSlotIndex, ksn)

        return kcv
    }

    private fun keyInjectionCipherDukptAes(params: KeyParam, tmkSlotIndex: Short, keyUsage: Byte): ByteArray? {
        val ksn: ByteArray = getKeyValue(params, KeyParamKey.KSN.name, 12)
        val key: ByteArray = getKeyValue(params, KeyParamKey.KEY.name, keySlot.spec.keySize)

        val kcv = if (0 > tmkSlotIndex) {
            controller.generateKcv("AES/ECB/NoPadding", "AES", key, getKeyValueOrNull(params, KeyParamKey.KCV.name, 3))
        } else {
            getKeyValueOrNull(params, KeyParamKey.KCV.name, 3)
        }

        val tmkIv: ByteArray? = if (0 <= tmkSlotIndex) {
            getKeyValue(params, KeyParamKey.IV.name, keySlot.spec.blockSize)
        } else null

        val deriveKeyType = when (keySlot.spec.keySize) {
            16 -> PEDDLL.VALUE_DUKPTAES_KEYTYPE_AES128.toByte()
            24 -> PEDDLL.VALUE_DUKPTAES_KEYTYPE_AES192.toByte()
            32 -> PEDDLL.VALUE_DUKPTAES_KEYTYPE_AES256.toByte()
            else -> throw ContextAwareError(Errors.NotSupported.name, "Key size not supported", mapOf("keySize" to keySlot.spec.keySize))
        }

        log(Level.WARN, javaClass.simpleName) { "Inject Cipher DUKPT AES ${keySlot.index}: ${keySlot.code} (KCV: ${kcv?.sliceArray(0..2)?.toHex()})" }

        // KCV must be null when the key is encrypted by TMK
        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_DUKPTAES, keyUsage, key, if (0 > tmkSlotIndex) kcv else null, tmkSlotIndex, ksn, deriveKeyType, tmkIv)

        return kcv
    }
}
