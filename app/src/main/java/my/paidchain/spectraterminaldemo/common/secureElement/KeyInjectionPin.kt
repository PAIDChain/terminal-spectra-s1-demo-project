package my.paidchain.spectraterminaldemo.common.secureElement

import com.spectratech.controllers.PEDDLL
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.getKeyValue
import my.paidchain.spectraterminaldemo.common.getKeyValueOrNull
import my.paidchain.spectraterminaldemo.common.log
import my.paidchain.spectraterminaldemo.common.toHex

abstract class KeyInjectionPin(private val controller: KeyInjection) {
    private lateinit var keySlot: KeySlot

    internal fun injectKey(keySlot: KeySlot, params: KeyParam) {
        this.keySlot = keySlot

        when (keySlot.spec) {
            KeySpec.AES_128 -> keyInjectionPinAes(params)
            KeySpec.AES_192 -> keyInjectionPinAes(params)
            KeySpec.AES_256 -> keyInjectionPinAes(params)
            KeySpec.TDEA -> keyInjectionPinTdea(params)
            KeySpec.DUKPT -> keyInjectionPinDukpt(params)
            KeySpec.DUKPT_AES_128 -> keyInjectionPinDukptAes(params)
            KeySpec.DUKPT_AES_192 -> keyInjectionPinDukptAes(params)
            KeySpec.DUKPT_AES_256 -> keyInjectionPinDukptAes(params)
        }
    }

    private fun keyInjectionPinAes(params: KeyParam) {
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
            null // KCV must be null when the key is encrypted by TMK
        }

        log(Level.WARN, javaClass.simpleName) { "Inject AES TPK ${keySlot.code} ${key.size}:${key.toHex()} (KCV: ${kcv?.toHex()})" }

        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_AES, PEDDLL.VALUE_KEY_USAGE_PIN, key, kcv, tmkSlotIndex)
    }

    private fun keyInjectionPinTdea(params: KeyParam) {
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
            null // KCV must be null when the key is encrypted by TMK
        }

        log(Level.WARN, javaClass.simpleName) { "Inject TDEA TPK ${keySlot.code} ${key.size}:${key.toHex()} (KCV: ${kcv?.toHex()})" }

        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_TDEA, PEDDLL.VALUE_KEY_USAGE_PIN, key, kcv, tmkSlotIndex)
    }

    private fun keyInjectionPinDukpt(params: KeyParam) {
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
            null // KCV must be null when the key is encrypted by TMK
        }

        log(Level.WARN, javaClass.simpleName) { "Inject DUKPT TPK ${keySlot.code} ${key.size}:${key.toHex()} (KCV: ${kcv?.toHex()})" }

        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_DUKPT, PEDDLL.VALUE_KEY_USAGE_PIN, key, kcv, tmkSlotIndex, ksn)
    }

    private fun keyInjectionPinDukptAes(params: KeyParam) {
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
            null // KCV must be null when the key is encrypted by TMK
        }

        log(Level.WARN, javaClass.simpleName) { "Inject DUKPT AES TPK ${keySlot.code} ${key.size}:${key.toHex()} (KCV: ${kcv?.toHex()})" }

        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_DUKPTAES, PEDDLL.VALUE_DUKPTAES_KEYUSAGE_PINBLKENCRYPT, key, kcv, tmkSlotIndex, ksn)
    }
}
