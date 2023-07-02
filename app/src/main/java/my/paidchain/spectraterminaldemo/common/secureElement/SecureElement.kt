package my.paidchain.spectraterminaldemo.common.secureElement

import android.app.Application
import com.spectratech.controllers.*
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import my.paidchain.spectraterminaldemo.common.GenericKeyValues
import my.paidchain.spectraterminaldemo.common.GenericType
import my.paidchain.spectraterminaldemo.common.GenericTypeValue
import my.paidchain.spectraterminaldemo.common.Level
import my.paidchain.spectraterminaldemo.common.awaitWith
import my.paidchain.spectraterminaldemo.common.log
import kotlin.reflect.KClass

data class KeySlot(
    val code: String,
    val type: KeyType,
    val spec: KeySpec,
    val index: Short,
    val usages: Array<KeyUsage>?,
    var tmkKeySlot: KeySlot?
)

enum class KeyParamKey(override val kClass: KClass<*>) : GenericType {
    KSN(ByteArray::class),
    KEY(ByteArray::class),
    KCV(ByteArray::class),
    IV(ByteArray::class),
}

data class KeyParamTypeValue<T>(
    override val type: KeyParamKey,
    override val value: T
) : GenericTypeValue<T>

typealias KeyParam = GenericKeyValues<*>

enum class KeyStatus {
    OK,
    EMPTY,
    ERROR
}

class SecureElement private constructor(private val app: Application) {
    private val keySlots = mapOf(
        "PC_DUKPT" to KeySlot(
            code = "PC_DUKPT",
            index = 1,
            type = KeyType.CIPHER,
            spec = KeySpec.DUKPT_AES_256,
            usages = arrayOf(KeyUsage.DUKPT_AES_DATA_ENCRYPT, KeyUsage.DUKPT_AES_DATA_DECRYPT),
            tmkKeySlot = null
        ),
        "BIMB_TMK_NORMAL" to KeySlot(
            code = "BIMB_TMK_NORMAL",
            index = 2,
            type = KeyType.TMK,
            spec = KeySpec.TDEA,
            usages = null,
            tmkKeySlot = null
        ),
        "BIMB_TMK_SSPN" to KeySlot(
            code = "BIMB_TMK_SSPN",
            index = 5,
            type = KeyType.TMK,
            spec = KeySpec.TDEA,
            usages = null,
            tmkKeySlot = null
        ),
        "BIMB_SCHEME_TPK_NORMAL" to KeySlot(
            code = "BIMB_SCHEME_TPK_NORMAL",
            index = 9,
            type = KeyType.PIN,
            spec = KeySpec.DUKPT,
            usages = null,
            tmkKeySlot = null
        ),
    )
    
    companion object {
        private var self: SecureElement? = null

        val instance: SecureElement
            get() = self ?: throw ContextAwareError(Errors.NotInitialized.name, "SecureElement initialization is required")

        fun init(app: Application) {
            SPDeviceController.enableDebugLog(true) // TODO

            self = SecureElement(app)
        }
    }

    suspend fun isKeysReady(): Boolean {
        return process { controller ->
            var isReady = true

            for (keySlot in keySlots) {
                val result = controller!!.keyStatus(app, keySlot.value.index)

                if (KeyStatus.OK != getKeyStatus(result.resultCode)) {
                    log(Level.INFO, javaClass.simpleName) { "Key ${keySlot.value.index}: ${keySlot.key} - MISSING (${result.resultCode})" }
                    isReady = false
                } else {
                    log(Level.INFO, javaClass.simpleName) { "Key ${keySlot.value.index}: ${keySlot.key} - READY" }
                }
            }

            return@process isReady
        }
    }

    suspend fun keyStatus(slotCode: String): KeyStatus {
        val keySlot = getKeySlot(slotCode)

        return process { controller ->
            val result = controller!!.keyStatus(app, keySlot.index)
            getKeyStatus(result.resultCode)
        }
    }

    suspend fun injectKey(slotCode: String, params: KeyParam): KeySlot {
        val keySlot = getKeySlot(slotCode)

        return process { controller ->
            KeyInjection(app, controller).apply { injectKey(keySlot, params) }
            keySlot
        }
    }

    suspend fun deleteKey(slotCode: String?): Boolean {
        val keySlot = if (null != slotCode) getKeySlot(slotCode) else null

        return process { controller ->
            if (null != keySlot) {
                controller.keyDelete(app, keySlot.index)
            } else {
                controller.keyDeleteAll(app)
            }
        }
    }

    private suspend fun <T> process(handler: (controller: KeyDllController) -> T): T {
        var controller: KeyDllController? = null

        try {
            return awaitWith { resolve, reject ->
                try {
                    controller = KeyDllController.getControllerInstance(app,
                        SecureElementDelegator(
                            fnConnected = {
                                val ret = handler(controller!!)
                                resolve(ret)
                            },
                            fnError = { error -> reject(error) }
                        )
                    )

                    controller!!.connectController()
                } catch (error: Throwable) {
                    reject(error)
                }
            }
        } finally {
            controller?.disconnectController()
            controller?.releaseControllerInstance()
        }
    }

    private fun getKeyStatus(resultCode: Int): KeyStatus {
        return when (resultCode) {
            -1 -> KeyStatus.OK
            1005 -> KeyStatus.EMPTY
            else -> KeyStatus.ERROR
        }
    }

    fun getKeySlot(code: String): KeySlot {
        return keySlots[code] ?: throw ContextAwareError(
            Errors.NotFound.name, "Key slot is not found", mapOf("code" to code)
        )
    }
}
