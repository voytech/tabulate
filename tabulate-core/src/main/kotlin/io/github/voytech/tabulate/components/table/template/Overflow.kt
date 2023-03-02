package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.components.table.model.ColumnDef
import io.github.voytech.tabulate.core.model.ModelExportStatus

data class OverflowOffsets(
    val columnOffsets: ColumnOffsets = ColumnOffsets(),
    val rowOffsets: RowOffsets = RowOffsets(),
    var partialStatus: ModelExportStatus = ModelExportStatus.ACTIVE,
) {

    fun reset() {
        columnOffsets.index = 0
        columnOffsets.nextIndex = 0
        rowOffsets.index = RowIndex(0)
        rowOffsets.nextIndex = RowIndex(0)
        rowOffsets.recordIndex = 0
        rowOffsets.nextRecordIndex = 0
    }

    fun save(status: ModelExportStatus) {
        partialStatus = status
    }

    fun align() {
        when (partialStatus) {
            ModelExportStatus.PARTIAL_X, ModelExportStatus.PARTIAL_XY -> {
                rowOffsets.reset()
                columnOffsets.onResume()
            }

            ModelExportStatus.PARTIAL_Y, ModelExportStatus.PARTIAL_YX -> {
                columnOffsets.reset()
                rowOffsets.onResume()
            }
            ModelExportStatus.ACTIVE, ModelExportStatus.FINISHED -> {}
        }
    }

    fun isValid(column: Int): Boolean = column >= columnOffsets.index

    fun <T> Iterable<T>?.crop(): Iterable<T>? =
        this?.drop(rowOffsets.recordIndex)?.asIterable()

    internal fun <T> List<ColumnDef<T>>.crop(): List<ColumnDef<T>> =
        drop(columnOffsets.index)

    fun getRowOffsetValue(): Int = getRowOffset().value

    fun getRowOffset(): RowIndex = rowOffsets.index

    fun getColumnOffset(): Int = columnOffsets.index

    fun setResumeFromRowIndex(rowIndex: RowIndex) {
        rowOffsets.nextIndex = rowIndex
    }

    fun setResumeFromColumnIndex(index: Int) {
        columnOffsets.nextIndex = index
    }

    fun setResumeFromRecordIndex(record: Int) {
        rowOffsets.nextRecordIndex = record
    }
}

data class ColumnOffsets(
    var index: Int = 0,
    var nextIndex: Int = 0,
) {
    internal fun onResume() {
        index = nextIndex
        nextIndex = 0
    }

    internal fun reset() {
        index = 0
    }
}

data class RowOffsets(
    var index: RowIndex = RowIndex(0),
    var recordIndex: Int = 0,
    var nextIndex: RowIndex = RowIndex(0),
    var nextRecordIndex: Int = 0,
) {
    internal fun onResume() {
        index = nextIndex
        recordIndex = nextRecordIndex
    }

    internal fun reset() {
        recordIndex = 0
    }

}