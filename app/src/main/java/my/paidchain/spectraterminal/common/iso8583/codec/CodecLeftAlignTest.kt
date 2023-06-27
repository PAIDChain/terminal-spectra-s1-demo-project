package my.paidchain.spectraterminal.common.iso8583.codec

import org.junit.Assert
import org.junit.Test
import my.paidchain.spectraterminal.common.Misc.Companion.hexStringToByteArray
import my.paidchain.spectraterminal.common.iso8583.Alignment
import my.paidchain.spectraterminal.common.iso8583.DataType
import my.paidchain.spectraterminal.common.iso8583.FieldConfig
import my.paidchain.spectraterminal.common.iso8583.LengthType

class CodecMockLeftAlign(parent: Codec?) : Codec(parent) {
    override fun pack(data: ByteArray, dataLength: Int, config: FieldConfig): PackResult {
        return PackResult(data, dataLength)
    }

    override fun unpack(data: ByteArray, config: FieldConfig): UnpackResult {
        return UnpackResult(data, data.size, data.size)
    }
}

@Suppress("FunctionName")
class CodecLeftAlignTest {
    @Test
    fun Should_be_able_to_pack_data() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            lengthType = LengthType.FIX,
            alignment = Alignment.LEFT,
            maxLength = 15,
            padding = byteArrayOf(0x30)
        )

        val codec = CodecLeftAlign(null)
        val buffer = "12345678901234".toByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecLeftAlign)
        Assert.assertArrayEquals(data, "123456789012340".toByteArray())
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
            padding = byteArrayOf(0x30)
        )

        val codec = CodecLeftAlign(CodecMockLeftAlign(null))
        val buffer = "12345678901234".toByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecLeftAlign)
        Assert.assertArrayEquals(data, "123456789012340".toByteArray())
    }

    @Test
    fun Should_be_able_to_pack_pass_thruogh_data_for_non_Fix_Length() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            lengthType = LengthType.LLVAR,
            alignment = Alignment.LEFT,
            maxLength = 6,
            padding = byteArrayOf(0x30)
        )

        val codec = CodecLeftAlign(null)
        val buffer = "0830313233".hexStringToByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecLeftAlign)
        Assert.assertArrayEquals(data, "0830313233".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_pack_data_type_N() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.N,
            lengthType = LengthType.FIX,
            alignment = Alignment.LEFT,
            maxLength = 4,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecLeftAlign(null)
        val buffer = "12".hexStringToByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecLeftAlign)
        Assert.assertArrayEquals(data, "1200".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_pack_data_type_N_with_mask_nibble() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.N,
            lengthType = LengthType.FIX,
            alignment = Alignment.LEFT,
            maxLength = 5,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecLeftAlign(null)
        val buffer = "1230".hexStringToByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecLeftAlign)
        Assert.assertArrayEquals(data, "123000".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_pack_data_type_B() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.B,
            lengthType = LengthType.FIX,
            alignment = Alignment.LEFT,
            maxLength = 5,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecLeftAlign(null)
        val buffer = "AABBCC".hexStringToByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecLeftAlign)
        Assert.assertArrayEquals(data, "AABBCC0000".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_pack_data_type_A() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.A,
            lengthType = LengthType.FIX,
            alignment = Alignment.LEFT,
            maxLength = 8,
            padding = byteArrayOf(0x30)
        )

        val codec = CodecLeftAlign(null)
        val buffer = "123".toByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecLeftAlign)
        Assert.assertArrayEquals(data, "12300000".toByteArray())
    }

    @Test
    fun Should_be_able_to_pack_data_type_AN() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.AN,
            lengthType = LengthType.FIX,
            alignment = Alignment.LEFT,
            maxLength = 8,
            padding = byteArrayOf(0x30)
        )

        val codec = CodecLeftAlign(null)
        val buffer = "123".toByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecLeftAlign)
        Assert.assertArrayEquals(data, "12300000".toByteArray())
    }

    @Test
    fun Should_be_able_to_pack_data_type_ANS() {
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.ANS,
            lengthType = LengthType.FIX,
            alignment = Alignment.LEFT,
            maxLength = 8,
            padding = byteArrayOf(0x30)
        )

        val codec = CodecLeftAlign(null)
        val buffer = "123".toByteArray()
        val (data) = codec.pack(buffer, buffer.size, config)

        Assert.assertTrue(codec is CodecLeftAlign)
        Assert.assertArrayEquals(data, "12300000".toByteArray())
    }

    @Test
    fun Should_be_able_to_unpack_data(){
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.N,
            lengthType = LengthType.FIX,
            alignment = Alignment.LEFT,
            maxLength = 19,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecLeftAlign(null)
        val (data, _, usedLength) = codec.unpack("12345678901234567890".hexStringToByteArray(), config)

        Assert.assertTrue(codec is CodecLeftAlign)
        Assert.assertArrayEquals(data, "12345678901234567890".hexStringToByteArray())
        Assert.assertEquals(usedLength, 10)
    }

    @Test
    fun Should_be_able_to_unpack_data_with_parent(){
        val config = FieldConfig(
            bitNo = 0,
            name = "",
            dataType = DataType.N,
            lengthType = LengthType.FIX,
            alignment = Alignment.LEFT,
            maxLength = 19,
            padding = byteArrayOf(0x00)
        )

        val codec = CodecLeftAlign(CodecMockLeftAlign(null))
        val (data, _, usedLength) = codec.unpack("12345678901234567890".hexStringToByteArray(), config)

        Assert.assertTrue(codec is CodecLeftAlign)
        Assert.assertArrayEquals(data, "12345678901234567890".hexStringToByteArray())
        Assert.assertEquals(usedLength, 10)
    }
}