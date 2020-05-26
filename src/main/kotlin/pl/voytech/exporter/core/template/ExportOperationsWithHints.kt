package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Description
import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.ColumnHint
import pl.voytech.exporter.core.model.hints.RowHint

abstract class RowOperationsWithHints(
    rowHints: List<RowHintOperation<out RowHint>>?
): RowOperations {

    private val hintOperations: DelegatingRowHintsOperations = DelegatingRowHintsOperations(rowHints)

    override fun renderHeaderRow(state: DelegateAPI, coordinates: Coordinates, rowHints: Set<RowHint>?){
        renderHeaderRow(state,coordinates).also {
            rowHints?.let { hintOperations.applyRowHints(state, coordinates, it) }
        }
    }

    override fun renderRow(state: DelegateAPI, coordinates: Coordinates, rowHints: Set<RowHint>?) {
        renderRow(state, coordinates).also {
            rowHints?.let { hintOperations.applyRowHints(state, coordinates, it) }
        }
    }

    abstract fun renderHeaderRow(state: DelegateAPI, coordinates: Coordinates)

    abstract fun renderRow(state: DelegateAPI, coordinates: Coordinates)

}

class ColumnOperationsWithHints(
    columnHints: List<ColumnHintOperation<out ColumnHint>>?
): ColumnOperation {

    private val hintOperations: DelegatingColumnHintsOperations = DelegatingColumnHintsOperations(columnHints)

    override fun renderColumn(state: DelegateAPI, coordinates: Coordinates, columnHints: Set<ColumnHint>?) {
        columnHints?.let { hintOperations.applyColumnHints(state, coordinates, columnHints) }
    }
}

abstract class HeaderCellOperationsWithHints(
    cellHints: List<CellHintOperation<out CellHint>>?
): HeaderCellOperation {

    private val hintOperations: DelegatingCellHintsOperations = DelegatingCellHintsOperations(cellHints)

    override fun renderHeaderCell(
        state: DelegateAPI,
        coordinates: Coordinates,
        columnTitle: Description?,
        cellHints: Set<CellHint>?
    ) {
        renderHeaderCell(state, coordinates, columnTitle?.title).also {
            cellHints?.let { hintOperations.applyCellHints(state, coordinates, it) }
        }
    }

    abstract fun renderHeaderCell(state: DelegateAPI, coordinates: Coordinates, columnTitle: String?)

}

abstract class RowCellOperationsWithHints(
    cellHints: List<CellHintOperation<out CellHint>>?
): RowCellOperation {

    private val hintOperations: DelegatingCellHintsOperations = DelegatingCellHintsOperations(cellHints)

    override fun renderRowCell(state: DelegateAPI, coordinates: Coordinates, value: CellValue?, cellHints: Set<CellHint>?) {
        renderRowCell(state, coordinates, value).also {
            cellHints?.let { hintOperations.applyCellHints(state, coordinates, it) }
        }
    }

    abstract fun renderRowCell(state: DelegateAPI, coordinates: Coordinates, value: CellValue?)

}