package io.github.voytech.tabulate.test.assertions

import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.test.AttributeTest
import java.util.zip.CRC32
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class AssertCellValue(
    private val expectedValue: Any,
    private val expectedColspan: Int? = null,
    private val expectedRowspan: Int? = null
) : AttributeTest<CellAttribute<*>> {
    override fun performTest(sheetName: String, def: Set<CellAttribute<*>>?) {
        /*assertNotNull(def?.cellValue, "Expected cell value to be present")
        assertValueEquals(expectedValue, def?.cellValue?.value, "Expected cell value to be $expectedValue")
        expectedColspan?.let {
            assertNotNull(def?.cellValue?.colSpan, "Expected cell collSpan to be present")
            assertEquals(expectedColspan, def?.cellValue?.colSpan, "Expected cell colSpan to be $it")
        }
        expectedRowspan?.let {
            assertNotNull(def?.cellValue?.rowSpan, "Expected cell rowSpan to be present")
            assertEquals(expectedRowspan, def?.cellValue?.rowSpan, "Expected cell rowSpan to be $it")
        }*/
        TODO("Not yet implemented")
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


interface AssertCellAttribute {
    fun testCellAttribute(cellAttribute: CellAttribute<*>)
    fun attributeClass(): KClass<out CellAttribute<*>>
}

class CellAttributesAssertions(private vararg val cellAttributesTests: AssertCellAttribute) : AttributeTest<CellAttribute<*>> {
    override fun performTest(sheetName: String, def: Set<CellAttribute<*>>?) {
        val cellExtensionByClass = def?.groupBy { it::class }
        cellAttributesTests.forEach {
            if (cellExtensionByClass?.containsKey(it.attributeClass()) == false) {
                fail("cell extension for class ${it.attributeClass().simpleName} not found for cell at $sheetName")
            }
            cellExtensionByClass?.get(it.attributeClass())?.forEach { extension ->
                it.testCellAttribute(extension)
            }
        }
    }

}

class AssertContainsCellAttributes(private vararg val targets: CellAttribute<*>) : AttributeTest<CellAttribute<*>> {
    override fun performTest(sheetName: String, def: Set<CellAttribute<*>>?) {
        val existingAttributes = def?.asIterable() ?: emptyList()
        assertEquals(
            targets.toSet(),
            targets.toSet().intersect(existingAttributes),
            "expected cell attribute set to contain all target extension instances"
        )
    }
}

class AssertMany(private vararg val cellTests: AttributeTest<CellAttribute<*>>) : AttributeTest<CellAttribute<*>> {
    override fun performTest(sheetName: String, def: Set<CellAttribute<*>>?) {
        cellTests.forEach { it.performTest(sheetName, def) }
    }
}

class AssertEqualsAttribute(
    private val expectedAttribute: CellAttribute<*>,
    private val onlyProperties: Set<KProperty1<out CellAttribute<*>, Any?>>? = null
) : AttributeTest<CellAttribute<*>> {

    @Suppress("UNCHECKED_CAST")
    private fun <A: CellAttribute<*>> A.matchProperties(expected: A) =
        onlyProperties!!.map { it as KProperty1<A, Any?>  }.all { it(this) ==  it(expected)}

    override fun performTest(sheetName: String, def: Set<CellAttribute<*>>?) {
        if (onlyProperties?.isNotEmpty() == true) {
            assertTrue(
                def?.any { it.matchProperties(expectedAttribute) } ?: false,
                "CellAttribute $expectedAttribute with matching properties ${onlyProperties.joinToString(",") { it.name }} not found"
            )
        } else {
            assertTrue(
                def?.contains(expectedAttribute) ?: false,
                "CellAttribute $expectedAttribute not found"
            )
        }
    }
}

class AssertNoAttribute(private val expectedAttribute: CellAttribute<*>) : AttributeTest<CellAttribute<*>> {

    override fun performTest(sheetName: String, def: Set<CellAttribute<*>>?) {
        assertTrue(
            def?.contains(expectedAttribute) == false,
            "CellAttribute $expectedAttribute found but not expected!"
        )
    }
}



class AssertAttributeExpression(
    private val attributeClass: KClass<out CellAttribute<*>>,
    private val invoke: (CellAttribute<*>) -> Unit
) :
    AssertCellAttribute {

    override fun testCellAttribute(cellAttribute: CellAttribute<*>) {
        invoke.invoke(cellAttribute)
    }

    override fun attributeClass(): KClass<out CellAttribute<*>> = attributeClass
}
