package my.paidchain.spectraterminaldemo.common.secureElement

import com.spectratech.controllers.PEDDLL
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.getKeyValue
import my.paidchain.spectraterminaldemo.common.getKeyValueOrNull

abstract class KeyInjectionTmk(private val controller: KeyInjection) {
    private lateinit var keySlot: KeySlot

    internal fun injectKey(keySlot: KeySlot, params: KeyParam) {
        if (true == keySlot.usages?.isNotEmpty()) {
            if (1 != keySlot.usages.size || null != keySlot.usages.find { it != KeyUsage.TMK }) {
                throw ContextAwareError(
                    Errors.InvalidParameter.name, "Invalid key usage", mapOf("values" to keySlot.usages, "expected" to KeyUsage.TMK.name)
                )
            }
        }

        this.keySlot = keySlot

        when (keySlot.spec) {
            KeySpec.AES_128,
            KeySpec.AES_192,
            KeySpec.AES_256 -> keyInjectionTmkAes(params)

            KeySpec.TDEA -> keyInjectionTmkTdea(params)
            KeySpec.DUKPT -> keyInjectionTmkDukpt(params)

            KeySpec.DUKPT_AES_128,
            KeySpec.DUKPT_AES_192,
            KeySpec.DUKPT_AES_256 -> keyInjectionTmkDukptAes(params)
        }
    }

    private fun keyInjectionTmkAes(params: KeyParam) {
        val key: ByteArray = getKeyValue(params, KeyParamKey.KEY.name, keySlot.spec.keySize)
        val kcv = controller.generateKcv("AES/ECB/NoPadding", "AES", key, getKeyValueOrNull(params, KeyParamKey.KCV.name, 3))

        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_AES, PEDDLL.VALUE_KEY_USAGE_TMK, key, kcv, -1)
    }

    private fun keyInjectionTmkTdea(params: KeyParam) {
        val key: ByteArray = getKeyValue(params, KeyParamKey.KEY.name, 16..24)
        val kcv = controller.generateKcv("DESede/ECB/NoPadding", "DESede", key, getKeyValueOrNull(params, KeyParamKey.KCV.name, 3))

        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_TDEA, PEDDLL.VALUE_KEY_USAGE_TMK, key, kcv, -1)
    }

    private fun keyInjectionTmkDukpt(params: KeyParam) {
        val ksn: ByteArray = getKeyValue(params, KeyParamKey.KSN.name, 10)
        val key: ByteArray = getKeyValue(params, KeyParamKey.KEY.name, keySlot.spec.keySize)
        val kcv = controller.generateKcv("DESede/ECB/NoPadding", "DESede", key, getKeyValueOrNull(params, KeyParamKey.KCV.name, 3))

        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_DUKPT, PEDDLL.VALUE_KEY_USAGE_TMK, key, kcv, -1, ksn)
    }

    private fun keyInjectionTmkDukptAes(params: KeyParam) {
        val ksn: ByteArray = getKeyValue(params, KeyParamKey.KSN.name, 12)
        val key: ByteArray = getKeyValue(params, KeyParamKey.KEY.name, keySlot.spec.keySize)
        val kcv = controller.generateKcv("AES/ECB/NoPadding", "AES", key, getKeyValueOrNull(params, KeyParamKey.KCV.name, 3))

        controller.inject(keySlot, PEDDLL.VALUE_KEY_ALGORITHM_DUKPTAES, PEDDLL.VALUE_KEY_USAGE_TMK, key, kcv, -1, ksn)
    }
}
