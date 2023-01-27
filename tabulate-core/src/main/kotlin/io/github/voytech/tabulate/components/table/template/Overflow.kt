package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.components.table.model.ColumnDef
import io.github.voytech.tabulate.core.model.ExportStatus

data class OverflowOffsets(
    val xOffsets: XOffsets = XOffsets(),
    val yOffsets: YOffsets = YOffsets(),
    var partialStatus: ExportStatus = ExportStatus.ACTIVE,
) {

    fun reset() {
        xOffsets.index = 0
        xOffsets.nextIndex = 0
        yOffsets.index = RowIndex(0)
        yOffsets.nextIndex = RowIndex(0)
        yOffsets.recordIndex = 0
        yOffsets.nextRecordIndex = 0
    }

    fun save(status: ExportStatus) {
        partialStatus = status
    }

    fun align() {
        when (partialStatus) {
            ExportStatus.PARTIAL_X, ExportStatus.PARTIAL_XY -> {
                yOffsets.reset()
                xOffsets.onResume()
            }

            ExportStatus.PARTIAL_Y, ExportStatus.PARTIAL_YX -> {
                xOffsets.reset()
                yOffsets.onResume()
            }
            ExportStatus.ACTIVE, ExportStatus.FINISHED -> {}
        }
    }

    fun isValid(column: Int): Boolean = column >= xOffsets.index

    fun <T> Iterable<T>?.crop(): Iterable<T>? =
        this?.drop(yOffsets.recordIndex)?.asIterable()

    internal fun <T> List<ColumnDef<T>>.crop(): List<ColumnDef<T>> =
        drop(xOffsets.index)

    fun getIndexValueOnY(): Int = getIndexOnY().value

    fun getIndexOnY(): RowIndex = yOffsets.index

    fun getIndexOnX(): Int = xOffsets.index

    fun setNextIndexOnY(rowIndex: RowIndex) {
        yOffsets.nextIndex = rowIndex
    }

    fun setNextIndexOnX(index: Int) {
        xOffsets.nextIndex = index
    }

    fun setNextRecordIndex(record: Int) {
        yOffsets.nextRecordIndex = record
    }
}

data class XOffsets(
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

data class YOffsets(
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