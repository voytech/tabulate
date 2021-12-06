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
import org.junit.jupiter.api.Disabled
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
    fun `should resolve AttributedRow from custom items when columns are scattered`() {
        val wrapper = createDefaultIterator<Product> {
            columns {
                column("c0") { index = 0 }
                column("c2") { index = 2 }
                column("c4") { index = 4 }
            }
            rows {
                newRow {
                    cell { value = "R0C0" }
                    cell(2) { value = "R0C2" }
                    cell(4) { value = "R0C4" }
                }
                newRow {
                    cell { value = "R1C0" }
                    cell(2) { value = "R1C2" }
                    cell(4) { value = "R1C4" }
                }
            }
        }
        val firstRow = wrapper.iterator.next()
        val secondRow = wrapper.iterator.next()
        assertNotNull(firstRow)
        assertNotNull(secondRow)
        with(firstRow) {
            assertEquals("R0C0",rowCellValues[ColumnKey("c0")]!!.value.value)
            assertEquals(0,rowCellValues[ColumnKey("c0")]!!.columnIndex)
            assertEquals("R0C2",rowCellValues[ColumnKey("c2")]!!.value.value)
            assertEquals(2,rowCellValues[ColumnKey("c2")]!!.columnIndex)
            assertEquals("R0C4",rowCellValues[ColumnKey("c4")]!!.value.value)
            assertEquals(4,rowCellValues[ColumnKey("c4")]!!.columnIndex)
        }
        with(secondRow) {
            assertEquals("R1C0",rowCellValues[ColumnKey("c0")]!!.value.value)
            assertEquals(0,rowCellValues[ColumnKey("c0")]!!.columnIndex)
            assertEquals("R1C2",rowCellValues[ColumnKey("c2")]!!.value.value)
            assertEquals(2,rowCellValues[ColumnKey("c2")]!!.columnIndex)
            assertEquals("R1C4",rowCellValues[ColumnKey("c4")]!!.value.value)
            assertEquals(4,rowCellValues[ColumnKey("c4")]!!.columnIndex)
        }
    }

    @Disabled("Consider such usage with not throwing errors.")
    @Test
    fun `should resolve AttributedRow from custom items with columns at various indices`() {
        val wrapper = createDefaultIterator<Product> {
            rows {
                newRow {
                    cell { value = "R0C0" }
                    cell(2) { value = "R0C2" }
                    cell(4) { value = "R0C4" }
                }
                newRow {
                    cell(1) { value = "R0C1" }
                    cell(3) { value = "R0C3" }
                }
            }
        }
        val firstRow = wrapper.iterator.next()
        val secondRow = wrapper.iterator.next()
        assertNotNull(firstRow)
        assertNotNull(secondRow)

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