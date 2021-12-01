package io.github.voytech.tabulate.resolver

import io.github.voytech.tabulate.api.builder.dsl.TableBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.createTableBuilder
import io.github.voytech.tabulate.api.builder.dsl.footer
import io.github.voytech.tabulate.api.builder.dsl.header
import io.github.voytech.tabulate.data.Product
import io.github.voytech.tabulate.model.ColumnKey
import io.github.voytech.tabulate.template.context.AdditionalSteps
import io.github.voytech.tabulate.template.iterators.EnumStepProvider
import io.github.voytech.tabulate.template.iterators.RowContextIterator
import io.github.voytech.tabulate.template.resolvers.BufferingRowContextResolver
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class RowIteratorTest {

    private data class Wrapper<T>(
        val iterator: RowContextIterator<T>,
        val resolver: BufferingRowContextResolver<T>,
        val customAttributes: Map<String, Any>
    )

    private fun <T> createDefaultIterator(block: TableBuilderApi<T>.() -> Unit): Wrapper<T> =
        mutableMapOf<String, Any>().let {
            it to BufferingRowContextResolver(createTableBuilder(block).build(), it)
        }.let {
            Wrapper(
                iterator = RowContextIterator(it.second, EnumStepProvider(AdditionalSteps::class.java)),
                resolver = it.second,
                customAttributes = it.first
            )
        }


    @Test
    fun `should resolve AttributedRow to null if no table definition nor data is provided`() {
        val wrapper = createDefaultIterator<Product> {  }
        assertFalse(wrapper.iterator.hasNext())
    }

    @Test
    fun `should resolve AttributedRow from custom row definition`() {
        val wrapper = createDefaultIterator<Product> {
                columns {
                    column(Product::code)
                }
                rows {
                    newRow {
                        cell {
                            value = "CustomProductCode"
                        }
                    }
                }
            }
        val resolvedIndexedAttributedRow = wrapper.iterator.next()
        assertNotNull(resolvedIndexedAttributedRow)
        assertEquals(0, resolvedIndexedAttributedRow.rowIndex)
        with(resolvedIndexedAttributedRow) {
            assertEquals("CustomProductCode",rowCellValues[ColumnKey.field(Product::code)]!!.value.value)
        }
    }

    @Test
    fun `should resolve AttributedRow from collection item and from custom items`() {
        val wrapper = createDefaultIterator<Product> {
            columns { column(Product::code) }
            rows {
                header("CODE")
                footer {
                    cell {
                        value = "footer"
                    }
                }
            }
        }
        wrapper.resolver.buffer(Product(
            "code1",
            "name1",
            "description1",
            "manufacturer1",
            LocalDate.now(),
            BigDecimal.TEN
        ))
        val header = wrapper.iterator.next()
        val value = wrapper.iterator.next()
        val footer = wrapper.iterator.next()
        assertFalse(wrapper.iterator.hasNext())
        assertNotNull(header)
        assertEquals(0, header.rowIndex)
        with(header) {
            assertEquals("CODE",rowCellValues[ColumnKey.field(Product::code)]!!.value.value)
        }
        assertNotNull(value)
        assertEquals(1, value.rowIndex)
        with(value) {
            assertEquals("code1",rowCellValues[ColumnKey.field(Product::code)]!!.value.value)
        }
        assertNotNull(footer)
        assertEquals(2, footer.rowIndex)
        with(footer) {
            assertEquals("footer",rowCellValues[ColumnKey.field(Product::code)]!!.value.value)
        }
    }

}