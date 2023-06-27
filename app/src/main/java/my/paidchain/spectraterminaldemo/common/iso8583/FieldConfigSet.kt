package my.paidchain.spectraterminaldemo.common.iso8583

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import my.paidchain.spectraterminaldemo.common.Misc.Companion.hexStringToByteArray

enum class DataType(val value: String) {
    A("A"),
    N("N"),
    AN("AN"),
    ANS("ANS"),
    Z("Z"),
    B("B")
}

data class IDataType(val dataType: DataType, val offset: Int)

enum class DataFormat(val value: String) {
    ASCII("ASCII"),
    EBCDIC("EBCDIC")
}

enum class LengthType(val value: String) {
    FIX("FIX"),
    LLVAR("LLVAR"),
    LLLVAR("LLLVAR")
}

data class ILengthType(val lengthType: LengthType, val offset: Int)

enum class Alignment(val value: String) {
    LEFT("LEFT"),
    RIGHT("RIGHT")
}

data class IAlignment(val alignment: Alignment, val offset: Int)

data class FieldConfig(
    var bitNo: Int,
    var name: String,
    var dataType: DataType,
    var dataFormat: DataFormat? = DataFormat.ASCII,
    var lengthType: LengthType,
    var alignment: Alignment,
    var maxLength: Int,
    var padding: ByteArray,
    var hidden: Boolean? = false,
    var description: String? = "",
    var fields: Array<FieldConfig>? = null
)

@Serializable
data class FieldConfigJson(
    var bitNo: Int,
    var name: String? = "",
    var dataType: String,
    var dataFormat: DataFormat = DataFormat.ASCII,
    var padding: String? = null,
    var hidden: Boolean = false,
    var description: String? = "",
    var fields: Array<FieldConfigJson>? = null
)

class FieldConfigSet {
    val configSet: Array<FieldConfig>

    constructor(json: Any? /* String | Array<FieldConfigJson> */) {
        val configSetJson: Array<FieldConfigJson> = when (json) {
            is String -> Json { ignoreUnknownKeys = true }.decodeFromString<Array<FieldConfigJson>>(
                json.ifEmpty { "[]" }
            )
            else -> if (null !== json) json as Array<FieldConfigJson> else arrayOf()
        }

        this.configSet = configSetJson.map {
            if (0 >= it.bitNo) throw IllegalArgumentException("bitNo must be positive value")

            val (dataType, dataTypeSize) = getDataType(it.dataType)
            val (lengthType, lengthTypeSize) = getLengthType(it.dataType, dataTypeSize)
            val (alignment, alignmentSize) = getAlignment(it.dataType, lengthTypeSize)

            val padding = getPadding(dataType, it.padding)

            if (null === padding && LengthType.FIX === lengthType) {
                throw IllegalArgumentException("Bit ${it.bitNo}: Field config padding is invalid")
            }

            inspect(
                FieldConfig(
                    bitNo = it.bitNo,
                    name = if (true === it.name?.isNotEmpty()) it.name.toString() else it.bitNo.toString(),
                    dataType,
                    dataFormat = it.dataFormat,
                    lengthType,
                    alignment,
                    maxLength = getMaxLength(it.dataType, alignmentSize),
                    padding = padding!!,
                    hidden = it.hidden,
                    description = it.description,
                    fields = if (null !== it.fields) FieldConfigSet(it.fields!!).configSet else null
                )
            )
        }.sortedWith { a, b ->
            when {
                a.bitNo < b.bitNo -> -1
                a.bitNo > b.bitNo -> 1
                else -> throw IllegalArgumentException("Bit ${b.bitNo} field is duplicated")
            }
        }.toTypedArray()
    }

    private fun inspect(config: FieldConfig): FieldConfig {
        inspectLengthType(config)
        return config
    }

    private fun inspectLengthType(config: FieldConfig) {
        val range = when (config.lengthType) {
            LengthType.LLVAR -> 0..99
            LengthType.LLLVAR -> 0..999
            else -> null
        }

        if (range !== null) {
            if (config.maxLength !in range) {
                throw IllegalArgumentException("Bit ${config.bitNo}: Field config max length (${config.maxLength}) is out of range")
            }
        }
    }

    private fun getPadding(dataType: DataType, padding: String?): ByteArray? {
        return when (dataType) {
            DataType.N -> {
                val value = if (null === padding || padding.isEmpty()) "0" else padding
                if (Regex("[0-9]").matches(value)) value.repeat(2).hexStringToByteArray() else null
            }
            DataType.B -> {
                val value = if (null === padding || padding.isEmpty()) "ff" else padding
                if (Regex("[0-9A-Fa-f]+").matches(value)) value.hexStringToByteArray() else null
            }
            else -> {
                val value = if (null === padding || padding.isEmpty()) "0" else padding
                value.toByteArray()
            }
        }
    }

    private fun getDataType(value: String): IDataType {
        if (value.startsWith(DataType.ANS.value)) {
            return IDataType(dataType = DataType.ANS, offset = 3)
        } else if (value.startsWith(DataType.AN.value)) {
            return IDataType(dataType = DataType.AN, offset = 2)
        } else if (value.startsWith(DataType.A.value)) {
            return IDataType(dataType = DataType.A, offset = 1)
        } else if (value.startsWith(DataType.N.value)) {
            return IDataType(dataType = DataType.N, offset = 1)
        } else if (value.startsWith(DataType.Z.value)) {
            return IDataType(dataType = DataType.Z, offset = 1)
        } else if (value.startsWith(DataType.B.value)) {
            return IDataType(dataType = DataType.B, offset = 1)
        }
        throw IllegalArgumentException("Invalid field dataType: $value")
    }

    private fun getLengthType(value: String, offset: Int): ILengthType {
        if (0 < offset) {
            val index = value.indexOf("...", offset)

            if (0 <= index) {
                return ILengthType(lengthType = LengthType.LLLVAR, offset = index + 3)
            } else {
                val index = value.indexOf("..", offset)

                if (0 <= index) {
                    return ILengthType(lengthType = LengthType.LLVAR, offset = index + 2)
                }
                return ILengthType(lengthType = LengthType.FIX, offset = offset)
            }
        }
        throw IllegalArgumentException("Invalid field lengthType: $value, offset: $offset")
    }

    private fun getAlignment(value: String, offset: Int): IAlignment {
        if (0 <= offset) {
            when (value.slice(offset..offset)) {
                "L" -> return IAlignment(alignment = Alignment.LEFT, offset = offset + 1)
                "R" -> return IAlignment(alignment = Alignment.RIGHT, offset = offset + 1)
            }
        }
        throw IllegalArgumentException("Invalid field alignment: $value, offset: $offset")
    }

    private fun getMaxLength(value: String, offset: Int): Int {
        if (0 <= offset) {
            val maxLength = value.slice(offset until value.length)

            if (maxLength.isNotEmpty()) {
                return maxLength.toInt()
            }
        }
        throw IllegalArgumentException("Missing field max length: $value, offset: $offset")
    }
}
