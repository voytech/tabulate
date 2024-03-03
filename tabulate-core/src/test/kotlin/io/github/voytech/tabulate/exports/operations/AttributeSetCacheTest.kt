package io.github.voytech.tabulate.exports.operations

import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.model.attributes.table.template
import io.github.voytech.tabulate.components.table.rendering.CellRenderable
import io.github.voytech.tabulate.components.table.rendering.RowEndRenderable
import io.github.voytech.tabulate.components.table.rendering.TableStartRenderable
import io.github.voytech.tabulate.components.table.rendering.asTableStart
import io.github.voytech.tabulate.core.DocumentFormat
import io.github.voytech.tabulate.core.StandaloneExportTemplate
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.model.border.DefaultBorderStyle
import io.github.voytech.tabulate.core.model.color.Colors
import io.github.voytech.tabulate.core.operation.*
import io.github.voytech.tabulate.support.mock.Spy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

class AttributeSetCacheTest {

    private fun createTableModelWithCellAttributes(block: ColumnLevelAttributesBuilderApi<Unit>.() -> Unit): Table<Unit> =
        createTableModel {
            columns { column(0) { attributes(block) } }
            rows { newRow { cell { value = "cell" } } }
        }

    private fun createTableModel(block: TableBuilderApi<Unit>.() -> Unit): Table<Unit> =
        createTableBuilder(block).build()

    @Test
    fun `should create AttributeSetCache for AttributedTable`() {
        val table = createTableModel { attributes { template { fileName = "filename" } } }
        val customAttributes = mutableMapOf<String, Any>()

        val attributedTable = table.asTableStart(StateAttributes(customAttributes))
        val cache = attributedTable.ensureAttributeSetBasedCache()
        assertNotNull(cache)
        assertEquals(cache, customAttributes["_attribute_set_based_cache"])

        val secondTable = createTableModel { attributes { template { fileName = "filename" } } }
        val secondAttributedTable = secondTable.asTableStart(StateAttributes(customAttributes))
        val sameCacheRef = secondAttributedTable.ensureAttributeSetBasedCache()
        assertNotNull(sameCacheRef)
        assertEquals(cache, sameCacheRef)
    }


    @Test
    fun `should setup and lookup internal caches when table attribute sets are equal`() {
        val customAttributes = mutableMapOf<String, Any>()

        val firstTable = createTableModel { attributes { template { fileName = "filename" } } }
        val firstTableContext: TableStartRenderable = firstTable.asTableStart(StateAttributes(customAttributes))

        val secondTable = createTableModel { attributes { template { fileName = "filename" } } }
        val secondTableContext: TableStartRenderable = secondTable.asTableStart(StateAttributes(customAttributes))

        val cache = firstTableContext.setupCacheAndGet().also { it!!["someKey"] = "someValue" }
        val secondCache = secondTableContext.setupCacheAndGet()

        assertEquals(secondCache, cache)
        assertTrue(cache!!.containsKey("someKey"))
        assertEquals(cache["someKey"], "someValue")
        assertTrue(secondCache!!.containsKey("someKey"))
        assertEquals(secondCache["someKey"], "someValue")
    }

    @Test
    fun `should setup and lookup internal caches when cell attribute sets are equal`() {
        val firstTable = createTableModelWithCellAttributes {
            text { color = Colors.BLACK }
            background { color = Colors.WHITE }
            borders { leftBorderStyle = DefaultBorderStyle.SOLID }
        }

        val secondTable = createTableModelWithCellAttributes {
            text { color = Colors.BLACK }
            background { color = Colors.WHITE }
            borders { leftBorderStyle = DefaultBorderStyle.SOLID }
        }
        val cellRenderable = firstTable.getFirstRenderableCellInExport(mutableMapOf())
        val customAttributesWithCache = cellRenderable.additionalAttributes
        cellRenderable.withAttributeSetBasedCache {
            cellRenderable.cacheOnAttributeSet("key") { "value" }
        }
        cellRenderable.withAttributeSetBasedCache {
            assertEquals("value", cellRenderable.getCachedOnAttributeSet("key"))
        }
        
        val secondExportCellRenderable = secondTable.getFirstRenderableCellInExport(customAttributesWithCache)
        secondExportCellRenderable.withAttributeSetBasedCache {
            assertEquals("value", secondExportCellRenderable.getCachedOnAttributeSet("key"))
        }
    }

    @Test
    fun `should setup internal caches and fail to lookup value in second cache for different table attribute sets`() {
        val customAttributes = mutableMapOf<String, Any>()

        val firstTable = createTableModel { attributes { template { fileName = "filename" } } }
        val firstTableContext: TableStartRenderable = firstTable.asTableStart(StateAttributes(customAttributes))

        val secondTable = createTableModel { attributes { template { fileName = "second_filename" } } }
        val secondTableContext: TableStartRenderable = secondTable.asTableStart(StateAttributes(customAttributes))

        val cache = firstTableContext.setupCacheAndGet().also { it!!["someKey"] = "someValue" }
        val secondCache = secondTableContext.setupCacheAndGet()

        assertNotEquals(secondCache, cache)
        assertTrue(cache!!.containsKey("someKey"))
        assertFalse(secondCache!!.containsKey("someKey"))
    }

    @Test
    fun `should setup internal caches and fail to lookup value in second cache for different cell attribute sets`() {
        val firstTable = createTableModelWithCellAttributes {
            text { color = Colors.BLACK }
            background { color = Colors.WHITE }
            borders { leftBorderStyle = DefaultBorderStyle.SOLID }
        }

        val cellRenderable = firstTable.getFirstRenderableCellInExport(mutableMapOf())
        val customAttributesWithCache = cellRenderable.additionalAttributes
        cellRenderable.withAttributeSetBasedCache {
            cellRenderable.cacheOnAttributeSet("key") { "value" }
        }
        cellRenderable.withAttributeSetBasedCache {
            assertEquals("value", cellRenderable.getCachedOnAttributeSet("key"))
        }

        val secondTable = createTableModelWithCellAttributes {
            text { color = Colors.BLACK }
            background { color = Colors.WHITE }
            borders { leftBorderStyle = DefaultBorderStyle.DOTTED }
        }
        val secondExportCellRenderable = secondTable.getFirstRenderableCellInExport(customAttributesWithCache)
        secondExportCellRenderable.withAttributeSetBasedCache {
            assertThrows<IllegalStateException> { secondExportCellRenderable.getCachedOnAttributeSet("key") }
        }
    }

    private fun Table<*>.getFirstRenderableCellInExport(attributes: MutableMap<String,Any>): CellRenderable {
        val spyFormat = DocumentFormat.format("spy")
        StandaloneExportTemplate(spyFormat).export(this, Unit, attributes)
        val secondExportRowRenderable = Spy.spy.readHistory().asSequence()
            .first { it.context is RowEndRenderable<*> }.context as RowEndRenderable<*>
        return secondExportRowRenderable.rowCellValues.firstNotNullOf { it.value }
    }


}