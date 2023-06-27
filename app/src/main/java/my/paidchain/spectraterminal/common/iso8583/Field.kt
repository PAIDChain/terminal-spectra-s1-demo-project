package my.paidchain.spectraterminal.common.iso8583

import my.paidchain.spectraterminal.common.Misc.Companion.push
import my.paidchain.spectraterminal.common.Misc.Companion.toByteArray
import my.paidchain.spectraterminal.common.iso8583.codec.Codec
import my.paidchain.spectraterminal.common.iso8583.codec.CodecDataType
import my.paidchain.spectraterminal.common.iso8583.codec.CodecDataTypeAns
import my.paidchain.spectraterminal.common.iso8583.codec.CodecDataTypeN
import my.paidchain.spectraterminal.common.iso8583.codec.CodecDataTypeZ
import my.paidchain.spectraterminal.common.iso8583.codec.PackResult
import my.paidchain.spectraterminal.common.iso8583.codec.CodecFixLength
import my.paidchain.spectraterminal.common.iso8583.codec.CodecLlVar
import my.paidchain.spectraterminal.common.iso8583.codec.CodecLllVar
import my.paidchain.spectraterminal.common.iso8583.codec.CodecLeftAlign
import my.paidchain.spectraterminal.common.iso8583.codec.CodecRightAlign
import java.nio.ByteBuffer

class Field {
    private val codecPack: Codec
    private val codecUnpack: Codec

    private val codecDataTypeANS: CodecDataType
    private val codecDataTypeN: CodecDataType
    private val codecDataTypeZ: CodecDataType

    private var _data: ByteArray?
    private var _dataLength: Int

    val config: FieldConfig
    var fields: MutableMap<String, Field>?

    constructor(config: FieldConfig) {
        this.config = config

        this._data = null
        this._dataLength = 0

        // Pack sequence: Alignment -> LengthType
        this.codecPack = codecLengthType(codecAlignment())
        // Unpack sequence: LengthType -> Alignment
        this.codecUnpack = codecAlignment(codecLengthType())

        this.codecDataTypeANS = CodecDataTypeAns()
        this.codecDataTypeN = CodecDataTypeN()
        this.codecDataTypeZ = CodecDataTypeZ()

        this.fields = null

        if (null !== config.fields) {
            if (true === config.fields!!.isNotEmpty()) {
                this.fields = mutableMapOf()

                for (c in config.fields!!) {
                    this.fields!![c.name] = Field(c)
                }
            }
        }
    }

    var data: Any? /* String | ByteArray | MutableMap<String, Any> */
        get() {
            if (null !== this.fields) {
                var ret: MutableMap<String, Any?>? = null

                for (field in this.fields!!) {
                    val data = field.value.data

                    if (null !== data) {
                        if (null === ret) {
                            ret = mutableMapOf()
                        }
                        ret[field.key] = data
                    }
                }

                return ret
            }

            if (null !== this._data && !this.config.hidden!!) {
                return when (this.config.dataType) {
                    DataType.N -> this.codecDataTypeN.unpack(
                        this._data!!,
                        this._dataLength,
                        this.config
                    )
                    DataType.Z -> this.codecDataTypeZ.unpack(
                        this._data!!,
                        this._dataLength,
                        this.config
                    )
                    DataType.B -> this._data
                    else -> this.codecDataTypeANS.unpack(
                        this._data!!,
                        this._dataLength,
                        this.config
                    )
                }
            }
            return null
        }
        set(value) {
            if (null === value) {
                this.clear()
                return
            }

            if (null !== this.fields) {
                if (value !is MutableMap<*, *>) {
                    throw IllegalArgumentException("Bit ${this.config.bitNo}: Data type field is expected")
                }

                if (null === value) {
                    this.clear()
                    return
                }

                for (v in value) {
                    val field = this.fields!![v.key]

                    if (null === field) {
                        throw IllegalArgumentException("Bit ${v.key} is not configured")
                    }

                    field.data = v.value
                }
            } else {
                when (this.config.dataType) {
                    DataType.N -> {
                        if (value !is String) {
                            throw IllegalArgumentException("Bit ${this.config.bitNo}: Data type string is expected")
                        }

                        this._data = this.codecDataTypeN.pack(value, this.config)
                        this._dataLength = value.length
                    }
                    DataType.B -> {
                        if (value !is ByteArray) {
                            throw IllegalArgumentException("Bit ${this.config.bitNo}: Data type byte is expected")
                        }

                        this._data = value
                        this._dataLength = value.size
                    }
                    DataType.Z -> {
                        if (value !is String) {
                            throw IllegalArgumentException("Bit ${this.config.bitNo}: Data type Z in string is expected")
                        }

                        this._data = this.codecDataTypeZ.pack(value, this.config)
                        this._dataLength = value.length
                    }
                    else -> {
                        if (value !is String) {
                            throw IllegalArgumentException("Bit ${this.config.bitNo}: Data type string is expected")
                        }

                        this._data = this.codecDataTypeANS.pack(value, this.config)
                        this._dataLength = value.length
                    }
                }
            }
        }

    fun clear() {
        this._data = null
        this._dataLength = 0

        if (null !== this.fields) {
            for (field in this.fields!!) {
                field.value.clear()
            }
        }
    }

    fun pack(): PackResult {
        if (null === this.fields) {
            if (null === this._data) {
                throw IllegalArgumentException("Bit ${this.config.bitNo}: Not initialized")
            }

            return this.codecPack.pack(this._data!!, this._dataLength, this.config)
        }

        val packs = this.fields!!.map {
            this.fields!![it.key]?.pack()
        }

        var buffer = ByteBuffer.allocate(1024)

        for (pack in packs){
            if (null !== pack){
                buffer = buffer.push(pack.data)
            }
        }

        val data = buffer.toByteArray()
        val result = PackResult(data, data.size)

        return this.codecPack.pack(result.data, result.data.size, this.config)
    }

    fun unpack(data: ByteArray): Int {
        val result = this.codecUnpack.unpack(data, this.config)

        if (null === this.fields) {
            this._data = result.data
            this._dataLength = result.dataLength
        } else {
            for (field in this.fields!!) {
                val usedLength = field.value.unpack(result.data)
                result.data = result.data.slice(usedLength until result.data.size).toByteArray()
            }
        }

        return if (null === result.usedLength) 0 else result.usedLength!!
    }

    private fun codecLengthType(args: Codec? = null): Codec {
        return when (this.config.lengthType.value) {
            "FIX" -> CodecFixLength(args)
            "LLVAR" -> CodecLlVar(args)
            "LLLVAR" -> CodecLllVar(args)
            else -> {
                throw IllegalArgumentException("Unsupported codec length ${this.config.lengthType.value}")
            }
        }
    }

    private fun codecAlignment(args: Codec? = null): Codec {
        return when (this.config.alignment.value) {
            "LEFT" -> CodecLeftAlign(args)
            "RIGHT" -> CodecRightAlign(args)
            else -> {
                throw IllegalArgumentException("Unsupported codec alignment ${this.config.alignment.value}")
            }
        }
    }
}