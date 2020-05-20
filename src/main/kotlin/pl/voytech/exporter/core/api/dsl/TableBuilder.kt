package pl.voytech.exporter.core.api.dsl

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.model.hints.*

fun <T> table(block: TableBuilder<T>.() -> Unit): Table<T> = TableBuilder<T>().apply(block).build()

class TableBuilder<T> {
    var name: String? = "untitled"
    private lateinit var columns : List<Column<T>>
    private var rows: List<Row<T>>? = null
    var showHeader: Boolean? = false
    var showFooter: Boolean? = false
    private var columnsDescription: Description? = null
    private var rowsDescription: Description? = null
    private var tableHints: Set<TableHint>? = null
    private var cellHints: Set<CellHint>? = null

    init {
        ColumnNextId.reset()
    }

    fun columns(block: ColumnsBuilder<T>.() -> Unit) {
        columns = ColumnsBuilder<T>().apply(block)
    }

    fun rows(block: RowsBuilder<T>.() -> Unit) {
        rows = RowsBuilder<T>().apply(block)
    }

    fun tableHints(block: HintsBuilder<TableHint>.() -> Unit) {
        tableHints = HintsBuilder<TableHint>().apply(block)
    }

    fun cellHints(block: HintsBuilder<CellHint>.() -> Unit) {
        cellHints = HintsBuilder<CellHint>().apply(block)
    }

    fun columnsDescription(block: DescriptionBuilder.() -> Unit) {
        columnsDescription = DescriptionBuilder().apply(block).build()
    }

    fun rowsDescription(block: DescriptionBuilder.() -> Unit) {
        rowsDescription = DescriptionBuilder().apply(block).build()
    }

    fun build() : Table<T> = Table(name, columns, rows, showHeader, showFooter,  columnsDescription, rowsDescription, tableHints, cellHints)
}

class ColumnsBuilder<T> : ArrayList<Column<T>>() {

    fun column(block: ColumnBuilder<T>.() -> Unit) {
        add(ColumnBuilder<T>().apply(block).build())
    }

    fun column(id: String, block: ColumnBuilder<T>.() -> Unit) {
        add(ColumnBuilder<T>(id = id).apply(block).build())
    }
}

class ColumnBuilder<T> {
    var id: String = "col-${ColumnNextId.nextId()}"
    private var columnTitle: Description? = null
    var columnType: CellType? = null
    var fromField: ((record: T) -> Any?)? = null
    var index: Int? = null
    private var columnHints: Set<ColumnHint>? = null
    private var cellHints: Set<CellHint>? = null

    constructor()

    constructor(id: String) {
        this.id = id
    }

    fun columnTitle(block: DescriptionBuilder.() -> Unit) {
        columnTitle = DescriptionBuilder().apply(block).build()
    }

    fun columnHints(block: HintsBuilder<ColumnHint>.() -> Unit) {
        columnHints = HintsBuilder<ColumnHint>().apply(block)
    }

    fun columnHints(vararg hints: ColumnHint) {
        columnHints = hints.asList().toHashSet()
    }

    fun cellHints(block: HintsBuilder<CellHint>.() -> Unit) {
        cellHints = HintsBuilder<CellHint>().apply(block)
    }

    fun cellHints(vararg hints: CellHint) {
        cellHints = hints.asList().toHashSet()
    }

    fun build() : Column<T> = Column(id, index, columnTitle, columnType, fromField, columnHints, cellHints)
}

class RowsBuilder<T> : ArrayList<Row<T>>() {

    fun row(block: RowBuilder<T>.() -> Unit) {
        add(RowBuilder<T>().apply(block).build())
    }

    fun row(selector: RowSelector<T>, block: RowBuilder<T>.() -> Unit) {
        val builder = RowBuilder<T>()
        builder.selector = selector
        add(builder.apply(block).build())
    }

}

class RowBuilder<T> {
    private var cells: Map<String, Cell<T>>? = null
    private var rowHints: Set<RowHint>? = null
    private var cellHints: Set<CellHint>? = null
    lateinit var selector: RowSelector<T>

    fun rowHints(block: HintsBuilder<RowHint>.() -> Unit) {
        rowHints = HintsBuilder<RowHint>().apply(block)
    }

    fun cellHints(block: HintsBuilder<CellHint>.() -> Unit) {
        cellHints = HintsBuilder<CellHint>().apply(block)
    }

    fun cells(block: CellsBuilder<T>.() -> Unit) {
        cells = CellsBuilder<T>().apply(block)
    }

    fun build() : Row<T> = Row(selector, rowHints, cellHints, cells)
}

class HintsBuilder<T> : HashSet<T>() {
    fun hint(hint: T) {
        add(hint)
    }
}

class CellsBuilder<T>: HashMap<String,Cell<T>>() {
    fun forColumn(id: String, block: CellBuilder<T>.() -> Unit) {
        put(id, CellBuilder<T>().apply(block).build())
    }
}

class CellBuilder<T> {
    private var cellHints: Set<CellHint>? = null
    var value: Any? = null
    var eval: RowCellEval<T>? = null
    var type: CellType? = null

    fun cellHints(block: HintsBuilder<CellHint>.() -> Unit) {
        cellHints = HintsBuilder<CellHint>().apply(block)
    }

    fun build(): Cell<T> = Cell(value, eval, type, cellHints)
}

class DescriptionBuilder {
    lateinit var title: String
    var hints: Set<Hint>? = null

    fun hints(block: HintsBuilder<Hint>.() -> Unit) {
        hints = HintsBuilder<Hint>().apply(block)
    }

    fun build(): Description = Description(title, hints)
}
