import org.junit.Assert
import org.junit.Test
import my.paidchain.spectraterminaldemo.common.iso8583.FieldConfigSet

@Suppress("FunctionName")
class FieldConfigSetTest {
    @Test
    fun Should_be_able_to_initialize() {
        val instance = FieldConfigSet(
            """
            [
                { "bitNo": 2, "dataType": "N..R19", "hidden": true, "description": "Primary account number (PAN)" },
                { "bitNo": 3, "dataType": "NR6", "description": "Processing Code" },
                { "bitNo": 4, "dataType": "NR12", "description": "Amount Transaction" },
                { "bitNo": 11, "dataType": "NR6", "description": "System trace audit number (STAN)" },
                { "bitNo": 12, "dataType": "NR6", "description": "Local transaction time (hhmmss)" },
                { "bitNo": 13, "dataType": "NR4", "description": "Local transaction date (MMDD)" },
                { "bitNo": 14, "dataType": "NR4", "description": "Expiration date" },
                { "bitNo": 22, "dataType": "NR3", "description": "Point of service entry mode" },
                { "bitNo": 23, "dataType": "NR3", "description": "Application PAN sequence number" },
                { "bitNo": 24, "dataType": "NR3", "description": "Network international identifier (NII)" },
                { "bitNo": 25, "dataType": "NR2", "description": "Point of service condition code" },
                { "bitNo": 26, "dataType": "NR2", "description": "Point of service capture code" },
                { "bitNo": 35, "dataType": "Z..R37", "description": "Track 2 data" },
                { "bitNo": 37, "dataType": "ANL12", "description": "Retrieval reference number" },
                { "bitNo": 38, "dataType": "ANL6", "description": "Authorization identification response" },
                { "bitNo": 39, "dataType": "ANL2", "description": "Response code" },
                { "bitNo": 41, "dataType": "ANSL8", "description": "Card acceptor terminal identification (TID)" },
                { "bitNo": 42, "dataType": "ANSL15", "description": "Card acceptor identification code (MID)" },
                { "bitNo": 45, "dataType": "ANS..R76", "description": "Track 1 data" },
                { "bitNo": 48, "dataType": "ANS...L999", "description": "Additional data (private)" },
                { "bitNo": 49, "dataType": "NR3", "description": "Currency code, transaction" },
                { "bitNo": 52, "dataType": "BL8", "description": "Personal identification number data (PIN Block)" },
                { "bitNo": 53, "dataType": "NR16", "description": "Security related control information" },
                { "bitNo": 54, "dataType": "ANS...L120", "description": "Additional amounts" },
                { "bitNo": 55, "dataType": "B...L999", "description": "ICC data - EMV having multiple tags" },
                { "bitNo": 58, "dataType": "ANS...L999", "description": "RESERVED" },
                { "bitNo": 59, "dataType": "A...L999", "description": "RESERVED" },
                { "bitNo": 60, "dataType": "B...L999", "description": "Batch number" },
                { "bitNo": 61, "dataType": "B...L999", "description": "RESERVED" },
                { "bitNo": 62, "dataType": "ANS...L999", "description": "RESERVED" },
                { "bitNo": 63, "dataType": "ANS...L999", "description": "RESERVED" },
                { "bitNo": 64, "dataType": "BL8", "description": "Message authentication code (MAC)" }
            ]
            """.trimIndent()
        )

        Assert.assertTrue(instance is FieldConfigSet)
        Assert.assertArrayEquals(
            instance.configSet.map { it.bitNo }.toTypedArray(), arrayOf(
                2, 3, 4, 11, 12, 13, 14, 22, 23, 24, 25, 26, 35, 37, 38, 39, 41, 42, 45, 48, 49, 52, 53, 54, 55, 58, 59, 60, 61, 62, 63, 64
            )
        )
        Assert.assertArrayEquals(
            instance.configSet.map { it.name }.toTypedArray(), arrayOf(
                "2", "3", "4", "11", "12", "13", "14", "22", "23", "24", "25", "26", "35", "37", "38", "39", "41", "42", "45", "48", "49",
                "52", "53", "54", "55", "58", "59", "60", "61", "62", "63", "64"
            )
        )
    }

    @Test
    fun Should_be_able_to_initialize_field_config_in_object_type_with_sorted_order() {
        val instance = FieldConfigSet(
            """
            [
              { "bitNo": 37, "name": "FIELD_37", "dataType": "AL12" },
              { "bitNo": 38, "dataType": "ANL6" },
              { "bitNo": 41, "name": "FIELD_41", "dataType": "ANSL8" },
              { "bitNo": 2, "name": "FIELD_TWO", "dataType": "N..R19" },
              { "bitNo": 52, "name": "52", "dataType": "BL8" },
              { "bitNo": 35, "name": "F35", "dataType": "Z..R37", "padding": "0" }
            ]
            """.trimIndent()
        )

//        instance.configSet.map { println(it) }

        Assert.assertTrue(instance is FieldConfigSet)
        Assert.assertArrayEquals(
            instance.configSet.map { it.bitNo }.toTypedArray(),
            arrayOf(2, 35, 37, 38, 41, 52)
        )
        Assert.assertArrayEquals(
            instance.configSet.map { it.name }.toTypedArray(), arrayOf(
                "FIELD_TWO",
                "F35",
                "FIELD_37",
                "38",
                "FIELD_41",
                "52"
            )
        )
    }

    @Test
    fun Should_be_able_to_initialize_empty_field_config() {
        val instance = FieldConfigSet("")

        Assert.assertTrue(instance is FieldConfigSet)
    }

    @Test
    fun Should_be_able_to_initialize_empty_field_config_with_nested_fields() {
        val instance = FieldConfigSet(
            """
            [
              { "bitNo": 37, "name": "FIELD_37", "dataType": "AL12" },
              {
                "bitNo": 60,
                "dataType": "ANS...L999",
                "description": "Data",
                "fields": [
                  { "bitNo": 1, "name": "hello", "dataType": "NR6" },
                  { "bitNo": 2, "name": "world", "dataType": "NR6" }
                ]
              }
            ]
            """.trimIndent()
        )

//        instance.configSet.map { println(it) }

        Assert.assertTrue(instance is FieldConfigSet)
    }

    @Test
    fun Should_be_able_to_initialize_empty_field_config_in_object_type() {
        val instance = FieldConfigSet(null)

        Assert.assertTrue(instance is FieldConfigSet)
    }

    @Test
    fun Should_catch_error_for_duplicated_config() {
        try {
            FieldConfigSet(
                """
                [
                  { "bitNo": 35, "dataType": "Z..R37" },
                  { "bitNo": 35, "dataType": "Z..R37" }
                ]
                """.trimIndent()
            )
            Assert.fail("Should not reach here")
        } catch (error: Exception) {
            Assert.assertTrue(error is IllegalArgumentException)
        }
    }
}