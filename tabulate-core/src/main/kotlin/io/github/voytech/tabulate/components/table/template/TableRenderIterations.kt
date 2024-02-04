package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.components.table.model.ColumnDef
import io.github.voytech.tabulate.core.model.RenderIterationsApi

data class TableRenderIterations(val iterations: RenderIterationsApi) {

    internal fun <T> pushNewIteration(def: ColumnDef<T>) {
        iterations.newRenderIteration(START_COLUMN to def.index)
    }

    internal fun <T> insertAsNextIteration(def: ColumnDef<T>) {
        iterations.getCurrentIterationAttributesOrNull().let { map ->
            val filtered = map.filter { it.key != END_COLUMN }
            iterations.insertAsNextRenderIteration(filtered + (START_COLUMN to def.index))
        }
    }

    internal fun <T> limitWithEndColumn(def: ColumnDef<T>) {
        iterations.appendAttributes(END_COLUMN to def.index - 1)
    }

    internal fun pushNewIteration(rowIndex: RowIndex, recordIndex: Int) {
        iterations.appendAttributes(END_ROW to rowIndex - 1, END_RECORD to recordIndex - 1)
        iterations.newRenderIteration(START_ROW to rowIndex, START_RECORD to recordIndex)
    }

    private fun getStartColumnIndex(): Int? = iterations.getCurrentIterationAttributeOrNull(START_COLUMN)

    internal fun getStartColumnIndexOrZero(): Int = getStartColumnIndex() ?: 0

    internal fun getEndColumnIndex(): Int? = iterations.getCurrentIterationAttributeOrNull(END_COLUMN)

    internal fun getStartRowIndex(): RowIndex? = iterations.getCurrentIterationAttributeOrNull(START_ROW)

    internal fun getEndRowIndex(): RowIndex? = iterations.getCurrentIterationAttributeOrNull(END_ROW)

    internal fun getStartRowIndexOrZero(): Int = getStartRowIndex()?.value ?: 0

    internal fun getStartRecordIndex(): Int? = iterations.getCurrentIterationAttributeOrNull(START_RECORD)

    internal fun getEndRecordIndex(): Int? = iterations.getCurrentIterationAttributeOrNull(END_RECORD)

    fun <T> List<T>.adjustRenderWindow(): Iterable<T> = let { list ->
        val fromStart = getStartRecordIndex() ?: 0
        val toEnd = getEndRecordIndex()
        if (toEnd != null) {
            list.subList(fromStart, toEnd + 1).asIterable()
        } else {
            list.drop(fromStart).asIterable()
        }
    }

    internal fun <T> List<ColumnDef<T>>.adjustRenderWindow(): List<ColumnDef<T>>  {
        val fromStart = getStartColumnIndex() ?: 0
        val toEnd = getEndColumnIndex()
        return if (toEnd != null) {
            subList(fromStart, toEnd + 1)
        } else {
            drop(fromStart)
        }
    }

    fun inRenderWindow(column: Int): Boolean {
        val fromStart = getStartColumnIndex() ?: 0
        val toEnd = getEndColumnIndex()
        return if (toEnd != null) {
            column >= fromStart && column <= toEnd
        } else {
            column >= fromStart
        }
    }

    companion object {
        const val START_COLUMN = "start_column"
        const val END_COLUMN = "end_column"
        const val START_ROW = "start_row"
        const val END_ROW = "end_row"
        const val START_RECORD = "start_record"
        const val END_RECORD = "end_record"
    }
}

