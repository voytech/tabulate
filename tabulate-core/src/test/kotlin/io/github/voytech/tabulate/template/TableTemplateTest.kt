package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.core.model.color.Colors
import io.github.voytech.tabulate.core.model.text.DefaultWeightStyle
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.components.table.template.export
import io.github.voytech.tabulate.components.table.template.tabulate
import io.github.voytech.tabulate.core.model.attributes.BackgroundAttribute
import io.github.voytech.tabulate.core.model.attributes.HeightAttribute
import io.github.voytech.tabulate.core.model.attributes.TextStylesAttribute
import io.github.voytech.tabulate.core.model.attributes.WidthAttribute
import io.github.voytech.tabulate.core.template.DocumentFormat
import io.github.voytech.tabulate.data.Product
import io.github.voytech.tabulate.data.Products
import io.github.voytech.tabulate.support.Spy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TableTemplateTest {

    @Test
    fun `should tabulate elements collection without additional features`() {

        Products.ITEMS.tabulate(DocumentFormat("spy"), Unit) {
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
        history.next().run { assertEquals("Products table", (context as TableStart).getSheetName()) }
        // Columns
        history.next().run { assertEquals(0, (context as ColumnStart).columnIndex) }
        history.next().run { assertEquals(1, (context as ColumnStart).columnIndex) }
        history.next().run { assertEquals(2, (context as ColumnStart).columnIndex) }
        history.next().run { assertEquals(3, (context as ColumnStart).columnIndex) }
        // Row 0
        history.next().run { assertEquals(0, (context as RowStart).getRow()) }
        history.next().run { assertEquals("code1", (context as CellContext).rawValue) }
        history.next().run { assertEquals("name1", (context as CellContext).rawValue) }
        history.next().run { assertEquals("description1", (context as CellContext).rawValue) }
        history.next().run { assertEquals("manufacturer1", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(0, (context as RowEnd<*>).getRow())
            assertEquals(4, context.getCells().keys.size)
        }
        // Row 1
        history.next().run { assertEquals(1, (context as RowStart).getRow()) }
        history.next().run { assertEquals("code2", (context as CellContext).rawValue) }
        history.next().run { assertEquals("name2", (context as CellContext).rawValue) }
        history.next().run { assertEquals("description2", (context as CellContext).rawValue) }
        history.next().run { assertEquals("manufacturer2", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(1, (context as RowEnd<*>).getRow())
            assertEquals(4, context.getCells().keys.size)
        }
        history.next().run { assertEquals(0, (context as ColumnEnd).columnIndex) }
        history.next().run { assertEquals(1, (context as ColumnEnd).columnIndex) }
        history.next().run { assertEquals(2, (context as ColumnEnd).columnIndex) }
        history.next().run { assertEquals(3, (context as ColumnEnd).columnIndex) }
        history.next().run { assertIs<TableEnd>(context) }
        assertFalse(history.hasNext())
    }

    @Test
    fun `should append header and footer rows`() {
        Products.ITEMS.tabulate(DocumentFormat("spy"), Unit) {
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
        history.next().run { assertEquals("Products table", (context as TableStart).getSheetName()) }
        // Columns - Opening
        history.next().run { assertEquals(0, (context as ColumnStart).columnIndex) }
        history.next().run { assertEquals(1, (context as ColumnStart).columnIndex) }
        history.next().run { assertEquals(2, (context as ColumnStart).columnIndex) }
        history.next().run { assertEquals(3, (context as ColumnStart).columnIndex) }

        // Header
        history.next().run { assertEquals(0, (context as RowStart).getRow()) }
        history.next().run { assertEquals("Code", (context as CellContext).rawValue) }
        history.next().run { assertEquals("Name", (context as CellContext).rawValue) }
        history.next().run { assertEquals("Description", (context as CellContext).rawValue) }
        history.next().run { assertEquals("Manufacturer", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(0, (context as RowEnd<*>).getRow())
            assertEquals(4, context.getCells().keys.size)
        }
        // Row 0
        history.next().run { assertEquals(1, (context as RowStart).getRow()) }
        history.next().run { assertEquals("code1", (context as CellContext).rawValue) }
        history.next().run { assertEquals("name1", (context as CellContext).rawValue) }
        history.next().run { assertEquals("description1", (context as CellContext).rawValue) }
        history.next().run { assertEquals("manufacturer1", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(1, (context as RowEnd<*>).getRow())
            assertEquals(4, context.getCells().keys.size)
        }
        // Row 1
        history.next().run { assertEquals(2, (context as RowStart).getRow()) }
        history.next().run { assertEquals("code2", (context as CellContext).rawValue) }
        history.next().run { assertEquals("name2", (context as CellContext).rawValue) }
        history.next().run { assertEquals("description2", (context as CellContext).rawValue) }
        history.next().run { assertEquals("manufacturer2", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(2, (context as RowEnd<*>).getRow())
            assertEquals(4, context.getCells().keys.size)
        }
        // Footer row
        history.next().run { assertEquals(3, (context as RowStart).getRow()) }
        history.next().run { assertEquals("First footer cell.", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(3, (context as RowEnd<*>).getRow())
            assertEquals(1, context.getCells().keys.size)
        }
        history.next().run { assertEquals(0, (context as ColumnEnd).columnIndex) }
        history.next().run { assertEquals(1, (context as ColumnEnd).columnIndex) }
        history.next().run { assertEquals(2, (context as ColumnEnd).columnIndex) }
        history.next().run { assertEquals(3, (context as ColumnEnd).columnIndex) }
        history.next().run { assertIs<TableEnd>(context) }
        assertFalse(history.hasNext())
    }

    @Test
    fun `should append custom row with higher offset`() {
        Products.ITEMS.tabulate(DocumentFormat("spy"), Unit) {
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
        history.next().run { assertEquals("Products table", (context as TableStart).getSheetName()) }
        // Columns - Opening
        history.next().run { assertEquals(0, (context as ColumnStart).columnIndex) }
        history.next().run { assertEquals(1, (context as ColumnStart).columnIndex) }
        history.next().run { assertEquals(2, (context as ColumnStart).columnIndex) }
        history.next().run { assertEquals(3, (context as ColumnStart).columnIndex) }
        // Row 0
        history.next().run { assertEquals(0, (context as RowStart).getRow()) }
        history.next().run { assertEquals("code1", (context as CellContext).rawValue) }
        history.next().run { assertEquals("name1", (context as CellContext).rawValue) }
        history.next().run { assertEquals("description1", (context as CellContext).rawValue) }
        history.next().run { assertEquals("manufacturer1", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(0, (context as RowEnd<*>).getRow())
            assertEquals(4, context.getCells().keys.size)
        }
        // Row 1
        history.next().run { assertEquals(1, (context as RowStart).getRow()) }
        history.next().run { assertEquals("code2", (context as CellContext).rawValue) }
        history.next().run { assertEquals("name2", (context as CellContext).rawValue) }
        history.next().run { assertEquals("description2", (context as CellContext).rawValue) }
        history.next().run { assertEquals("manufacturer2", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(1, (context as RowEnd<*>).getRow())
            assertEquals(4, context.getCells().keys.size)
        }
        // Footer row
        history.next().run { assertEquals(5, (context as RowStart).getRow()) }
        history.next().run { assertEquals("Custom row cell", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(5, (context as RowEnd<*>).getRow())
            assertEquals(1, context.getCells().keys.size)
        }
        // Columns closing.
        history.next().run { assertEquals(0, (context as ColumnEnd).columnIndex) }
        history.next().run { assertEquals(1, (context as ColumnEnd).columnIndex) }
        history.next().run { assertEquals(2, (context as ColumnEnd).columnIndex) }
        history.next().run { assertEquals(3, (context as ColumnEnd).columnIndex) }
        history.next().run { assertIs<TableEnd>(context) }
        assertFalse(history.hasNext())
    }

    @Test
    fun `should correctly resolve attributes on cell level`() {
        table {
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
        }.export(DocumentFormat("spy"), Unit)

        val history = Spy.spy.readHistory()
        // Table
        history.next().run { assertEquals("Products table", (context as TableStart).getSheetName()) }
        // Columns - Opening
        history.next().run { assertEquals(0, (context as ColumnStart).columnIndex) }
        history.next().run { assertEquals(1, (context as ColumnStart).columnIndex) }
        // Row 0
        history.next().run { assertEquals(0, (context as RowStart).getRow()) }
        history.next().run { assertEquals("Green cell", (context as CellContext).rawValue) }
        history.next().run { assertEquals(Colors.GREEN, (attribute as TextStylesAttribute).fontColor) }
        history.next().run { assertEquals(Colors.WHITE, (attribute as BackgroundAttribute).color) }
        history.next().run { assertEquals("White cell", (context as CellContext).rawValue) }
        history.next().run { assertEquals(Colors.WHITE, (attribute as TextStylesAttribute).fontColor) }
        history.next().run { assertEquals(Colors.WHITE, (attribute as BackgroundAttribute).color) }
        history.next().run {
            assertEquals(0, (context as RowEnd<*>).getRow())
            assertEquals(2, context.getCells().keys.size)
        }
        // Row 1
        history.next().run { assertEquals(1, (context as RowStart).getRow()) }
        history.next().run { assertEquals("White cell", (context as CellContext).rawValue) }
        history.next().run { assertEquals(Colors.WHITE, (attribute as TextStylesAttribute).fontColor) }
        history.next().run { assertEquals(Colors.WHITE, (attribute as BackgroundAttribute).color) }
        history.next().run {
            assertEquals(1, (context as RowEnd<*>).getRow())
            assertEquals(1, context.getCells().keys.size)
        }
        // Row 1
        history.next().run { assertEquals(2, (context as RowStart).getRow()) }
        history.next().run { assertEquals("Red cell", (context as CellContext).rawValue) }
        history.next().run { assertEquals(Colors.RED, (attribute as TextStylesAttribute).fontColor) }
        history.next().run { assertEquals(Colors.WHITE, (attribute as BackgroundAttribute).color) }
        history.next().run { assertEquals("Black cell", (context as CellContext).rawValue) }
        history.next().run { assertEquals(Colors.BLACK, (attribute as TextStylesAttribute).fontColor) }
        history.next().run { assertEquals(Colors.WHITE, (attribute as BackgroundAttribute).color) }
        history.next().run {
            assertEquals(2, (context as RowEnd<*>).getRow())
            assertEquals(2, context.getCells().keys.size)
        }
        // Columns closing.
        history.next().run { assertEquals(0, (context as ColumnEnd).columnIndex) }
        history.next().run { assertEquals(1, (context as ColumnEnd).columnIndex) }
        history.next().run { assertIs<TableEnd>(context) }
        assertFalse(history.hasNext())

    }

    @Test
    fun `should merge complex attributes from multiple levels`() {
        table {
            name = "Products table"
            attributes {
                text { fontColor = Colors.BLACK }
                columnWidth { 100.px() }
                rowHeight { 20.pt() }
            }
            columns {
                column(0) {
                    attributes {
                        text { strikeout = true }
                        width { 45.px() }
                    }
                }
                column(1) {}
            }
            rows {
                newRow {
                    attributes {
                        text { weight = DefaultWeightStyle.BOLD }
                        height { 50.pt() }
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
        }.export(DocumentFormat("spy"), Unit)
        val history = Spy.spy.readHistory()
        // Table
        history.next().run { assertEquals("Products table", (context as TableStart).getSheetName()) }
        // Columns - Opening 0
        history.next().run { assertEquals(0, (context as ColumnStart).columnIndex) }
        // Column 0 attribute
        history.next().run { assertEquals(45, (attribute as WidthAttribute).value.value.toInt()) }
        // Columns - Opening 1
        history.next().run { assertEquals(1, (context as ColumnStart).columnIndex) }
        // Column 1 attribute
        history.next().run { assertEquals(100, (attribute as WidthAttribute).value.value.toInt()) }
        // Row 0
        history.next().run { assertEquals(0, (context as RowStart).getRow()) }
        history.next().run { assertEquals(50, (attribute as HeightAttribute).value.value.toInt()) }
        // Row 0, cell 0
        history.next().run { assertEquals("Black, strikeout, bold, italic cell", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(Colors.BLACK, (attribute as TextStylesAttribute).fontColor)
            assertEquals(true, attribute.strikeout)
            assertEquals(DefaultWeightStyle.BOLD, attribute.weight)
            assertEquals(true, attribute.italic)
        }
        history.next().run { assertTrue(context is RowEnd<*>) }
        // Row 1, cell 0
        history.next().run { assertEquals(1, (context as RowStart).getRow()) }
        history.next().run { assertEquals(20, (attribute as HeightAttribute).value.value.toInt()) }
        history.next().run { assertEquals("Black, strikeout, bold cell", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(Colors.BLACK, (attribute as TextStylesAttribute).fontColor)
            assertEquals(true, attribute.strikeout)
            assertEquals(DefaultWeightStyle.BOLD, attribute.weight)
            assertEquals(false, attribute.italic)
        }
        history.next().run { assertTrue(context is RowEnd<*>) }
        // Row 2, cell 0
        history.next().run { assertEquals(2, (context as RowStart).getRow()) }
        history.next().run { assertEquals(20, (attribute as HeightAttribute).value.value.toInt()) }
        history.next().run { assertEquals("Black, strikeout cell", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(Colors.BLACK, (attribute as TextStylesAttribute).fontColor)
            assertEquals(true, attribute.strikeout)
            assertEquals(DefaultWeightStyle.NORMAL, attribute.weight)
            assertEquals(false, attribute.italic)
        }
        history.next().run { assertEquals("Black cell", (context as CellContext).rawValue) }
        history.next().run {
            assertEquals(Colors.BLACK, (attribute as TextStylesAttribute).fontColor)
            assertEquals(false, attribute.strikeout)
            assertEquals(DefaultWeightStyle.NORMAL, attribute.weight)
            assertEquals(false, attribute.italic)
        }
        history.next().run { assertIs<RowEnd<*>>(context) }
        // Columns - Closing 0
        history.next().run { assertEquals(0, (context as ColumnEnd).columnIndex) }
        // Columns - Closing 1
        history.next().run { assertEquals(1, (context as ColumnEnd).columnIndex) }
        history.next().run { assertIs<TableEnd>(context) }
        assertFalse(history.hasNext())
    }

}