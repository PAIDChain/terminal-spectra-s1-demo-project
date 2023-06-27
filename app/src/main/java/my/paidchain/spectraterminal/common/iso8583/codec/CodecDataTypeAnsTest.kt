package my.paidchain.spectraterminal.common.iso8583.codec

import org.junit.Assert
import org.junit.Test
import my.paidchain.spectraterminal.common.Misc.Companion.hexStringToByteArray
import my.paidchain.spectraterminal.common.iso8583.FieldConfig
import my.paidchain.spectraterminal.common.iso8583.DataType
import my.paidchain.spectraterminal.common.iso8583.DataFormat
import my.paidchain.spectraterminal.common.iso8583.LengthType
import my.paidchain.spectraterminal.common.iso8583.Alignment

@Suppress("FunctionName")
class CodecDataTypeAnsTest {
    @Test
    fun Should_be_able_to_pack_data_in_ASCII_format() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            dataFormat = DataFormat.ASCII,
            lengthType = LengthType.FIX,
            alignment = Alignment.RIGHT,
            maxLength = 6,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecDataTypeAns()
        val result = codec.pack("123456789012345", config)

        Assert.assertTrue(codec is CodecDataTypeAns)
        Assert.assertArrayEquals(result, "313233343536373839303132333435".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_pack_data_in_EBCDIC_format() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            dataFormat = DataFormat.EBCDIC,
            lengthType = LengthType.FIX,
            alignment = Alignment.RIGHT,
            maxLength = 6,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecDataTypeAns()
        val result = codec.pack("123456789012345", config)

        Assert.assertTrue(codec is CodecDataTypeAns)
        Assert.assertArrayEquals(result, "F1F2F3F4F5F6F7F8F9F0F1F2F3F4F5".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_unpack_data_in_ASCII_format() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            dataFormat = DataFormat.ASCII,
            lengthType = LengthType.FIX,
            alignment = Alignment.RIGHT,
            maxLength = 6,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecDataTypeAns()
        val result =
            codec.unpack("313233343536373839303132333435".hexStringToByteArray(), 0, config)

        Assert.assertTrue(codec is CodecDataTypeAns)
        Assert.assertEquals(result, "123456789012345")
    }

    @Test
    fun Should_be_able_to_unpack_data_in_EBCDIC_format() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            dataFormat = DataFormat.EBCDIC,
            lengthType = LengthType.FIX,
            alignment = Alignment.RIGHT,
            maxLength = 6,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecDataTypeAns()
        val result =
            codec.unpack("F1F2F3F4F5F6F7F8F9F0F1F2F3F4F5".hexStringToByteArray(), 0, config)

        Assert.assertTrue(codec is CodecDataTypeAns)
        Assert.assertEquals(result, "123456789012345")
    }
}