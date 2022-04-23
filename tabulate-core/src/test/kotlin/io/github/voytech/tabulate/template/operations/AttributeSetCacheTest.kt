package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.components.table.api.builder.dsl.ColumnLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.dsl.TableBuilderApi
import io.github.voytech.tabulate.components.table.api.builder.dsl.createTableBuilder
import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.model.attributes.Colors
import io.github.voytech.tabulate.components.table.model.attributes.cell.background
import io.github.voytech.tabulate.components.table.model.attributes.cell.borders
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.DefaultBorderStyle
import io.github.voytech.tabulate.components.table.model.attributes.cell.text
import io.github.voytech.tabulate.components.table.model.attributes.table.template
import io.github.voytech.tabulate.components.table.operation.TableOpeningContext
import io.github.voytech.tabulate.components.table.operation.createContext
import io.github.voytech.tabulate.components.table.template.AccumulatingRowContextResolver
import io.github.voytech.tabulate.components.table.template.RowContextIterator
import io.github.voytech.tabulate.core.template.operation.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

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
        val firstTableContext: TableOpeningContext = firstTable.createContext(customAttributes)

        val secondTable = createTableModel { attributes { template { fileName = "filename" } } }
        val secondTableContext: TableOpeningContext = secondTable.createContext(customAttributes)

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
            text { fontColor = Colors.BLACK }
            background { color = Colors.WHITE }
            borders { leftBorderStyle = DefaultBorderStyle.SOLID }
        }

        val secondTable = createTableModelWithCellAttributes {
            text { fontColor = Colors.BLACK }
            background { color = Colors.WHITE }
            borders { leftBorderStyle = DefaultBorderStyle.SOLID }
        }

        val iterator = RowContextIterator(AccumulatingRowContextResolver(firstTable, customAttributes))
        val attributedCell = iterator.next().rowCellValues.firstNotNullOf { it.value }
        attributedCell.withAttributeSetBasedCache {
            attributedCell.cacheOnAttributeSet("key", "value")
        }
        attributedCell.withAttributeSetBasedCache {
            assertEquals("value",attributedCell.getCachedOnAttributeSet("key"))
        }

        val secondIterator = RowContextIterator(AccumulatingRowContextResolver(secondTable, customAttributes))
        val secondAttributedCell = secondIterator.next().rowCellValues.firstNotNullOf { it.value }
        secondAttributedCell.withAttributeSetBasedCache {
            assertEquals("value",secondAttributedCell.getCachedOnAttributeSet("key"))
        }
    }

    @Test
    fun `should setup internal caches and fail to lookup value in second cache for different table attribute sets`() {
        val customAttributes = mutableMapOf<String, Any>()

        val firstTable = createTableModel { attributes { template { fileName = "filename" } } }
        val firstTableContext: TableOpeningContext = firstTable.createContext(customAttributes)

        val secondTable = createTableModel { attributes { template { fileName = "second_filename" } } }
        val secondTableContext: TableOpeningContext = secondTable.createContext(customAttributes)

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
            text { fontColor = Colors.BLACK }
            background { color = Colors.WHITE }
            borders { leftBorderStyle = DefaultBorderStyle.SOLID }
        }
        val iterator = RowContextIterator(AccumulatingRowContextResolver(firstTable, customAttributes))
        val attributedCell = iterator.next().rowCellValues.firstNotNullOf { it.value }
        attributedCell.withAttributeSetBasedCache {
            attributedCell.cacheOnAttributeSet("key", "value")
        }
        attributedCell.withAttributeSetBasedCache {
            assertEquals("value",attributedCell.getCachedOnAttributeSet("key"))
        }

        val secondTable = createTableModelWithCellAttributes {
            text { fontColor = Colors.BLACK }
            background { color = Colors.WHITE }
            borders { leftBorderStyle = DefaultBorderStyle.DOTTED }
        }
        val secondIterator = RowContextIterator(AccumulatingRowContextResolver(secondTable, customAttributes))
        val secondAttributedCell = secondIterator.next().rowCellValues.firstNotNullOf { it.value }
        secondAttributedCell.withAttributeSetBasedCache {
            assertThrows<IllegalStateException> { secondAttributedCell.getCachedOnAttributeSet("key") }
        }
    }

    @Test
    fun `should correctly perform withAttributeSetBasedCache() scoping`() {
        val customAttributes = mutableMapOf<String, Any>()

        val firstTable = createTableModel { attributes { template { fileName = "filename" } } }
        val firstTableContext: TableOpeningContext = firstTable.createContext(customAttributes)

        val secondTable = createTableModel { attributes { template { fileName = "filename" } } }
        val secondTableContext: TableOpeningContext = secondTable.createContext(customAttributes)

        val thirdTable = createTableModel { attributes { template { fileName = "third_table_filename_differs" } } }
        val thirdTableContext: TableOpeningContext = thirdTable.createContext(customAttributes)

        firstTableContext.withAttributeSetBasedCache {
            firstTableContext.cacheOnAttributeSet("someKey", "someValue")
        }
        val error = assertThrows<IllegalStateException> {
            firstTableContext.getCachedOnAttributeSet("someKey")
        }
        assertEquals("cannot resolve cached value in scope!", error.message)


        secondTableContext.withAttributeSetBasedCache {
            secondTableContext.let { tableContext ->
                tableContext.cacheOnAttributeSet("someKey", "tryOverride")
                assertEquals("someValue", tableContext.getCachedOnAttributeSet("someKey"))
            }
        }

        thirdTableContext.withAttributeSetBasedCache {
            thirdTableContext.let { tableContext ->
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