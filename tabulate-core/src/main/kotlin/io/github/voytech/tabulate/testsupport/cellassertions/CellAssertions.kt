package io.github.voytech.tabulate.testsupport.cellassertions

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.template.operations.CellValue
import io.github.voytech.tabulate.template.operations.Coordinates
import io.github.voytech.tabulate.testsupport.CellDefinition
import io.github.voytech.tabulate.testsupport.CellTest
import java.util.zip.CRC32
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class AssertCellValue<E>(
    private val expectedValue: Any,
    private val expectedColspan: Int? = null,
    private val expectedRowspan: Int? = null
) : CellTest<E> {
    override fun performCellTest(api: E, coordinates: Coordinates, def: CellDefinition?) {
        assertNotNull(def?.cellValue, "Expected cell value to be present")
        assertValueEquals(expectedValue, def?.cellValue?.value, "Expected cell value to be $expectedValue")
        expectedColspan?.let {
            assertNotNull(def?.cellValue?.colSpan, "Expected cell collSpan to be present")
            assertEquals(expectedColspan, def?.cellValue?.colSpan, "Expected cell colSpan to be $it")
        }
        expectedRowspan?.let {
            assertNotNull(def?.cellValue?.rowSpan, "Expected cell rowSpan to be present")
            assertEquals(expectedRowspan, def?.cellValue?.rowSpan, "Expected cell rowSpan to be $it")
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

class AssertCellValueExpr<E>(private val invoke: (CellValue) -> Unit) : CellTest<E> {
    override fun performCellTest(api: E, coordinates: Coordinates, def: CellDefinition?) {
        assertNotNull(def?.cellValue, "Expected cell value to be present")
        def?.cellValue?.let { invoke.invoke(it) }
    }
}

interface AssertCellAttribute {
    fun testCellAttribute(cellAttribute: CellAttribute)
    fun attributeClass(): KClass<out CellAttribute>
}

class CellAttributesAssertions<E>(private vararg val cellAttributesTests: AssertCellAttribute) : CellTest<E> {
    override fun performCellTest(api: E, coordinates: Coordinates, def: CellDefinition?) {
        val cellExtensionByClass = def?.cellAttributes?.groupBy { it::class }
        cellAttributesTests.forEach {
            if (cellExtensionByClass?.containsKey(it.attributeClass()) == false) {
                fail("cell extension for class ${it.attributeClass().simpleName} not found for cell at $coordinates")
            }
            cellExtensionByClass?.get(it.attributeClass())?.forEach { extension ->
                it.testCellAttribute(extension)
            }
        }
    }
}

class AssertContainsCellAttributes<E>(private vararg val targets: CellAttribute) : CellTest<E> {
    override fun performCellTest(api: E, coordinates: Coordinates, def: CellDefinition?) {
        val existingAttributes = def?.cellAttributes?.asIterable() ?: emptyList()
        assertEquals(
            targets.toSet(),
            targets.toSet().intersect(existingAttributes),
            "expected cell attribute set to contain all target extension instances"
        )
    }
}

class AssertMany<E>(private vararg val cellTests: CellTest<E>) : CellTest<E> {
    override fun performCellTest(api: E, coordinates: Coordinates, def: CellDefinition?) {
        cellTests.forEach { it.performCellTest(api, coordinates, def) }
    }
}

class AssertEqualsAttribute<E>(
    private val expectedAttribute: CellAttribute,
    private val onlyProperties: Set<KProperty1<out CellAttribute, Any?>>? = null
) : CellTest<E> {

    @Suppress("UNCHECKED_CAST")
    private fun <A: CellAttribute> A.matchProperties(expected: A) =
        onlyProperties!!.map { it as KProperty1<A, Any?>  }.all { it(this) ==  it(expected)}

    override fun performCellTest(api: E, coordinates: Coordinates, def: CellDefinition?) {
        if (onlyProperties?.isNotEmpty() == true) {
            assertTrue(
                def?.cellAttributes?.any { it.matchProperties(expectedAttribute) } ?: false,
                "CellAttribute $expectedAttribute with matching properties ${onlyProperties.joinToString(",") { it.name }} not found"
            )
        } else {
            assertTrue(
                def?.cellAttributes?.contains(expectedAttribute) ?: false,
                "CellAttribute $expectedAttribute not found"
            )
        }
    }
}

class AssertNoAttribute<E>(private val expectedAttribute: CellAttribute) : CellTest<E> {

    override fun performCellTest(api: E, coordinates: Coordinates, def: CellDefinition?) {
        assertTrue(
            def?.cellAttributes?.contains(expectedAttribute) == false,
            "CellAttribute $expectedAttribute found but not expected!"
        )
    }
}



class AssertAttributeExpression(
    private val attributeClass: KClass<out CellAttribute>,
    private val invoke: (CellAttribute) -> Unit
) :
    AssertCellAttribute {

    override fun testCellAttribute(cellAttribute: CellAttribute) {
        invoke.invoke(cellAttribute)
    }

    override fun attributeClass(): KClass<out CellAttribute> = attributeClass
}
