package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.api.builder.dsl.CustomTable
import io.github.voytech.tabulate.model.attributes.cell.*
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.model.attributes.column.width
import io.github.voytech.tabulate.support.Spy
import io.github.voytech.tabulate.support.Spy.Companion.operationPriorities
import io.github.voytech.tabulate.template.TabulationFormat.Companion.format
import io.github.voytech.tabulate.template.export
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

        CustomTable {
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
            assertTrue { TableCreationContext::class.java == context.javaClass }
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
}