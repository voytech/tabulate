package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.components.table.api.builder.dsl.borders
import io.github.voytech.tabulate.components.table.api.builder.dsl.table
import io.github.voytech.tabulate.components.table.api.builder.dsl.text
import io.github.voytech.tabulate.components.table.api.builder.dsl.width
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.components.table.template.export
import io.github.voytech.tabulate.core.model.attributes.BordersAttribute
import io.github.voytech.tabulate.core.model.attributes.TextStylesAttribute
import io.github.voytech.tabulate.core.model.attributes.WidthAttribute
import io.github.voytech.tabulate.core.model.color.Colors
import io.github.voytech.tabulate.core.template.DocumentFormat.Companion.format
import io.github.voytech.tabulate.support.Spy
import io.github.voytech.tabulate.support.Spy.Companion.operationPriorities
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TableOperationsWithAttributeSupportTest {

    @Test
    fun `should dispatch attributes to corresponding operations`() {
        operationPriorities[BordersAttribute::class.java] = -1   // should be rendered first,
        operationPriorities[TextStylesAttribute::class.java] = 1 // should be rendered next.

        table {
            columns {
                column(0) {
                    attributes { width { 100.px() } }
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
            assertTrue { TableStart::class.java == context.javaClass }
        }
        history.next().run {
            assertTrue { ColumnStart::class.java == context.javaClass }
        }
        history.next().run {
            assertTrue { ColumnStart::class.java == context.javaClass }
            assertTrue { attribute is WidthAttribute }
        }
        history.next().run {
            assertTrue { RowStart::class.java == context.javaClass }
        }
        history.next().run {
            assertTrue { CellContext::class.java == context.javaClass }
            assertTrue { attribute is BordersAttribute }
        }
        history.next().run {
            assertTrue { CellContext::class.java == context.javaClass }
            assertNull(attribute)
        }
        history.next().run {
            assertTrue { CellContext::class.java == context.javaClass }
            assertTrue { attribute is TextStylesAttribute }
        }
        history.next().run {
            assertTrue { RowEnd::class.java == context.javaClass }
        }
        history.next().run {
            assertTrue { ColumnEnd::class.java == context.javaClass }
        }
        history.next().run { assertIs<TableEnd>(context) }
        assertFalse(history.hasNext())
    }

}