package my.paidchain.spectraterminal.common.iso8583.codec

import java.nio.charset.Charset
import my.paidchain.spectraterminal.common.Misc.Companion.hexStringToByteArray
import my.paidchain.spectraterminal.common.Misc.Companion.toHex
import my.paidchain.spectraterminal.common.iso8583.Alignment
import my.paidchain.spectraterminal.common.iso8583.DataFormat
import my.paidchain.spectraterminal.common.iso8583.FieldConfig

class CodecDataTypeN : CodecDataType() {
    override fun pack(value: String, config: FieldConfig): ByteArray {
        if (DataFormat.EBCDIC === config.dataFormat) {
            return value.toByteArray(Charset.forName("IBM037"))
        }

        val ret =
            if (0 !== value.length % 2) {
                if (Alignment.LEFT === config.alignment) "${value}0" else "0${value}"
            } else value

        return ret.hexStringToByteArray()
    }

    override fun unpack(data: ByteArray, dataLength: Int, config: FieldConfig): String {
        if (DataFormat.EBCDIC === config.dataFormat) {
            return data.toString(Charset.forName("IBM037"))
        }

        val dataLength = if (dataLength > config.maxLength) config.maxLength else dataLength
        var bcd = data.toHex()

        if (0 !== dataLength % 2 && bcd.length === dataLength + 1) {
            bcd =
                if (Alignment.LEFT === config.alignment) bcd.slice(0 until bcd.length - 1) else bcd.substring(
                    1
                )
        }

        return bcd
    }
}
