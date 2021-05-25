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
import io.github.voytech.tabulate.model.id
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
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
            assertEquals(columns.size, 5)
            assertEquals("nr", columns[0].id.id, "nr 0 should have id 'nr'")
            assertEquals(Product::code.id(), columns[1].id.ref, "nr 1 should have id ref 'Product::code'")
            assertEquals(Product::name.id(), columns[2].id.ref, "nr 2 should have id ref 'Product::name'")
            assertEquals(Product::description.id(), columns[3].id.ref, "nr 3 should have id ref 'Product::description'")
            assertEquals(Product::manufacturer.id(), columns[4].id.ref, "nr 4 should have id ref 'Product::manufacturer'")
            assertEquals(columns.first().columnAttributes?.size, 1)
            assertEquals(columns.first().columnAttributes?.first(), ColumnWidthAttribute(px = 100))

            val zeroRows = getRowsAt(0)
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
    @Disabled("Fix inconsistent behaviour when registering rows where one is indexed and second uses predicate")
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
                    cells {
                        cell { value = "1" }
                        cell { value = "First item" }
                    }
                }
            }
        }.build()) {
            assertNotNull(this)
            assertEquals(rows!!.size, 2)
            rows!!.first().let { header ->
                assertEquals(2,header.cells!!.size)
                assertEquals("Code", header.cells!![ColumnKey(ref = Product::code)]!!.value)
                assertEquals("Description", header.cells!![ColumnKey(ref = Product::description)]!!.value)
            }
            rows!!.last().let { firstRow ->
                assertEquals(2,firstRow.cells!!.size)
                assertEquals("1", firstRow.cells!![ColumnKey(ref = Product::code)]!!.value)
                assertEquals("First item", firstRow.cells!![ColumnKey(ref = Product::description)]!!.value)
            }
        }
    }
}

