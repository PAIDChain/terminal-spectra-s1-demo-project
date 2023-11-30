package my.paidchain.spectraterminaldemo.common.secureElement

import com.spectratech.controllers.PEDDLL
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.Misc.Companion.toHex
import my.paidchain.spectraterminaldemo.common.getKeyValue
import my.paidchain.spectraterminaldemo.common.getKeyValueOrNull
import my.paidchain.spectraterminaldemo.common.log

abstract class KeyInjectionMac(private val controller: KeyInjection) {
    private lateinit var keySlot: KeySlot

    internal fun injectKey(keySlot: KeySlot, params: KeyParam): ByteArray? {
        this.keySlot = keySlot

        return when (keySlot.spec) {
            KeySpec.AES_128 -> keyInjectionMacAes(params)
            KeySpec.AES_192 -> keyInjectionMacAes(params)
            KeySpec.AES_256 -> keyInjectionMacAes(params)
            KeySpec.TDEA -> keyInjectionMacTdea(params)
            KeySpec.DUKPT -> keyInjectionMacDukpt(params)
            KeySpec.DUKPT_AES_128 -> keyInjectionMacDukptAes(params)
            KeySpec.DUKPT_AES_192 -> keyInjectionMacDukptAes(params)
            KeySpec.DUKPT_AES_256 -> keyInjectionMacDukptAes(params)
            KeySpec.RAW -> throw ContextAwareError(
                Errors.NotSupported.name, "Key spec is not supported by Mac type", mapOf("spec" to keySlot.spec, "keySlot" to keySlot)
            )
        }
    }

    private fun keyInjectionMacAes(params: KeyParam): ByteArray? {
        if (true == keySlot.usages?.isNotEmpty()) {
            if (1 != keySlot.usages?.size || null != keySlot.usages?.find { it != KeyUsage.MAC }) {
                throw ContextAwareError(
                    Errors.InvalidParameter.name, "Invalid key usage", mapOf("values" to keySlot.usages, "expected" to KeyUsage.MAC.name)
                )
            }
        }

        val key: ByteArray = getKeyValue(params, KeyParamKey.KEY.name, keySlot.spec.keySize)

        val tmkSlotIndex = keySlot.tmkKeySlot?.index ?: -1

        val kcv = if (0 > tmkSlotIndex) {
            controller.generateKcv("AES/ECB/NoPadding", "AES", key, getKeyValueOrNull(params, KeyParamKey.KCV.name, 3))
        } else {
            getKeyValueOrNull(params, KeyParamKey.KCV.name, 3)
        }

        log(Level.WARN, javaClass.simpleName) { "Inject MAC AES ${keySlot.index}: ${keySlot.code} (KCV: ${kcv?.sliceArray(0..2)?.toHex()})" }

        // KCV must be null when the key is encrypted by TMK
        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_AES, PEDDLL.VALUE_KEY_USAGE_MAC, key, if (0 > tmkSlotIndex) kcv else null, tmkSlotIndex)

        return kcv
    }

    private fun keyInjectionMacTdea(params: KeyParam): ByteArray? {
        if (true == keySlot.usages?.isNotEmpty()) {
            if (1 != keySlot.usages?.size || null != keySlot.usages?.find { it != KeyUsage.MAC }) {
                throw ContextAwareError(
                    Errors.InvalidParameter.name, "Invalid key usage", mapOf("values" to keySlot.usages, "expected" to KeyUsage.MAC.name)
                )
            }
        }

        val key: ByteArray = getKeyValue(params, KeyParamKey.KEY.name, keySlot.spec.keySize)

        val tmkSlotIndex = keySlot.tmkKeySlot?.index ?: -1

        val kcv = if (0 > tmkSlotIndex) {
            controller.generateKcv("DESede/ECB/NoPadding", "DESede", key, getKeyValueOrNull(params, KeyParamKey.KCV.name, 3))
        } else {
            getKeyValueOrNull(params, KeyParamKey.KCV.name, 3)
        }

        log(Level.WARN, javaClass.simpleName) { "Inject MAC TDEA ${keySlot.index}: ${keySlot.code} (KCV: ${kcv?.sliceArray(0..2)?.toHex()})" }

        // KCV must be null when the key is encrypted by TMK
        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_TDEA, PEDDLL.VALUE_KEY_USAGE_MAC, key, if (0 > tmkSlotIndex) kcv else null, tmkSlotIndex)

        return kcv
    }

    private fun keyInjectionMacDukpt(params: KeyParam): ByteArray? {
        if (true == keySlot.usages?.isNotEmpty()) {
            if (1 != keySlot.usages?.size || null != keySlot.usages?.find { it != KeyUsage.MAC }) {
                throw ContextAwareError(
                    Errors.InvalidParameter.name, "Invalid key usage", mapOf("values" to keySlot.usages, "expected" to KeyUsage.MAC.name)
                )
            }
        }

        val ksn: ByteArray = getKeyValue(params, KeyParamKey.KSN.name, 10)

        val key: ByteArray = getKeyValue(params, KeyParamKey.KEY.name, keySlot.spec.keySize)

        val tmkSlotIndex = keySlot.tmkKeySlot?.index ?: -1

        val kcv = if (0 > tmkSlotIndex) {
            controller.generateKcv("DESede/ECB/NoPadding", "DESede", key, getKeyValueOrNull(params, KeyParamKey.KCV.name, 3))
        } else {
            getKeyValueOrNull(params, KeyParamKey.KCV.name, 3)
        }

        log(Level.WARN, javaClass.simpleName) { "Inject MAC DUKPT ${keySlot.index}: ${keySlot.code} (KCV: ${kcv?.sliceArray(0..2)?.toHex()})" }

        // KCV must be null when the key is encrypted by TMK
        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_DUKPT, PEDDLL.VALUE_KEY_USAGE_MAC, key, if (0 > tmkSlotIndex) kcv else null, tmkSlotIndex, ksn)

        return kcv
    }

    private fun keyInjectionMacDukptAes(params: KeyParam): ByteArray? {
        val ksn: ByteArray = getKeyValue(params, KeyParamKey.KSN.name, 12)
        val key: ByteArray = getKeyValue(params, KeyParamKey.KEY.name, keySlot.spec.keySize)

        val usage = (PEDDLL.VALUE_DUKPTAES_KEYUSAGE_MACGENERATE.toInt() or PEDDLL.VALUE_DUKPTAES_KEYUSAGE_MACVERIFY.toInt()).toByte()

        val tmkSlotIndex = keySlot.tmkKeySlot?.index ?: -1

        val kcv = if (0 > tmkSlotIndex) {
            controller.generateKcv("AES/ECB/NoPadding", "AES", key, getKeyValueOrNull(params, KeyParamKey.KCV.name, 3))
        } else {
            getKeyValueOrNull(params, KeyParamKey.KCV.name, 3)
        }

        log(Level.WARN, javaClass.simpleName) { "Inject MAC DUKPT AES ${keySlot.index}: ${keySlot.code} (KCV: ${kcv?.sliceArray(0..2)?.toHex()})" }

        // KCV must be null when the key is encrypted by TMK
        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_DUKPTAES, usage, key, if (0 > tmkSlotIndex) kcv else null, tmkSlotIndex, ksn)

        return kcv
    }
}
