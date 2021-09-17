package io.github.voytech.tabulate.resolver

import io.github.voytech.tabulate.api.builder.dsl.cell
import io.github.voytech.tabulate.api.builder.dsl.footer
import io.github.voytech.tabulate.api.builder.dsl.header
import io.github.voytech.tabulate.api.builder.dsl.table
import io.github.voytech.tabulate.data.Product
import io.github.voytech.tabulate.model.CellType
import io.github.voytech.tabulate.model.ColumnKey
import io.github.voytech.tabulate.template.context.DefaultSteps
import io.github.voytech.tabulate.template.iterators.EnumStepProvider
import io.github.voytech.tabulate.template.iterators.OperationContextIterator
import io.github.voytech.tabulate.template.resolvers.BufferingRowContextResolver
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class RowIteratorTest {

    @Test
    fun `should resolve AttributedRow to null if no table definition nor data is provided`() {
        val resolver = BufferingRowContextResolver(
            table<Product> {  }.build(),
            mutableMapOf()
        )
        val iterator = OperationContextIterator(resolver, EnumStepProvider(DefaultSteps::class.java))
        assertFalse(iterator.hasNext())
    }

    @Test
    fun `should resolve AttributedRow from custom row definition`() {
        mutableMapOf<String, Any>()
        val resolver = BufferingRowContextResolver(
            table<Product> {
                columns {
                    column(Product::code)
                }
                rows {
                    row {
                        cell {
                            value = "CustomProductCode"
                        }
                    }
                }
            }.build(),
            mutableMapOf()
        )
        val iterator = OperationContextIterator(resolver, EnumStepProvider(DefaultSteps::class.java))
        val resolvedIndexedAttributedRow = iterator.next()
        assertNotNull(resolvedIndexedAttributedRow)
        assertEquals(0, resolvedIndexedAttributedRow.rowIndex)
        with(resolvedIndexedAttributedRow) {
            assertEquals("CustomProductCode",rowCellValues[ColumnKey.field(Product::code)]!!.value.value)
            assertEquals(CellType.STRING,rowCellValues[ColumnKey.field(Product::code)]!!.value.type)
        }
    }

    @Test
    fun `should resolve AttributedRow from collection item and from custom items`() {
        val resolver = BufferingRowContextResolver(
            table<Product> {
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
        val iterator = OperationContextIterator(resolver, EnumStepProvider(DefaultSteps::class.java))
        resolver.buffer(Product(
            "code1",
            "name1",
            "description1",
            "manufacturer1",
            LocalDate.now(),
            BigDecimal.TEN
        ))
        val header = iterator.next()
        val value = iterator.next()
        val footer = iterator.next()
        assertFalse(iterator.hasNext())
        assertNotNull(header)
        assertEquals(0, header.rowIndex)
        with(header) {
            assertEquals("CODE",rowCellValues[ColumnKey.field(Product::code)]!!.value.value)
            assertEquals(CellType.STRING,rowCellValues[ColumnKey.field(Product::code)]!!.value.type)
        }
        assertNotNull(value)
        assertEquals(1, value.rowIndex)
        with(value) {
            assertEquals("code1",rowCellValues[ColumnKey.field(Product::code)]!!.value.value)
            assertEquals(CellType.STRING,rowCellValues[ColumnKey.field(Product::code)]!!.value.type)
        }
        assertNotNull(footer)
        assertEquals(2, footer.rowIndex)
        with(footer) {
            assertEquals("footer",rowCellValues[ColumnKey.field(Product::code)]!!.value.value)
            assertEquals(CellType.STRING,rowCellValues[ColumnKey.field(Product::code)]!!.value.type)
        }
    }

}