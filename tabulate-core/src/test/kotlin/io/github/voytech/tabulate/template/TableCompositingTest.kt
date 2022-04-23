package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.components.table.api.builder.dsl.plus
import io.github.voytech.tabulate.components.table.api.builder.dsl.table
import io.github.voytech.tabulate.components.table.api.builder.dsl.typedTable
import io.github.voytech.tabulate.core.template.DocumentFormat
import io.github.voytech.tabulate.data.Product
import io.github.voytech.tabulate.data.Products
import io.github.voytech.tabulate.components.table.model.attributes.Colors
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.text
import io.github.voytech.tabulate.components.table.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.components.table.model.attributes.column.width
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.components.table.template.export
import io.github.voytech.tabulate.components.table.template.tabulate
import io.github.voytech.tabulate.support.Spy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TableCompositingTest {

    @Test
    fun `should merge custom table with another custom table`() {
        val customTable = table {
            name = "Products table"
            attributes {
                text { fontColor = Colors.BLACK }
            }
            columns {
                column(0) {
                    attributes {
                        width { px = 45 }
                    }
                }
            }
        }
        val overrideTable = table {
            name = "Name override"
            rows {
                newRow {
                    cell {
                        value = "I gave a value."
                    }
                }
            }
        }
        (customTable + overrideTable).export(DocumentFormat("spy"), Unit)
        val history = Spy.spy.readHistory()
        // Table
        history.next().run { assertEquals("Name override", (context as TableOpeningContext).getSheetName()) }
        // Column 0
        history.next().run { assertEquals(0, (context as ColumnOpeningContext).columnIndex) }
        // Column 0 attribute
        history.next().run { assertEquals(45, (attribute as ColumnWidthAttribute).px) }
        // Row 0
        history.next().run { assertEquals(0, (context as RowOpeningContext).getRow()) }
        // Row 0, cell 0
        history.next().run { assertEquals("I gave a value.", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(Colors.BLACK, (attribute as CellTextStylesAttribute).fontColor)
        }
        history.next().run { assertTrue(context is RowClosingContext<*>) }
        // Column 0
        history.next().run { assertEquals(0, (context as ColumnClosingContext).columnIndex) }
        history.next().run { assertIs<TableClosingContext>(context) }
        assertFalse(history.hasNext())
    }

    @Test
    fun `should merge custom table with typed table`() {
        val customTable = table {
            name = "Products table"
            attributes {
                text { fontColor = Colors.BLACK }
            }
            columns {
                column(0) {
                    attributes {
                        width { px = 45 }
                    }
                }
            }
        }
        val overrideTable = typedTable<Product> {
            name = "Name override"
            columns {
                column(Product::code)
            }
        }
        Products.items(1).tabulate(DocumentFormat("spy"), Unit,customTable + overrideTable)
        val history = Spy.spy.readHistory()
        // Table
        history.next().run { assertEquals("Name override", (context as TableOpeningContext).getSheetName()) }
        // Column 0
        history.next().run { assertEquals(0, (context as ColumnOpeningContext).columnIndex) }
        // Column 0 attribute
        history.next().run { assertEquals(45, (attribute as ColumnWidthAttribute).px) }
        // Column 1
        history.next().run { assertEquals(1, (context as ColumnOpeningContext).columnIndex) }
        // Row 0
        history.next().run { assertEquals(0, (context as RowOpeningContext).getRow()) }
        // Row 0, cell 0
        history.next().run { assertEquals("code1", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(Colors.BLACK, (attribute as CellTextStylesAttribute).fontColor)
        }
        history.next().run { assertTrue(context is RowClosingContext<*>) }
        // Column 0
        history.next().run { assertEquals(0, (context as ColumnClosingContext).columnIndex) }
        // Column 1
        history.next().run { assertEquals(1, (context as ColumnClosingContext).columnIndex) }
        history.next().run { assertIs<TableClosingContext>(context) }
        assertFalse(history.hasNext())
    }

    @Test
    fun `should merge typed table with another typed table`() {
        val baseTable = typedTable<Product> {
            name = "Products table"
            attributes {
                text { fontColor = Colors.BLACK }
            }
            columns {
                column(Product::code) {
                    attributes {
                        width { px = 45 }
                    }
                }
            }
        }
        val overrideTable = typedTable<Product> {
            name = "Name override"
            columns {
                column(Product::name)
            }
        }
        Products.items(1).tabulate(DocumentFormat("spy"), Unit,baseTable + overrideTable)
        val history = Spy.spy.readHistory()
        // Table
        history.next().run { assertEquals("Name override", (context as TableOpeningContext).getSheetName()) }
        // Column 0
        history.next().run { assertEquals(0, (context as ColumnOpeningContext).columnIndex) }
        // Column 0 attribute
        history.next().run { assertEquals(45, (attribute as ColumnWidthAttribute).px) }
        // Column 1
        history.next().run { assertEquals(1, (context as ColumnOpeningContext).columnIndex) }
        // Row 0
        history.next().run { assertEquals(0, (context as RowOpeningContext).getRow()) }
        // Row 0, cell 0
        history.next().run { assertEquals("code1", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(Colors.BLACK, (attribute as CellTextStylesAttribute).fontColor)
        }
        // Row 0, cell 1
        history.next().run { assertEquals("name1", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(Colors.BLACK, (attribute as CellTextStylesAttribute).fontColor)
        }
        history.next().run { assertTrue(context is RowClosingContext<*>) }
        // Column 0
        history.next().run { assertEquals(0, (context as ColumnClosingContext).columnIndex) }
        // Column 1
        history.next().run { assertEquals(1, (context as ColumnClosingContext).columnIndex) }
        history.next().run { assertIs<TableClosingContext>(context) }
        assertFalse(history.hasNext())
    }

    @Test
    fun `should merge typed table with inferred type table`() {
        val baseTable = typedTable<Product> {
            name = "Products table"
            attributes {
                text { fontColor = Colors.BLACK }
            }
            columns {
                column(Product::code) {
                    attributes {
                        width { px = 45 }
                    }
                }
            }
        }
        Products.items(1).tabulate(DocumentFormat("spy"), Unit,baseTable + typedTable { name = "Name override" })
        val history = Spy.spy.readHistory()
        // Table
        history.next().run { assertEquals("Name override", (context as TableOpeningContext).getSheetName()) }
        // Column 0
        history.next().run { assertEquals(0, (context as ColumnOpeningContext).columnIndex) }
        // Column 0 attribute
        history.next().run { assertEquals(45, (attribute as ColumnWidthAttribute).px) }
        // Row 0
        history.next().run { assertEquals(0, (context as RowOpeningContext).getRow()) }
        // Row 0, cell 0
        history.next().run { assertEquals("code1", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(Colors.BLACK, (attribute as CellTextStylesAttribute).fontColor)
        }
        history.next().run { assertTrue(context is RowClosingContext<*>) }
        // Column 0
        history.next().run { assertEquals(0, (context as ColumnClosingContext).columnIndex) }
        history.next().run { assertIs<TableClosingContext>(context) }
        assertFalse(history.hasNext())
    }

}