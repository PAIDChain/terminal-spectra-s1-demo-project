package my.paidchain.spectraterminaldemo.common.iso8583.codec

import org.junit.Assert
import org.junit.Test
import my.paidchain.spectraterminaldemo.common.Misc.Companion.hexStringToByteArray
import my.paidchain.spectraterminaldemo.common.iso8583.Alignment
import my.paidchain.spectraterminaldemo.common.iso8583.DataType
import my.paidchain.spectraterminaldemo.common.iso8583.FieldConfig
import my.paidchain.spectraterminaldemo.common.iso8583.LengthType

@Suppress("FunctionName")
class CodecDataTypeZTest {
    @Test
    fun Should_be_able_to_pack_data() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.Z,
            lengthType = LengthType.LLVAR,
            alignment = Alignment.LEFT,
            maxLength = 37,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecDataTypeZ()
        val result = codec.pack("4570660700090007=010810114784448000001", config)

        Assert.assertTrue(codec is CodecDataTypeZ)
        Assert.assertArrayEquals(result, "4570660700090007d010810114784448000001".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_pack_data_with_left_alignment() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.Z,
            lengthType = LengthType.LLVAR,
            alignment = Alignment.LEFT,
            maxLength = 37,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecDataTypeZ()
        val result = codec.pack("4570660700090007=01081011478444800000", config)

        Assert.assertTrue(codec is CodecDataTypeZ)
        Assert.assertArrayEquals(result, "4570660700090007d010810114784448000000".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_pack_data_with_right_alignment() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.Z,
            lengthType = LengthType.LLVAR,
            alignment = Alignment.RIGHT,
            maxLength = 37,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecDataTypeZ()
        val result = codec.pack("4570660700090007=01081011478444800000", config)

        Assert.assertTrue(codec is CodecDataTypeZ)
        Assert.assertArrayEquals(result, "04570660700090007d01081011478444800000".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_unpack_data() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.Z,
            lengthType = LengthType.LLVAR,
            alignment = Alignment.LEFT,
            maxLength = 37,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecDataTypeZ()
        val data = "4570660700090007d0108101147844480000".hexStringToByteArray()
        val result = codec.unpack(data, data.size * 2, config)

        Assert.assertTrue(codec is CodecDataTypeZ)
        Assert.assertEquals(result, "4570660700090007=0108101147844480000")
    }

    @Test
    fun Should_be_able_to_unpack_data_with_left_alignment() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.Z,
            lengthType = LengthType.LLVAR,
            alignment = Alignment.LEFT,
            maxLength = 37,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecDataTypeZ()
        val data = "4570660700090007d010810114784448000000".hexStringToByteArray()
        val result = codec.unpack(data, data.size * 2, config)

        Assert.assertTrue(codec is CodecDataTypeZ)
        Assert.assertEquals(result, "4570660700090007=01081011478444800000")
    }

    @Test
    fun Should_be_able_to_unpack_data_with_right_alignment() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.Z,
            lengthType = LengthType.LLVAR,
            alignment = Alignment.RIGHT,
            maxLength = 37,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecDataTypeZ()
        val data = "04570660700090007d01081011478444800000".hexStringToByteArray()
        val result = codec.unpack(data, data.size * 2, config)

        Assert.assertTrue(codec is CodecDataTypeZ)
        Assert.assertEquals(result, "4570660700090007=01081011478444800000")
    }
}
