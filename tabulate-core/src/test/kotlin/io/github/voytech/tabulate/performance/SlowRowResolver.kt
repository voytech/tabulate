package io.github.voytech.tabulate.performance

import io.github.voytech.tabulate.Either
import io.github.voytech.tabulate.components.table.model.ColumnKey
import io.github.voytech.tabulate.components.table.model.RowDef
import io.github.voytech.tabulate.components.table.model.SourceRow
import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.rendering.*
import io.github.voytech.tabulate.components.table.rendering.createCellContext
import io.github.voytech.tabulate.components.table.rendering.createRowEnd
import io.github.voytech.tabulate.components.table.rendering.createRowStart
import io.github.voytech.tabulate.components.table.template.*
import io.github.voytech.tabulate.core.layout.CrossedAxis
import io.github.voytech.tabulate.core.model.ModelExportContext
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.operation.Ok
import io.github.voytech.tabulate.support.createTableContext

internal class SlowRowResolver<T : Any>(
    private val tableModel: Table<T>,
    private val customAttributes: MutableMap<String, Any>,
    val ctx: ModelExportContext = tableModel.createTableContext(customAttributes),
    offsets: TableRenderIterations = TableRenderIterations(ctx),
    listener: CaptureRowCompletion<T>? = null
) : AbstractRowContextResolver<T>(tableModel, StateAttributes(customAttributes), offsets, listener) {

    private val customRows = tableModel.rows?.filter { it.qualifier.index != null }
    private val rowsWithPredicates = tableModel.rows?.filter { it.qualifier.matching != null }

    private fun getRowsAt(index: RowIndex): List<RowDef<T>>? {
        return customRows
            ?.filter { it.shouldInsertRow(SourceRow(index)) }
    }

    private fun hasRowsAt(index: RowIndex): Boolean = !getRowsAt(index).isNullOrEmpty()

    private fun getRows(sourceRow: SourceRow<T>): Set<RowDef<T>> {
        val customRows = getRowsAt(sourceRow.rowIndex)?.toSet()
        val matchingRows = rowsWithPredicates?.filter { it.shouldApplyWhen(sourceRow) }?.toSet()
        return customRows?.let { matchingRows?.plus(it) ?: it } ?: matchingRows ?: emptySet()
    }

    private fun hasCustomRows(sourceRow: SourceRow<T>): Boolean {
        return hasRowsAt(sourceRow.rowIndex)
    }

    private fun resolveAttributedRow(
        tableRowIndex: RowIndex,
        record: IndexedValue<T>? = null
    ): ContextResult<RowEndRenderable<T>> = SourceRow(tableRowIndex, record?.index, record?.value).let { sourceRow ->
        val rowDefinitions = getRows(sourceRow)
        with(SyntheticRow(tableModel, rowDefinitions)) {
            val start = createRowStart(rowIndex = tableRowIndex.value, customAttributes = customAttributes)
            val maybeCells = mapEachCell({ row, column ->
                row.createCellContext(row = sourceRow, column = column, customAttributes)
            }, { _ -> false })
            return when (maybeCells) {
                is Either.Left -> tryRenderRowEnd(start, maybeCells.value)
                is Either.Right -> OverflowResult(
                    io.github.voytech.tabulate.core.operation.RenderingSkipped(
                        CrossedAxis.Y
                    )
                )
            }
        }
    }

    private fun SyntheticRow<T>.tryRenderRowEnd(
        rowStart: RowStartRenderable,
        cells: Map<ColumnKey<T>, CellRenderable>
    ): ContextResult<RowEndRenderable<T>> =
        createRowEnd(rowStart, cells).let { rowEnd ->
            rowEnd.render().let { result ->
                when (result) {
                    null, Ok -> SuccessResult(rowEnd)
                    else -> OverflowResult(result as io.github.voytech.tabulate.core.operation.RenderingSkipped)
                }
            }
        }

    override fun resolve(requestedIndex: RowIndex): IndexedResult<RowEndRenderable<T>>? {
        return if (hasCustomRows(SourceRow(requestedIndex))) {
            IndexedResult(requestedIndex, null, resolveAttributedRow(requestedIndex))
        } else null
    }

    override fun getNextRecord(): IndexedValue<T>? {
        throw error("Not used for test purposes - only custom records tested")
    }
}