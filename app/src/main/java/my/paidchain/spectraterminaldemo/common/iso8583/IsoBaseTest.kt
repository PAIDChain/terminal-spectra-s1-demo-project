package my.paidchain.spectraterminaldemo.common.iso8583

import my.paidchain.spectraterminaldemo.common.Misc.Companion.hexStringToByteArray
import org.junit.Assert
import org.junit.Test

@Suppress("FunctionName")
class IsoBaseTest {
    @Test
    fun Should_be_able_to_initialize() {
        val iso = IsoBase(
            """
            [
                { "bitNo": 2, "name": "pan", "dataType": "N..R19", "hidden": true, "description": "Primary account number (PAN)" },
                { "bitNo": 3, "name": "procCode", "dataType": "NR6", "description": "Processing Code" },
                { "bitNo": 4, "name": "amount", "dataType": "NR12", "description": "Amount Transaction" },
                { "bitNo": 11, "name": "stan", "dataType": "NR6", "description": "System trace audit number (STAN)" },
                { "bitNo": 12, "name": "localTime", "dataType": "NR6", "description": "Local transaction time (hhmmss)" },
                { "bitNo": 13, "name": "localDate", "dataType": "NR4", "description": "Local transaction date (MMDD)" },
                { "bitNo": 14, "name": "expiryDate", "dataType": "NR4", "description": "Expiration date" },
                { "bitNo": 22, "name": "posEntryMode", "dataType": "NR3", "description": "Point of service entry mode" },
                { "bitNo": 23, "name": "panSeqNo", "dataType": "NR3", "description": "Application PAN sequence number" },
                { "bitNo": 24, "name": "nii", "dataType": "NR3", "description": "Network international identifier (NII)" },
                { "bitNo": 25, "name": "posConditionCode", "dataType": "NR2", "description": "Point of service condition code" },
                { "bitNo": 26, "name": "posCaptureCode", "dataType": "NR2", "description": "Point of service capture code" },
                { "bitNo": 35, "name": "track2", "dataType": "Z..L37", "description": "Track 2 data" },
                { "bitNo": 37, "name": "rrn", "dataType": "ANL12", "description": "Retrieval reference number" },
                { "bitNo": 38, "name": "approvalCode", "dataType": "ANL6", "description": "Authorization identification response" },
                { "bitNo": 39, "name": "responseCode", "dataType": "ANL2", "description": "Response code" },
                { "bitNo": 41, "name": "tid", "dataType": "ANSL8", "description": "Card acceptor terminal identification (TID)" },
                { "bitNo": 42, "name": "mid", "dataType": "ANSL15", "description": "Card acceptor identification code (MID)" },
                { "bitNo": 45, "name": "track1", "dataType": "ANS..R76", "description": "Track 1 data" },
                { "bitNo": 48, "name": "", "dataType": "ANS...L999", "description": "Additional data (private)" },
                { "bitNo": 49, "name": "currencyCode", "dataType": "NR3", "description": "Currency code, transaction" },
                { "bitNo": 52, "name": "pin", "dataType": "BL8", "padding": "FF", "description": "Personal identification number data (PIN Block)" },
                { "bitNo": 53, "name": "", "dataType": "NR16", "description": "Security related control information" },
                { "bitNo": 54, "name": "", "dataType": "ANS...L120", "description": "Additional amounts" },
                { "bitNo": 55, "name": "", "dataType": "B...L999", "description": "ICC data - EMV having multiple tags" },
                { "bitNo": 58, "name": "", "dataType": "ANS...L999", "description": "RESERVED" },
                { "bitNo": 59, "name": "", "dataType": "ANS...L999", "description": "RESERVED" },
                { "bitNo": 60, "name": "batch", "dataType": "ANS...L999", "description": "Batch", "fields": [ { "bitNo": 1, "name": "batchNo", "dataType": "NR6", "description": "Batch Number1" } ] },
                { "bitNo": 61, "name": "", "dataType": "B...L999", "description": "RESERVED" },
                { "bitNo": 62, "name": "", "dataType": "ANS...L999", "description": "RESERVED" },
                { "bitNo": 63, "name": "", "dataType": "ANS...L999", "description": "RESERVED" },
                { "bitNo": 64, "name": "", "dataType": "BL8", "description": "Message authentication code (MAC)" }
            ]
            """.trimIndent()
        )

        Assert.assertTrue(iso is IsoBase)
    }

    @Test
    fun Should_be_able_to_initialize_with_field_config_in_object_type() {
        val iso = IsoBase(
            arrayOf(
                FieldConfigJson(2, "pan", "N..R19", hidden = true, description = "Primary account number (PAN)"),
                FieldConfigJson(3, "procCode", "NR6", description = "Processing Code"),
                FieldConfigJson(4, "amount", "NR12", description = "Amount Transaction"),
                FieldConfigJson(11, "stan", "NR6", description = "System trace audit number (STAN)"),
                FieldConfigJson(12, "localTime", "NR6", description = "Local transaction time (hhmmss)"),
                FieldConfigJson(13, "localDate", "NR4", description = "Local transaction date (MMDD)"),
                FieldConfigJson(14, "expiryDate", "NR4", description = "Expiration date"),
                FieldConfigJson(22, "posEntryMode", "NR3", description = "Point of service entry mode"),
                FieldConfigJson(23, "panSeqNo", "NR3", description = "Application PAN sequence number"),
                FieldConfigJson(24, "nii", "NR3", description = "Network international identifier (NII)"),
                FieldConfigJson(25, "posConditionCode", "NR2", description = "Point of service condition code"),
                FieldConfigJson(26, "posCaptureCode", "NR2", description = "Point of service capture code"),
                FieldConfigJson(35, "track2", "Z..L37", description = "Track 2 data"),
                FieldConfigJson(37, "rrn", "ANL12", description = "Retrieval reference number"),
                FieldConfigJson(38, "approvalCode", "ANL6", description = "Authorization identification response"),
                FieldConfigJson(39, "responseCode", "ANL2", description = "Response code"),
                FieldConfigJson(41, "tid", "ANSL8", description = "Card acceptor terminal identification (TID)"),
                FieldConfigJson(42, "mid", "ANSL15", description = "Card acceptor identification code (MID)"),
                FieldConfigJson(45, "track1", "ANS..R76", description = "Track 1 data"),
                FieldConfigJson(48, "", "ANS...L999", description = "Additional data (private)"),
                FieldConfigJson(49, "currencyCode", "NR3", description = "Currency code, transaction"),
                FieldConfigJson(52, "pin", "BL8", padding = "FF", description = "Personal identification number data (PIN Block)"),
                FieldConfigJson(53, "", "NR16", description = "Security related control information"),
                FieldConfigJson(54, "", "ANS...L120", description = "Additional amounts"),
                FieldConfigJson(55, "", "B...L999", description = "ICC data - EMV having multiple tags"),
                FieldConfigJson(58, "", "ANS...L999", description = "RESERVED"),
                FieldConfigJson(59, "", "ANS...L999", description = "RESERVED"),
                FieldConfigJson(60, "batch", "ANS...L999", description = "Batch", fields = arrayOf(FieldConfigJson(1, "batchNo", "NR6", description = "Batch Number1"))),
                FieldConfigJson(61, "", "B...L999", description = "RESERVED"),
                FieldConfigJson(62, "", "ANS...L999", description = "RESERVED"),
                FieldConfigJson(63, "", "ANS...L999", description = "RESERVED"),
                FieldConfigJson(64, "", "BL8", description = "Message authentication code (MAC)")
            )
        )

        Assert.assertTrue(iso is IsoBase)
    }

    @Test
    fun Should_catch_error_when_config_is_empty() {
        try {
            IsoBase("")
            Assert.fail("Should not reach here")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_be_able_to_set_and_get_and_clear_field_by_bitNo() {
        val iso = IsoBase(arrayOf(FieldConfigJson(2, dataType = "NL8")))

        iso.set(2, "12345678")
        Assert.assertEquals(iso.get(2), "12345678")

        iso.set(2)
        Assert.assertEquals(iso.get(2), null)
    }

    @Test
    fun Should_be_able_to_set_and_get_and_clear_field_by_name() {
        val iso = IsoBase(arrayOf(FieldConfigJson(2, "hello", dataType = "NL8")))

        iso.set("hello", "12345678")
        Assert.assertEquals(iso.get("hello"), "12345678")

        iso.set("hello")
        Assert.assertEquals(iso.get("hello"), null)
    }

    @Test
    fun Should_be_able_to_set_and_get_and_clear_field_for_data_type_A_AN_ANS() {
        val iso = IsoBase(
            arrayOf(
                FieldConfigJson(2, dataType = "AL8"),
                FieldConfigJson(3, dataType = "ANL8"),
                FieldConfigJson(4, dataType = "ANSL8")
            )
        )

        for (bitNo in arrayOf(2, 3, 4)) {
            iso.set(bitNo, "12345678")
            Assert.assertEquals(iso.get(bitNo), "12345678")

            iso.set(bitNo)
            Assert.assertEquals(iso.get(bitNo), null)
        }
    }

    @Test
    fun Should_be_able_to_set_and_get_and_clear_field_for_data_type_N() {
        val iso = IsoBase(arrayOf(FieldConfigJson(2, dataType = "NL8")))

        iso.set(2, "12345678")
        Assert.assertEquals(iso.get(2), "12345678")

        iso.set(2)
        Assert.assertEquals(iso.get(2), null)
    }

    @Test
    fun Should_be_able_to_set_and_get_and_clear_field_for_data_type_B() {
        val iso = IsoBase(arrayOf(FieldConfigJson(2, dataType = "BL8", padding = "0")))

        iso.set(2, "1122334455667788".hexStringToByteArray())
        Assert.assertArrayEquals(iso.get(2) as ByteArray, "1122334455667788".hexStringToByteArray())

        iso.set(2)
        Assert.assertEquals(iso.get(2), null)
    }

    @Test
    fun Should_be_able_to_set_and_get_and_clear_nested_fields() {
        val iso = IsoBase(
            arrayOf(
                FieldConfigJson(2, "alice", dataType = "AL11"),
                FieldConfigJson(
                    3, "bob", "ANS...L999", fields = arrayOf(
                        FieldConfigJson(1, "hello", "AL5"),
                        FieldConfigJson(2, "world", "AL5"),
                        FieldConfigJson(
                            3, "others", "ANS...L999", fields = arrayOf(
                                FieldConfigJson(1, dataType = "ANSL10")
                            )
                        )
                    )
                )
            )
        )

        iso.set(2, "Hello Alice")
        Assert.assertEquals(iso.get("alice"), "Hello Alice")

        val map = mapOf("hello" to "Hello", "world" to "World", "others" to mapOf("1" to "Hello Bean"))
        iso.set(3, map)
        Assert.assertEquals(iso.get("bob"), map)
        Assert.assertEquals(iso.get("3.hello"), "Hello")
        Assert.assertEquals(iso.get("3.world"), "World")
        Assert.assertEquals(iso.get("3.others"), mapOf("1" to "Hello Bean"))

        Assert.assertArrayEquals(iso.bits.toTypedArray(), arrayOf(2, 3))

        iso.set(2)
        Assert.assertEquals(iso.get(2), null)

        iso.set("3.world")
        Assert.assertEquals(iso.get("bob"), mapOf("hello" to "Hello", "others" to mapOf("1" to "Hello Bean")))

        iso.set(3)
        Assert.assertEquals(iso.get(3), null)

        iso.clear()
        Assert.assertArrayEquals(iso.bits.toTypedArray(), arrayOf())
    }

    @Test
    fun Should_catch_error_when_set_unknown_nested_field() {
        val iso = IsoBase(
            arrayOf(FieldConfigJson(2, dataType = "ANS...L999", fields = arrayOf(FieldConfigJson(1, "hello", "AL5"))))
        )

        try {
            iso.set(2, mapOf("world" to "Hello"))
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_catch_error_when_set_nested_field_as_non_object() {
        val iso = IsoBase(
            arrayOf(FieldConfigJson(2, dataType = "ANS...L999", fields = arrayOf(FieldConfigJson(1, "hello", "AL5"))))
        )

        try {
            iso.set(2, "Hello")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_be_able_to_set_and_clear_and_but_not_able_to_get_hidden_field() {
        val iso = IsoBase(arrayOf(FieldConfigJson(2, dataType = "BL8", padding = "0", hidden = true)))

        iso.set(2, "1122334455667788".hexStringToByteArray())
        Assert.assertEquals(iso.get(2), null)
        Assert.assertEquals(iso.bits.find { it -> 2 === it }, 2)

        iso.set(2, null)
        Assert.assertEquals(iso.get(2), null)
        Assert.assertEquals(iso.bits.find { it -> 2 === it }, null)
    }

    @Test
    fun Should_be_able_to_clear_all_field() {
        val iso = IsoBase(arrayOf(FieldConfigJson(3, dataType = "NR6")))

        iso.set(3, "123456")
        Assert.assertTrue(null !== iso.get(3))

        iso.clear()
        Assert.assertEquals(iso.get(3), null)
    }

    @Test
    fun Should_catch_error_when_get_field_with_bitNo_is_out_of_range() {
        val iso = IsoBase(arrayOf(FieldConfigJson(3, dataType = "NR6")))

        try {
            iso.get(0)
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_catch_error_when_get_nested_field_with_bitNo_is_out_of_range() {
        val iso = IsoBase(arrayOf(FieldConfigJson(3, dataType = "NR6")))

        try {
            iso.get("3.hello")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_catch_error_when_set_invalid_data_type_for_A_AN_ANS_Z() {
        val iso = IsoBase(
            arrayOf(
                FieldConfigJson(2, dataType = "AL8"),
                FieldConfigJson(3, dataType = "ANL8"),
                FieldConfigJson(4, dataType = "ANSL8"),
                FieldConfigJson(5, dataType = "ZL8")
            )
        )

        for (bitNo in arrayOf(2, 3, 4, 5)) {
            try {
                iso.set(bitNo, "12345678".hexStringToByteArray())
            } catch (error: Exception) {
                Assert.assertTrue(error is IllegalArgumentException)
            }
        }
    }

    @Test
    fun Should_catch_error_when_set_invalid_data_type_for_N() {
        val iso = IsoBase(arrayOf(FieldConfigJson(2, dataType = "NL8")))

        try {
            iso.set(2, "1234".hexStringToByteArray())
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_catch_error_when_set_invalid_data_type_for_B() {
        val iso = IsoBase(arrayOf(FieldConfigJson(2, dataType = "BL8", padding = "0")))

        try {
            iso.set(2, "12345678".hexStringToByteArray())
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_be_able_to_get_field_config_by_bitNo() {
        val iso = IsoBase(arrayOf(FieldConfigJson(3, dataType = "NR6")))
        val config = iso.getConfig(3)

        Assert.assertEquals(config.bitNo, 3)
        Assert.assertEquals(config.name, "3")
        Assert.assertEquals(config.dataType, DataType.N)
        Assert.assertEquals(config.lengthType, LengthType.FIX)
        Assert.assertEquals(config.alignment, Alignment.RIGHT)
        Assert.assertEquals(config.maxLength, 6)
        Assert.assertArrayEquals(config.padding, "00".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_get_field_config_by_name() {
        val iso = IsoBase(arrayOf(FieldConfigJson(3, "hello", dataType = "NR6")))
        val config = iso.getConfig("hello")

        Assert.assertEquals(config.bitNo, 3)
        Assert.assertEquals(config.name, "hello")
        Assert.assertEquals(config.dataType, DataType.N)
        Assert.assertEquals(config.lengthType, LengthType.FIX)
        Assert.assertEquals(config.alignment, Alignment.RIGHT)
        Assert.assertEquals(config.maxLength, 6)
        Assert.assertArrayEquals(config.padding, "00".hexStringToByteArray())
    }

    @Test
    fun Should_catch_error_when_get_field_config_with_bitNo_is_out_of_range() {
        val iso = IsoBase(arrayOf(FieldConfigJson(3, dataType = "NR6")))

        try {
            iso.getConfig(0)
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_be_able_to_serialize_message() {
        val iso = IsoBase(
            arrayOf(
                FieldConfigJson(2, "pan", "N..R19"),
                FieldConfigJson(3, "procCode", "NR6"),
                FieldConfigJson(11, "stan", "NR6"),
                FieldConfigJson(24, "nii", "NR3"),
                FieldConfigJson(35, "track2", "Z..L37", hidden = true),
                FieldConfigJson(41, "tid", "ANSL8"),
                FieldConfigJson(42, "mid", "ANSL15"),
                FieldConfigJson(52, "pin", "BL8"),
                FieldConfigJson(60, "batch", "ANS...L999", fields = arrayOf(FieldConfigJson(1, "batchNo", "NR6"))),
                FieldConfigJson(63, "", "ANS...L999"),
                FieldConfigJson(64, "", "BL8")
            )
        )

        iso.set(2, "54444333322221111")
        iso.set(3, "930000")
        iso.set(11, "000001")
        iso.set(24, "555")
        iso.set(35, "4570660700090007=01081011478444800000")
        iso.set(41, "00000001")
        iso.set(42, "330000000000001")
        iso.set(52, "6da6dfccaecdb0ca".hexStringToByteArray())
        iso.set("60.batchNo", "012345")
        iso.set(63, "3:PP00.01MSANDES:PP00.01MSANDES")
        iso.set(64, "1234567890abcdef".hexStringToByteArray())

        val buffer = iso.pack()
        val crossCheckBuffer = "6020010020c01013170544443333222211119300000000010555374570660700090007d01081011478444800000030303030303030313333303030303030303030303030316da6dfccaecdb0ca00030123450031333a505030302e30314d53414e4445533a505030302e30314d53414e4445531234567890abcdef".hexStringToByteArray()
        Assert.assertArrayEquals(buffer, crossCheckBuffer)

        iso.unpack(buffer)

        Assert.assertArrayEquals(iso.bits.toTypedArray(), arrayOf(2, 3, 11, 24, 35, 41, 42, 52, 60, 63, 64))
        Assert.assertEquals(iso.get(2), "54444333322221111")
        Assert.assertEquals(iso.get(3), "930000")
        Assert.assertEquals(iso.get(11), "000001")
        Assert.assertEquals(iso.get(24), "555")
        Assert.assertEquals(iso.get(35), null)
        Assert.assertEquals(iso.get(41), "00000001")
        Assert.assertEquals(iso.get(42), "330000000000001")
        Assert.assertArrayEquals(iso.get(52) as ByteArray, "6da6dfccaecdb0ca".hexStringToByteArray())
        Assert.assertEquals(iso.get(60), mapOf("batchNo" to "012345"))
        Assert.assertEquals(iso.get("60.batchNo"), "012345")
        Assert.assertEquals(iso.get(63), "3:PP00.01MSANDES:PP00.01MSANDES")
        Assert.assertArrayEquals(iso.get(64) as ByteArray, "1234567890abcdef".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_serialize_message_with_EBCDIC_data_format() {
        val iso = IsoBase(
            arrayOf(
                FieldConfigJson(2, "pan", "N..R19"),
                FieldConfigJson(41, "tid", "ANSL8", dataFormat = DataFormat.EBCDIC),
                FieldConfigJson(64, "", "BL8")
            )
        )

        iso.set(2, "54444333322221111")
        iso.set(41, "12345678")
        iso.set(64, "1234567890abcdef".hexStringToByteArray())

        val buffer = iso.pack()

        val crossCheckBuffer = "400000000080000117054444333322221111f1f2f3f4f5f6f7f81234567890abcdef".hexStringToByteArray()
        Assert.assertArrayEquals(buffer, crossCheckBuffer)

        iso.unpack(buffer)

        Assert.assertArrayEquals(iso.bits.toTypedArray(), arrayOf(2, 41, 64))
        Assert.assertEquals(iso.get(2), "54444333322221111")
        Assert.assertEquals(iso.get(41), "12345678")
        Assert.assertArrayEquals(iso.get(64) as ByteArray, "1234567890abcdef".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_serialize_message_with_Z_data_type() {
        val iso = IsoBase(
            arrayOf(
                FieldConfigJson(2, "pan", "N..R19"),
                FieldConfigJson(35, "track2", "Z..L37"),
                FieldConfigJson(62, "", "ANS...L999")
            )
        )

        iso.set(2, "4444333322221118")
        iso.set(35, "4570660700090007=01081011478444800000")
        iso.set(62, "000010")

        val buffer = iso.pack()

        val crossCheckBuffer = "4000000020000004164444333322221118374570660700090007d0108101147844480000000006303030303130".hexStringToByteArray()
        Assert.assertArrayEquals(buffer, crossCheckBuffer)

        iso.unpack(buffer)

        Assert.assertArrayEquals(iso.bits.toTypedArray(), arrayOf(2, 35, 62))
        Assert.assertEquals(iso.get(2), "4444333322221118")
        Assert.assertEquals(iso.get(35), "4570660700090007=01081011478444800000")
        Assert.assertEquals(iso.get(62), "000010")
    }

    @Test
    fun Should_catch_error_when_set_data_to_unconfigured_field() {
        val iso = IsoBase(arrayOf(FieldConfigJson(3, dataType = "NR6")))

        try {
            iso.set(2, "4444333322221118")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_catch_error_when_serialize_message_with_unconfigured_field() {
        val iso = IsoBase(arrayOf(FieldConfigJson(3, dataType = "NR6")))

        try {
            iso.unpack("4000000000000000164444333322221118".hexStringToByteArray())
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_catch_error_when_serialize_message_with_uninitialized_field() {
        val iso = IsoBase(
            arrayOf(
                FieldConfigJson(
                    60, dataType = "ANS...L999", fields = arrayOf(
                        FieldConfigJson(1, dataType = "NR6"), FieldConfigJson(2, dataType = "NR6")
                    )
                )
            )
        )

        iso.set("60.1", "012345")

        try {
            iso.pack()
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun Should_be_able_to_serialize_sale_request_message() {
        val iso = IsoBase(
            arrayOf(
                FieldConfigJson(3, "procCode", "NR6"),
                FieldConfigJson(4, "amount", "NR12"),
                FieldConfigJson(11, "stan", "NR6"),
                FieldConfigJson(22, "posEntryMode", "NR3"),
                FieldConfigJson(23, "panSeqNo", "NR3"),
                FieldConfigJson(24, "nii", "NR3"),
                FieldConfigJson(25, "posConditionCode", "NR2"),
                FieldConfigJson(35, "track2", "Z..L37"),
                FieldConfigJson(41, "tid", "ANSL8", dataFormat = DataFormat.EBCDIC),
                FieldConfigJson(42, "mid", "ANSL15", dataFormat = DataFormat.EBCDIC),
                FieldConfigJson(55, "icc", "B...L999"),
                FieldConfigJson(62, "", "ANS...L999", fields = arrayOf(FieldConfigJson(1, "invoiceNo", "NR6", dataFormat = DataFormat.EBCDIC)))
            )
        )

        iso.set(3, "000000")
        iso.set(4, "000000000011")
        iso.set(11, "000155")
        iso.set(22, "071")
        iso.set(23, "000")
        iso.set(24, "003")
        iso.set(25, "00")
        iso.set(35, "4693080253531293027052261631218900000")
        iso.set(41, "42140222")
        iso.set(42, "000042601400222")
        iso.set(55, "820220008407A0000000031010950500000000009A032301279C01005F2A0204585F3401009F02060000000000119F03060000000000009F0607A00000000310109F0902008C9F100706011203A000009F1A0204589F1E0838353130494343009F2608C26C9C8C797CB21A9F2701809F3303E0F8C89F3501229F360200AA9F3704727CD7489F410400000054".hexStringToByteArray())
        iso.set("62.invoiceNo", "000052")

        val buffer = iso.pack()

//        println(buffer.toHex())

        val crossCheckBuffer = "3020078020c00204000000000000000011000155007100000003003746930802535312930270522616312189000000f4f2f1f4f0f2f2f2f0f0f0f0f4f2f6f0f1f4f0f0f2f2f20140820220008407a0000000031010950500000000009a032301279c01005f2a0204585f3401009f02060000000000119f03060000000000009f0607a00000000310109f0902008c9f100706011203a000009f1a0204589f1e0838353130494343009f2608c26c9c8c797cb21a9f2701809f3303e0f8c89f3501229f360200aa9f3704727cd7489f4104000000540006f0f0f0f0f5f2".hexStringToByteArray()
        Assert.assertArrayEquals(buffer, crossCheckBuffer)

        iso.unpack(buffer)

        Assert.assertArrayEquals(iso.bits.toTypedArray(), arrayOf(3, 4, 11, 22, 23, 24, 25, 35, 41, 42, 55, 62))
        Assert.assertEquals(iso.get(3), "000000")
        Assert.assertEquals(iso.get(4), "000000000011")
        Assert.assertEquals(iso.get(11), "000155")
        Assert.assertEquals(iso.get(22), "071")
        Assert.assertEquals(iso.get(23), "000")
        Assert.assertEquals(iso.get(24), "003")
        Assert.assertEquals(iso.get(35), "4693080253531293027052261631218900000")
        Assert.assertEquals(iso.get(41), "42140222")
        Assert.assertEquals(iso.get(42), "000042601400222")
        Assert.assertArrayEquals(iso.get(55) as ByteArray, "820220008407A0000000031010950500000000009A032301279C01005F2A0204585F3401009F02060000000000119F03060000000000009F0607A00000000310109F0902008C9F100706011203A000009F1A0204589F1E0838353130494343009F2608C26C9C8C797CB21A9F2701809F3303E0F8C89F3501229F360200AA9F3704727CD7489F410400000054".hexStringToByteArray())
        Assert.assertEquals(iso.get("62.invoiceNo"), "000052")
    }

    @Test
    fun Should_be_able_to_serialize_sale_response_message() {
        val iso = IsoBase(
            arrayOf(
                FieldConfigJson(3, "procCode", "NR6"),
                FieldConfigJson(4, "amount", "NR12"),
                FieldConfigJson(11, "stan", "NR6"),
                FieldConfigJson(12, "localTime", "NR6"),
                FieldConfigJson(13, "localDate", "NR4"),
                FieldConfigJson(24, "nii", "NR3"),
                FieldConfigJson(37, "rrn", "ANL12", dataFormat = DataFormat.EBCDIC),
                FieldConfigJson(38, "approvalCode", "ANL6", dataFormat = DataFormat.EBCDIC),
                FieldConfigJson(39, "responseCode", "ANL2", dataFormat = DataFormat.EBCDIC),
                FieldConfigJson(41, "tid", "ANSL8", dataFormat = DataFormat.EBCDIC),
                FieldConfigJson(55, "icc", "B...L999")
            )
        )

        iso.set(3, "000000")
        iso.set(4, "000000000011")
        iso.set(11, "000155")
        iso.set(12, "161014")
        iso.set(13, "0127")
        iso.set(24, "003")
        iso.set(37, "001851000155")
        iso.set(38, "108404")
        iso.set(39, "00")
        iso.set(41, "42140222")
        iso.set(55, "911022860206808800000000000000000000".hexStringToByteArray())

        val buffer = iso.pack()

//        println(buffer.toHex())

        val crossCheckBuffer = "303801000e80020000000000000000001100015516101401270003f0f0f1f8f5f1f0f0f0f1f5f5f1f0f8f4f0f4f0f0f4f2f1f4f0f2f2f20018911022860206808800000000000000000000".hexStringToByteArray()
        Assert.assertArrayEquals(buffer, crossCheckBuffer)

        iso.unpack(buffer)

        Assert.assertArrayEquals(iso.bits.toTypedArray(), arrayOf(3, 4, 11, 12, 13, 24, 37, 38, 39, 41, 55))
        Assert.assertEquals(iso.get(3), "000000")
        Assert.assertEquals(iso.get(4), "000000000011")
        Assert.assertEquals(iso.get(11), "000155")
        Assert.assertEquals(iso.get(12), "161014")
        Assert.assertEquals(iso.get(13), "0127")
        Assert.assertEquals(iso.get(24), "003")
        Assert.assertEquals(iso.get(37), "001851000155")
        Assert.assertEquals(iso.get(38), "108404")
        Assert.assertEquals(iso.get(39), "00")
        Assert.assertEquals(iso.get(41), "42140222")
        Assert.assertArrayEquals(iso.get(55) as ByteArray, "911022860206808800000000000000000000".hexStringToByteArray())
    }

    @Test
    fun Should_be_able_to_serialize_settlement_request_message() {
        val iso = IsoBase(
            arrayOf(
                FieldConfigJson(3, "procCode", "NR6"),
                FieldConfigJson(11, "stan", "NR6"),
                FieldConfigJson(24, "nii", "NR3"),
                FieldConfigJson(41, "tid", "ANSL8", dataFormat = DataFormat.EBCDIC),
                FieldConfigJson(42, "mid", "ANSL15", dataFormat = DataFormat.EBCDIC),
                FieldConfigJson(
                    60, "batch", "ANS...L999", fields = arrayOf(
                        FieldConfigJson(1, "batchNo", "NR6", dataFormat = DataFormat.EBCDIC)
                    )
                ),
                FieldConfigJson(
                    63, "summary", "ANS...L999", fields = arrayOf(
                        FieldConfigJson(1, "creditCardSaleCount", "NR3", dataFormat = DataFormat.EBCDIC),
                        FieldConfigJson(2, "creditCardSaleAmount", "NR12", dataFormat = DataFormat.EBCDIC),
                        FieldConfigJson(3, "creditCardRefundCount", "NR3", dataFormat = DataFormat.EBCDIC),
                        FieldConfigJson(4, "creditCardRefundAmount", "NR12", dataFormat = DataFormat.EBCDIC),

                        FieldConfigJson(5, "debitCardSaleCount", "NR3", dataFormat = DataFormat.EBCDIC),
                        FieldConfigJson(6, "debitCardSaleAmount", "NR12", dataFormat = DataFormat.EBCDIC),
                        FieldConfigJson(7, "debitCardRefundCount", "NR3", dataFormat = DataFormat.EBCDIC),
                        FieldConfigJson(8, "debitCardRefundAmount", "NR12", dataFormat = DataFormat.EBCDIC),

                        FieldConfigJson(9, "authCardSaleCount", "NR3", dataFormat = DataFormat.EBCDIC),
                        FieldConfigJson(10, "authCardSaleAmount", "NR12", dataFormat = DataFormat.EBCDIC),
                        FieldConfigJson(11, "authCardRefundCount", "NR3", dataFormat = DataFormat.EBCDIC),
                        FieldConfigJson(12, "authCardRefundAmount", "NR12", dataFormat = DataFormat.EBCDIC)
                    )
                )
            )
        )

        iso.set(3, "920000")
        iso.set(11, "000160")
        iso.set(24, "003")
        iso.set(41, "42140222")
        iso.set(42, "000042601400222")

        iso.set("60.batchNo", "000012")

        iso.set("63.creditCardSaleCount", "000")
        iso.set("63.creditCardSaleAmount", "000000000000")
        iso.set("63.creditCardRefundCount", "000")
        iso.set("63.creditCardRefundAmount", "000000000000")

        iso.set("63.debitCardSaleCount", "000")
        iso.set("63.debitCardSaleAmount", "000000000000")
        iso.set("63.debitCardRefundCount", "000")
        iso.set("63.debitCardRefundAmount", "000000000000")

        iso.set("63.authCardSaleCount", "000")
        iso.set("63.authCardSaleAmount", "000000000000")
        iso.set("63.authCardRefundCount", "000")
        iso.set("63.authCardRefundAmount", "000000000000")

        val buffer = iso.pack()

//        println(buffer.toHex())

        val crossCheckBuffer = "2020010000c000129200000001600003f4f2f1f4f0f2f2f2f0f0f0f0f4f2f6f0f1f4f0f0f2f2f20006f0f0f0f0f1f20090f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f0".hexStringToByteArray()
        Assert.assertArrayEquals(buffer, crossCheckBuffer)

        iso.unpack(buffer)

        Assert.assertArrayEquals(iso.bits.toTypedArray(), arrayOf(3, 11, 24, 41, 42, 60, 63))
        Assert.assertEquals(iso.get(3), "920000")
        Assert.assertEquals(iso.get(11), "000160")
        Assert.assertEquals(iso.get(24), "003")
        Assert.assertEquals(iso.get(41), "42140222")
        Assert.assertEquals(iso.get(42), "000042601400222")

        Assert.assertEquals(iso.get("60.batchNo"), "000012")

        Assert.assertEquals(iso.get("63.creditCardSaleCount"), "000")
        Assert.assertEquals(iso.get("63.creditCardSaleAmount"), "000000000000")
        Assert.assertEquals(iso.get("63.creditCardRefundCount"), "000")
        Assert.assertEquals(iso.get("63.creditCardRefundAmount"), "000000000000")

        Assert.assertEquals(iso.get("63.debitCardSaleCount"), "000")
        Assert.assertEquals(iso.get("63.debitCardSaleAmount"), "000000000000")
        Assert.assertEquals(iso.get("63.debitCardRefundCount"), "000")
        Assert.assertEquals(iso.get("63.debitCardRefundAmount"), "000000000000")

        Assert.assertEquals(iso.get("63.authCardSaleCount"), "000")
        Assert.assertEquals(iso.get("63.authCardSaleAmount"), "000000000000")
        Assert.assertEquals(iso.get("63.authCardRefundCount"), "000")
        Assert.assertEquals(iso.get("63.authCardRefundAmount"), "000000000000")

    }
}
