package io.github.voytech.tabulate.test.assertions

import io.github.voytech.tabulate.template.operations.CellValue
import io.github.voytech.tabulate.test.ValueTest
import java.util.zip.CRC32
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

class AssertCellValue(
    private val expectedValue: Any,
    private val expectedColspan: Int? = null,
    private val expectedRowspan: Int? = null
) : ValueTest {
    override fun performTest(value: CellValue?) {
        assertNotNull(value, "Expected cell value to be present")
        assertValueEquals(expectedValue, value.value, "Expected cell value to be $expectedValue")
        expectedColspan?.let {
            assertNotNull(value.colSpan, "Expected cell collSpan to be present")
            assertEquals(expectedColspan, value.colSpan, "Expected cell colSpan to be $it")
        }
        expectedRowspan?.let {
            assertNotNull(value.rowSpan, "Expected cell rowSpan to be present")
            assertEquals(expectedRowspan, value.rowSpan, "Expected cell rowSpan to be $it")
        }
    }

    private fun assertValueEquals(expected: Any?, found: Any?, description: String) {
        if (expected?.javaClass == found?.javaClass) {
            if (expected is ByteArray && found is ByteArray) {
                val crc = CRC32()
                assertEquals(checkSum(crc, expected), checkSum(crc, found), description)
            } else {
                assertEquals(expected, found, description)
            }
        } else fail("Incompatible cell value types!")
    }

    private fun checkSum(crc: CRC32, bytes: ByteArray): Long {
        crc.reset()
        crc.update(bytes)
        return crc.value.also { crc.reset() }
    }

}