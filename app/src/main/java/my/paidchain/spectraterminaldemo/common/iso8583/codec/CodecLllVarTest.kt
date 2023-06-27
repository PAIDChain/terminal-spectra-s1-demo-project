package my.paidchain.spectraterminaldemo.common.iso8583.codec

import org.junit.Assert
import org.junit.Test
import my.paidchain.spectraterminaldemo.common.Misc.Companion.hexStringToByteArray
import my.paidchain.spectraterminaldemo.common.iso8583.Alignment
import my.paidchain.spectraterminaldemo.common.iso8583.DataType
import my.paidchain.spectraterminaldemo.common.iso8583.FieldConfig
import my.paidchain.spectraterminaldemo.common.iso8583.LengthType

class CodecMockLllVar(parent: Codec?) : Codec(parent) {
    override fun pack(data: ByteArray, dataLength: Int, config: FieldConfig): PackResult {
        return PackResult(data, dataLength)
    }

    override fun unpack(data: ByteArray, config: FieldConfig): UnpackResult {
        return UnpackResult(data, data.size, data.size)
    }
}

@Suppress("FunctionName")
class CodecLllVarTest {
    @Test
    fun Should_be_able_to_pack_data() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            lengthType = LengthType.LLLVAR,
            alignment = Alignment.RIGHT,
            maxLength = 15,
            padding = byteArrayOf(0x30)
        )

        val codec = CodecLllVar(null)
        val buffer = "123456789".toByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecLllVar)
        Assert.assertArrayEquals(data, "0009313233343536373839".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_pack_data_with_parent() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            lengthType = LengthType.LLLVAR,
            alignment = Alignment.RIGHT,
            maxLength = 15,
            padding = byteArrayOf(0x30)
        )

        val codec = CodecLllVar(CodecMockLllVar(null))
        val buffer = "123456789".toByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecLllVar)
        Assert.assertArrayEquals(data, "0009313233343536373839".hexStringToByteArray())
    }

    @Test
    fun Should_catch_error_when_pack_data_for_data_overflow() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            lengthType = LengthType.LLLVAR,
            alignment = Alignment.RIGHT,
            maxLength = 15,
            padding = byteArrayOf(0x30)
        )

        val codec = CodecLllVar(null)
        val buffer = "1234567890123456".toByteArray()

        try {
            codec.pack(buffer, buffer.size, config)
            Assert.fail("Should not reach here")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_be_able_to_unpack_data() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            lengthType = LengthType.LLLVAR,
            alignment = Alignment.RIGHT,
            maxLength = 15,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecLllVar(null)
        val (data, _, usedLength) = codec.unpack(
            "0009313233343536373839".hexStringToByteArray(),
            config
        )

        Assert.assertTrue(codec is CodecLllVar)
        Assert.assertArrayEquals(data, "123456789".toByteArray())
        Assert.assertEquals(usedLength, 11)
    }

    @Test
    fun Should_be_able_to_unpack_data_with_N_data_type() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.N,
            lengthType = LengthType.LLLVAR,
            alignment = Alignment.RIGHT,
            maxLength = 19,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecLllVar(null)
        val (data, _, usedLength) = codec.unpack(
            "00164444333322221111".hexStringToByteArray(),
            config
        )

        Assert.assertTrue(codec is CodecLllVar)
        Assert.assertArrayEquals(data, "4444333322221111".hexStringToByteArray())
        Assert.assertEquals(usedLength, 10)
    }

    @Test
    fun Should_be_able_to_unpack_data_with_Z_data_type() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.Z,
            lengthType = LengthType.LLLVAR,
            alignment = Alignment.RIGHT,
            maxLength = 37,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecLllVar(null)
        val (data, _, usedLength) = codec.unpack(
            "00374570660700090007d010810114784448000000".hexStringToByteArray(),
            config
        )

        Assert.assertTrue(codec is CodecLllVar)
        Assert.assertArrayEquals(
            data,
            "4570660700090007d010810114784448000000".hexStringToByteArray()
        )
        Assert.assertEquals(usedLength, 21)
    }

    @Test
    fun Should_be_able_to_unpack_data_with_parent() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            lengthType = LengthType.LLLVAR,
            alignment = Alignment.RIGHT,
            maxLength = 15,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecLllVar(CodecMockLllVar(null))
        val (data, _, usedLength) = codec.unpack(
            "0009313233343536373839".hexStringToByteArray(),
            config
        )

        Assert.assertTrue(codec is CodecLllVar)
        Assert.assertArrayEquals(data, "123456789".toByteArray())
        Assert.assertEquals(usedLength, 11)
    }

    @Test
    fun Should_catch_error_when_unpack_data_for_header_underflow() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            lengthType = LengthType.LLLVAR,
            alignment = Alignment.RIGHT,
            maxLength = 15,
            padding = byteArrayOf(0x30)
        )

        val codec = CodecLllVar(null)

        try {
            codec.unpack("".hexStringToByteArray(), config)
            Assert.fail("Should not reach here")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_catch_error_when_unpack_data_for_data_underflow(){
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            lengthType = LengthType.LLLVAR,
            alignment = Alignment.RIGHT,
            maxLength = 15,
            padding = byteArrayOf(0x30)
        )

        val codec = CodecLllVar(null)

        try {
            codec.unpack("000230".hexStringToByteArray(), config)
            Assert.fail("Should not reach here")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_catch_error_when_unpack_data_for_invalid_header_length(){
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            lengthType = LengthType.LLLVAR,
            alignment = Alignment.RIGHT,
            maxLength = 15,
            padding = byteArrayOf(0x30)
        )

        val codec = CodecLllVar(null)

        try {
            codec.unpack("009a30".hexStringToByteArray(), config)
            Assert.fail("Should not reach here")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }
}
