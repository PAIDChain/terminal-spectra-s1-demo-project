package my.paidchain.spectraterminal.common.iso8583.codec

import org.junit.Assert
import org.junit.Test
import my.paidchain.spectraterminal.common.Misc.Companion.hexStringToByteArray
import my.paidchain.spectraterminal.common.iso8583.Alignment
import my.paidchain.spectraterminal.common.iso8583.DataType
import my.paidchain.spectraterminal.common.iso8583.FieldConfig
import my.paidchain.spectraterminal.common.iso8583.LengthType

class CodecMockRightAlign(parent: Codec?) : Codec(parent) {
    override fun pack(data: ByteArray, dataLength: Int, config: FieldConfig): PackResult {
        return PackResult(data, dataLength)
    }

    override fun unpack(data: ByteArray, config: FieldConfig): UnpackResult {
        return UnpackResult(data, data.size, data.size)
    }
}

@Suppress("FunctionName")
class CodecRightAlignTest {
    @Test
    fun Should_be_able_to_pack_data() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            lengthType = LengthType.FIX,
            alignment = Alignment.RIGHT,
            maxLength = 15,
            padding = byteArrayOf(0x30)
        )

        val codec = CodecRightAlign(null)
        val buffer = "12345678901234".toByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecRightAlign)
        Assert.assertArrayEquals(data, "012345678901234".toByteArray())
    }

    @Test
    fun Should_be_able_to_pack_data_with_parent() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            lengthType = LengthType.FIX,
            alignment = Alignment.RIGHT,
            maxLength = 15,
            padding = byteArrayOf(0x30)
        )

        val codec = CodecRightAlign(CodecMockRightAlign(null))
        val buffer = "12345678901234".toByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecRightAlign)
        Assert.assertArrayEquals(data, "012345678901234".toByteArray())
    }

    @Test
    fun Should_be_able_to_pack_pass_thruogh_data_for_non_Fix_Length() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            lengthType = LengthType.LLVAR,
            alignment = Alignment.RIGHT,
            maxLength = 6,
            padding = byteArrayOf(0x30)
        )

        val codec = CodecRightAlign(null)
        val buffer = "0830313233".hexStringToByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecRightAlign)
        Assert.assertArrayEquals(data, "0830313233".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_pack_data_type_N() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.N,
            lengthType = LengthType.FIX,
            alignment = Alignment.RIGHT,
            maxLength = 4,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecRightAlign(null)
        val buffer = "12".hexStringToByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecRightAlign)
        Assert.assertArrayEquals(data, "0012".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_pack_data_type_N_with_mask_nibble() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.N,
            lengthType = LengthType.FIX,
            alignment = Alignment.RIGHT,
            maxLength = 5,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecRightAlign(null)
        val buffer = "0123".hexStringToByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecRightAlign)
        Assert.assertArrayEquals(data, "000123".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_pack_data_type_B() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.B,
            lengthType = LengthType.FIX,
            alignment = Alignment.RIGHT,
            maxLength = 5,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecRightAlign(null)
        val buffer = "AABBCC".hexStringToByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecRightAlign)
        Assert.assertArrayEquals(data, "0000AABBCC".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_pack_data_type_A() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.A,
            lengthType = LengthType.FIX,
            alignment = Alignment.RIGHT,
            maxLength = 8,
            padding = byteArrayOf(0x30)
        )

        val codec = CodecRightAlign(null)
        val buffer = "123".toByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecRightAlign)
        Assert.assertArrayEquals(data, "00000123".toByteArray())
    }

    @Test
    fun Should_be_able_to_pack_data_type_AN() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.AN,
            lengthType = LengthType.FIX,
            alignment = Alignment.RIGHT,
            maxLength = 8,
            padding = byteArrayOf(0x30)
        )

        val codec = CodecRightAlign(null)
        val buffer = "123".toByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecRightAlign)
        Assert.assertArrayEquals(data, "00000123".toByteArray())
    }

    @Test
    fun Should_be_able_to_pack_data_type_ANS() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            lengthType = LengthType.FIX,
            alignment = Alignment.RIGHT,
            maxLength = 8,
            padding = byteArrayOf(0x30)
        )

        val codec = CodecRightAlign(null)
        val buffer = "123".toByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecRightAlign)
        Assert.assertArrayEquals(data, "00000123".toByteArray())
    }

    @Test
    fun Should_be_able_to_unpack_data(){
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.N,
            lengthType = LengthType.FIX,
            alignment = Alignment.RIGHT,
            maxLength = 19,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecRightAlign(null)
        val (data, _, usedLength) = codec.unpack("01234567890123456789".hexStringToByteArray(), config)

        Assert.assertTrue(codec is CodecRightAlign)
        Assert.assertArrayEquals(data, "01234567890123456789".hexStringToByteArray())
        Assert.assertEquals(usedLength, 10)
    }

    @Test
    fun Should_be_able_to_unpack_data_with_parent(){
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.N,
            lengthType = LengthType.FIX,
            alignment = Alignment.RIGHT,
            maxLength = 19,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecRightAlign(CodecMockRightAlign(null))
        val (data, _, usedLength) = codec.unpack("01234567890123456789".hexStringToByteArray(), config)

        Assert.assertTrue(codec is CodecRightAlign)
        Assert.assertArrayEquals(data, "01234567890123456789".hexStringToByteArray())
        Assert.assertEquals(usedLength, 10)
    }
}