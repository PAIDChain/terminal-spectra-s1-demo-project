package my.paidchain.spectraterminal.common.iso8583.codec

import my.paidchain.spectraterminal.common.iso8583.DataFormat
import my.paidchain.spectraterminal.common.iso8583.DataType
import my.paidchain.spectraterminal.common.iso8583.FieldConfig

class CodecFixLength : Codec {
    constructor(parent: Codec?) : super(parent)

    override fun pack(data: ByteArray, dataLength: Int, config: FieldConfig): PackResult {
        val ret: PackResult =
            if (this.parent is Codec) this.parent.pack(data, dataLength, config) else PackResult(
                data,
                dataLength
            )

        if (ret.dataLength > config.maxLength) {
            throw IllegalArgumentException("Bit ${config.bitNo}: Data length overflow")
        }

        return ret
    }

    override fun unpack(data: ByteArray, config: FieldConfig): UnpackResult {
        val ret: UnpackResult =
            if (this.parent is Codec) this.parent.unpack(data, config) else UnpackResult(
                data,
                data.size,
                data.size
            )

        val dataLength =
            if (DataType.N === config.dataType && DataFormat.ASCII === config.dataFormat) {
                (config.maxLength + (if (0 !== config.maxLength % 2) 1 else 0)) / 2
            } else config.maxLength

        ret.data =
            ret.data.slice(0 until if (dataLength > ret.data.size) ret.data.size else dataLength)
                .toByteArray()
        ret.dataLength = config.maxLength
        ret.usedLength = ret.data.size

        return ret
    }
}