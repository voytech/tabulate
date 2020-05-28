package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.NextId

class ExportingState(
    val delegate: DelegateAPI,
    val tableName: String = "table-${NextId.nextId()}",
    val firstRow: Int? = 0,
    val firstColumn: Int? = 0
) {
    var rowIndex: Int = 0
    var columnIndex: Int = 0

    fun nextColumnIndex(index: Int): ExportingState {
        columnIndex = index
        return this
    }

    fun nextRowIndex(index: Int): ExportingState {
        rowIndex = index
        return this
    }

    fun nextRowIndex(): ExportingState {
        rowIndex ++
        return this
    }

    fun coordinates(): Coordinates = Coordinates(tableName, (firstRow?:0) + rowIndex, (firstColumn?:0) + columnIndex)

}