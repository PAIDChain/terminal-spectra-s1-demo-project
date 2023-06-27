package my.paidchain.spectraterminal.common.iso8583.codec

import my.paidchain.spectraterminal.common.Misc.Companion.hexStringToByteArray
import my.paidchain.spectraterminal.common.Misc.Companion.toHex
import my.paidchain.spectraterminal.common.iso8583.Alignment
import my.paidchain.spectraterminal.common.iso8583.FieldConfig

class CodecDataTypeZ : CodecDataType() {
    override fun pack(value: String, config: FieldConfig): ByteArray {
        val value = value.replace("=", "d")

        if (0 !== value.length % 2) {
            return if (Alignment.LEFT == config.alignment) {
                val data = "${value}0".hexStringToByteArray()
                val padding = config.padding[0].toInt() and 0x0f // Pad nibble

                data[data.size - 1] = (data[data.size - 1].toInt() or padding).toByte()
                data
            } else {
                val data = "0${value}".hexStringToByteArray()
                val padding = config.padding[0].toInt() and 0xf0 // Pad nibble

                data[data.size - 1] = (data[data.size - 1].toInt() or padding).toByte()
                data
            }
        }

        return value.hexStringToByteArray()
    }

    override fun unpack(data: ByteArray, dataLength: Int, config: FieldConfig): String {
        val bcd = data.toHex().replace("d", "=")
        val dataLength = if (dataLength > config.maxLength) config.maxLength else dataLength

        if (0 !== dataLength % 2 && bcd.length == dataLength + 1) {
            return if (Alignment.LEFT == config.alignment) {
                bcd.substring(0, bcd.length - 1)
            } else {
                bcd.substring(1)
            }
        }

        return bcd
    }
}