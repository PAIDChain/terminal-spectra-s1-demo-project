package my.paidchain.spectraterminal.common.iso8583.codec

import java.nio.charset.Charset
import my.paidchain.spectraterminal.common.iso8583.DataFormat
import my.paidchain.spectraterminal.common.iso8583.FieldConfig

class CodecDataTypeAns : CodecDataType() {
    override fun pack(value: String, config: FieldConfig): ByteArray {
        if (DataFormat.EBCDIC === config.dataFormat) {
            return value.toByteArray(Charset.forName("IBM037"))
        }
        return value.toByteArray()
    }

    override fun unpack(data: ByteArray, dataLength: Int, config: FieldConfig): String {
        if (DataFormat.EBCDIC === config.dataFormat) {
            return data.toString(Charset.forName("IBM037"))
        }
        return String(data)
    }
}
