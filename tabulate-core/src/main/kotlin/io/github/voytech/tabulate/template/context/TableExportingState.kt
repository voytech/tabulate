package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.ColumnDef
import io.github.voytech.tabulate.model.ColumnKey
import io.github.voytech.tabulate.model.NextId
import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.template.iterators.OperationContextIterator
import io.github.voytech.tabulate.template.resolvers.BufferingRowContextResolver

/**
 * @author Wojciech Mąka
 */
class TableExportingState<T>(
    val tableModel: Table<T>,
    val tableName: String = "table-${NextId.nextId()}",
    val firstRow: Int? = 0,
    val firstColumn: Int? = 0
) {
    private val stateAttributes = mutableMapOf<String, Any>()
    private val indexIncrement = MutableRowIndex()
    private val rowContextResolver: BufferingRowContextResolver<T> = BufferingRowContextResolver()
    private lateinit var rowContextIterator: OperationContextIterator<T, AttributedRow<T>>

    init {
        stateAttributes["_tableId"] = tableName
        createIterator()
    }

    fun incrementIndex()  = indexIncrement.inc()

    fun setRowIndex(index: Int) {
        indexIncrement.assign(index)
    }

    fun getRowIndex(): RowIndex = indexIncrement.getRowIndex()

    fun mark(label: IndexLabel): RowIndex {
        return indexIncrement.mark(label.name).also {
            createIterator()
        }
    }

    fun bufferAndNext(record: T): AttributedRow<T>? {
        rowContextResolver.buffer(record)
        return getNextRowContext()
    }

    private fun createIterator() {
        rowContextIterator = OperationContextIterator(rowContextResolver)
        rowContextIterator.setState(this)
    }

    fun getNextRowContext(): AttributedRow<T>? {
        return if (rowContextIterator.hasNext()) rowContextIterator.next() else null
    }

    internal fun createRowContext(
        relativeRowIndex: Int,
        rowAttributes: Set<RowAttribute>,
        cells: Map<ColumnKey<T>, AttributedCell>,
    ): AttributedRow<T> {
        return AttributedRow(
            rowIndex = (firstRow ?: 0) + relativeRowIndex,
            rowAttributes = rowAttributes,
            rowCellValues = cells
        ).apply { additionalAttributes = stateAttributes }
    }

    internal fun createCellContext(
        relativeRowIndex: Int,
        relativeColumnIndex: Int,
        value: CellValue,
        attributes: Set<CellAttribute>,
    ): AttributedCell {
        return AttributedCell(
            value = value,
            attributes = attributes,
            rowIndex = (firstRow ?: 0) + relativeRowIndex,
            columnIndex = (firstColumn ?: 0) + relativeColumnIndex,
        ).apply { additionalAttributes = stateAttributes }
    }

    internal fun createColumnContext(
        indexedColumn: IndexedValue<ColumnDef<T>>,
        phase: ColumnRenderPhase,
    ): AttributedColumn {
        return AttributedColumn(
            columnIndex = (firstColumn ?: 0) + indexedColumn.index,
            currentPhase = phase,
            columnAttributes = indexedColumn.value.columnAttributes?.filter { ext ->
                ((ColumnRenderPhase.BEFORE_FIRST_ROW == phase) && ext.beforeFirstRow()) ||
                        ((ColumnRenderPhase.AFTER_LAST_ROW == phase) && ext.afterLastRow())
            }?.toSet(),
        ).apply { additionalAttributes = stateAttributes }
    }

}
