package my.paidchain.spectraterminal.common.iso8583

import org.junit.Assert
import org.junit.Test
import java.nio.ByteBuffer
import my.paidchain.spectraterminal.common.Misc.Companion.hexStringToByteArray

@Suppress("FunctionName")
class BitmapTest {
    @Test
    fun Should_be_able_to_initialize_by_number_of_bit() {
        val bitmap = Bitmap(65)

        Assert.assertTrue(bitmap is Bitmap)
        Assert.assertEquals(bitmap.bitsLength, 128) // Multiply of 64 bits block size
    }

    @Test
    fun Should_be_able_to_initialize_by_exact_number_of_bit() {
        val bitmap = Bitmap(64)

        Assert.assertTrue(bitmap is Bitmap)
        Assert.assertEquals(bitmap.bitsLength, 64) // Multiply of 64 bits block size
    }

    @Test
    fun Should_catch_error_for_number_of_bit_is_0() {
        try {
            Bitmap(0)
            Assert.fail("Should not reach here")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_catch_error_for_number_of_bit_less_than_0() {
        try {
            Bitmap(-1)
            Assert.fail("Should not reach here")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_be_able_to_initialize_bitmap() {
        val bitmap = Bitmap("302407C000C09A7F".hexStringToByteArray())

        Assert.assertTrue(bitmap is Bitmap)
        Assert.assertEquals(bitmap.bitsLength, 64)
        Assert.assertArrayEquals(
            bitmap.bits.toTypedArray(),
            arrayOf(
                3, 4, 11, 14, 22, 23, 24, 25, 26, 41, 42, 49, 52, 53, 55, 58, 59, 60, 61, 62, 63, 64
            )
        )
    }

    @Test
    fun Should_be_able_to_initialize_extended_bitmap() {
        // Create extended bitmap with extra bytes behind
        val buffer = ByteArray(192 / 8)

        buffer.fill(-1)
        ByteBuffer.wrap(buffer).put(8, 127)

        val bitmap = Bitmap(buffer)

        Assert.assertTrue(bitmap is Bitmap)
        Assert.assertEquals(bitmap.bitsLength, 128)
        Assert.assertArrayEquals(
            bitmap.bits.toTypedArray(), arrayOf(
                2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 66, 67, 68, 69, 70, 71, 72, 73, 74,
                75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128
            )
        )
    }

    @Test
    fun Should_catch_error_with_invalid_extended_bit_in_bitmap_buffer() {
        try {
            Bitmap("ff".repeat(9).hexStringToByteArray())
            Assert.fail("Should not reach here")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_catch_error_with_invalid_bitmap_buffer_size() {
        val buffer = ByteArray(7)

        buffer.fill(-1)
        ByteBuffer.wrap(buffer).put(0, 127) // Turn off the extended bit at bit 1

        try {
            Bitmap(buffer)
            Assert.fail("Should not reach here")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_catch_error_with_invalid_parameter_type() {
        try {
            Bitmap("")
            Assert.fail("Should not reach here")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_be_able_to_get_bit_value() {
        val bitmap = Bitmap("2020010000C01003".hexStringToByteArray())

        Assert.assertTrue(bitmap.get(3))
        Assert.assertFalse(bitmap.get(2))
    }

    @Test
    fun Should_be_able_to_get_first_bit_value() {
        val bitmap = Bitmap(170)
        Assert.assertFalse(bitmap.get(1))
    }

    @Test
    fun Should_be_able_to_get_last_bit_value() {
        val bitmap = Bitmap(170)
        Assert.assertFalse(bitmap.get(192))
    }

    @Test
    fun Should_catch_error_when_get_bit_number_is_overflow() {
        val bitmap = Bitmap(170)

        try {
            bitmap.get(193)
            Assert.fail("Should not reach here")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_catch_error_when_get_bit_number_is_underflow() {
        val bitmap = Bitmap(170)

        try {
            bitmap.get(0)
            Assert.fail("Should not reach here")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_be_able_to_set_first_usable_bit_value() {
        val bitmap = Bitmap(170)

        bitmap.set(2, true)

        Assert.assertTrue(bitmap.get(2))

        // Extended bits are set correctly
        Assert.assertFalse(bitmap.get(1))
        Assert.assertFalse(bitmap.get(65))
        Assert.assertFalse(bitmap.get(128))
    }

    @Test
    fun Should_be_able_to_set_last_bit_value() {
        val bitmap = Bitmap(170)

        bitmap.set(191, true)

        Assert.assertTrue(bitmap.get(191))

        // Extended bits are set correctly
        Assert.assertTrue(bitmap.get(1))
        Assert.assertTrue(bitmap.get(65))
        Assert.assertFalse(bitmap.get(129))
    }

    @Test
    fun Should_catch_error_when_set_extended_bit() {
        val bitmap = Bitmap(170)

        try {
            bitmap.set(65, true)
            Assert.fail("Should not reach here")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_catch_error_when_set_bit_number_is_overflow() {
        val bitmap = Bitmap(170)

        try {
            bitmap.set(193, true)
            Assert.fail("Should not reach here")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_catch_error_when_set_bit_number_is_underflow() {
        val bitmap = Bitmap(170)

        try {
            bitmap.set(0, true)
            Assert.fail("Should not reach here")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_be_able_to_clear_first_usable_bit_value() {
        val bitmap = Bitmap(170)

        bitmap.set(2, true)
        Assert.assertTrue(bitmap.get(2))

        bitmap.set(2, false)
        Assert.assertFalse(bitmap.get(2))

        // Extended bits are set correctly
        Assert.assertFalse(bitmap.get(1))
        Assert.assertFalse(bitmap.get(65))
        Assert.assertFalse(bitmap.get(129))
    }

    @Test
    fun Should_be_able_to_clear_last_bit_value() {
        val bitmap = Bitmap(170)

        bitmap.set(192, true)
        Assert.assertTrue(bitmap.get(192))

        bitmap.set(192, false)
        Assert.assertFalse(bitmap.get(192))

        // Extended bits are set correctly
        Assert.assertFalse(bitmap.get(1))
        Assert.assertFalse(bitmap.get(65))
        Assert.assertFalse(bitmap.get(129))
    }

    @Test
    fun Should_be_able_to_clear_all_bit_values() {
        val bitmap = Bitmap(170)

        bitmap.clear()

        Assert.assertFalse(bitmap.get(1))
        Assert.assertFalse(bitmap.get(192))

        // Extended bits are set correctly
        Assert.assertFalse(bitmap.get(1))
        Assert.assertFalse(bitmap.get(65))
        Assert.assertFalse(bitmap.get(129))
    }

    @Test
    fun Should_catch_error_when_clear_extended_bit() {
        val bitmap = Bitmap(170)

        try {
            bitmap.set(65, false)
            Assert.fail("Should not reach here")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_catch_error_when_clear_bit_number_is_overflow() {
        val bitmap = Bitmap(170)

        try {
            bitmap.set(193, false)
            Assert.fail("Should not reach here")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_catch_error_when_clear_bit_number_is_underflow() {
        val bitmap = Bitmap(170)

        try {
            bitmap.set(0, false)
            Assert.fail("Should not reach here")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }
}