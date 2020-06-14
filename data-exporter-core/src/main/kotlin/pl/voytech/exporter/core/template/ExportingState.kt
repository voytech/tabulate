package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.NextId
import pl.voytech.exporter.core.model.RowData

/**
 * A state of single table export.
 * @author Wojciech Mąka
 */
class ExportingState<A>(
    val delegate: DelegateAPI<A>,
    val tableName: String = "table-${NextId.nextId()}",
    val firstRow: Int? = 0,
    val firstColumn: Int? = 0
) {
    var rowIndex: Int = 0 //objectIndex
    var columnIndex: Int = 0 //objectFieldIndex

    fun withColumnIndex(index: Int): ExportingState<A> {
        columnIndex = index
        return this
    }

    fun withRowIndex(index: Int): ExportingState<A> {
        rowIndex = index
        return this
    }

    fun nextRowIndex(): ExportingState<A> {
        rowIndex++
        return this
    }

    fun coordinates(): Coordinates =
        Coordinates(tableName, (firstRow ?: 0) + rowIndex, (firstColumn ?: 0) + columnIndex)

    fun <T> rowContext(dataset: Collection<T>, record: T? = null, objectIndex: Int? = null) =
        RowData(rowIndex, objectIndex, record, dataset)

}