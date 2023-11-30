package my.paidchain.spectraterminaldemo.common.secureElement

import android.app.Application
import com.spectratech.controllers.KeyDllController
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.GenericKeyValues
import my.paidchain.spectraterminaldemo.common.GenericType
import my.paidchain.spectraterminaldemo.common.GenericTypeValue
import my.paidchain.spectraterminaldemo.common.Misc.Companion.hexStringToByteArray
import my.paidchain.spectraterminaldemo.common.Misc.Companion.toHex
import java.security.KeyPair
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

enum class KeyStatus {
    OK,
    EMPTY,
    ERROR,
    INVALID_KCV,
    INVALID_PARAM,
    INVALID_KEY_USAGE
}

enum class KeyParamKey(override val kClass: KClass<*>) : GenericType {
    KSN(ByteArray::class),
    KEY(ByteArray::class),
    KCV(ByteArray::class),
    IV(ByteArray::class),
    DATA(ByteArray::class),
    CIPHER_MODE(CipherMode::class)
}

data class KeyParamTypeValue<T>(
    override val type: KeyParamKey,
    override val value: T
) : GenericTypeValue<T>

typealias KeyParam = GenericKeyValues<*>

class KeySlot constructor(
    private val ops: SecureElementOps,
    val code: String,
    val type: KeyType,
    val spec: KeySpec,
    val index: Short,
    val isOptional: Boolean,
    val isInjectionRequired: Boolean,
    val usages: Array<KeyUsage>?,
    var tmkKeySlot: KeySlot?,
) {
    private val app: Application = ops.secureElement.app

    var kcv: ByteArray?
        get() = ops.keyValue!!.getOrNull("KCV:$index:$code")?.hexStringToByteArray()
        set(value) {
            ops.keyValue!!.deleteLike("KCV:$index:%")

            if (null != value) {
                ops.keyValue!!.set("KCV:$index:$code", value.toHex())
            }
        }

    var rawValue: ByteArray?
        get() = ops.keyValue!!.getOrNull("RAW:$index:$code")?.hexStringToByteArray()
        set(value) {
            ops.keyValue!!.deleteLike("RAW:$index:%")

            if (null != value) {
                ops.keyValue!!.set("RAW:$index:$code", value.toHex())
            }
        }

    val status: KeyStatus
        get() {
            return if (0 <= index) {
                process { controller ->
                    val result = controller.keyStatus(app, index)
                    when (result.resultCode) {
                        -1 -> KeyStatus.OK
                        1002 -> KeyStatus.INVALID_PARAM
                        1005 -> KeyStatus.EMPTY
                        1006 -> KeyStatus.INVALID_KCV
                        1010 -> KeyStatus.INVALID_KEY_USAGE
                        else -> KeyStatus.ERROR
                    }
                }
            } else {
                if (null != ops.keyValue!!.getOrNull("RAW:$index:$code")) {
                    KeyStatus.OK
                } else {
                    KeyStatus.EMPTY
                }
            }
        }

    fun clear(): Boolean {
        return process { controller ->
            val result = controller.keyDelete(app, index)
            this.kcv = null
            result
        }
    }

    fun inject(params: KeyParam): KeySlot {
        return process { controller ->
            val keyInjection = KeyInjection(app, ops, controller)

            // Inject key and update KCV (first 3 bytes)
            this.kcv = keyInjection.injectKey(this, params)?.sliceArray(0..2)
            this
        }
    }

    fun encrypt(params: KeyParam): ByteArray {
        if (KeyType.CIPHER != type && KeyType.TMK != type) {
            throw ContextAwareError(
                Errors.NotSupported.name, "Unsupported encryption key type", mapOf("type" to type, "expected" to listOf(KeyType.CIPHER.name, KeyType.TMK.name))
            )
        }

        return process { controller ->
            KeyCipher(app, controller).encrypt(this@KeySlot, params)
        }
    }

    fun decrypt(params: KeyParam): ByteArray {
        if (KeyType.CIPHER != type && KeyType.TMK != type) {
            throw ContextAwareError(
                Errors.NotSupported.name, "Unsupported encryption key type", mapOf("type" to type, "expected" to listOf(KeyType.CIPHER.name, KeyType.TMK.name))
            )
        }

        return process { controller ->
            KeyCipher(app, controller).decrypt(this@KeySlot, params)
        }
    }

    fun generateRsaKeyPair(keySize: Int): KeyPair {
        return ops.secureElement.generateRsaKeyPair(keySize)
    }

    private fun <T> process(handler: (controller: KeyDllController) -> T): T {
        synchronized(this.app) {
            var controller: KeyDllController? = null
            val result = CompletableFuture<T>()

            try {
                controller = KeyDllController.getControllerInstance(app,
                    SecureElementDelegator(
                        fnConnected = {
                            val ret = handler(controller!!)
                            result.complete(ret)
                        },
                        fnError = { error -> result.completeExceptionally(error) }
                    )
                )

                controller!!.connectController()

                return result.get()
            } finally {
                controller?.disconnectController()
                controller?.releaseControllerInstance()
            }
        }
    }
}