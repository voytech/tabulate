package io.github.voytech.tabulate.resolver

import io.github.voytech.tabulate.api.builder.dsl.createTableBuilder
import io.github.voytech.tabulate.api.builder.dsl.footer
import io.github.voytech.tabulate.api.builder.dsl.header
import io.github.voytech.tabulate.data.Product
import io.github.voytech.tabulate.model.ColumnKey
import io.github.voytech.tabulate.template.context.IndexMarker
import io.github.voytech.tabulate.template.context.RowIndex
import io.github.voytech.tabulate.template.resolvers.BufferingRowContextResolver
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.math.BigDecimal
import java.time.LocalDate

class RowResolverTest {

    @Test
    fun `should resolve AttributedRow to null if no table definition nor data is provided`() {
        val resolver = BufferingRowContextResolver(
            createTableBuilder<Product> {  }.build(),
            mutableMapOf()
        )
        val resolvedIndexedAttributedRow = resolver.resolve(RowIndex(0))
        assertNull(resolvedIndexedAttributedRow)
    }

    @ParameterizedTest
    @ValueSource(ints =  [0,  1] )
    fun `should resolve AttributedRow from custom row definition`(index: Int) {
        mutableMapOf<String, Any>()
        val resolver = BufferingRowContextResolver(
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
            mutableMapOf()
        )
        val resolvedIndexedAttributedRow = resolver.resolve(RowIndex(0))
        assertNotNull(resolvedIndexedAttributedRow)
        assertEquals(index, resolvedIndexedAttributedRow!!.index)
        with(resolvedIndexedAttributedRow.value) {
            assertEquals("CustomProductCode",rowCellValues[ColumnKey.field(Product::code)]!!.value.value)
        }
    }

    @Test
    fun `should resolve AttributedRow from collection item`() {
        val resolver = BufferingRowContextResolver(
            createTableBuilder<Product> {
                columns { column(Product::code) }
            }.build(),
            mutableMapOf()
        )
        resolver.buffer(Product(
            "code1",
            "name1",
            "description1",
            "manufacturer1",
            LocalDate.now(),
            BigDecimal.TEN
        ))
        val resolvedIndexedAttributedRow = resolver.resolve(RowIndex())
        assertNotNull(resolvedIndexedAttributedRow)
        assertEquals(0, resolvedIndexedAttributedRow!!.index)
        with(resolvedIndexedAttributedRow.value) {
            assertEquals("code1",rowCellValues[ColumnKey.field(Product::code)]!!.value.value)
        }
    }

    @Test
    fun `should resolve AttributedRow from collection item and from custom items`() {
        val resolver = BufferingRowContextResolver(
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
            mutableMapOf()
        )
        resolver.buffer(Product(
            "code1",
            "name1",
            "description1",
            "manufacturer1",
            LocalDate.now(),
            BigDecimal.TEN
        ))
        val header = resolver.resolve(RowIndex())
        val value = resolver.resolve(RowIndex(1))
        val footer = resolver.resolve(RowIndex(2,mapOf(Pair("TRAILING_ROWS",IndexMarker("TRAILING_ROWS",0)))))
        assertNotNull(header)
        assertEquals(0, header!!.index)
        with(header.value) {
            assertEquals("CODE",rowCellValues[ColumnKey.field(Product::code)]!!.value.value)
        }
        assertNotNull(value)
        assertEquals(1, value!!.index)
        with(value.value) {
            assertEquals("code1",rowCellValues[ColumnKey.field(Product::code)]!!.value.value)
        }
        assertNotNull(footer)
        assertEquals(2, footer!!.index)
        with(footer.value) {
            assertEquals("footer",rowCellValues[ColumnKey.field(Product::code)]!!.value.value)
        }
    }

}