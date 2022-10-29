package io.github.voytech.tabulate.resolver

import io.github.voytech.tabulate.components.table.api.builder.dsl.createTableBuilder
import io.github.voytech.tabulate.components.table.api.builder.dsl.footer
import io.github.voytech.tabulate.components.table.api.builder.dsl.header
import io.github.voytech.tabulate.data.Product
import io.github.voytech.tabulate.components.table.model.ColumnKey
import io.github.voytech.tabulate.components.table.template.AccumulatingRowContextResolver
import io.github.voytech.tabulate.components.table.template.OverflowOffsets
import io.github.voytech.tabulate.components.table.template.RowIndex
import io.github.voytech.tabulate.components.table.template.Step
import io.github.voytech.tabulate.support.success
import io.github.voytech.tabulate.support.successfulRowComplete
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.math.BigDecimal
import java.time.LocalDate

class RowResolverTest {

    @Test
    fun `should resolve AttributedRow to null if no table definition nor data is provided`() {
        val resolver = AccumulatingRowContextResolver(
            createTableBuilder<Product> {  }.build(),
            mutableMapOf(), OverflowOffsets(), successfulRowComplete()
        )
        val resolvedIndexedAttributedRow = resolver.resolve(RowIndex(0))
        assertNull(resolvedIndexedAttributedRow)
    }

    @ParameterizedTest
    @ValueSource(ints =  [0,  1] )
    fun `should resolve AttributedRow from custom row definition`(index: Int) {
        mutableMapOf<String, Any>()
        val resolver = AccumulatingRowContextResolver(
            createTableBuilder<Product> {
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
            }.build(),
            mutableMapOf(),
            OverflowOffsets(),
            successfulRowComplete()
        )
        val resolvedIndexedAttributedRow = resolver.resolve(RowIndex(0))
        assertNotNull(resolvedIndexedAttributedRow)
        assertEquals(index, resolvedIndexedAttributedRow!!.rowIndex.value)
        with(resolvedIndexedAttributedRow.result) {
            assertEquals("CustomProductCode",success().rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
        }
    }

    @Test
    fun `should resolve AttributedRow from collection item`() {
        val resolver = AccumulatingRowContextResolver(
            createTableBuilder<Product> {
                columns { column(Product::code) }
            }.build(),
            mutableMapOf(),
            OverflowOffsets(),
            successfulRowComplete()
        )
        resolver.append(Product(
            "code1",
            "name1",
            "description1",
            "manufacturer1",
            LocalDate.now(),
            BigDecimal.TEN
        ))
        val resolvedIndexedAttributedRow = resolver.resolve(RowIndex())
        assertNotNull(resolvedIndexedAttributedRow)
        assertEquals(0, resolvedIndexedAttributedRow!!.rowIndex.value)
        with(resolvedIndexedAttributedRow.result) {
            assertEquals("code1",success().rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
        }
    }

    @Test
    fun `should resolve AttributedRow from collection item and from custom items`() {
        val resolver = AccumulatingRowContextResolver(
            createTableBuilder<Product> {
                columns { column(Product::code) }
                rows {
                    header("CODE")
                    footer {
                        cell {
                            value = "footer"
                        }
                    }
                }
            }.build(),
            mutableMapOf(),
            OverflowOffsets(),
            successfulRowComplete()
        )
        resolver.append(Product(
            "code1",
            "name1",
            "description1",
            "manufacturer1",
            LocalDate.now(),
            BigDecimal.TEN
        ))
        val header = resolver.resolve(RowIndex())
        val value = resolver.resolve(RowIndex(1))
        val footer = resolver.resolve(RowIndex(2, Step("TRAILING_ROWS",0,0)))
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