package pl.voytech.exporter.testutils.cellassertions

import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.template.CellValue
import pl.voytech.exporter.core.template.Coordinates
import pl.voytech.exporter.core.template.DelegateAPI
import pl.voytech.exporter.testutils.CellDefinition
import pl.voytech.exporter.testutils.CellTest
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

class AssertCellValue<E>(
    private val expectedValue: Any,
    private val expectedType: CellType? = null,
    private val expectedColspan: Int? = null,
    private val expectedRowspan: Int? = null
) : CellTest<E> {
    override fun performCellTest(api: E, coordinates: Coordinates, def: CellDefinition?) {
        assertNotNull(def?.cellValue, "Expected cell value to be present")
        assertEquals(expectedValue, def?.cellValue?.value, "Expected cell value to be $expectedValue")
        expectedType?.let {
            assertNotNull(def?.cellValue?.type, "Expected cell type to be present")
            assertEquals(expectedType, def?.cellValue?.type, "Expected cell type to be $it")
        }
        expectedColspan?.let {
            assertNotNull(def?.cellValue?.colSpan, "Expected cell collSpan to be present")
            assertEquals(expectedColspan, def?.cellValue?.colSpan, "Expected cell colSpan to be $it")
        }
        expectedRowspan?.let {
            assertNotNull(def?.cellValue?.rowSpan, "Expected cell rowSpan to be present")
            assertEquals(expectedRowspan, def?.cellValue?.rowSpan, "Expected cell rowSpan to be $it")
        }
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

class AssertEqualAttribute(private val expectedAttribute: CellAttribute) : AssertCellAttribute {

    override fun testCellAttribute(cellAttribute: CellAttribute) {
        assertEquals(expectedAttribute, cellAttribute, "expected attribute to equal $expectedAttribute")
    }

    override fun attributeClass(): KClass<out CellAttribute> = expectedAttribute::class
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
