package io.github.voytech.tabulate.components.table.template

import io.github.voytech.tabulate.components.table.model.ColumnDef
import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.operation.RowEnd
import io.github.voytech.tabulate.core.template.ExportInstance
import io.github.voytech.tabulate.core.template.TemplateContext
import io.github.voytech.tabulate.core.template.ExportStatus

/**
 * Represents entire state produced throughout exporting process. State itself is separated from [TabulationTemplate].
 * [TabulationTemplate] creates [TypedTableTemplateContext] instance internally at every 'tabulate' method invocation so that
 * no additional state needs to be stored by [TabulationTemplate].
 * [TypedTableTemplateContext] manages following properties:
 * @property rowContextResolver - a strategy for transforming requested row index, table model, and collection elements
 * into [RowContextWithCells] which is used then by renderer.
 * @property rowContextIterator - iterates over elements and uses [RowContextResolver] in order to resolve [RowContextWithCells] for
 * requested index.
 * @property stateAttributes - map of generic attributes that may be shared during exporting.
 * @since 0.1.0
 * @author Wojciech Mąka
 */
class TableTemplateContext<T : Any>(
    tableModel: Table<T>,
    stateAttributes: MutableMap<String, Any>,
    instance: ExportInstance,
    val dataSource: Iterable<T>? = null,
    private var remainingRecords: Iterable<T>? = dataSource,
) : TemplateContext<TableTemplateContext<T>, Table<T>>(tableModel, stateAttributes, instance) {

    private lateinit var rowContextResolver: AccumulatingRowContextResolver<T>

    private lateinit var rowContextIterator: RowContextIterator<T>

    val indices = OverflowOffsets() //TODO rename to  PaginationProgress ?

    fun cropDataSource(): Iterable<T>? = with(indices) {
        remainingRecords?.crop().also { remainingRecords = it }
    }

    init {
        stateAttributes.computeIfAbsent("_sheetName") { tableModel.name }
    }

    internal fun setupRowResolver(captureRowCompletion: CaptureRowCompletion<T>) {
        rowContextResolver = AccumulatingRowContextResolver(model, stateAttributes, indices, captureRowCompletion)
        rowContextIterator = RowContextIterator(rowContextResolver, this)
    }

    fun beforeResume() {
        indices.onResume()
    }

    fun keepStatus() {
        indices.keepStatus(status)
    }

    /**
     * Captures next element to be rendered at some point of time.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    fun capture(record: T): ContextResult<RowEnd<T>>? {
        rowContextResolver.append(record)
        return next()
    }

    /**
     * Resolves next element.
     * @author Wojciech Mąka
     * @since 0.1.0
     */
    fun next(): ContextResult<RowEnd<T>>? {
        return if (rowContextIterator.hasNext()) rowContextIterator.next() else null
    }

    fun reset() {
        indices.reset()
    }
}

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

    fun keepStatus(status: ExportStatus) {
        partialStatus = status
    }

    fun onResume() {
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