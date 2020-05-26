package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.NextId

class ExportingState(
    val delegate: DelegateAPI,
    val tableName: String = "table-${NextId.nextId()}"
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

    fun coordinates(): Coordinates = Coordinates(tableName, rowIndex, columnIndex)

}