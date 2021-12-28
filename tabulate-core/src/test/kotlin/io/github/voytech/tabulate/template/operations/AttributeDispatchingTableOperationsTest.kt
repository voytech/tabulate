package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.api.builder.dsl.CustomTable
import io.github.voytech.tabulate.model.attributes.cell.*
import io.github.voytech.tabulate.model.attributes.column.width
import io.github.voytech.tabulate.support.Spy.Companion.spy
import io.github.voytech.tabulate.template.TabulationFormat.Companion.format
import io.github.voytech.tabulate.template.export
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AttributeDispatchingTableOperationsTest {

    @Test
    fun `should dispatch attributes to corresponding operations`() {
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

        val history = spy.readHistory()
        history.next().run {
            assertTrue { TableContext::class.java == context.javaClass }
        }
        history.next().run {
            assertTrue { ColumnContext::class.java == context.javaClass }
        }
        history.next().run {
            assertTrue { RowContext::class.java == context.javaClass }
        }
        history.next().run {
            assertTrue { RowCellContext::class.java == context.javaClass }
            assertTrue { attribute is CellBordersAttribute }
        }
        history.next().run {
            assertTrue { RowCellContext::class.java == context.javaClass }
            assertNull(attribute)
        }
        history.next().run {
            assertTrue { RowCellContext::class.java == context.javaClass }
            assertTrue { attribute is CellTextStylesAttribute }
        }
        history.next().run {
            assertTrue { RowContextWithCells::class.java == context.javaClass }
        }
        history.next().run {
            assertTrue { ColumnContext::class.java == context.javaClass } // TODO remove this buggy column operation invocation at the end
        }
        assertFalse(history.hasNext())
    }
}