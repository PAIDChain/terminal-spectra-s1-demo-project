package my.paidchain.spectraterminaldemo.common.secureElement

import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.Misc.Companion.toHex
import my.paidchain.spectraterminaldemo.common.getKeyValue
import my.paidchain.spectraterminaldemo.common.getKeyValueOrNull
import my.paidchain.spectraterminaldemo.common.log

abstract class KeyInjectionRaw(private val controller: KeyInjection) {
    private lateinit var keySlot: KeySlot

    internal fun injectKey(keySlot: KeySlot, params: KeyParam): ByteArray {
        this.keySlot = keySlot

        return when (keySlot.spec) {
            KeySpec.RAW -> keyInjectionRaw(params)

            KeySpec.AES_128,
            KeySpec.AES_192,
            KeySpec.AES_256,
            KeySpec.TDEA,
            KeySpec.DUKPT,
            KeySpec.DUKPT_AES_128,
            KeySpec.DUKPT_AES_192,
            KeySpec.DUKPT_AES_256 -> throw ContextAwareError(
                Errors.NotSupported.name, "Key spec is not supported by Raw type", mapOf("spec" to keySlot.spec, "keySlot" to keySlot)
            )
        }
    }

    private fun keyInjectionRaw(params: KeyParam): ByteArray {
        val key: ByteArray = getKeyValue(params, KeyParamKey.KEY.name)
        val kcv = controller.generateKcv("", "SHA-256", key, getKeyValueOrNull(params, KeyParamKey.KCV.name, 3))

        log(Level.WARN, javaClass.simpleName) { "Inject RAW ${keySlot.index}: ${keySlot.code} (KCV: ${kcv.sliceArray(0..2).toHex()})" }

        keySlot.rawValue = key

        return kcv
    }
}
