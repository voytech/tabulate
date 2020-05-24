package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Description
import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.ColumnHint
import pl.voytech.exporter.core.model.hints.RowHint

abstract class BasicOperationsWithHints<T>(
    rowHints: List<RowHintOperation<out RowHint>>?
): BasicOperations<T> {

    private val hintOperations: DelegatingRowHintsOperations = DelegatingRowHintsOperations(rowHints)

    override fun renderHeaderRow(state: DelegateState, coordinates: Coordinates, rowHints: Set<RowHint>?){
        renderHeaderRow(state,coordinates).also {
            rowHints?.let { hintOperations.applyRowHints(state, coordinates.rowIndex, it) }
        }
    }

    override fun renderRow(state: DelegateState, coordinates: Coordinates, rowHints: Set<RowHint>?) {
        renderRow(state, coordinates).also {
            rowHints?.let { hintOperations.applyRowHints(state, coordinates.rowIndex, it) }
        }
    }

    abstract fun renderHeaderRow(state: DelegateState, coordinates: Coordinates)

    abstract fun renderRow(state: DelegateState, coordinates: Coordinates)

}

class ColumnOperationWithHints(
    columnHints: List<ColumnHintOperation<out ColumnHint>>?
): ColumnOperation {

    private val hintOperations: DelegatingColumnHintsOperations = DelegatingColumnHintsOperations(columnHints)

    override fun renderColumn(state: DelegateState, columnIndex: Int, columnHints: Set<ColumnHint>?) {
        columnHints?.let { hintOperations.applyColumnHints(state, columnIndex, columnHints) }
    }
}

abstract class HeaderCellOperationWithHints(
    cellHints: List<CellHintOperation<out CellHint>>?
): HeaderCellOperation {

    private val hintOperations: DelegatingCellHintsOperations = DelegatingCellHintsOperations(cellHints)

    override fun renderHeaderCell(
        state: DelegateState,
        coordinates: Coordinates,
        columnTitle: Description?,
        cellHints: Set<CellHint>?
    ) {
        renderHeaderCell(state, coordinates, columnTitle?.title).also {
            cellHints?.let { hintOperations.applyCellHints(state, coordinates, it) }
        }
    }

    abstract fun renderHeaderCell(state: DelegateState, coordinates: Coordinates, columnTitle: String?)

}

abstract class RowCellOperationWithHints(
    cellHints: List<CellHintOperation<out CellHint>>?
): RowCellOperation {

    private val hintOperations: DelegatingCellHintsOperations = DelegatingCellHintsOperations(cellHints)

    override fun renderRowCell(state: DelegateState, coordinates: Coordinates, value: CellValue?, cellHints: Set<CellHint>?) {
        renderRowCell(state, coordinates, value).also {
            cellHints?.let { hintOperations.applyCellHints(state, coordinates, it) }
        }
    }

    abstract fun renderRowCell(state: DelegateState, coordinates: Coordinates, value: CellValue?)

}