package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.components.table.api.builder.dsl.table
import io.github.voytech.tabulate.core.template.DocumentFormat.Companion.format
import io.github.voytech.tabulate.components.table.model.attributes.Colors
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.borders
import io.github.voytech.tabulate.components.table.model.attributes.cell.text
import io.github.voytech.tabulate.components.table.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.components.table.model.attributes.column.width
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.support.ShadowingCellTextStylesAttributeTestRenderOperation
import io.github.voytech.tabulate.support.Spy
import io.github.voytech.tabulate.support.Spy.Companion.operationPriorities
import io.github.voytech.tabulate.components.table.template.export
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TableOperationsWithAttributeSupportTest {

    @Test
    fun `should dispatch attributes to corresponding operations`() {
        operationPriorities[CellBordersAttribute::class.java] = -1   // should be rendered first,
        operationPriorities[CellTextStylesAttribute::class.java] = 1 // should be rendered next.

        table {
            columns {
                column(0) {
                    attributes { width { px = 100 } }
                }
            }
            rows {
                newRow {
                    cell { value = "cell" }
                    attributes {
                        text { fontColor = Colors.BLACK }
                        borders { topBorderColor = Colors.WHITE }
                    }
                }
            }
        }.export(format("spy"), Unit)

        val history = Spy.spy.readHistory()
        history.next().run {
            assertTrue { TableOpeningContext::class.java == context.javaClass }
        }
        history.next().run {
            assertTrue { ColumnOpeningContext::class.java == context.javaClass }
        }
        history.next().run {
            assertTrue { ColumnOpeningContext::class.java == context.javaClass }
            assertTrue { attribute is ColumnWidthAttribute }
        }
        history.next().run {
            assertTrue { RowOpeningContext::class.java == context.javaClass }
        }
        history.next().run {
            assertTrue { CellContext::class.java == context.javaClass }
            assertTrue { attribute is CellBordersAttribute }
        }
        history.next().run {
            assertTrue { CellContext::class.java == context.javaClass }
            assertNull(attribute)
        }
        history.next().run {
            assertTrue { CellContext::class.java == context.javaClass }
            assertTrue { attribute is CellTextStylesAttribute }
        }
        history.next().run {
            assertTrue { RowClosingContext::class.java == context.javaClass }
        }
        history.next().run {
            assertTrue { ColumnClosingContext::class.java == context.javaClass }
        }
        history.next().run { assertIs<TableClosingContext>(context) }
        assertFalse(history.hasNext())
    }

    @Test
    fun `should use user defined attribute operation instead of factory provided`() {

        table {
            rows {
                newRow {
                    cell { value = "cell" }
                    attributes {
                        text { fontColor = Colors.BLACK }
                    }
                }
            }
        }.export(format("spy"), Unit)

        val history = Spy.spy.readHistory()
        history.next().run {
            assertTrue { TableOpeningContext::class.java == context.javaClass }
        }
        history.next().run {
            assertTrue { ColumnOpeningContext::class.java == context.javaClass }
        }
        history.next().run {
            assertTrue { RowOpeningContext::class.java == context.javaClass }
        }
        history.next().run {
            assertTrue { CellContext::class.java == context.javaClass }
            assertNull(attribute)
        }
        history.next().run {
            assertTrue { CellContext::class.java == context.javaClass }
            assertTrue { attribute is CellTextStylesAttribute }
            // Below operation is loaded by ServiceLoader as standalone operation (not from factory)
            assertTrue { operation is ShadowingCellTextStylesAttributeTestRenderOperation }
        }
        history.next().run {
            assertTrue { RowClosingContext::class.java == context.javaClass }
        }
        history.next().run {
            assertTrue { ColumnClosingContext::class.java == context.javaClass }
        }
        history.next().run { assertIs<TableClosingContext>(context) }
        assertFalse(history.hasNext())
    }
}