package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Description
import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.ColumnHint
import pl.voytech.exporter.core.model.hints.RowHint
import pl.voytech.exporter.core.model.hints.TableHint

abstract class HintsResolvingExportOperations<T>(
    tableHints: List<TableHintOperation<out TableHint>>?,
    rowHints: List<RowHintOperation<out RowHint>>?,
    cellHints: List<CellHintOperation<out CellHint>>?,
    columnHints: List<ColumnHintOperation<out ColumnHint>>?
) : ExportOperations<T>  {
    private val hintOperations: HintsOperations = DelegatingHintsOperations(tableHints, rowHints, cellHints, columnHints)

    override fun renderColumn(state: DelegateState, columnIndex: Int, columnHints: Set<ColumnHint>?) {
        columnHints?.let { hintOperations.applyColumnHints(state, columnIndex, columnHints) }
    }

    override fun renderColumnTitleCell(
        state: DelegateState,
        coordinates: Coordinates,
        columnTitle: Description?,
        cellHints: Set<CellHint>?
    ) {
        renderColumnTitleCell(state, coordinates, columnTitle?.title).also {
            cellHints?.let { hintOperations.applyCellHints(state, coordinates, it) }
        }
    }

    override fun renderRow(state: DelegateState, coordinates: Coordinates, rowHints: Set<RowHint>?) {
        renderRow(state, coordinates).also {
            rowHints?.let { hintOperations.applyRowHints(state, coordinates.rowIndex, it) }
        }
    }

    override fun renderRowCell(state: DelegateState, coordinates: Coordinates, value: CellValue?, cellHints: Set<CellHint>?) {
        renderRowCell(state, coordinates, value).also {
            cellHints?.let { hintOperations.applyCellHints(state, coordinates, it) }
        }
    }

    abstract fun renderColumnTitleCell(state: DelegateState, coordinates: Coordinates, columnTitle: String?)

    abstract fun renderRow(state: DelegateState, coordinates: Coordinates)

    abstract fun renderRowCell(state: DelegateState, coordinates: Coordinates, value: CellValue?)

}