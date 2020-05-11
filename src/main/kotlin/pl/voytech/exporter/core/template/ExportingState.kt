package pl.voytech.exporter.core.template

class ExportingState(val delegate: DelegateState) {
    var rowIndex: Int = 0
    var columnIndex: Int = 0
    lateinit var columnId: String

    fun nextColumnIndex(index: Int): ExportingState {
        columnIndex = index
        return this
    }

    fun nextRowIndex(index: Int): ExportingState {
        rowIndex = index
        return this
    }
}