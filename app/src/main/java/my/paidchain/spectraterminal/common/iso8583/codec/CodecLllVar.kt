package my.paidchain.spectraterminal.common.iso8583.codec

import my.paidchain.spectraterminal.common.Misc.Companion.hexStringToByteArray
import my.paidchain.spectraterminal.common.Misc.Companion.toHex
import my.paidchain.spectraterminal.common.iso8583.DataType
import my.paidchain.spectraterminal.common.iso8583.FieldConfig

class CodecLllVar(parent: Codec?) : Codec(parent) {
    override fun pack(data: ByteArray, dataLength: Int, config: FieldConfig): PackResult {
        val ret: PackResult =
            if (this.parent is Codec) this.parent.pack(data, dataLength, config) else PackResult(
                data,
                dataLength
            )

        if (config.maxLength < ret.dataLength) {
            throw IllegalArgumentException("Bit ${config.bitNo}: Data length is overflow. Length: ${ret.dataLength}, expected: ${config.maxLength}")
        }

        val value = "000${ret.dataLength}"
        val header = value.slice(value.length - 4 until value.length).hexStringToByteArray()

        ret.data = header + ret.data

        return ret
    }

    override fun unpack(data: ByteArray, config: FieldConfig): UnpackResult {
        val ret: UnpackResult =
            if (this.parent is Codec) this.parent.unpack(data, config) else UnpackResult(
                data,
                data.size,
                data.size
            )

        if (ret.data.isEmpty()) {
            throw IllegalArgumentException("Bit ${config.bitNo}: Header length is underflow")
        }

        val value = ret.data.slice(0..1).toByteArray().toHex()
        val dataLength = value.toInt()
        val unitLength =
            if (DataType.N === config.dataType || DataType.Z === config.dataType) 2 else 1
        val bufferLength =
            (dataLength + if (DataType.N === config.dataType || DataType.Z === config.dataType) if (0 !== dataLength % 2) 1 else 0 else 0) / unitLength

        if (bufferLength + 2 > ret.data.size) {
            throw IllegalArgumentException("Bit ${config.bitNo}: Data length is underflow. dataLength: ${ret.data.size}, bufferLength: ${bufferLength + 2}")
        }

        ret.data = ret.data.slice(2 until bufferLength + 2).toByteArray()
        ret.dataLength = dataLength
        ret.usedLength = bufferLength + 2

        return ret
    }
}
