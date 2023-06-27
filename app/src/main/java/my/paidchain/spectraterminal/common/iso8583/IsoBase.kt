package my.paidchain.spectraterminal.common.iso8583

import my.paidchain.spectraterminal.common.Misc.Companion.push
import my.paidchain.spectraterminal.common.Misc.Companion.toByteArray
import java.nio.ByteBuffer

data class IField(val rootField: Field, val field: Field)

class IsoBase {
    private val fields: MutableMap<String, Field>
    private var _bitmap: Bitmap? = null

    val lastBitNo: Int

    val bitmap: Bitmap
        get() {
            if (null === this._bitmap) {
                this._bitmap = Bitmap(this.lastBitNo)
            }
            return this._bitmap!!
        }

    val bits get() = this.bitmap.bits

    constructor(json: Any? /* String | List<FieldConfigJson> */) {
        val fieldConfigSet = FieldConfigSet(json)

        if (fieldConfigSet.configSet.isEmpty()) {
            throw IllegalArgumentException("Field config set is empty")
        }

        this.lastBitNo = fieldConfigSet.configSet.last().bitNo

        this.fields = mutableMapOf()

        for (config in fieldConfigSet.configSet) {
            val field = Field(config)

            this.fields[config.bitNo.toString()] = field
            this.fields[config.name] = field
        }
    }

    fun get(bitNoOrName: Any /* Int | String */): Any? {
        return getField(bitNoOrName).field.data
    }

    fun set(bitNoOrName: Any /* Int | String */, data: Any? = null /* String | ByteArray | MutableMap<String, Any?> */) {
        val (rootField, field) = getField(bitNoOrName)

        field.data = data
        this.bitmap.set(rootField.config.bitNo, null !== data)
    }

    fun clear() {
        for (field in this.fields) {
            field.value.clear()
        }
        this.bitmap.clear()
    }

    fun getConfig(bitNoOrName: Any /* Int | String */): FieldConfig {
        return getField(bitNoOrName).field.config
    }

    fun pack(): ByteArray {
        var buffer = ByteBuffer.allocate(2048)

        buffer.put(this.bitmap.map)

        for (bitNo in this.bitmap.bits) {
            buffer = buffer.push(this.fields[bitNo.toString()]?.pack()?.data)
        }

        return buffer.toByteArray()
    }

    fun unpack(data: ByteArray) {
        val bitmap = Bitmap(data)

        this.clear()

        var buffer = data.slice(bitmap.length until data.size)

        for (bitNo in bitmap.bits) {
            val field = this.fields[bitNo.toString()]

            if (null === field) {
                throw IllegalArgumentException("Bit $bitNo: Field config is not configured")
            }

            buffer = buffer.slice(field.unpack(buffer.toByteArray()) until buffer.size)
        }

        this._bitmap = bitmap
    }

    private fun getField(bitNoOrName: Any /* Int | String */): IField {
        val paths = when (bitNoOrName) {
            is Int -> listOf(bitNoOrName.toString())
            is String -> bitNoOrName.split(".")
            else -> throw IllegalArgumentException("Invalid data type for bitNo or name")
        }
        val rootField = this.fields[paths.first()]

        var field = rootField

        if (1 < paths.size && null !== field) {
            var fields = field.fields

            for (key in paths.slice(1 until paths.size)) {
                if (null === fields) {
                    break
                }

                field = fields[key]

                if (null === field) {
                    break
                }

                fields = field.fields
            }
        }

        if (null === field) {
            throw IllegalArgumentException("Field not found ($bitNoOrName)")
        }

        return IField(rootField!!, field)
    }
}
