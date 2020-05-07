package pl.voytech.exporter.core.api.dsl

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.hints.CellType
import pl.voytech.exporter.core.model.hints.CellTypeHint
import pl.voytech.exporter.core.model.hints.Hint
import java.time.LocalDate

fun <T> table(block: TableBuilder<T>.() -> Unit): Table<T> = TableBuilder<T>().apply(block).build()

class TableBuilder<T> {
    private var columns : List<Column<T>> = mutableListOf()
    var showHeader: Boolean? = false
    var showFooter: Boolean? = false
    var name: String? = ""
    var headerText: String? = ""
    var footerText: String? =""

    fun columns(block: ColumnsBuilder<T>.() -> Unit) {
        columns = ColumnsBuilder<T>().apply(block)
    }

    fun build() : Table<T> = Table(columns, showHeader, showFooter, name, headerText, footerText)
}

class ColumnsBuilder<T> : ArrayList<Column<T>>() {

    fun column(block: ColumnBuilder<T>.() -> Unit) {
        add(ColumnBuilder<T>().apply(block).build())
    }

    fun column(title: String, block: ColumnBuilder<T>.() -> Unit) {
        add(ColumnBuilder<T>(title = title).apply(block).build())
    }

}

class ColumnBuilder<T> {
    private var rowRanges: List<LongRange> = mutableListOf(infinite())
    var columnTitle: String? = ""
    var fromField: (record: T) -> Any? = fun (record: T) { record }
    private var hints: List<Hint>? = mutableListOf()
    private var cells: Map<Row, Cell>? = mutableMapOf()

    constructor()

    constructor(title: String) {
        this.columnTitle = title
    }

    fun hints(block: HintsBuilder.() -> Unit) {
        hints = HintsBuilder().apply(block)
    }

    fun rowRanges(block: RowRangesBuilder.() -> Unit) {
        rowRanges = RowRangesBuilder().apply(block)
    }

    fun cells(block: CellsBuilder.() -> Unit) {
        cells = CellsBuilder().apply(block)
    }

    fun build() : Column<T> = Column(rowRanges, columnTitle, fromField, hints, cells)
}

class HintsBuilder : ArrayList<Hint>() {
    fun hint(hint: Hint) {
        add(hint)
    }
}

class RowRangesBuilder: ArrayList<LongRange>() {

    fun range(range: LongRange) {
        add(range)
    }

    fun range(block: LongRangeBuilder.() -> Unit) {
        add(LongRangeBuilder().apply(block).build())
    }
}

class LongRangeBuilder {
    var startAt: Long = 0L
    var endAt: Long = Long.MAX_VALUE

    fun build(): LongRange = LongRange(startAt, endAt)
}

class CellsBuilder: HashMap<Row,Cell>() {
    fun at(index: Int, block: CellBuilder.() -> Unit) {
        put(Row(index), CellBuilder().apply(block).build())
    }
    fun at(index: Row.Position, block: CellBuilder.() -> Unit) {
        put(Row(index), CellBuilder().apply(block).build())
    }
}

class CellBuilder {
    private var hints: List<Hint>? = mutableListOf()
    lateinit var value: Any

    fun hints(block: HintsBuilder.() -> Unit) {
        hints = HintsBuilder().apply(block)
    }

    fun build(): Cell = Cell(hints, value)
}

