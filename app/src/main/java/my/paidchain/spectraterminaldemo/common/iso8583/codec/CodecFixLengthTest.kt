package my.paidchain.spectraterminaldemo.common.iso8583.codec

import org.junit.Assert
import org.junit.Test
import my.paidchain.spectraterminaldemo.common.Misc.Companion.hexStringToByteArray
import my.paidchain.spectraterminaldemo.common.iso8583.Alignment
import my.paidchain.spectraterminaldemo.common.iso8583.DataType
import my.paidchain.spectraterminaldemo.common.iso8583.FieldConfig
import my.paidchain.spectraterminaldemo.common.iso8583.LengthType

class CodecMockFixLength(parent: Codec?) : Codec(parent) {
    override fun pack(data: ByteArray, dataLength: Int, config: FieldConfig): PackResult {
        return PackResult(data, dataLength)
    }

    override fun unpack(data: ByteArray, config: FieldConfig): UnpackResult {
        return UnpackResult(data, data.size, data.size)
    }
}

@Suppress("FunctionName")
class CodecFixLengthTest {
    @Test
    fun Should_be_able_to_pack_data() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            lengthType = LengthType.FIX,
            alignment = Alignment.LEFT,
            maxLength = 15,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecFixLength(null)
        val buffer = "313233343536373839303132333435".hexStringToByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecFixLength)
        Assert.assertArrayEquals(data, "313233343536373839303132333435".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_pack_data_with_parent() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            lengthType = LengthType.FIX,
            alignment = Alignment.LEFT,
            maxLength = 15,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecFixLength(CodecMockFixLength(null))
        val buffer = "313233343536373839303132333435".hexStringToByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecFixLength)
        Assert.assertArrayEquals(data, "313233343536373839303132333435".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_pack_data_type_N_with_mask_nibble() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.N,
            lengthType = LengthType.FIX,
            alignment = Alignment.RIGHT,
            maxLength = 19,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecFixLength(null)
        val buffer = "1234567890123456789".hexStringToByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecFixLength)
        Assert.assertArrayEquals(data, "1234567890123456789".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_pack_data_type_N_without_mask_nibble() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.N,
            lengthType = LengthType.FIX,
            alignment = Alignment.RIGHT,
            maxLength = 19,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecFixLength(null)
        val buffer = "1234567890123456".hexStringToByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecFixLength)
        Assert.assertArrayEquals(data, "1234567890123456".hexStringToByteArray())
    }

    @Test
    fun Should_catch_error_when_data_overflow() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            lengthType = LengthType.FIX,
            alignment = Alignment.LEFT,
            maxLength = 15,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecFixLength(null)
        val buffer = "31323334353637383930313233343536".hexStringToByteArray()

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
            lengthType = LengthType.FIX,
            alignment = Alignment.LEFT,
            maxLength = 15,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecFixLength(null)
        val (data, _, usedLength) = codec.unpack(
            "313233343536373839303132333435".hexStringToByteArray(),
            config
        )

        Assert.assertTrue(codec is CodecFixLength)
        Assert.assertArrayEquals(data, "313233343536373839303132333435".hexStringToByteArray())
        Assert.assertEquals(usedLength, 15)
    }

    @Test
    fun Should_be_able_to_unpack_data_with_parent() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            lengthType = LengthType.FIX,
            alignment = Alignment.LEFT,
            maxLength = 15,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecFixLength(CodecMockFixLength(null))
        val (data, _, usedLength) = codec.unpack(
            "313233343536373839303132333435".hexStringToByteArray(),
            config
        )

        Assert.assertTrue(codec is CodecFixLength)
        Assert.assertArrayEquals(data, "313233343536373839303132333435".hexStringToByteArray())
        Assert.assertEquals(usedLength, 15)
    }

    @Test
    fun Should_be_able_to_unpack_data_type_N_with_mask_nibble() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.N,
            lengthType = LengthType.FIX,
            alignment = Alignment.RIGHT,
            maxLength = 19,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecFixLength(null)
        val (data, _, usedLength) = codec.unpack(
            "1234567890123456789".hexStringToByteArray(),
            config
        )

        Assert.assertTrue(codec is CodecFixLength)
        Assert.assertArrayEquals(data, "1234567890123456789".hexStringToByteArray())
        Assert.assertEquals(usedLength, 9)
    }

    @Test
    fun Should_be_able_to_unpack_data_type_N_without_mask_nibble() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.N,
            lengthType = LengthType.FIX,
            alignment = Alignment.RIGHT,
            maxLength = 6,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecFixLength(null)
        val (data, _, usedLength) = codec.unpack(
            "930000".hexStringToByteArray(),
            config
        )

        Assert.assertTrue(codec is CodecFixLength)
        Assert.assertArrayEquals(data, "930000".hexStringToByteArray())
        Assert.assertEquals(usedLength, 3)
    }
}
