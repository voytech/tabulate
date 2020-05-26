package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.hints.*
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class DelegatingTableHintsOperations(
    private val tableHints: List<TableHintOperation<out TableHint>>?
) : TableHintsOperation {

    private fun hintOperationByClass(hint: KClass<out TableHint>) : TableHintOperation<TableHint>? {
        return tableHints?.find { operation -> operation.hintType() == hint } as TableHintOperation<TableHint>
    }

    override fun applyTableHints(state: DelegateAPI, hints: Set<TableHint>) {
        hints.forEach { hint ->
            hintOperationByClass(hint::class)?.apply(state, hint)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class DelegatingRowHintsOperations(
    private val rowHints: List<RowHintOperation<out RowHint>>?
) : RowHintsOperation {

    private fun hintOperationByClass(hint: KClass<out RowHint>) : RowHintOperation<RowHint>? {
        return rowHints?.find { operation -> operation.hintType() == hint } as RowHintOperation<RowHint>
    }

    override fun applyRowHints(state: DelegateAPI, coordinates: Coordinates, hints: Set<RowHint>)  {
        hints.forEach { hint ->
            hintOperationByClass(hint::class)?.apply(state, coordinates, hint)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class DelegatingColumnHintsOperations(
    private val columnHints: List<ColumnHintOperation<out ColumnHint>>?
) : ColumnHintsOperation {

    private fun hintOperationByClass(hint: KClass<out ColumnHint>) : ColumnHintOperation<ColumnHint>? {
        return columnHints?.find { operation -> operation.hintType() == hint } as ColumnHintOperation<ColumnHint>
    }

    override fun applyColumnHints(state: DelegateAPI, coordinates: Coordinates, hints: Set<ColumnHint>)  {
        hints.forEach { hint ->
            hintOperationByClass(hint::class)?.apply(state, coordinates, hint)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class DelegatingCellHintsOperations(
    private val cellHints: List<CellHintOperation<out CellHint>>?
) : CellHintsOperation {

    private fun hintOperationByClass(hint: KClass<out CellHint>) : CellHintOperation<CellHint>? {
        return cellHints?.find { operation -> operation.hintType() == hint } as CellHintOperation<CellHint>
    }

    override fun applyCellHints(state: DelegateAPI, coordinates: Coordinates, hints: Set<CellHint>)  {
        hints.forEach { hint ->
            hintOperationByClass(hint::class)?.apply(state, coordinates, hint)
        }
    }
}