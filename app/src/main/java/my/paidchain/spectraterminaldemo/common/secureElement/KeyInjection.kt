package my.paidchain.spectraterminaldemo.common.secureElement

import android.app.Application
import com.spectratech.controllers.KeyDllController
import com.spectratech.controllers.PEDDLL
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.Misc.Companion.toHex
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

enum class KeyType {
    TMK,
    PIN,
    MAC,
    CIPHER,
    RAW
}

enum class KeySpec(val keySize: Int, val blockSize: Int, val keyType: Byte) {
    AES_128(keySize = 16, 16, 0x00),
    AES_192(keySize = 24, 16, 0x00),
    AES_256(keySize = 32, 16, 0x00),
    TDEA(keySize = 16, 8, 0x00),
    DUKPT(keySize = 16, 8, 0x00),
    DUKPT_AES_128(keySize = 16, 16, PEDDLL.VALUE_DUKPTAES_KEYTYPE_AES128.toByte()),
    DUKPT_AES_192(keySize = 24, 16, PEDDLL.VALUE_DUKPTAES_KEYTYPE_AES192.toByte()),
    DUKPT_AES_256(keySize = 32, 16, PEDDLL.VALUE_DUKPTAES_KEYTYPE_AES256.toByte()),
    RAW(keySize = 0, blockSize = 0, keyType = 0x00)
}

enum class KeyUsage(val value: Byte) {
    VOID(PEDDLL.VALUE_KEY_USAGE_EMPTY),

    TMK(PEDDLL.VALUE_KEY_USAGE_TMK),
    PIN(PEDDLL.VALUE_KEY_USAGE_PIN),
    MAC(PEDDLL.VALUE_KEY_USAGE_MAC),
    SIGNATURE(PEDDLL.VALUE_KEY_USAGE_SIGNATURE),
    ENCRYPT(PEDDLL.VALUE_KEY_USAGE_ENCRYPT),
    DECRYPT(PEDDLL.VALUE_KEY_USAGE_DECRYPT),
    BOTH(PEDDLL.VALUE_KEY_USAGE_BOTH),

    SSL_SIGNATURE(PEDDLL.VALUE_KEY_USAGE_SSLSIGNATURE),

    DUKPT_AES_PIN_BLOCK_ENCRYPT(PEDDLL.VALUE_DUKPTAES_KEYUSAGE_PINBLKENCRYPT),
    DUKPT_AES_PIN_BLOCK_DECRYPT(PEDDLL.VALUE_DUKPTAES_KEYUSAGE_PINBLKDECRYPT),
    DUKPT_AES_MAC_GENERATE(PEDDLL.VALUE_DUKPTAES_KEYUSAGE_MACGENERATE),
    DUKPT_AES_MAC_VERIFY(PEDDLL.VALUE_DUKPTAES_KEYUSAGE_MACVERIFY),
    DUKPT_AES_DATA_ENCRYPT(PEDDLL.VALUE_DUKPTAES_KEYUSAGE_DATAENCRYPT),
    DUKPT_AES_DATA_DECRYPT(PEDDLL.VALUE_DUKPTAES_KEYUSAGE_DATADECRYPT),
    DUKPT_AES_MPP(PEDDLL.VALUE_DUKPTAES_KEYUSAGE_MPP)
}

class KeyInjection(private val app: Application, internal val ops: SecureElementOps, private val controller: KeyDllController) {
    private inner class KeyInjectionTmkDelegator : KeyInjectionTmk(this)
    private inner class KeyInjectionPinDelegator : KeyInjectionPin(this)
    private inner class KeyInjectionMacDelegator : KeyInjectionMac(this)
    private inner class KeyInjectionCipherDelegator : KeyInjectionCipher(this)
    private inner class KeyInjectionRawDelegator : KeyInjectionRaw(this)

    fun injectKey(keySlot: KeySlot, params: KeyParam): ByteArray? {
        return when (keySlot.type) {
            KeyType.TMK -> KeyInjectionTmkDelegator().injectKey(keySlot, params)
            KeyType.PIN -> KeyInjectionPinDelegator().injectKey(keySlot, params)
            KeyType.MAC -> KeyInjectionMacDelegator().injectKey(keySlot, params)
            KeyType.CIPHER -> KeyInjectionCipherDelegator().injectKey(keySlot, params)
            KeyType.RAW -> KeyInjectionRawDelegator().injectKey(keySlot, params)
        }
    }

    internal fun inject(keySlot: KeySlot, algorithm: Byte, usage: Byte, key: ByteArray, kcv: ByteArray?, tmkSlotIndex: Short, ksn: ByteArray? = null, deriveKeyType: Byte? = null, tmkIv: ByteArray? = null) {
        if (!controller.keyDelete(app, keySlot.index)) {
            throw ContextAwareError(Errors.Failed.name, "Key deletion failed", mapOf("slotIndex" to keySlot.index, "slotCode" to keySlot.code))
        }

        val ret = if (null == ksn) {
            controller.keyInject(app, tmkSlotIndex, keySlot.index, key, algorithm, usage, kcv)
        } else {
            when (algorithm) {
                PEDDLL.VALUE_KEY_ALGORITHM_DUKPT -> {
                    controller.keyInject(app, ksn, tmkSlotIndex, keySlot.index, key, algorithm, usage, kcv)
                }

                PEDDLL.VALUE_KEY_ALGORITHM_DUKPTAES -> {
                    if (0 <= tmkSlotIndex && true == tmkIv?.isEmpty()) {
                        throw ContextAwareError(Errors.ParameterMissing.name, "TMK IV is missing", mapOf("keySlot" to keySlot))
                    }
                    controller.keyInject(app, deriveKeyType!!, ksn, tmkSlotIndex, keySlot.index, key, algorithm, usage, kcv, tmkIv)
                }

                else -> 0
            }
        }

        if (-1 != ret) {
            throw ContextAwareError(
                Errors.Failed.name, "Key injection failed", mapOf(
                    "ret" to ret, "kcv" to kcv?.toHex(), "keySlot" to keySlot.code, "keyIndex" to keySlot.index
                )
            )
        }
    }

    internal fun generateKcv(spec: String, algorithm: String, key: ByteArray, provided: ByteArray? = null): ByteArray {
        val kcv = when (algorithm) {
            "SHA-256" -> {
                val md = MessageDigest.getInstance(algorithm)

                md.update(key)
                md.digest()
            }

            else -> {
                val cipher = Cipher.getInstance(spec)

                cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, algorithm))
                cipher.doFinal(ByteArray(cipher.blockSize))
            }
        }

        if (null != provided) {
            val expected = kcv.sliceArray(0..2)

            if (!provided.contentEquals(expected)) {
                throw ContextAwareError(
                    Errors.Mismatched.name, "KCV is mismatched", mapOf("value" to provided.toHex(), "expected" to expected.toHex())
                )
            }
        }

        return kcv
    }
}
