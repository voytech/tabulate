package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.components.table.api.builder.dsl.borders
import io.github.voytech.tabulate.components.table.api.builder.dsl.table
import io.github.voytech.tabulate.components.table.api.builder.dsl.text
import io.github.voytech.tabulate.components.table.api.builder.dsl.width
import io.github.voytech.tabulate.components.table.rendering.*
import io.github.voytech.tabulate.components.table.template.export
import io.github.voytech.tabulate.core.model.attributes.BordersAttribute
import io.github.voytech.tabulate.core.model.attributes.TextStylesAttribute
import io.github.voytech.tabulate.core.model.attributes.WidthAttribute
import io.github.voytech.tabulate.core.model.color.Colors
import io.github.voytech.tabulate.core.DocumentFormat.Companion.format
import io.github.voytech.tabulate.support.mock.Spy
import io.github.voytech.tabulate.support.mock.Spy.Companion.operationPriorities
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
                        text { color = Colors.BLACK }
                        borders { topBorderColor = Colors.WHITE }
                    }
                }
            }
        }.export(format("spy"), Unit)

        val history = Spy.spy.readHistory()
        history.next().run {
            assertTrue { TableStartRenderable::class.java == context.javaClass }
        }
        history.next().run {
            assertTrue { ColumnStartRenderable::class.java == context.javaClass }
        }
        history.next().run {
            assertTrue { ColumnStartRenderable::class.java == context.javaClass }
            assertTrue { attribute is WidthAttribute }
        }
        history.next().run {
            assertTrue { RowStartRenderable::class.java == context.javaClass }
        }
        history.next().run {
            assertTrue { CellRenderable::class.java == context.javaClass }
            assertTrue { attribute is BordersAttribute }
        }
        history.next().run {
            assertTrue { CellRenderable::class.java == context.javaClass }
            assertNull(attribute)
        }
        history.next().run {
            assertTrue { CellRenderable::class.java == context.javaClass }
            assertTrue { attribute is TextStylesAttribute }
        }
        history.next().run {
            assertTrue { RowEndRenderable::class.java == context.javaClass }
        }
        history.next().run {
            assertTrue { ColumnEndRenderable::class.java == context.javaClass }
        }
        history.next().run { assertIs<TableEndRenderable>(context) }
        assertFalse(history.hasNext())
    }

}