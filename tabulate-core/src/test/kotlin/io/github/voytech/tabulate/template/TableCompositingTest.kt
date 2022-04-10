package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.api.builder.dsl.CustomTable
import io.github.voytech.tabulate.api.builder.dsl.Table
import io.github.voytech.tabulate.api.builder.dsl.plus
import io.github.voytech.tabulate.api.builder.dsl.with
import io.github.voytech.tabulate.data.Product
import io.github.voytech.tabulate.data.Products
import io.github.voytech.tabulate.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.model.attributes.cell.Colors
import io.github.voytech.tabulate.model.attributes.cell.text
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.model.attributes.column.width
import io.github.voytech.tabulate.support.Spy
import io.github.voytech.tabulate.template.operations.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TableCompositingTest {

    @Test
    fun `should merge custom table with another custom table`() {
        val customTable = CustomTable {
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
        val overrideTable = CustomTable {
            name = "Name override"
            rows {
                newRow {
                    cell {
                        value = "I gave a value."
                    }
                }
            }
        }
        (customTable + overrideTable).export(TabulationFormat("spy"), Unit)
        val history = Spy.spy.readHistory()
        // Table
        history.next().run { assertEquals("Name override", (context as TableOpeningContext).getTableId()) }
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
        val customTable = CustomTable {
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
        val overrideTable = Table<Product> {
            name = "Name override"
            columns {
                column(Product::code)
            }
        }
        Products.items(1).tabulate(TabulationFormat("spy"), Unit,customTable + overrideTable)
        val history = Spy.spy.readHistory()
        // Table
        history.next().run { assertEquals("Name override", (context as TableOpeningContext).getTableId()) }
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
        val baseTable = Table<Product> {
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
        val overrideTable = Table<Product> {
            name = "Name override"
            columns {
                column(Product::name)
            }
        }
        Products.items(1).tabulate(TabulationFormat("spy"), Unit,baseTable with overrideTable)
        val history = Spy.spy.readHistory()
        // Table
        history.next().run { assertEquals("Name override", (context as TableOpeningContext).getTableId()) }
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

}