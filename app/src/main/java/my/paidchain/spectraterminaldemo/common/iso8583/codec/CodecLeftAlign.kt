package my.paidchain.spectraterminaldemo.common.iso8583.codec

import my.paidchain.spectraterminaldemo.common.iso8583.DataType
import my.paidchain.spectraterminaldemo.common.iso8583.FieldConfig
import my.paidchain.spectraterminaldemo.common.iso8583.LengthType

class CodecLeftAlign(parent: Codec?) : Codec(parent) {
    override fun pack(data: ByteArray, dataLength: Int, config: FieldConfig): PackResult {
        val ret: PackResult =
            if (this.parent is Codec) this.parent.pack(data, dataLength, config) else PackResult(
                data,
                dataLength
            )

        if (LengthType.FIX !== config.lengthType || config.padding.isEmpty()) {
            return ret
        }

        when (config.dataType) {
            DataType.N, DataType.Z -> {
                val maxLength = (config.maxLength + if (0 !== config.maxLength % 2) 1 else 0) / 2
                val shortLength = maxLength - ret.data.size

                if (0 < shortLength) {
                    val padding = ByteArray(shortLength)

                    padding.fill(config.padding[0])
                    ret.data = ret.data + padding
                }
            }
            DataType.B, DataType.A, DataType.AN, DataType.ANS -> {
                val shortLength = config.maxLength - ret.data.size

                if (0 < shortLength) {
                    val padding = ByteArray(shortLength)

                    padding.fill(config.padding[0])
                    ret.data = ret.data + padding
                }
            }
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

        if ((DataType.N === config.dataType || DataType.Z === config.dataType) && 0 !== ret.dataLength % 2) {
            ret.data[ret.data.size - 1] =
                (ret.data[ret.data.size - 1].toInt() and 0xf0).toByte() // Remove nibble
        }

        return ret
    }
}