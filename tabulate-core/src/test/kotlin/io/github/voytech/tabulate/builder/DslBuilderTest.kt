package io.github.voytech.tabulate.builder

import io.github.voytech.tabulate.components.document.api.builder.dsl.createDocument
import io.github.voytech.tabulate.components.table.api.builder.dsl.createTableBuilder
import io.github.voytech.tabulate.components.table.api.builder.dsl.header
import io.github.voytech.tabulate.components.table.api.builder.dsl.table
import io.github.voytech.tabulate.components.table.model.ColumnKey
import io.github.voytech.tabulate.components.table.model.id
import io.github.voytech.tabulate.data.Product
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class DslBuilderTest {

    @Test
    fun `should describe table model of columns only`() {
        with(createTableBuilder<Product> {
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
            assertEquals("nr", columns[0].id.name, "nr 0 should have id 'nr'")
            assertEquals(Product::code.id(), columns[1].id.property, "nr 1 should have id ref 'Product::code'")
            assertEquals(Product::name.id(), columns[2].id.property, "nr 2 should have id ref 'Product::name'")
            assertEquals(Product::description.id(), columns[3].id.property, "nr 3 should have id ref 'Product::description'")
            assertEquals(Product::manufacturer.id(), columns[4].id.property, "nr 4 should have id ref 'Product::manufacturer'")
        }
    }

    /* TODO needs rewrite
    @Test
    fun `should describe table model of columns and rows`() {
        with(createTableBuilder<Product> {
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
                newRow {
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
            assertEquals("nr", columns[0].id.name, "nr 0 should have id 'nr'")
            assertEquals(Product::code.id(), columns[1].id.property, "nr 1 should have id ref 'Product::code'")
            assertEquals(Product::name.id(), columns[2].id.property, "nr 2 should have id ref 'Product::name'")
            assertEquals(Product::description.id(), columns[3].id.property, "nr 3 should have id ref 'Product::description'")
            assertEquals(Product::manufacturer.id(), columns[4].id.property, "nr 4 should have id ref 'Product::manufacturer'")
            assertEquals(columns.first().columnAttributes?.size, 1)
            assertEquals(columns.first().columnAttributes?.get(ColumnWidthAttribute::class.java), ColumnWidthAttribute(px = 100))

            val indexedRows = IndexedTableRows(this)
            val zeroRows = indexedRows.getRowsAt(RowIndex(0))
            assertEquals(zeroRows?.size, 1)
            assertEquals(rows?.size, 1)
            with(zeroRows?.first()!!) {
                assertNotNull(cells)
                assertEquals(rowAttributes?.size, 1)
                assertEquals(rowAttributes?.get(RowHeightAttribute::class.java), RowHeightAttribute(px = 20))
                assert(cells?.containsKey(ColumnKey("nr")) ?: false)
                with(cells?.get(ColumnKey("nr"))!!) {
                    assertEquals(resolveRawValue(), "cell value at: 0.0")
                    assertEquals(cellAttributes?.size, 1)
                    assertEquals(cellAttributes?.get(CellTextStylesAttribute::class.java), CellTextStylesAttribute(fontFamily = "Courier"))
                }
            }
        }
    }

    @Test
    fun `should define custom attributes on column level`() {
        with(createTableBuilder<Product> {
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
                assertEquals(Product::code.id(), column.id.property, "nr 1 should have id ref 'Product::code'")
                assertNotNull(column.cellAttributes)
                assertEquals(1, column.cellAttributes!!.size)
                column.cellAttributes[CellTextStylesAttribute::class.java].let { attribute ->
                    assertEquals(Colors.AERO, attribute?.fontColor)
                    assertEquals(12, attribute?.fontSize)
                    assertEquals("Times New Roman", attribute?.fontFamily)
                }
            }
        }
    }

    @Test
    fun `should define custom attributes on all levels`() {
        with(createTableBuilder<Product> {
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
                newRow {
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
            assertEquals(Product::code.id(), columns[0].id.property, "nr 1 should have id ref 'Product::code'")
            assertEquals(1, columns.first().columnAttributes?.size)
            assertEquals(1, columns.first().cellAttributes?.size)
            assertEquals(ColumnWidthAttribute(px = 100), columns.first().columnAttributes?.get(ColumnWidthAttribute::class.java))
            assertEquals(
                CellTextStylesAttribute(fontColor = Colors.AERO, fontFamily = "Times New Roman", fontSize = 12),
                columns.first().cellAttributes?.get(CellTextStylesAttribute::class.java)
            )
            val indexedRows = IndexedTableRows(this)
            val zeroRows = indexedRows.getRowsAt(RowIndex(0))
            assertEquals(zeroRows?.size, 1)
            assertEquals(rows?.size, 1)
            with(zeroRows?.first()!!) {
                assertNotNull(cells)
                assertEquals(1,rowAttributes?.size)
                assertEquals(RowHeightAttribute(px = 100), rowAttributes?.get(RowHeightAttribute::class.java))
                assertEquals(1,cells?.size)
                with(cells?.values?.first()) {
                    assertEquals(this?.cellAttributes?.size, 1)
                    assertEquals(this?.cellAttributes?.get(CellTextStylesAttribute::class.java), CellTextStylesAttribute(fontColor = Colors.BLACK))
                }
            }
            assertEquals(1, tableAttributes!!.size)
            assertEquals(TemplateFileAttribute(fileName = "some_template_file.ext"), tableAttributes
                .get(TemplateFileAttribute::class.java))
        }
    }
*/
    @Test
    fun `should define table with header`() {
        with(createTableBuilder<Product> {
            name = "Products table"
            columns {
                column(Product::code)
                column(Product::description)
            }
            rows {
                header("Code", "Description")
                newRow {
                    cell { value = "1" }
                    cell { value = "First item" }
                }
            }
        }.build()) {
            assertNotNull(this)
            assertEquals(rows!!.size, 2)
            rows.first().let { header ->
                assertEquals(2,header.cells!!.size)
                assertEquals("Code", header.cells[ColumnKey(property = Product::code.id())]!!.value)
                assertEquals("Description", header.cells[ColumnKey(property = Product::description.id())]!!.value)
            }
            rows.last().let { firstRow ->
                assertEquals(2,firstRow.cells!!.size)
                assertEquals("1", firstRow.cells[ColumnKey(property = Product::code.id())]!!.value)
                assertEquals("First item", firstRow.cells[ColumnKey(property = Product::description.id())]!!.value)
            }
        }
    }

    @Test
    fun `should define table with header using row predicate literal notation`() {
        with(createTableBuilder<Product> {
            name = "Products table"
            columns {
                column(Product::code)
                column(Product::description)
            }
            rows {
                atIndex { header() } newRow {
                    cell { value = "Code" }
                    cell { value = "Description" }
                }
                newRow {
                    cell { value = "1" }
                    cell { value = "First item" }
                }
            }
        }.build()) {
            assertNotNull(this)
            assertEquals(rows!!.size, 2)
            rows.first().let { header ->
                assertEquals(2,header.cells!!.size)
                assertEquals("Code", header.cells[ColumnKey(property = Product::code.id())]!!.value)
                assertEquals("Description", header.cells[ColumnKey(property = Product::description.id())]!!.value)
            }
            rows.last().let { firstRow ->
                assertEquals(2,firstRow.cells!!.size)
                assertEquals("1", firstRow.cells[ColumnKey(property = Product::code.id())]!!.value)
                assertEquals("First item", firstRow.cells[ColumnKey(property = Product::description.id())]!!.value)
            }
        }
    }

    /* TODO needs rewrite.
    @Test
    fun `should define table with attributed header`() {
        with(createTableBuilder<Product> {
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
                newRow {
                    cell { value = "1" }
                    cell { value = "First item" }
                }
            }
        }.build()) {
            assertNotNull(this)
            assertEquals(2, rows!!.size)
            rows.first().let { header ->
                assertEquals(2,header.cells!!.size)
                assertEquals("Code", header.cells[ColumnKey(property = Product::code.id())]!!.value)
                assertEquals("Description", header.cells[ColumnKey(property = Product::description.id())]!!.value)
                assertEquals(1, header.rowAttributes!!.size)
                assertEquals(RowHeightAttribute(px = 100), header.rowAttributes.get(RowHeightAttribute::class.java))
                header.cells.let { cells ->
                    assertEquals(CellTextStylesAttribute(fontColor = Colors.BLACK),
                        cells[ColumnKey(property = Product::code.id())]!!.cellAttributes!![CellTextStylesAttribute::class.java]
                    )
                    assertEquals(CellTextStylesAttribute(fontColor = Colors.BLACK), cells[ColumnKey(property = Product::description.id())]!!.cellAttributes!![CellTextStylesAttribute::class.java])
                }
            }
            rows.last().let { firstRow ->
                assertEquals(2,firstRow.cells!!.size)
                assertEquals("1", firstRow.cells[ColumnKey(property = Product::code.id())]!!.value)
                assertEquals("First item", firstRow.cells[ColumnKey(property = Product::description.id())]!!.value)
            }
        }
    }
    */


    @Test
    fun `should describe table model as part of document`() {
        val document = createDocument {
            table<Product> {
                name = "Products table"
                columns {
                    column("nr")
                    column(Product::code)
                    column(Product::name)
                    column(Product::description)
                    column(Product::manufacturer)
                }
            }
            table<Unit> {
                name = "Other table"
                columns {
                    column("nr")
                }
            }
        }
        assertNotNull(document)
    }
}

