package my.paidchain.spectraterminaldemo.common.secureElement

import android.app.Application
import my.paidchain.spectraterminaldemo.common.ContextAwareError
import my.paidchain.spectraterminaldemo.common.Errors
import java.security.KeyPair
import java.security.KeyPairGenerator

data class SecureElementOps(
    val secureElement: SecureElement,
    var keyValue: KeyValueOps?
)

data class KeyValueOps(
    val get: (key: String) -> String,
    val getOrNull: ((key: String) -> String?),
    val set: (key: String, value: String?) -> Unit,
    val deleteLike: (key: String) -> Unit
)

class SecureElement private constructor(val app: Application) {
    val keySlots: MutableMap<String, KeySlot>

    private val ops: SecureElementOps

    init {
        ops = SecureElementOps(secureElement = this, keyValue = KeyValueOps(
            get = { key -> throw ContextAwareError(Errors.NotInitialized.name, "Get key value is not initialized", mapOf("key" to key)) },
            getOrNull = { null },
            set = { _, _ -> },
            deleteLike = { }
        ))

        keySlots = mutableMapOf(
            "PC_DUKPT" to KeySlot(
                code = "PC_DUKPT",
                index = 1,
                type = KeyType.CIPHER,
                spec = KeySpec.DUKPT_AES_256,
                usages = arrayOf(KeyUsage.DUKPT_AES_DATA_ENCRYPT, KeyUsage.DUKPT_AES_DATA_DECRYPT),
                tmkKeySlot = null,
                isInjectionRequired = false,
                isOptional = true,
                ops = ops
            ),
            "BIMB_TMK_NORMAL" to KeySlot(
                code = "BIMB_TMK_NORMAL",
                index = 2,
                type = KeyType.TMK,
                spec = KeySpec.TDEA,
                usages = null,
                tmkKeySlot = null,
                isInjectionRequired = false,
                isOptional = true,
                ops = ops
            ),
            "BIMB_SCHEME_TPK_NORMAL" to KeySlot(
                code = "BIMB_SCHEME_TPK_NORMAL",
                index = 4,
                type = KeyType.PIN,
                spec = KeySpec.DUKPT,
                usages = null,
                tmkKeySlot = null,
                isInjectionRequired = false,
                isOptional = true,
                ops = ops
            )
        )

        keySlots["BIMB_SCHEME_TPK_NORMAL"]?.tmkKeySlot = keySlots["BIMB_TMK_NORMAL"]
    }

    companion object {
        private var self: SecureElement? = null

        val instance: SecureElement
            get() = self ?: throw ContextAwareError(Errors.NotInitialized.name, "SecureElement initialization is required")

        var isReady: Boolean = false
            private set

        fun init(app: Application): SecureElement {
            self = SecureElement(app)
            return self!!
        }
    }

    fun getKeySlot(code: String): KeySlot {
        return keySlots[code] ?: throw ContextAwareError(
            Errors.NotFound.name, "Key slot is not found", mapOf("code" to code)
        )
    }

    fun clearKeys() {
        keySlots.forEach { (_, keySlot) ->
            keySlot.clear()
        }
    }

    fun generateRsaKeyPair(keySize: Int): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA").apply {
            initialize(keySize)
        }

        return keyPairGenerator.generateKeyPair()
    }
}
