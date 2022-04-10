package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.api.builder.dsl.CustomTable
import io.github.voytech.tabulate.api.builder.dsl.footer
import io.github.voytech.tabulate.api.builder.dsl.header
import io.github.voytech.tabulate.data.Product
import io.github.voytech.tabulate.data.Products
import io.github.voytech.tabulate.model.attributes.cell.*
import io.github.voytech.tabulate.model.attributes.cell.enums.DefaultWeightStyle
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.model.attributes.column.columnWidth
import io.github.voytech.tabulate.model.attributes.column.width
import io.github.voytech.tabulate.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.model.attributes.row.height
import io.github.voytech.tabulate.model.attributes.row.rowHeight
import io.github.voytech.tabulate.support.Spy
import io.github.voytech.tabulate.support.TestExportOperationsFactory
import io.github.voytech.tabulate.template.TabulationFormat.Companion.format
import io.github.voytech.tabulate.template.operations.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TabulationTemplateTest {

    @Test
    fun `should tabulate elements collection without additional features`() {

        Products.ITEMS.tabulate(TabulationFormat("spy"), Unit) {
            name = "Products table"
            columns {
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
            }
        }

        val history = Spy.spy.readHistory()
        // Table
        history.next().run { assertEquals("Products table", (context as TableOpeningContext).getTableId()) }
        // Columns
        history.next().run { assertEquals(0, (context as ColumnOpeningContext).columnIndex) }
        history.next().run { assertEquals(1, (context as ColumnOpeningContext).columnIndex) }
        history.next().run { assertEquals(2, (context as ColumnOpeningContext).columnIndex) }
        history.next().run { assertEquals(3, (context as ColumnOpeningContext).columnIndex) }
        // Row 0
        history.next().run { assertEquals(0, (context as RowOpeningContext).getRow()) }
        history.next().run { assertEquals("code1", (context as CellContext).rawValue) }
        history.next().run { assertEquals("name1", (context as CellContext).rawValue) }
        history.next().run { assertEquals("description1", (context as CellContext).rawValue) }
        history.next().run { assertEquals("manufacturer1", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(0, (context as RowClosingContext<*>).getRow())
            assertEquals(4, context.getCells().keys.size)
        }
        // Row 1
        history.next().run { assertEquals(1, (context as RowOpeningContext).getRow()) }
        history.next().run { assertEquals("code2", (context as CellContext).rawValue) }
        history.next().run { assertEquals("name2", (context as CellContext).rawValue) }
        history.next().run { assertEquals("description2", (context as CellContext).rawValue) }
        history.next().run { assertEquals("manufacturer2", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(1, (context as RowClosingContext<*>).getRow())
            assertEquals(4, context.getCells().keys.size)
        }
        history.next().run { assertEquals(0, (context as ColumnClosingContext).columnIndex) }
        history.next().run { assertEquals(1, (context as ColumnClosingContext).columnIndex) }
        history.next().run { assertEquals(2, (context as ColumnClosingContext).columnIndex) }
        history.next().run { assertEquals(3, (context as ColumnClosingContext).columnIndex) }
        history.next().run { assertIs<TableClosingContext>(context) }
        assertFalse(history.hasNext())
    }

    @Test
    fun `should perform subsequent exports on the same TabulationTemplate`() {
        TabulationTemplate(format("spy")).let { tabulation ->
            tabulation.export(Products.ITEMS, Unit) {
                name = "Products table"
                columns {
                    column(Product::code)
                    column(Product::name)
                    column(Product::description)
                    column(Product::manufacturer)
                }
            }
            val firstPassRenderingContext = TestExportOperationsFactory.CURRENT_RENDERING_CONTEXT_INSTANCE
            tabulation.export(Products.ITEMS, Unit) {
                name = "Product codes list"
                columns { column(Product::code) }
            }
            val secondPassRenderingContext = TestExportOperationsFactory.CURRENT_RENDERING_CONTEXT_INSTANCE
            assertNotEquals(firstPassRenderingContext, secondPassRenderingContext)
            Spy.spy.readHistory()
        }
    }

    @Test
    fun `should append header and footer rows`() {
        Products.ITEMS.tabulate(TabulationFormat("spy"), Unit) {
            name = "Products table"
            columns {
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
            }
            rows {
                header("Code", "Name", "Description", "Manufacturer")
                footer {
                    cell { value = "First footer cell." }
                }
            }
        }

        val history = Spy.spy.readHistory()
        // Table
        history.next().run { assertEquals("Products table", (context as TableOpeningContext).getTableId()) }
        // Columns - Opening
        history.next().run { assertEquals(0, (context as ColumnOpeningContext).columnIndex) }
        history.next().run { assertEquals(1, (context as ColumnOpeningContext).columnIndex) }
        history.next().run { assertEquals(2, (context as ColumnOpeningContext).columnIndex) }
        history.next().run { assertEquals(3, (context as ColumnOpeningContext).columnIndex) }

        // Header
        history.next().run { assertEquals(0, (context as RowOpeningContext).getRow()) }
        history.next().run { assertEquals("Code", (context as CellContext).rawValue) }
        history.next().run { assertEquals("Name", (context as CellContext).rawValue) }
        history.next().run { assertEquals("Description", (context as CellContext).rawValue) }
        history.next().run { assertEquals("Manufacturer", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(0, (context as RowClosingContext<*>).getRow())
            assertEquals(4, context.getCells().keys.size)
        }
        // Row 0
        history.next().run { assertEquals(1, (context as RowOpeningContext).getRow()) }
        history.next().run { assertEquals("code1", (context as CellContext).rawValue) }
        history.next().run { assertEquals("name1", (context as CellContext).rawValue) }
        history.next().run { assertEquals("description1", (context as CellContext).rawValue) }
        history.next().run { assertEquals("manufacturer1", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(1, (context as RowClosingContext<*>).getRow())
            assertEquals(4, context.getCells().keys.size)
        }
        // Row 1
        history.next().run { assertEquals(2, (context as RowOpeningContext).getRow()) }
        history.next().run { assertEquals("code2", (context as CellContext).rawValue) }
        history.next().run { assertEquals("name2", (context as CellContext).rawValue) }
        history.next().run { assertEquals("description2", (context as CellContext).rawValue) }
        history.next().run { assertEquals("manufacturer2", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(2, (context as RowClosingContext<*>).getRow())
            assertEquals(4, context.getCells().keys.size)
        }
        // Footer row
        history.next().run { assertEquals(3, (context as RowOpeningContext).getRow()) }
        history.next().run { assertEquals("First footer cell.", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(3, (context as RowClosingContext<*>).getRow())
            assertEquals(1, context.getCells().keys.size)
        }
        history.next().run { assertEquals(0, (context as ColumnClosingContext).columnIndex) }
        history.next().run { assertEquals(1, (context as ColumnClosingContext).columnIndex) }
        history.next().run { assertEquals(2, (context as ColumnClosingContext).columnIndex) }
        history.next().run { assertEquals(3, (context as ColumnClosingContext).columnIndex) }
        history.next().run { assertIs<TableClosingContext>(context) }
        assertFalse(history.hasNext())
    }

    @Test
    fun `should append custom row with higher offset`() {
        Products.ITEMS.tabulate(TabulationFormat("spy"), Unit) {
            name = "Products table"
            columns {
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
            }
            rows {
                newRow(5) {
                    cell {
                        value = "Custom row cell"
                    }
                }
            }
        }

        val history = Spy.spy.readHistory()
        // Table
        history.next().run { assertEquals("Products table", (context as TableOpeningContext).getTableId()) }
        // Columns - Opening
        history.next().run { assertEquals(0, (context as ColumnOpeningContext).columnIndex) }
        history.next().run { assertEquals(1, (context as ColumnOpeningContext).columnIndex) }
        history.next().run { assertEquals(2, (context as ColumnOpeningContext).columnIndex) }
        history.next().run { assertEquals(3, (context as ColumnOpeningContext).columnIndex) }
        // Row 0
        history.next().run { assertEquals(0, (context as RowOpeningContext).getRow()) }
        history.next().run { assertEquals("code1", (context as CellContext).rawValue) }
        history.next().run { assertEquals("name1", (context as CellContext).rawValue) }
        history.next().run { assertEquals("description1", (context as CellContext).rawValue) }
        history.next().run { assertEquals("manufacturer1", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(0, (context as RowClosingContext<*>).getRow())
            assertEquals(4, context.getCells().keys.size)
        }
        // Row 1
        history.next().run { assertEquals(1, (context as RowOpeningContext).getRow()) }
        history.next().run { assertEquals("code2", (context as CellContext).rawValue) }
        history.next().run { assertEquals("name2", (context as CellContext).rawValue) }
        history.next().run { assertEquals("description2", (context as CellContext).rawValue) }
        history.next().run { assertEquals("manufacturer2", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(1, (context as RowClosingContext<*>).getRow())
            assertEquals(4, context.getCells().keys.size)
        }
        // Footer row
        history.next().run { assertEquals(5, (context as RowOpeningContext).getRow()) }
        history.next().run { assertEquals("Custom row cell", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(5, (context as RowClosingContext<*>).getRow())
            assertEquals(1, context.getCells().keys.size)
        }
        // Columns closing.
        history.next().run { assertEquals(0, (context as ColumnClosingContext).columnIndex) }
        history.next().run { assertEquals(1, (context as ColumnClosingContext).columnIndex) }
        history.next().run { assertEquals(2, (context as ColumnClosingContext).columnIndex) }
        history.next().run { assertEquals(3, (context as ColumnClosingContext).columnIndex) }
        history.next().run { assertIs<TableClosingContext>(context) }
        assertFalse(history.hasNext())
    }

    @Test
    fun `should correctly resolve attributes on cell level`() {
        CustomTable {
            name = "Products table"
            attributes {
                text { fontColor = Colors.BLACK }
                background { color = Colors.WHITE }
            }
            columns {
                column(0) {
                    attributes {
                        text { fontColor = Colors.RED }
                    }
                }
                column(1) {}
            }
            rows {
                newRow {
                    attributes { text { fontColor = Colors.WHITE } }
                    cell {
                        attributes { text { fontColor = Colors.GREEN } }
                        value = "Green cell"
                    }
                    cell { value = "White cell" }
                }
                newRow {
                    attributes { text { fontColor = Colors.WHITE } }
                    cell {
                        value = "White cell"
                    }
                }
                newRow {
                    cell {
                        value = "Red cell"
                    }
                    cell {
                        value = "Black cell"
                    }
                }
            }
        }.export(TabulationFormat("spy"), Unit)

        val history = Spy.spy.readHistory()
        // Table
        history.next().run { assertEquals("Products table", (context as TableOpeningContext).getTableId()) }
        // Columns - Opening
        history.next().run { assertEquals(0, (context as ColumnOpeningContext).columnIndex) }
        history.next().run { assertEquals(1, (context as ColumnOpeningContext).columnIndex) }
        // Row 0
        history.next().run { assertEquals(0, (context as RowOpeningContext).getRow()) }
        history.next().run { assertEquals("Green cell", (context as CellContext).rawValue) }
        history.next().run { assertEquals(Colors.GREEN, (attribute as CellTextStylesAttribute).fontColor) }
        history.next().run { assertEquals(Colors.WHITE, (attribute as CellBackgroundAttribute).color) }
        history.next().run { assertEquals("White cell", (context as CellContext).rawValue) }
        history.next().run { assertEquals(Colors.WHITE, (attribute as CellTextStylesAttribute).fontColor) }
        history.next().run { assertEquals(Colors.WHITE, (attribute as CellBackgroundAttribute).color) }
        history.next().run {
            assertEquals(0, (context as RowClosingContext<*>).getRow())
            assertEquals(2, context.getCells().keys.size)
        }
        // Row 1
        history.next().run { assertEquals(1, (context as RowOpeningContext).getRow()) }
        history.next().run { assertEquals("White cell", (context as CellContext).rawValue) }
        history.next().run { assertEquals(Colors.WHITE, (attribute as CellTextStylesAttribute).fontColor) }
        history.next().run { assertEquals(Colors.WHITE, (attribute as CellBackgroundAttribute).color) }
        history.next().run {
            assertEquals(1, (context as RowClosingContext<*>).getRow())
            assertEquals(1, context.getCells().keys.size)
        }
        // Row 1
        history.next().run { assertEquals(2, (context as RowOpeningContext).getRow()) }
        history.next().run { assertEquals("Red cell", (context as CellContext).rawValue) }
        history.next().run { assertEquals(Colors.RED, (attribute as CellTextStylesAttribute).fontColor) }
        history.next().run { assertEquals(Colors.WHITE, (attribute as CellBackgroundAttribute).color) }
        history.next().run { assertEquals("Black cell", (context as CellContext).rawValue) }
        history.next().run { assertEquals(Colors.BLACK, (attribute as CellTextStylesAttribute).fontColor) }
        history.next().run { assertEquals(Colors.WHITE, (attribute as CellBackgroundAttribute).color) }
        history.next().run {
            assertEquals(2, (context as RowClosingContext<*>).getRow())
            assertEquals(2, context.getCells().keys.size)
        }
        // Columns closing.
        history.next().run { assertEquals(0, (context as ColumnClosingContext).columnIndex) }
        history.next().run { assertEquals(1, (context as ColumnClosingContext).columnIndex) }
        history.next().run { assertIs<TableClosingContext>(context) }
        assertFalse(history.hasNext())

    }

    @Test
    fun `should merge complex attributes from multiple levels`() {
        CustomTable {
            name = "Products table"
            attributes {
                text { fontColor = Colors.BLACK }
                columnWidth { px = 100 }
                rowHeight { px = 20 }
            }
            columns {
                column(0) {
                    attributes {
                        text { strikeout = true }
                        width { px = 45 }
                    }
                }
                column(1) {}
            }
            rows {
                newRow {
                    attributes {
                        text { weight = DefaultWeightStyle.BOLD }
                        height { px = 50 }
                    }
                    cell {
                        attributes { text { italic = true } }
                        value = "Black, strikeout, bold, italic cell"
                    }
                }
                newRow {
                    attributes { text { weight = DefaultWeightStyle.BOLD } }
                    cell {
                        value = "Black, strikeout, bold cell"
                    }
                }
                newRow {
                    cell {
                        value = "Black, strikeout cell"
                    }
                    cell {
                        value = "Black cell"
                    }
                }
            }
        }.export(TabulationFormat("spy"), Unit)
        val history = Spy.spy.readHistory()
        // Table
        history.next().run { assertEquals("Products table", (context as TableOpeningContext).getTableId()) }
        // Columns - Opening 0
        history.next().run { assertEquals(0, (context as ColumnOpeningContext).columnIndex) }
        // Column 0 attribute
        history.next().run { assertEquals(45, (attribute as ColumnWidthAttribute).px) }
        // Columns - Opening 1
        history.next().run { assertEquals(1, (context as ColumnOpeningContext).columnIndex) }
        // Column 1 attribute
        history.next().run { assertEquals(100, (attribute as ColumnWidthAttribute).px) }
        // Row 0
        history.next().run { assertEquals(0, (context as RowOpeningContext).getRow()) }
        history.next().run { assertEquals(50, (attribute as RowHeightAttribute).px) }
        // Row 0, cell 0
        history.next().run { assertEquals("Black, strikeout, bold, italic cell", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(Colors.BLACK, (attribute as CellTextStylesAttribute).fontColor)
            assertEquals(true, attribute.strikeout)
            assertEquals(DefaultWeightStyle.BOLD, attribute.weight)
            assertEquals(true, attribute.italic)
        }
        history.next().run { assertTrue(context is RowClosingContext<*>) }
        // Row 1, cell 0
        history.next().run { assertEquals(1, (context as RowOpeningContext).getRow()) }
        history.next().run { assertEquals(20, (attribute as RowHeightAttribute).px) }
        history.next().run { assertEquals("Black, strikeout, bold cell", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(Colors.BLACK, (attribute as CellTextStylesAttribute).fontColor)
            assertEquals(true, attribute.strikeout)
            assertEquals(DefaultWeightStyle.BOLD, attribute.weight)
            assertEquals(false, attribute.italic)
        }
        history.next().run { assertTrue(context is RowClosingContext<*>) }
        // Row 2, cell 0
        history.next().run { assertEquals(2, (context as RowOpeningContext).getRow()) }
        history.next().run { assertEquals(20, (attribute as RowHeightAttribute).px) }
        history.next().run { assertEquals("Black, strikeout cell", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(Colors.BLACK, (attribute as CellTextStylesAttribute).fontColor)
            assertEquals(true, attribute.strikeout)
            assertEquals(DefaultWeightStyle.NORMAL, attribute.weight)
            assertEquals(false, attribute.italic)
        }
        history.next().run { assertEquals("Black cell", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(Colors.BLACK, (attribute as CellTextStylesAttribute).fontColor)
            assertEquals(false, attribute.strikeout)
            assertEquals(DefaultWeightStyle.NORMAL, attribute.weight)
            assertEquals(false, attribute.italic)
        }
        history.next().run { assertIs<RowClosingContext<*>>(context) }
        // Columns - Closing 0
        history.next().run { assertEquals(0, (context as ColumnClosingContext).columnIndex) }
        // Columns - Closing 1
        history.next().run { assertEquals(1, (context as ColumnClosingContext).columnIndex) }
        history.next().run { assertIs<TableClosingContext>(context) }
        assertFalse(history.hasNext())
    }

}