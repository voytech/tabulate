package io.github.voytech.tabulate.dsl

import io.github.voytech.tabulate.api.builder.dsl.header
import io.github.voytech.tabulate.api.builder.dsl.style
import io.github.voytech.tabulate.api.builder.dsl.table
import io.github.voytech.tabulate.data.Product
import io.github.voytech.tabulate.model.ColumnKey
import io.github.voytech.tabulate.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.model.attributes.cell.Colors
import io.github.voytech.tabulate.model.attributes.cell.text
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.model.attributes.column.width
import io.github.voytech.tabulate.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.model.attributes.row.height
import io.github.voytech.tabulate.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.model.attributes.table.template
import io.github.voytech.tabulate.model.id
import io.github.voytech.tabulate.template.context.RowIndex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class DslBuilderTest {

    @Test
    fun `should describe table model of columns only`() {
        with(table<Product> {
            name = "Products table"
            columns {
                column("nr")
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
            }
        }.build()) {
            assertNotNull(this)
            assertEquals(columns.size, 5)
            assertEquals("nr", columns[0].id.id, "nr 0 should have id 'nr'")
            assertEquals(Product::code.id(), columns[1].id.ref, "nr 1 should have id ref 'Product::code'")
            assertEquals(Product::name.id(), columns[2].id.ref, "nr 2 should have id ref 'Product::name'")
            assertEquals(Product::description.id(), columns[3].id.ref, "nr 3 should have id ref 'Product::description'")
            assertEquals(Product::manufacturer.id(), columns[4].id.ref, "nr 4 should have id ref 'Product::manufacturer'")
        }
    }

    @Test
    fun `should describe table model of columns and rows`() {
        with(table<Product> {
            name = "Products table"
            firstRow = 1
            firstColumn = 1
            columns {
                column("nr") { attributes { width { px = 100 } } }
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
            }
            rows {
                row {
                    attributes { height { px = 20 } }
                    cells {
                        cell {
                            attributes { text { fontFamily = "Courier" } }
                            value = "cell value at: 0.0"
                        }
                    }
                }
            }
        }.build()) {
            assertNotNull(this)
            assertEquals("Products table",name)
            assertEquals(1, firstColumn)
            assertEquals(1, firstRow)
            assertEquals( 5,columns.size)
            assertEquals("nr", columns[0].id.id, "nr 0 should have id 'nr'")
            assertEquals(Product::code.id(), columns[1].id.ref, "nr 1 should have id ref 'Product::code'")
            assertEquals(Product::name.id(), columns[2].id.ref, "nr 2 should have id ref 'Product::name'")
            assertEquals(Product::description.id(), columns[3].id.ref, "nr 3 should have id ref 'Product::description'")
            assertEquals(Product::manufacturer.id(), columns[4].id.ref, "nr 4 should have id ref 'Product::manufacturer'")
            assertEquals(columns.first().columnAttributes?.size, 1)
            assertEquals(columns.first().columnAttributes?.first(), ColumnWidthAttribute(px = 100))

            val zeroRows = getRowsAt(RowIndex(0))
            assertEquals(zeroRows?.size, 1)
            assertEquals(rows?.size, 1)
            with(zeroRows?.first()!!) {
                assertNotNull(cells)
                assertEquals(rowAttributes?.size, 1)
                assertEquals(rowAttributes?.first(), RowHeightAttribute(px = 20))
                assert(cells?.containsKey(ColumnKey("nr")) ?: false)
                with(cells?.get(ColumnKey("nr"))!!) {
                    assertEquals(resolveRawValue(), "cell value at: 0.0")
                    assertEquals(cellAttributes?.size, 1)
                    assertEquals(cellAttributes?.first(), CellTextStylesAttribute(fontFamily = "Courier"))
                }
            }
        }
    }

    @Test
    fun `should define style attributes on column level`() {
        with(table<Product> {
            name = "Products table"
            columns {
                column(Product::code) {
                    style {
                        text {
                            fontColor = Colors.AERO
                            fontFamily = "Times New Roman"
                            fontSize = 12
                        }
                    }
                }
            }
        }.build()) {
            assertNotNull(this)
            assertEquals(columns.size, 1)
            columns.first().let { column ->
                assertEquals(Product::code.id(), column.id.ref, "nr 1 should have id ref 'Product::code'")
                assertNotNull(column.cellAttributes)
                assertEquals(1, column.cellAttributes!!.size)
                (column.cellAttributes?.first() as CellTextStylesAttribute).let { attribute ->
                    assertEquals(Colors.AERO, attribute.fontColor)
                    assertEquals(12, attribute.fontSize)
                    assertEquals("Times New Roman", attribute.fontFamily)
                }
            }
        }
    }


    @Test
    fun `should define custom attributes on column level`() {
        with(table<Product> {
            name = "Products table"
            columns {
                column(Product::code) {
                    attributes {
                        text {
                            fontColor = Colors.AERO
                            fontFamily = "Times New Roman"
                            fontSize = 12
                        }
                    }
                }
            }
        }.build()) {
            assertNotNull(this)
            assertEquals(columns.size, 1)
            columns.first().let { column ->
                assertEquals(Product::code.id(), column.id.ref, "nr 1 should have id ref 'Product::code'")
                assertNotNull(column.cellAttributes)
                assertEquals(1, column.cellAttributes!!.size)
                (column.cellAttributes?.first() as CellTextStylesAttribute).let { attribute ->
                    assertEquals(Colors.AERO, attribute.fontColor)
                    assertEquals(12, attribute.fontSize)
                    assertEquals("Times New Roman", attribute.fontFamily)
                }
            }
        }
    }

    @Test
    fun `should define custom attributes on all levels`() {
        with(table<Product> {
            name = "Products table"
            columns {
                column(Product::code) {
                    attributes {
                        width {
                            px = 100
                        }
                        text {
                            fontColor = Colors.AERO
                            fontFamily = "Times New Roman"
                            fontSize = 12
                        }
                    }
                }
            }
            rows {
                row {
                    attributes {
                        height {
                            px = 100
                        }
                    }
                    cell {
                        attributes {
                            text {
                                fontColor = Colors.BLACK
                            }
                        }
                    }
                }
            }
            attributes {
                template { fileName = "some_template_file.ext" }
            }
        }.build()) {
            assertNotNull(this)
            assertEquals(1, columns.size )
            assertEquals(Product::code.id(), columns[0].id.ref, "nr 1 should have id ref 'Product::code'")
            assertEquals(1, columns.first().columnAttributes?.size)
            assertEquals(1, columns.first().cellAttributes?.size)
            assertEquals(ColumnWidthAttribute(px = 100), columns.first().columnAttributes?.first())
            assertEquals(
                CellTextStylesAttribute(fontColor = Colors.AERO, fontFamily = "Times New Roman", fontSize = 12),
                columns.first().cellAttributes?.first()
            )
            val zeroRows = getRowsAt(RowIndex(0))
            assertEquals(zeroRows?.size, 1)
            assertEquals(rows?.size, 1)
            with(zeroRows?.first()!!) {
                assertNotNull(cells)
                assertEquals(1,rowAttributes?.size)
                assertEquals(RowHeightAttribute(px = 100), rowAttributes?.first())
                assertEquals(1,cells?.size)
                with(cells?.values?.first()) {
                    assertEquals(this?.cellAttributes?.size, 1)
                    assertEquals(this?.cellAttributes?.first(), CellTextStylesAttribute(fontColor = Colors.BLACK))
                }
            }
            assertEquals(1, tableAttributes!!.size)
            assertEquals(TemplateFileAttribute(fileName = "some_template_file.ext"),tableAttributes!!.first())
        }
    }

    @Test
    fun `should define table with header`() {
        with(table<Product> {
            name = "Products table"
            columns {
                column(Product::code)
                column(Product::description)
            }
            rows {
                header("Code", "Description")
                row {
                    cell { value = "1" }
                    cell { value = "First item" }
                }
            }
        }.build()) {
            assertNotNull(this)
            assertEquals(rows!!.size, 2)
            rows!!.first().let { header ->
                assertEquals(2,header.cells!!.size)
                assertEquals("Code", header.cells!![ColumnKey(ref = Product::code.id())]!!.value)
                assertEquals("Description", header.cells!![ColumnKey(ref = Product::description.id())]!!.value)
            }
            rows!!.last().let { firstRow ->
                assertEquals(2,firstRow.cells!!.size)
                assertEquals("1", firstRow.cells!![ColumnKey(ref = Product::code.id())]!!.value)
                assertEquals("First item", firstRow.cells!![ColumnKey(ref = Product::description.id())]!!.value)
            }
        }
    }

    @Test
    fun `should define table with attributed header`() {
        with(table<Product> {
            name = "Products table"
            columns {
                column(Product::code)
                column(Product::description)
            }
            rows {
                header {
                    columnTitle(Product::code) {
                        value = "Code"
                        attributes {
                            text { fontColor = Colors.BLACK }
                        }
                    }
                    columnTitle(Product::description) {
                        value = "Description"
                        attributes {
                            text { fontColor = Colors.BLACK }
                        }
                    }
                    attributes {
                        height { px = 100 }
                    }
                }
                row {
                    cell { value = "1" }
                    cell { value = "First item" }
                }
            }
        }.build()) {
            assertNotNull(this)
            assertEquals(rows!!.size, 2)
            rows!!.first().let { header ->
                assertEquals(2,header.cells!!.size)
                assertEquals("Code", header.cells!![ColumnKey(ref = Product::code.id())]!!.value)
                assertEquals("Description", header.cells!![ColumnKey(ref = Product::description.id())]!!.value)
                assertEquals(1, header.rowAttributes!!.size)
                assertEquals(RowHeightAttribute(px = 100), header.rowAttributes!!.first())
                header.cells!!.let { cells ->
                    assertEquals(CellTextStylesAttribute(fontColor = Colors.BLACK), cells[ColumnKey(ref = Product::code.id())]!!.cellAttributes!!.first())
                    assertEquals(CellTextStylesAttribute(fontColor = Colors.BLACK), cells[ColumnKey(ref = Product::description.id())]!!.cellAttributes!!.first())
                }
            }
            rows!!.last().let { firstRow ->
                assertEquals(2,firstRow.cells!!.size)
                assertEquals("1", firstRow.cells!![ColumnKey(ref = Product::code.id())]!!.value)
                assertEquals("First item", firstRow.cells!![ColumnKey(ref = Product::description.id())]!!.value)
            }
        }
    }
}

