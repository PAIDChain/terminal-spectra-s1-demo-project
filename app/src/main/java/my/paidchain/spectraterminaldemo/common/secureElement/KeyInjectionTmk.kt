package my.paidchain.spectraterminaldemo.common.secureElement

import com.spectratech.controllers.PEDDLL
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.Misc.Companion.toHex
import my.paidchain.spectraterminaldemo.common.getKeyValue
import my.paidchain.spectraterminaldemo.common.getKeyValueOrNull
import my.paidchain.spectraterminaldemo.common.log

abstract class KeyInjectionTmk(private val controller: KeyInjection) {
    private lateinit var keySlot: KeySlot

    internal fun injectKey(keySlot: KeySlot, params: KeyParam): ByteArray {
        if (true == keySlot.usages?.isNotEmpty()) {
            if (1 != keySlot.usages.size || null != keySlot.usages.find { it != KeyUsage.TMK }) {
                throw ContextAwareError(
                    Errors.InvalidParameter.name, "Invalid key usage", mapOf("values" to keySlot.usages, "expected" to KeyUsage.TMK.name)
                )
            }
        }

        this.keySlot = keySlot

        return when (keySlot.spec) {
            KeySpec.AES_128,
            KeySpec.AES_192,
            KeySpec.AES_256 -> keyInjectionTmkAes(params)

            KeySpec.TDEA -> keyInjectionTmkTdea(params)
            KeySpec.DUKPT -> keyInjectionTmkDukpt(params)

            KeySpec.DUKPT_AES_128,
            KeySpec.DUKPT_AES_192,
            KeySpec.DUKPT_AES_256 -> keyInjectionTmkDukptAes(params)

            KeySpec.RAW -> throw ContextAwareError(
                Errors.NotSupported.name, "Key spec is not supported by Tmk type", mapOf("spec" to keySlot.spec, "keySlot" to keySlot)
            )
        }
    }

    private fun keyInjectionTmkAes(params: KeyParam): ByteArray {
        val key: ByteArray = getKeyValue(params, KeyParamKey.KEY.name, keySlot.spec.keySize)
        val kcv = controller.generateKcv("AES/ECB/NoPadding", "AES", key, getKeyValueOrNull(params, KeyParamKey.KCV.name, 3))

        log(Level.WARN, javaClass.simpleName) { "Inject TMK AES ${keySlot.index}: ${keySlot.code} (KCV: ${kcv.sliceArray(0..2).toHex()})" }

        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_AES, PEDDLL.VALUE_KEY_USAGE_TMK, key, kcv, -1)

        return kcv
    }

    private fun keyInjectionTmkTdea(params: KeyParam): ByteArray {
        val key: ByteArray = getKeyValue(params, KeyParamKey.KEY.name, 16..24)
        val kcv = controller.generateKcv("DESede/ECB/NoPadding", "DESede", key, getKeyValueOrNull(params, KeyParamKey.KCV.name, 3))

        log(Level.WARN, javaClass.simpleName) { "Inject TMK TDEA ${keySlot.index}: ${keySlot.code} (KCV: ${kcv.sliceArray(0..2).toHex()})" }

        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_TDEA, PEDDLL.VALUE_KEY_USAGE_TMK, key, kcv, -1)

        return kcv
    }

    private fun keyInjectionTmkDukpt(params: KeyParam): ByteArray {
        val ksn: ByteArray = getKeyValue(params, KeyParamKey.KSN.name, 10)
        val key: ByteArray = getKeyValue(params, KeyParamKey.KEY.name, keySlot.spec.keySize)
        val kcv = controller.generateKcv("DESede/ECB/NoPadding", "DESede", key, getKeyValueOrNull(params, KeyParamKey.KCV.name, 3))

        log(Level.WARN, javaClass.simpleName) { "Inject TMK DUKPT ${keySlot.index}: ${keySlot.code} (KCV: ${kcv.sliceArray(0..2).toHex()})" }

        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_DUKPT, PEDDLL.VALUE_KEY_USAGE_TMK, key, kcv, -1, ksn)

        return kcv
    }

    private fun keyInjectionTmkDukptAes(params: KeyParam): ByteArray {
        val ksn: ByteArray = getKeyValue(params, KeyParamKey.KSN.name, 12)
        val key: ByteArray = getKeyValue(params, KeyParamKey.KEY.name, keySlot.spec.keySize)
        val kcv = controller.generateKcv("AES/ECB/NoPadding", "AES", key, getKeyValueOrNull(params, KeyParamKey.KCV.name, 3))

        log(Level.WARN, javaClass.simpleName) { "Inject TMK DUKPT AES ${keySlot.index}: ${keySlot.code} (KCV: ${kcv.sliceArray(0..2).toHex()})" }

        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_DUKPTAES, PEDDLL.VALUE_KEY_USAGE_TMK, key, kcv, -1, ksn)

        return kcv
    }
}
