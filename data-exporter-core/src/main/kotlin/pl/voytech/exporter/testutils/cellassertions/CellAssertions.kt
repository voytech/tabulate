package pl.voytech.exporter.testutils.cellassertions

import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.template.CellValue
import pl.voytech.exporter.core.template.Coordinates
import pl.voytech.exporter.core.template.DelegateAPI
import pl.voytech.exporter.testutils.CellDefinition
import pl.voytech.exporter.testutils.CellTest
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

class AssertCellValue<E>(private val expectedValue: Any, private val expectedType: CellType? = null) : CellTest<E> {
    override fun performCellTest(api: DelegateAPI<E>, coordinates: Coordinates, def: CellDefinition?) {
        assertNotNull(def?.cellValue, "Expected cell value to be present")
        assertEquals(expectedValue, def?.cellValue?.value, "Expected cell value to be $expectedValue")
        expectedType?.let {
            assertNotNull(def?.cellValue?.type, "Expected cell type to be present")
            assertEquals(expectedType, def?.cellValue?.type, "Expected cell type to be $it")
        }
    }
}

class AssertCellValueExpr<E>(private val invoke: (CellValue) -> Unit) : CellTest<E> {
    override fun performCellTest(api: DelegateAPI<E>, coordinates: Coordinates, def: CellDefinition?) {
        assertNotNull(def?.cellValue, "Expected cell value to be present")
        def?.cellValue?.let { invoke.invoke(it) }
    }
}

interface AssertCellExtension {
    fun testCellExtension(cellExtension: CellExtension)
    fun extensionClass(): KClass<out CellExtension>
}

class CellExtensionsAssertions<E>(private vararg val cellExtensionsTests: AssertCellExtension) : CellTest<E> {
    override fun performCellTest(api: DelegateAPI<E>, coordinates: Coordinates, def: CellDefinition?) {
        val cellExtensionByClass = def?.cellExtensions?.groupBy { it::class }
        cellExtensionsTests.forEach {
            if (cellExtensionByClass?.containsKey(it.extensionClass()) == false) {
                fail("cell extension for class ${it.extensionClass().simpleName} not found for cell at $coordinates")
            }
            cellExtensionByClass?.get(it.extensionClass())?.forEach { extension ->
                it.testCellExtension(extension)
            }
        }
    }
}

class AssertContainsCellExtensions<E>(private vararg val targets: CellExtension) : CellTest<E> {
    override fun performCellTest(api: DelegateAPI<E>, coordinates: Coordinates, def: CellDefinition?) {
        val existingExtensions = def?.cellExtensions?.asIterable() ?: emptyList()
        assertEquals(
            targets.toSet(),
            targets.toSet().intersect(existingExtensions),
            "expected cell extension set to contain all target extension instances"
        )
    }
}

class AssertMany<E>(private vararg val cellTests: CellTest<E>) : CellTest<E> {
    override fun performCellTest(api: DelegateAPI<E>, coordinates: Coordinates, def: CellDefinition?) {
        cellTests.forEach { it.performCellTest(api, coordinates, def) }
    }
}

class AssertEqualExtension(private val expectedExtension: CellExtension) : AssertCellExtension {

    override fun testCellExtension(cellExtension: CellExtension) {
        assertEquals(expectedExtension, cellExtension, "expected extension to equal $expectedExtension")
    }

    override fun extensionClass(): KClass<out CellExtension> = expectedExtension::class
}

class AssertExtensionExpression(
    private val extensionClass: KClass<out CellExtension>,
    private val invoke: (CellExtension) -> Unit
) :
    AssertCellExtension {

    override fun testCellExtension(cellExtension: CellExtension) {
        invoke.invoke(cellExtension)
    }

    override fun extensionClass(): KClass<out CellExtension> = extensionClass
}
