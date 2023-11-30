package my.paidchain.spectraterminaldemo.common.secureElement

import android.app.Application
import com.spectratech.controllers.KKeyDllController.CipherModeConstant
import com.spectratech.controllers.KeyDllController
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors

enum class CipherMode(val value: Int) {
    CBC(CipherModeConstant.K_CbcMode.toInt()),
    CBC_MASK(CipherModeConstant.K_CbcModeMask.toInt())
}

class KeyCipher(private val app: Application, private val controller: KeyDllController) {
    companion object {
        fun padDataToBlockSize(data: ByteArray, blockSize: Int): ByteArray {
            val paddingLength = if (0 != data.size % blockSize) {
                blockSize - data.size % blockSize
            } else blockSize

            val padding = ByteArray(paddingLength) { paddingLength.toByte() }
            return data + padding
        }

        fun unpadDataFromBlockSize(data: ByteArray, blockSize: Int): ByteArray {
            val paddingLength = data.last().toInt()

            if (paddingLength > data.size || paddingLength > blockSize) {
                throw ContextAwareError(
                    Errors.OutOfRange.name, "Data is underflow", mapOf(
                        "paddingLength" to paddingLength, "dataSize" to data.size, "blockSize" to blockSize
                    )
                )
            }

            return data.copyOf(data.size - paddingLength)
        }
    }

    private inner class KeyCipherAesDelegator : KeyCipherAes(app, controller)
    private inner class KeyCipherTdeaDelegator : KeyCipherTdea(app, controller)
    private inner class KeyCipherDukptDelegator : KeyCipherDukpt(app, controller)
    private inner class KeyCipherDukptAesDelegator : KeyCipherDukptAes(app, controller)

    fun encrypt(keySlot: KeySlot, params: KeyParam): ByteArray {
        return when (keySlot.spec) {
            KeySpec.AES_128,
            KeySpec.AES_192,
            KeySpec.AES_256 -> KeyCipherAesDelegator().encrypt(keySlot, params)

            KeySpec.TDEA -> KeyCipherTdeaDelegator().encrypt(keySlot, params)

            KeySpec.DUKPT -> KeyCipherDukptDelegator().encrypt(keySlot, params)

            KeySpec.DUKPT_AES_128,
            KeySpec.DUKPT_AES_192,
            KeySpec.DUKPT_AES_256 -> KeyCipherDukptAesDelegator().encrypt(keySlot, params)

            KeySpec.RAW -> throw ContextAwareError(Errors.NotSupported.name, "Encryption is not supported by Raw type", mapOf("keySlot" to keySlot))
        }
    }

    fun decrypt(keySlot: KeySlot, params: KeyParam): ByteArray {
        return when (keySlot.spec) {
            KeySpec.AES_128,
            KeySpec.AES_192,
            KeySpec.AES_256 -> KeyCipherAesDelegator().decrypt(keySlot, params)

            KeySpec.TDEA -> KeyCipherTdeaDelegator().decrypt(keySlot, params)

            KeySpec.DUKPT -> KeyCipherDukptDelegator().decrypt(keySlot, params)

            KeySpec.DUKPT_AES_128,
            KeySpec.DUKPT_AES_192,
            KeySpec.DUKPT_AES_256 -> KeyCipherDukptAesDelegator().decrypt(keySlot, params)

            KeySpec.RAW -> throw ContextAwareError(Errors.NotSupported.name, "Decryption is not supported by Raw type", mapOf("keySlot" to keySlot))
        }
    }
}