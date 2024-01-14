package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.core.model.color.Colors
import io.github.voytech.tabulate.core.model.border.DefaultBorderStyle
import io.github.voytech.tabulate.components.table.model.attributes.table.template
import io.github.voytech.tabulate.components.table.rendering.TableStartRenderable
import io.github.voytech.tabulate.components.table.rendering.asTableStart
import io.github.voytech.tabulate.components.table.template.*
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.operation.*
import io.github.voytech.tabulate.core.operation.withAttributeSetBasedCache
import io.github.voytech.tabulate.support.createTableContext
import io.github.voytech.tabulate.support.success
import io.github.voytech.tabulate.support.successfulRowComplete
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
        val customAttributes = mutableMapOf<String, Any>()

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
        val firstTableContext = firstTable.createTableContext(customAttributes)
        val iterator = RowContextIterator(
            AccumulatingRowContextResolver(
                firstTable, StateAttributes(customAttributes), TableRenderIterations(firstTableContext), successfulRowComplete()
            ), TableRenderIterations(firstTableContext)
        )
        val attributedCell = iterator.next().success().rowCellValues.firstNotNullOf { it.value }
        attributedCell.withAttributeSetBasedCache {
            attributedCell.cacheOnAttributeSet("key") { "value" }
        }
        attributedCell.withAttributeSetBasedCache {
            assertEquals("value", attributedCell.getCachedOnAttributeSet("key"))
        }
        val secondTableContext = secondTable.createTableContext(customAttributes)
        val secondIterator = RowContextIterator(
            AccumulatingRowContextResolver(
                secondTable, StateAttributes(customAttributes), TableRenderIterations(secondTableContext), successfulRowComplete()
            ), TableRenderIterations(secondTableContext)
        )
        val secondAttributedCell = secondIterator.next().success().rowCellValues.firstNotNullOf { it.value }
        secondAttributedCell.withAttributeSetBasedCache {
            assertEquals("value", secondAttributedCell.getCachedOnAttributeSet("key"))
        }
    }

    @Test
    fun `should setup internal caches and fail to lookup value in second cache for different table attribute sets`() {
        val customAttributes = mutableMapOf<String, Any>()

        val firstTable = createTableModel { attributes { template { fileName = "filename" } } }
        val firstTableContext: TableStartRenderable = firstTable.asTableStart(customAttributes)

        val secondTable = createTableModel { attributes { template { fileName = "second_filename" } } }
        val secondTableContext: TableStartRenderable = secondTable.asTableStart(customAttributes)

        val cache = firstTableContext.setupCacheAndGet().also { it!!["someKey"] = "someValue" }
        val secondCache = secondTableContext.setupCacheAndGet()

        assertNotEquals(secondCache, cache)
        assertTrue(cache!!.containsKey("someKey"))
        assertFalse(secondCache!!.containsKey("someKey"))
    }

    @Test
    fun `should setup internal caches and fail to lookup value in second cache for different cell attribute sets`() {
        val customAttributes = mutableMapOf<String, Any>()
        val firstTable = createTableModelWithCellAttributes {
            text { color = Colors.BLACK }
            background { color = Colors.WHITE }
            borders { leftBorderStyle = DefaultBorderStyle.SOLID }
        }
        val firstTableContext = firstTable.createTableContext(customAttributes)
        val iterator = RowContextIterator(
            AccumulatingRowContextResolver(
                firstTable, StateAttributes(customAttributes),TableRenderIterations(firstTableContext),
                successfulRowComplete()
            ), TableRenderIterations(firstTableContext)
        )
        val attributedCell = iterator.next().success().rowCellValues.firstNotNullOf { it.value }
        attributedCell.withAttributeSetBasedCache {
            attributedCell.cacheOnAttributeSet("key") { "value" }
        }
        attributedCell.withAttributeSetBasedCache {
            assertEquals("value", attributedCell.getCachedOnAttributeSet("key"))
        }

        val secondTable = createTableModelWithCellAttributes {
            text { color = Colors.BLACK }
            background { color = Colors.WHITE }
            borders { leftBorderStyle = DefaultBorderStyle.DOTTED }
        }
        val secondTableContext = secondTable.createTableContext(customAttributes)
        val secondIterator = RowContextIterator(
            AccumulatingRowContextResolver(
                secondTable, StateAttributes(customAttributes), TableRenderIterations(secondTableContext), successfulRowComplete()
            ), TableRenderIterations(secondTableContext)
        )
        val secondAttributedCell = secondIterator.next().success().rowCellValues.firstNotNullOf { it.value }
        secondAttributedCell.withAttributeSetBasedCache {
            assertThrows<IllegalStateException> { secondAttributedCell.getCachedOnAttributeSet("key") }
        }
    }


}