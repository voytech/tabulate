package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.api.builder.dsl.createTableBuilder
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.model.attributes.alias.TableAttribute
import io.github.voytech.tabulate.model.attributes.cell.Colors
import io.github.voytech.tabulate.model.attributes.cell.borders
import io.github.voytech.tabulate.model.attributes.cell.text
import io.github.voytech.tabulate.model.attributes.column.width
import io.github.voytech.tabulate.support.*
import io.github.voytech.tabulate.template.iterators.RowContextIterator
import io.github.voytech.tabulate.template.resolvers.AccumulatingRowContextResolver
import io.github.voytech.tabulate.template.resolvers.RowCompletionListener
import org.junit.jupiter.api.Test

class AttributeDispatchingTableOperationsTest {
    private fun createAttributeOperationsContainer(spy: Spy) =
        AttributesOperationsContainer<TestRenderingContext>().also {
            it.registerAttributesOperations(object : AttributeRenderOperationsFactory<TestRenderingContext> {
                override fun createCellAttributeRenderOperations(): Set<CellAttributeRenderOperation<TestRenderingContext, out CellAttribute>> =
                    setOf(TestCellTextStyleRenderOperation(spy), TestBorderStyleRenderOperation(spy))

                override fun createColumnAttributeRenderOperations(): Set<ColumnAttributeRenderOperation<TestRenderingContext, out ColumnAttribute>> =
                    setOf(TestColumnWidthRenderOperation(spy))

                override fun createRowAttributeRenderOperations(): Set<RowAttributeRenderOperation<TestRenderingContext, out RowAttribute>> =
                    setOf(TestRowHeightRenderOperation(spy))

                override fun createTableAttributeRenderOperations(): Set<TableAttributeRenderOperation<TestRenderingContext, out TableAttribute>> =
                    setOf(TestTableTemplateAttributeRenderOperation(spy))

            })
        }

    @Test
    fun `should dispatch attributes to corresponding operations`() {
        val spy = Spy()
        val container = createAttributeOperationsContainer(spy)
        val dispatchingTableOperations = AttributeDispatchingTableOperations(
            container,
            object : TableExportOperations<TestRenderingContext> {
                override fun renderRowCell(renderingContext: TestRenderingContext, context: RowCellContext) {

                }

                override fun beginRow(renderingContext: TestRenderingContext, context: RowContext) {

                }

                override fun <T> endRow(renderingContext: TestRenderingContext, context: RowContextWithCells<T>) {

                }

                override fun renderColumn(renderingContext: TestRenderingContext, context: ColumnContext) {

                }

                override fun createTable(renderingContext: TestRenderingContext, context: TableContext) {

                }
            }, false
        )

        val table = createTableBuilder<Unit> {
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
        }.build(dispatchingTableOperations.createAttributeTransformerContainer())
        val additionalAttributes = mutableMapOf<String, Any>()
        val iterator = RowContextIterator(AccumulatingRowContextResolver(
            table, additionalAttributes,
            object : RowCompletionListener<Unit> {
                override fun onAttributedCellResolved(cell: AttributedCell) =
                    dispatchingTableOperations.renderRowCell(TestRenderingContext(), cell)

                override fun onAttributedRowResolved(row: AttributedRow<Unit>) =
                    dispatchingTableOperations.beginRow(TestRenderingContext(), row)

                override fun onAttributedRowResolved(row: AttributedRowWithCells<Unit>) =
                    dispatchingTableOperations.endRow(TestRenderingContext(), row)
            }
        ))
        val attributedTable = table.createContext(additionalAttributes)
        dispatchingTableOperations.createTable(TestRenderingContext(), attributedTable)
        iterator.next()
        spy.assertOrder(
            TestBorderStyleRenderOperation(),
            TestCellTextStyleRenderOperation()
        )
    }
}