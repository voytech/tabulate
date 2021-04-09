package pl.voytech.exporter.core.dsl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import pl.voytech.exporter.core.api.builder.dsl.table
import pl.voytech.exporter.core.model.ColumnKey
import pl.voytech.exporter.core.model.attributes.cell.text
import pl.voytech.exporter.core.model.attributes.column.width
import pl.voytech.exporter.core.model.attributes.row.height
import pl.voytech.exporter.data.Product

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
            assertEquals("nr", columns[0].id.id,"nr 0 should have id 'nr'")
            assertEquals(Product::code, columns[1].id.ref,"nr 1 should have id ref 'Product::code'")
            assertEquals(Product::name, columns[2].id.ref,"nr 2 should have id ref 'Product::name'")
            assertEquals(Product::description, columns[3].id.ref,"nr 3 should have id ref 'Product::description'")
            assertEquals(Product::manufacturer, columns[4].id.ref,"nr 4 should have id ref 'Product::manufacturer'")
        }
    }

    @Test
    fun `should describe table model of columns and rows`() {
        with(table<Product> {
            name = "Products table"
            columns {
                column("nr") { attributes(width { px = 100 }) }
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
            }
            rows {
                row {
                    attributes(height { px = 20 })
                    cells {
                        cell {
                            attributes(text { fontFamily = "Courier" })
                            value = "cell value at: 0.0"
                        }
                    }
                }
            }
        }.build()) {
            assertNotNull(this)
            assertEquals(columns.size, 5)
            assertEquals("nr", columns[0].id.id,"nr 0 should have id 'nr'")
            assertEquals(Product::code, columns[1].id.ref,"nr 1 should have id ref 'Product::code'")
            assertEquals(Product::name, columns[2].id.ref,"nr 2 should have id ref 'Product::name'")
            assertEquals(Product::description, columns[3].id.ref,"nr 3 should have id ref 'Product::description'")
            assertEquals(Product::manufacturer, columns[4].id.ref,"nr 4 should have id ref 'Product::manufacturer'")
            assertEquals(columns.first().columnAttributes?.size, 1)
            assertEquals(columns.first().columnAttributes?.first(), width { px = 100 })

            val zeroRows = getRowsAt(0)
            assertEquals(zeroRows?.size, 1)
            assertEquals(rows?.size, 1)
            with(zeroRows?.first()!!) {
                assertNotNull(cells)
                assertEquals(rowAttributes?.size, 1)
                assertEquals(rowAttributes?.first(), height { px = 20 })
                assert(cells?.containsKey(ColumnKey("nr")) ?: false)
                with(cells?.get(ColumnKey("nr"))!!) {
                    assertEquals(resolveRawValue(), "cell value at: 0.0")
                    assertEquals(cellAttributes?.size, 1)
                    assertEquals(cellAttributes?.first(), text { fontFamily = "Courier" })
                }
            }
        }
    }
}

