package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.ColumnHint
import pl.voytech.exporter.core.model.hints.RowHint
import pl.voytech.exporter.core.model.hints.TableHint
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class DelegatingHintsOperations(
    private val tableHints: List<TableHintOperation<out TableHint>>?,
    private val rowHints: List<RowHintOperation<out RowHint>>?,
    private val cellHints: List<CellHintOperation<out CellHint>>?,
    private val columnHints: List<ColumnHintOperation<out ColumnHint>>?
) : HintsOperations {

    private fun tableHintOperationByClass(hint: KClass<out TableHint>) : TableHintOperation<TableHint>? {
        return tableHints?.find { operation -> operation.hintType() == hint } as TableHintOperation<TableHint>
    }

    private fun cellHintOperationByClass(hint: KClass<out CellHint>) : CellHintOperation<CellHint>? {
        return cellHints?.find { operation -> operation.hintType() == hint } as CellHintOperation<CellHint>
    }

    private fun rowHintOperationByClass(hint: KClass<out RowHint>) : RowHintOperation<RowHint>? {
        return rowHints?.find { operation -> operation.hintType() == hint } as RowHintOperation<RowHint>
    }

    private fun columnHintOperationByClass(hint: KClass<out ColumnHint>) : ColumnHintOperation<ColumnHint>? {
        return columnHints?.find { operation -> operation.hintType() == hint } as ColumnHintOperation<ColumnHint>
    }

    override fun applyTableHints(state: DelegateState, hints: Set<TableHint>) {
        hints.forEach { hint ->
            tableHintOperationByClass(hint::class)?.apply(state, hint)
        }
    }

    override fun applyColumnHints(state: DelegateState, columnIndex: Int, hints: Set<ColumnHint>) {
        hints.forEach { hint ->
            columnHintOperationByClass(hint::class)?.apply(state, columnIndex, hint)
        }
    }

    override fun applyColumnHeaderCellHints(state: DelegateState, columnIndex: Int, hints: Set<CellHint>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun applyRowHints(state: DelegateState, rowIndex: Int, hints: Set<RowHint>) {
        hints.forEach { hint ->
            rowHintOperationByClass(hint::class)?.apply(state, rowIndex, hint)
        }
    }

    override fun applyCellHints(state: DelegateState, coordinates: Coordinates, hints: Set<CellHint>) {
        hints.forEach { hint  ->
            cellHintOperationByClass(hint::class)?.apply(state, coordinates,hint)
        }
    }
}