package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.api.builder.dsl.ColumnLevelAttributesBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.TableBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.createTableBuilder
import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.model.attributes.cell.Colors
import io.github.voytech.tabulate.model.attributes.cell.background
import io.github.voytech.tabulate.model.attributes.cell.borders
import io.github.voytech.tabulate.model.attributes.cell.enums.DefaultBorderStyle
import io.github.voytech.tabulate.model.attributes.cell.text
import io.github.voytech.tabulate.model.attributes.table.template
import io.github.voytech.tabulate.template.iterators.RowContextIterator
import io.github.voytech.tabulate.template.resolvers.BufferingRowContextResolver
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AttributeSetCacheTest {

    private fun createTableModelWithCellAttributes(block: ColumnLevelAttributesBuilderApi<Unit>.() -> Unit): Table<Unit> =
        createTableModel {
            columns { column(0) { attributes(block) } }
            rows { newRow { cell { value = "cell" }} }
        }

    private fun createTableModel(block: TableBuilderApi<Unit>.() -> Unit): Table<Unit> =
        createTableBuilder(block).build()

    @Test
    fun `should create AttributeSetCache for AttributedTable`() {
        val table = createTableModel { attributes { template { fileName = "filename" } } }
        val customAttributes = mutableMapOf<String, Any>()

        val attributedTable = table.createContext(customAttributes)
        val cache = attributedTable.ensureAttributeSetBasedCache()
        assertNotNull(cache)
        assertEquals(cache, customAttributes["_attribute_set_based_cache"])

        val secondTable = createTableModel { attributes { template { fileName = "filename" } } }
        val secondAttributedTable = secondTable.createContext(customAttributes)
        val sameCacheRef = secondAttributedTable.ensureAttributeSetBasedCache()
        assertNotNull(sameCacheRef)
        assertEquals(cache, sameCacheRef)
    }

    @Test
    fun `should setup and lookup internal caches when table attribute sets are equal`() {
        val customAttributes = mutableMapOf<String, Any>()

        val firstTable = createTableModel { attributes { template { fileName = "filename" } } }
        val firstAttributedTable: AttributedTable = firstTable.createContext(customAttributes)

        val secondTable = createTableModel { attributes { template { fileName = "filename" } } }
        val secondAttributedTable: AttributedTable = secondTable.createContext(customAttributes)

        val cache = firstAttributedTable.setupCacheAndGet().also { it!!["someKey"] = "someValue" }
        val secondCache = secondAttributedTable.setupCacheAndGet()

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
            text { fontColor = Colors.BLACK }
            background { color = Colors.WHITE }
            borders { leftBorderStyle = DefaultBorderStyle.SOLID }
        }

        val secondTable = createTableModelWithCellAttributes {
            text { fontColor = Colors.BLACK }
            background { color = Colors.WHITE }
            borders { leftBorderStyle = DefaultBorderStyle.SOLID }
        }

        val iterator = RowContextIterator(BufferingRowContextResolver(firstTable, customAttributes))
        val attributedCell = iterator.next().rowCellValues.firstNotNullOf { it.value }
        attributedCell.withAttributeSetBasedCache {
            attributedCell.skipAttributes().cacheOnAttributeSet("key", "value")
        }
        attributedCell.withAttributeSetBasedCache {
            assertEquals("value",attributedCell.skipAttributes().getCachedOnAttributeSet("key"))
        }

        val secondIterator = RowContextIterator(BufferingRowContextResolver(secondTable, customAttributes))
        val secondAttributedCell = secondIterator.next().rowCellValues.firstNotNullOf { it.value }
        secondAttributedCell.withAttributeSetBasedCache {
            assertEquals("value",secondAttributedCell.skipAttributes().getCachedOnAttributeSet("key"))
        }
    }

    @Test
    fun `should setup internal caches and fail to lookup value in second cache for different table attribute sets`() {
        val customAttributes = mutableMapOf<String, Any>()

        val firstTable = createTableModel { attributes { template { fileName = "filename" } } }
        val firstAttributedTable: AttributedTable = firstTable.createContext(customAttributes)

        val secondTable = createTableModel { attributes { template { fileName = "second_filename" } } }
        val secondAttributedTable: AttributedTable = secondTable.createContext(customAttributes)

        val cache = firstAttributedTable.setupCacheAndGet().also { it!!["someKey"] = "someValue" }
        val secondCache = secondAttributedTable.setupCacheAndGet()

        assertNotEquals(secondCache, cache)
        assertTrue(cache!!.containsKey("someKey"))
        assertFalse(secondCache!!.containsKey("someKey"))
    }

    @Test
    fun `should setup internal caches and fail to lookup value in second cache for different cell attribute sets`() {
        val customAttributes = mutableMapOf<String, Any>()

        val firstTable = createTableModelWithCellAttributes {
            text { fontColor = Colors.BLACK }
            background { color = Colors.WHITE }
            borders { leftBorderStyle = DefaultBorderStyle.SOLID }
        }
        val iterator = RowContextIterator(BufferingRowContextResolver(firstTable, customAttributes))
        val attributedCell = iterator.next().rowCellValues.firstNotNullOf { it.value }
        attributedCell.withAttributeSetBasedCache {
            attributedCell.skipAttributes().cacheOnAttributeSet("key", "value")
        }
        attributedCell.withAttributeSetBasedCache {
            assertEquals("value",attributedCell.skipAttributes().getCachedOnAttributeSet("key"))
        }

        val secondTable = createTableModelWithCellAttributes {
            text { fontColor = Colors.BLACK }
            background { color = Colors.WHITE }
            borders { leftBorderStyle = DefaultBorderStyle.DOTTED }
        }
        val secondIterator = RowContextIterator(BufferingRowContextResolver(secondTable, customAttributes))
        val secondAttributedCell = secondIterator.next().rowCellValues.firstNotNullOf { it.value }
        secondAttributedCell.withAttributeSetBasedCache {
            assertThrows<IllegalStateException> { secondAttributedCell.skipAttributes().getCachedOnAttributeSet("key") }
        }
    }

    @Test
    fun `should correctly perform withAttributeSetBasedCache() scoping`() {
        val customAttributes = mutableMapOf<String, Any>()

        val firstTable = createTableModel { attributes { template { fileName = "filename" } } }
        val firstAttributedTable: AttributedTable = firstTable.createContext(customAttributes)

        val secondTable = createTableModel { attributes { template { fileName = "filename" } } }
        val secondAttributedTable: AttributedTable = secondTable.createContext(customAttributes)

        val thirdTable = createTableModel { attributes { template { fileName = "third_table_filename_differs" } } }
        val thirdAttributedTable: AttributedTable = thirdTable.createContext(customAttributes)

        firstAttributedTable.withAttributeSetBasedCache {
            firstAttributedTable.skipAttributes().cacheOnAttributeSet("someKey", "someValue")
        }
        val error = assertThrows<IllegalStateException> {
            firstAttributedTable.skipAttributes().getCachedOnAttributeSet("someKey")
        }
        assertEquals("cannot resolve cached value in scope!", error.message)


        secondAttributedTable.withAttributeSetBasedCache {
            secondAttributedTable.skipAttributes().let { tableContext ->
                tableContext.cacheOnAttributeSet("someKey", "tryOverride")
                assertEquals("someValue", tableContext.getCachedOnAttributeSet("someKey"))
            }
        }

        thirdAttributedTable.withAttributeSetBasedCache {
            thirdAttributedTable.skipAttributes().let { tableContext ->
                tableContext.cacheOnAttributeSet(
                    "someKey",
                    "thisIsNewValueInNewInternalCacheCosAttributesDiffers"
                )
                assertEquals(
                    "thisIsNewValueInNewInternalCacheCosAttributesDiffers",
                    tableContext.getCachedOnAttributeSet("someKey")
                )
            }
        }
    }

}