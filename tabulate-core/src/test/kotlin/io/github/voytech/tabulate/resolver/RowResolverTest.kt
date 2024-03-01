package io.github.voytech.tabulate.resolver

import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.table.api.builder.dsl.createTableBuilder
import io.github.voytech.tabulate.components.table.model.ColumnKey
import io.github.voytech.tabulate.components.table.template.RowIndex
import io.github.voytech.tabulate.components.table.template.Step
import io.github.voytech.tabulate.components.table.template.export
import io.github.voytech.tabulate.core.DocumentFormat
import io.github.voytech.tabulate.data.Product
import io.github.voytech.tabulate.support.Spy
import io.github.voytech.tabulate.support.createRowsRenderer
import io.github.voytech.tabulate.support.success
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.math.BigDecimal
import java.time.LocalDate

class RowResolverTest {

    @Test
    fun `should resolve AttributedRow to null if no table definition nor data is provided`() {
        val table = createTableBuilder<Product> {  }.build()
        val renderer = table.createRowsRenderer(mutableMapOf())
        val resolvedIndexedAttributedRow = renderer.resolve(RowIndex(0))
        assertNull(resolvedIndexedAttributedRow)
    }

    @ParameterizedTest
    @ValueSource(ints =  [0,  1] )
    fun `should resolve AttributedRow from custom row definition`(index: Int) {
        typedTable {
            columns {
                column(Product::code)
            }
            rows {
                newRow(index) {
                    cell {
                        value = "CustomProductCode"
                    }
                }
            }
        }.export(DocumentFormat.format("spy"),Unit)
        val iterator = Spy.spy.readHistory()
        assertTrue(iterator.hasNext())
        while (iterator.hasNext()) {
            val renderable = iterator.next()
            println(renderable)
        }
    }

    @Test
    fun `should resolve AttributedRow from collection item`() {
        val table = createTableBuilder {
            columns { column(Product::code) }
        }.build()
        val attribs = mutableMapOf<String, Any>()
        val renderer = table.createRowsRenderer(attribs, listOf(Product(
            "code1",
            "name1",
            "description1",
            "manufacturer1",
            LocalDate.now(),
            BigDecimal.TEN
        )))
        val resolvedIndexedAttributedRow = renderer.resolve(RowIndex())
        assertNotNull(resolvedIndexedAttributedRow)
        assertEquals(0, resolvedIndexedAttributedRow!!.rowIndex.value)
        with(resolvedIndexedAttributedRow.result) {
            assertEquals("code1",success().rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
        }
    }

    @Test
    fun `should resolve AttributedRow from collection item and from custom items`() {
        val table = createTableBuilder {
            columns { column(Product::code) }
            rows {
                header("CODE")
                footer {
                    cell {
                        value = "footer"
                    }
                }
            }
        }.build()
        val attribs = mutableMapOf<String, Any>()
        val renderer = table.createRowsRenderer(attribs, listOf(Product(
            "code1",
            "name1",
            "description1",
            "manufacturer1",
            LocalDate.now(),
            BigDecimal.TEN
        )))

        val header = renderer.resolve(RowIndex())
        val value = renderer.resolve(RowIndex(1))
        val footer = renderer.resolve(RowIndex(2, Step("TRAILING_ROWS",0,0)))
        assertNotNull(header)
        assertEquals(0, header!!.rowIndex.value)
        with(header.result) {
            assertEquals("CODE",success().rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
        }
        assertNotNull(value)
        assertEquals(1, value!!.rowIndex.value)
        with(value.result) {
            assertEquals("code1",success().rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
        }
        assertNotNull(footer)
        assertEquals(2, footer!!.rowIndex.value)
        with(footer.result) {
            assertEquals("footer",success().rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
        }
    }

}