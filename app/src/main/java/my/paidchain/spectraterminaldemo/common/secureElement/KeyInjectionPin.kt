package my.paidchain.spectraterminaldemo.common.secureElement

import com.spectratech.controllers.PEDDLL
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.Misc.Companion.toHex
import my.paidchain.spectraterminaldemo.common.getKeyValue
import my.paidchain.spectraterminaldemo.common.getKeyValueOrNull
import my.paidchain.spectraterminaldemo.common.log

abstract class KeyInjectionPin(private val controller: KeyInjection) {
    private lateinit var keySlot: KeySlot

    internal fun injectKey(keySlot: KeySlot, params: KeyParam): ByteArray? {
        this.keySlot = keySlot

        return when (keySlot.spec) {
            KeySpec.AES_128 -> keyInjectionPinAes(params)
            KeySpec.AES_192 -> keyInjectionPinAes(params)
            KeySpec.AES_256 -> keyInjectionPinAes(params)
            KeySpec.TDEA -> keyInjectionPinTdea(params)
            KeySpec.DUKPT -> keyInjectionPinDukpt(params)
            KeySpec.DUKPT_AES_128 -> keyInjectionPinDukptAes(params)
            KeySpec.DUKPT_AES_192 -> keyInjectionPinDukptAes(params)
            KeySpec.DUKPT_AES_256 -> keyInjectionPinDukptAes(params)
            KeySpec.RAW -> throw ContextAwareError(
                Errors.NotSupported.name, "Key spec is not supported by Pin type", mapOf("spec" to keySlot.spec, "keySlot" to keySlot)
            )
        }
    }

    private fun keyInjectionPinAes(params: KeyParam): ByteArray? {
        if (true == keySlot.usages?.isNotEmpty()) {
            if (1 != keySlot.usages?.size || null != keySlot.usages?.find { it != KeyUsage.PIN }) {
                throw ContextAwareError(
                    Errors.InvalidParameter.name, "Invalid key usage", mapOf("values" to keySlot.usages, "expected" to KeyUsage.PIN.name)
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

        log(Level.WARN, javaClass.simpleName) { "Inject AES TPK ${keySlot.index}: ${keySlot.code} (KCV: ${kcv?.sliceArray(0..2)?.toHex()})" }

        // KCV must be null when the key is encrypted by TMK
        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_AES, PEDDLL.VALUE_KEY_USAGE_PIN, key, if (0 > tmkSlotIndex) kcv else null, tmkSlotIndex)

        return kcv
    }

    private fun keyInjectionPinTdea(params: KeyParam): ByteArray? {
        if (true == keySlot.usages?.isNotEmpty()) {
            if (1 != keySlot.usages?.size || null != keySlot.usages?.find { it != KeyUsage.PIN }) {
                throw ContextAwareError(
                    Errors.InvalidParameter.name, "Invalid key usage", mapOf("values" to keySlot.usages, "expected" to KeyUsage.PIN.name)
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

        log(Level.WARN, javaClass.simpleName) { "Inject TDEA TPK ${keySlot.index}: ${keySlot.code} (KCV: ${kcv?.sliceArray(0..2)?.toHex()})" }

        // KCV must be null when the key is encrypted by TMK
        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_TDEA, PEDDLL.VALUE_KEY_USAGE_PIN, key, if (0 > tmkSlotIndex) kcv else null, tmkSlotIndex)

        return kcv
    }

    private fun keyInjectionPinDukpt(params: KeyParam): ByteArray? {
        if (true == keySlot.usages?.isNotEmpty()) {
            if (1 != keySlot.usages?.size || null != keySlot.usages?.find { it != KeyUsage.PIN }) {
                throw ContextAwareError(
                    Errors.InvalidParameter.name, "Invalid key usage", mapOf("values" to keySlot.usages, "expected" to KeyUsage.PIN.name)
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

        log(Level.WARN, javaClass.simpleName) { "Inject DUKPT TPK ${keySlot.index}: ${keySlot.code} (KCV: ${kcv?.sliceArray(0..2)?.toHex()})" }

        // KCV must be null when the key is encrypted by TMK
        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_DUKPT, PEDDLL.VALUE_KEY_USAGE_PIN, key, if (0 > tmkSlotIndex) kcv else null, tmkSlotIndex, ksn)

        return kcv
    }

    private fun keyInjectionPinDukptAes(params: KeyParam): ByteArray? {
        if (true == keySlot.usages?.isNotEmpty()) {
            if (1 != keySlot.usages?.size || null != keySlot.usages?.find { it != KeyUsage.DUKPT_AES_PIN_BLOCK_ENCRYPT }) {
                throw ContextAwareError(
                    Errors.InvalidParameter.name, "Invalid key usage", mapOf("values" to keySlot.usages, "expected" to KeyUsage.DUKPT_AES_PIN_BLOCK_ENCRYPT.name)
                )
            }
        }

        val ksn: ByteArray = getKeyValue(params, KeyParamKey.KSN.name, 12)
        val key: ByteArray = getKeyValue(params, KeyParamKey.KEY.name, keySlot.spec.keySize)

        val tmkSlotIndex = keySlot.tmkKeySlot?.index ?: -1

        val kcv = if (0 > tmkSlotIndex) {
            controller.generateKcv("AES/ECB/NoPadding", "AES", key, getKeyValueOrNull(params, KeyParamKey.KCV.name, 3))
        } else {
            getKeyValueOrNull(params, KeyParamKey.KCV.name, 3)
        }

        log(Level.WARN, javaClass.simpleName) { "Inject DUKPT AES TPK ${keySlot.index}: ${keySlot.code} (KCV: ${kcv?.sliceArray(0..2)?.toHex()})" }

        // KCV must be null when the key is encrypted by TMK
        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_DUKPTAES, PEDDLL.VALUE_DUKPTAES_KEYUSAGE_PINBLKENCRYPT, key, if (0 > tmkSlotIndex) kcv else null, tmkSlotIndex, ksn)

        return kcv
    }
}
