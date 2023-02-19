package io.github.voytech.tabulate.resolver

import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.table.model.ColumnKey
import io.github.voytech.tabulate.components.table.model.PredicateLiteral
import io.github.voytech.tabulate.components.table.model.RowCellExpression
import io.github.voytech.tabulate.components.table.model.and
import io.github.voytech.tabulate.core.model.color.Colors
import io.github.voytech.tabulate.core.model.attributes.BackgroundAttribute
import io.github.voytech.tabulate.core.model.background.DefaultFillType
import io.github.voytech.tabulate.components.table.template.AccumulatingRowContextResolver
import io.github.voytech.tabulate.components.table.template.OverflowOffsets
import io.github.voytech.tabulate.components.table.template.RowContextIterator
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.data.Product
import io.github.voytech.tabulate.support.createTableContext
import io.github.voytech.tabulate.support.success
import io.github.voytech.tabulate.support.successfulRowComplete
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigDecimal
import java.time.LocalDate
import java.util.stream.Stream

class RowIteratorTest {

    internal data class Wrapper<T: Any>(
        val iterator: RowContextIterator<T>,
        val resolver: AccumulatingRowContextResolver<T>,
        val customAttributes: Map<String, Any>
    )

    private fun <T: Any> createDefaultIterator(block: TableBuilderApi<T>.() -> Unit): Wrapper<T> =
        createTableBuilder(block).build().let { table ->
            mutableMapOf<String, Any>().let {
                it to AccumulatingRowContextResolver(table, StateAttributes(it), OverflowOffsets(), successfulRowComplete())
            }.let {
                Wrapper(
                    iterator = RowContextIterator(it.second, OverflowOffsets(),table.createTableContext(it.first)),
                    resolver = it.second,
                    customAttributes = it.first
                )
            }
        }

    @Test
    fun `should resolve AttributedRow to null if no table definition nor data is provided`() {
        val wrapper = createDefaultIterator<Product> { }
        assertFalse(wrapper.iterator.hasNext())
    }

    @Test
    fun `should resolve AttributedRow having no cells`() {
        val wrapper = createDefaultIterator<Product> {
            columns {
                column(Product::code)
            }
            rows {
                newRow {
                    attributes {
                        text { fontColor = Colors.WHITE }
                    }
                }
            }
        }
        val resolvedIndexedAttributedRow = wrapper.iterator.next().success()
        assertNotNull(resolvedIndexedAttributedRow)
        assertEquals(0, resolvedIndexedAttributedRow.rowIndex)
        with(resolvedIndexedAttributedRow) {
            assertEquals(0, rowCellValues.size)
        }
    }

    @Test
    fun `should resolve AttributedRow from custom row definition`() {
        val wrapper = createDefaultIterator {
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
        val resolvedIndexedAttributedRow = wrapper.iterator.next().success()
        assertNotNull(resolvedIndexedAttributedRow)
        assertEquals(0, resolvedIndexedAttributedRow.rowIndex)
        with(resolvedIndexedAttributedRow) {
            assertEquals("CustomProductCode", rowCellValues[ColumnKey.field(Product::code)]!!.value)
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
                    cell { value = "R1C2" }
                    cell { value = "R1C4" }
                }
                newRow(3) {
                    cell { value = "R3C0" }
                }
                atIndex { gt(3) and lt(6) } newRow {
                    cell { expression = RowCellExpression { "R${it.rowIndex.getIndex()}C0" } } // TODO <-- simplify this
                }
            }
        }
        val firstRow = wrapper.iterator.next().success()
        val secondRow = wrapper.iterator.next().success()
        val thirdRow = wrapper.iterator.next().success()
        val fourthRow = wrapper.iterator.next().success()
        val fifthRow = wrapper.iterator.next().success()
        assertFalse(wrapper.iterator.hasNext())
        assertNotNull(firstRow)
        assertNotNull(secondRow)
        assertNotNull(thirdRow)
        assertNotNull(fourthRow)
        assertNotNull(fifthRow)
        with(firstRow) {
            assertEquals(0, rowIndex)
            assertEquals("R0C0", rowCellValues[ColumnKey("c0")]!!.cellValue.value)
            assertEquals(0, rowCellValues[ColumnKey("c0")]!!.columnIndex)
            assertEquals("R0C2", rowCellValues[ColumnKey("c2")]!!.cellValue.value)
            assertEquals(2, rowCellValues[ColumnKey("c2")]!!.columnIndex)
            assertEquals("R0C4", rowCellValues[ColumnKey("c4")]!!.cellValue.value)
            assertEquals(4, rowCellValues[ColumnKey("c4")]!!.columnIndex)
        }
        with(secondRow) {
            assertEquals(1, rowIndex)
            assertEquals("R1C0", rowCellValues[ColumnKey("c0")]!!.cellValue.value)
            assertEquals(0, rowCellValues[ColumnKey("c0")]!!.columnIndex)
            assertEquals("R1C2", rowCellValues[ColumnKey("c2")]!!.cellValue.value)
            assertEquals(2, rowCellValues[ColumnKey("c2")]!!.columnIndex)
            assertEquals("R1C4", rowCellValues[ColumnKey("c4")]!!.cellValue.value)
            assertEquals(4, rowCellValues[ColumnKey("c4")]!!.columnIndex)
        }
        with(thirdRow) {
            assertEquals(3, rowIndex)
            assertEquals("R3C0", rowCellValues[ColumnKey("c0")]!!.cellValue.value)
            assertEquals(0, rowCellValues[ColumnKey("c0")]!!.columnIndex)
        }
        with(fourthRow) {
            assertEquals(4, rowIndex)
            assertEquals("R4C0", rowCellValues[ColumnKey("c0")]!!.cellValue.value)
            assertEquals(0, rowCellValues[ColumnKey("c0")]!!.columnIndex)
        }
        with(fifthRow) {
            assertEquals(5, rowIndex)
            assertEquals("R5C0", rowCellValues[ColumnKey("c0")]!!.cellValue.value)
            assertEquals(0, rowCellValues[ColumnKey("c0")]!!.columnIndex)
        }
    }

    @Test
    fun `should resolve AttributedRow from custom item with attributes applied trough separate row definition`() {
        val wrapper = createDefaultIterator<Product> {
            rows {
                newRow {
                    cell {
                        value = "R0C0"
                        attributes {
                            background { fill = DefaultFillType.BRICKS }
                        }
                    }
                    cell { value = "R0C1" }
                    cell { value = "R0C2" }
                }
                matching { eq(0) } assign {
                    cell {
                        attributes {
                            background { color = Colors.WHITE }
                        }
                    }
                }
            }
        }
        val firstRow = wrapper.iterator.next().success()
        assertFalse(wrapper.iterator.hasNext())
        assertNotNull(firstRow)
        with(firstRow) {
            assertEquals(0, rowIndex)
            assertEquals("R0C0", rowCellValues[ColumnKey("column-0")]!!.cellValue.value)
            assertEquals(0, rowCellValues[ColumnKey("column-0")]!!.columnIndex)
            assertEquals(1, rowCellValues[ColumnKey("column-0")]!!.attributes!!.size)
            with(rowCellValues[ColumnKey("column-0")]!!.attributes!![BackgroundAttribute::class.java]) {
                assertTrue(this is BackgroundAttribute)
                assertEquals(Colors.WHITE, (this as BackgroundAttribute).color)
                assertEquals(DefaultFillType.BRICKS, this.fill)
            }
        }
    }

    @Test
    fun `should resolve AttributedRow containing cell attribute re-defined at column builder with same index`() {
        val template = table {
            columns {
                column(0) {
                    attributes {
                        background {
                            fill = DefaultFillType.BRICKS
                            color = Colors.GREEN
                        }
                    }
                }
            }
        }
        val wrapper = createDefaultIterator<Product>(template + {
            columns {
                column(0) {
                    attributes {
                        background {
                            color = Colors.BLACK
                        }
                    }
                }
            }
            rows {
                newRow {
                    cell { value = "R0C0" }
                }
            }
        })
        val first = wrapper.iterator.next().success()
        assertFalse(wrapper.iterator.hasNext())
        assertNotNull(first)
        with(first) {
            assertEquals(0, rowIndex)
            assertEquals(1, rowCellValues[ColumnKey("column-0")]!!.attributes!!.size)
            with(rowCellValues[ColumnKey("column-0")]!!.attributes!![BackgroundAttribute::class.java]) {
                assertTrue(this is BackgroundAttribute)
                assertEquals(Colors.BLACK, (this as BackgroundAttribute).color)
                assertEquals(DefaultFillType.BRICKS, this.fill)
            }
        }
    }

    @ParameterizedTest
    @MethodSource("indexPredicateLiteralProvider")
    fun `should resolve AttributedRow containing cell attribute re-defined at row builder with same index predicate literal`(
        firstLiteral: RowIndexPredicateBuilderApi.() -> PredicateLiteral,
        secondLiteral: RowIndexPredicateBuilderApi.() -> PredicateLiteral
    ) {
        val template = table {
            rows {
                atIndex(firstLiteral) newRow {
                    cell { value = "?" }
                    attributes {
                        background {
                            fill = DefaultFillType.BRICKS
                            color = Colors.GREEN
                        }
                    }
                }
            }
        }
        val wrapper = createDefaultIterator<Product>(template + {
            rows {
                atIndex(secondLiteral) newRow {
                    cell(0) { value = "R0C0" }
                    attributes {
                        background {
                            color = Colors.BLACK
                        }
                    }
                }
            }
        })
        val first = wrapper.iterator.next().success()
        assertNotNull(first)
        with(first) {
            assertEquals("R0C0", rowCellValues[ColumnKey("column-0")]!!.cellValue.value)
            assertEquals(1, rowCellValues[ColumnKey("column-0")]!!.attributes!!.size)
            with(rowCellValues[ColumnKey("column-0")]!!.attributes!![BackgroundAttribute::class.java]) {
                assertTrue(this is BackgroundAttribute)
                assertEquals(Colors.BLACK, (this as BackgroundAttribute).color)
                assertEquals(DefaultFillType.BRICKS, this.fill)
            }
        }
    }

    @Test
    fun `should resolve AttributedRow containing cell attribute merged from different row definitions`() {
        val template = table {
            rows {
                atIndex { gte(0) and lte(3) } newRow {
                    cell { value = "?" }
                    attributes {
                        background {
                            fill = DefaultFillType.BRICKS
                            color = Colors.GREEN
                        }
                    }
                }
            }
        }
        val wrapper = createDefaultIterator<Product>(template + {
            rows {
                atIndex { eq(1) } newRow {
                    cell(0) { value = "R0C0" }
                    attributes {
                        background {
                            color = Colors.BLACK
                        }
                    }
                }
            }
        })

        val first = wrapper.iterator.next().success()
        val second = wrapper.iterator.next().success()
        val third = wrapper.iterator.next().success()
        val fourth = wrapper.iterator.next().success()
        assertFalse(wrapper.iterator.hasNext())
        assertNotNull(first)
        assertNotNull(second)
        assertNotNull(third)
        assertNotNull(fourth)
        listOf(first, third, fourth).forEach {
            assertEquals("?", it.rowCellValues[ColumnKey("column-0")]!!.cellValue.value)
            assertEquals(1, it.rowCellValues[ColumnKey("column-0")]!!.attributes!!.size)
            with(it.rowCellValues[ColumnKey("column-0")]!!.attributes!![BackgroundAttribute::class.java]) {
                assertTrue(this is BackgroundAttribute)
                assertEquals(Colors.GREEN, (this as BackgroundAttribute).color)
                assertEquals(DefaultFillType.BRICKS, this.fill)
            }
        }
        with(second) {
            assertEquals("R0C0", rowCellValues[ColumnKey("column-0")]!!.cellValue.value)
            assertEquals(1, rowCellValues[ColumnKey("column-0")]!!.attributes!!.size)
            with(rowCellValues[ColumnKey("column-0")]!!.attributes!![BackgroundAttribute::class.java]) {
                assertTrue(this is BackgroundAttribute)
                assertEquals(Colors.BLACK, (this as BackgroundAttribute).color)
                assertEquals(DefaultFillType.BRICKS, this.fill)
            }
        }
    }

    @Test
    fun `should resolve AttributedRow's in proper order`() {
        val wrapper = createDefaultIterator<Product> {
            rows {
                footer { cell { value = "footer" } }
                header("header")
            }
        }
        val header = wrapper.iterator.next().success()
        val footer = wrapper.iterator.next().success()
        assertFalse(wrapper.iterator.hasNext())
        assertNotNull(header)
        assertNotNull(footer)
        with(header) {
            assertEquals(0, rowIndex)
        }
        with(footer) {
            assertEquals(1, rowIndex)
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
    fun `should resolve AttributedRows when rows are scattered`() {
        val wrapper = createDefaultIterator<Product> {
            columns { column(Product::code) }
            rows {
                newRow(2) { cell { value = "R2C0" } }
                newRow(3) { cell { value = "R3C0" } }
                newRow(7) { cell { value = "R7C0" } }
                newTrailingRow { cell { value = "FOOTER_R0C0" } }
            }
        }
        val firstRow = wrapper.iterator.next().success()
        wrapper.resolver.buffer()
        val secondRow = wrapper.iterator.next().success()
        val thirdRow = wrapper.iterator.next().success()
        val fourthRow = wrapper.iterator.next().success()
        val trailingRow = wrapper.iterator.next().success()
        assertFalse(wrapper.iterator.hasNext())
        assertNotNull(firstRow)
        assertNotNull(secondRow)
        assertNotNull(thirdRow)
        assertNotNull(fourthRow)
        assertNotNull(trailingRow)
        with(firstRow) {
            assertEquals(2, rowIndex)
            assertEquals("R2C0", rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
        }
        with(secondRow) {
            assertEquals(3, rowIndex)
            assertEquals("R3C0", rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
        }
        with(thirdRow) {
            assertEquals(4, rowIndex)
            assertEquals("C0", rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
        }
        with(fourthRow) {
            assertEquals(7, rowIndex)
            assertEquals("R7C0", rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
        }
        with(trailingRow) {
            assertEquals(8, rowIndex)
            assertEquals("FOOTER_R0C0", rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
        }
    }

    @Test
    fun `should resolve footer AttributedRow`() {
        val wrapper = createDefaultIterator<Product> {
            columns { column(Product::code) }
            rows {
                newRow(2) { cell { value = "R2C0" } }
                newTrailingRow { cell { value = "T0C0" } }
            }
        }
        val firstRow = wrapper.iterator.next().success()
        val footerRow = wrapper.iterator.next().success()
        assertFalse(wrapper.iterator.hasNext())
        assertNotNull(firstRow)
        assertNotNull(footerRow)
        with(firstRow) {
            assertEquals(2, rowIndex)
            assertEquals("R2C0", rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
        }
        with(footerRow) {
            assertEquals(3, rowIndex)
            assertEquals("T0C0", rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
        }
    }

    @Test
    fun `should resolve trailing AttributedRow when row indices are scattered`() {
        val wrapper = createDefaultIterator<Product> {
            columns { column(Product::code) }
            rows {
                newRow(2) { cell { value = "R2C0" } }
                newTrailingRow(2) { cell { value = "T2C0" } }
                newTrailingRow(3) { cell { value = "T3C0" } }
                newTrailingRow(5) { cell { value = "T5C0" } }
            }
        }
        val firstRow = wrapper.iterator.next().success()
        wrapper.resolver.buffer()
        val secondRow = wrapper.iterator.next().success()
        val trailingRow1 = wrapper.iterator.next().success()
        val trailingRow2 = wrapper.iterator.next().success()
        val trailingRow3 = wrapper.iterator.next().success()
        assertFalse(wrapper.iterator.hasNext())
        assertNotNull(firstRow)
        assertNotNull(secondRow)
        assertNotNull(trailingRow1)
        assertNotNull(trailingRow2)
        assertNotNull(trailingRow3)
        with(firstRow) {
            assertEquals(2, rowIndex)
            assertEquals("R2C0", rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
        }
        with(secondRow) {
            assertEquals(3, rowIndex)
            assertEquals("C0", rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
        }
        with(trailingRow1) {
            assertEquals(6, rowIndex)
            assertEquals("T2C0", rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
        }
        with(trailingRow2) {
            assertEquals(7, rowIndex)
            assertEquals("T3C0", rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
        }
        with(trailingRow3) {
            assertEquals(9, rowIndex)
            assertEquals("T5C0", rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
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
        wrapper.resolver.buffer()
        val header = wrapper.iterator.next().success()
        val value = wrapper.iterator.next().success()
        val footer = wrapper.iterator.next().success()
        assertFalse(wrapper.iterator.hasNext())
        assertNotNull(header)
        assertEquals(0, header.rowIndex)
        with(header) {
            assertEquals("CODE", rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
        }
        assertNotNull(value)
        assertEquals(1, value.rowIndex)
        with(value) {
            assertEquals("C0", rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
        }
        assertNotNull(footer)
        assertEquals(2, footer.rowIndex)
        with(footer) {
            assertEquals("footer", rowCellValues[ColumnKey.field(Product::code)]!!.cellValue.value)
        }
    }

    private fun AccumulatingRowContextResolver<Product>.buffer(code: String = "C0") =
        append(Product(
            code,
            "Product code - $code",
            "Product description",
            "tangibles",
            LocalDate.now(),
            BigDecimal.valueOf(1000)
        ))

    companion object {
        private fun indexLiteral(block: RowIndexPredicateBuilderApi.() -> PredicateLiteral): RowIndexPredicateBuilderApi.() -> PredicateLiteral =
            block

        @JvmStatic
        fun indexPredicateLiteralProvider(): Stream<Arguments> {
            return listOf(
                Arguments.of(indexLiteral { eq(0) }, indexLiteral { eq(0) }),
                Arguments.of(indexLiteral { gt(0) and lt(2) }, indexLiteral { gt(0) and lt(2) }),
                Arguments.of(indexLiteral { gte(0) and lte(1) }, indexLiteral { gte(0) and lte(1) }),
                Arguments.of(indexLiteral { gt(0) and lt(2) }, indexLiteral { lt(2) and gt(0) }),
                Arguments.of(indexLiteral { gte(0) and lte(1) }, indexLiteral { lte(1) and gte(0) }),
            ).stream()
        }
    }

}