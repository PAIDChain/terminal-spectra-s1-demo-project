package my.paidchain.spectraterminaldemo.common.secureElement

import com.spectratech.controllers.PEDDLL
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.getKeyValue
import my.paidchain.spectraterminaldemo.common.getKeyValueOrNull

abstract class KeyInjectionMac(private val controller: KeyInjection) {
    private lateinit var keySlot: KeySlot

    internal fun injectKey(keySlot: KeySlot, params: KeyParam) {
        this.keySlot = keySlot

        when (keySlot.spec) {
            KeySpec.AES_128 -> keyInjectionMacAes(params)
            KeySpec.AES_192 -> keyInjectionMacAes(params)
            KeySpec.AES_256 -> keyInjectionMacAes(params)
            KeySpec.TDEA -> keyInjectionMacTdea(params)
            KeySpec.DUKPT -> keyInjectionMacDukpt(params)
            KeySpec.DUKPT_AES_128 -> keyInjectionMacDukptAes(params)
            KeySpec.DUKPT_AES_192 -> keyInjectionMacDukptAes(params)
            KeySpec.DUKPT_AES_256 -> keyInjectionMacDukptAes(params)
        }
    }

    private fun keyInjectionMacAes(params: KeyParam) {
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
            null // KCV must be null when the key is encrypted by TMK
        }

        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_AES, PEDDLL.VALUE_KEY_USAGE_MAC, key, kcv, tmkSlotIndex)
    }

    private fun keyInjectionMacTdea(params: KeyParam) {
        if (true == keySlot.usages?.isNotEmpty()) {
            if (1 != keySlot.usages?.size || null != keySlot.usages?.find { it != KeyUsage.MAC }) {
                throw ContextAwareError(
                    Errors.InvalidParameter.name, "Invalid key usage", mapOf("values" to keySlot.usages, "expected" to KeyUsage.MAC.name)
                )
            }
        }

        val key: ByteArray = getKeyValue(params, KeyParamKey.KEY.name, keySlot.spec.keySize)
        val kcv = controller.generateKcv("DESede/ECB/NoPadding", "DESede", key, getKeyValueOrNull(params, KeyParamKey.KCV.name, 3))

        val tmkSlotIndex = keySlot.tmkKeySlot?.index ?: -1

        if (0 > tmkSlotIndex) {
            throw ContextAwareError(Errors.ParameterMissing.name, "MAC key TMK is not specified", mapOf("keySlot" to keySlot))
        }

        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_TDEA, PEDDLL.VALUE_KEY_USAGE_MAC, key, kcv, tmkSlotIndex)
    }

    private fun keyInjectionMacDukpt(params: KeyParam) {
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
            null // KCV must be null when the key is encrypted by TMK
        }

        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_DUKPT, PEDDLL.VALUE_KEY_USAGE_MAC, key, kcv, tmkSlotIndex, ksn)
    }

    private fun keyInjectionMacDukptAes(params: KeyParam) {
        val ksn: ByteArray = getKeyValue(params, KeyParamKey.KSN.name, 12)
        val key: ByteArray = getKeyValue(params, KeyParamKey.KEY.name, keySlot.spec.keySize)

        val usage = (PEDDLL.VALUE_DUKPTAES_KEYUSAGE_MACGENERATE.toInt() or PEDDLL.VALUE_DUKPTAES_KEYUSAGE_MACVERIFY.toInt()).toByte()

        val tmkSlotIndex = keySlot.tmkKeySlot?.index ?: -1

        val kcv = if (0 > tmkSlotIndex) {
            controller.generateKcv("AES/ECB/NoPadding", "AES", key, getKeyValueOrNull(params, KeyParamKey.KCV.name, 3))
        } else {
            null // KCV must be null when the key is encrypted by TMK
        }

        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_DUKPTAES, usage, key, kcv, tmkSlotIndex, ksn)
    }
}
