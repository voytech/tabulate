package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.NextId

class ExportingState<A>(
    val delegate: DelegateAPI<A>,
    val tableName: String = "table-${NextId.nextId()}",
    val firstRow: Int? = 0,
    val firstColumn: Int? = 0
) {
    var rowIndex: Int = 0
    var columnIndex: Int = 0

    fun nextColumnIndex(index: Int): ExportingState<A> {
        columnIndex = index
        return this
    }

    fun nextRowIndex(index: Int): ExportingState<A> {
        rowIndex = index
        return this
    }

    fun nextRowIndex(): ExportingState<A> {
        rowIndex ++
        return this
    }

    fun coordinates(): Coordinates = Coordinates(tableName, (firstRow?:0) + rowIndex, (firstColumn?:0) + columnIndex)

}