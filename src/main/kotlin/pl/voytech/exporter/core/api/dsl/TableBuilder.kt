package pl.voytech.exporter.core.api.dsl

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.hints.ColumnHint
import pl.voytech.exporter.core.model.hints.Hint
import pl.voytech.exporter.core.model.hints.TableHint

fun <T> table(block: TableBuilder<T>.() -> Unit): Table<T> = TableBuilder<T>().apply(block).build()

class TableBuilder<T> {
    var name: String? = "untitled"
    private lateinit var columns : List<Column<T>>
    private var rows: List<Row<T>>? = null
    var showHeader: Boolean? = false
    var showFooter: Boolean? = false
    private var columnsDescription: Description? = null
    private var rowsDescription: Description? = null
    private var hints: List<TableHint>? = null

    init {
        ColumnNextId.reset()
    }

    fun columns(block: ColumnsBuilder<T>.() -> Unit) {
        columns = ColumnsBuilder<T>().apply(block)
    }

    fun rows(block: RowsBuilder<T>.() -> Unit) {
        rows = RowsBuilder<T>().apply(block)
    }

    fun hints(block: HintsBuilder<TableHint>.() -> Unit) {
        hints = HintsBuilder<TableHint>().apply(block)
    }

    fun columnsDescription(block: DescriptionBuilder.() -> Unit) {
        columnsDescription = DescriptionBuilder().apply(block).build()
    }

    fun rowsDescription(block: DescriptionBuilder.() -> Unit) {
        rowsDescription = DescriptionBuilder().apply(block).build()
    }

    fun build() : Table<T> = Table(name, columns, rows, showHeader, showFooter,  columnsDescription, rowsDescription, hints)
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
    var columnType: CellType = CellType.STRING
    lateinit var fromField: (record: T) -> Any?
    private var hints: List<ColumnHint>? = null

    constructor()

    constructor(id: String) {
        this.id = id
    }

    fun columnTitle(block: DescriptionBuilder.() -> Unit) {
        columnTitle = DescriptionBuilder().apply(block).build()
    }

    fun hints(block: HintsBuilder<ColumnHint>.() -> Unit) {
        hints = HintsBuilder<ColumnHint>().apply(block)
    }

    fun build() : Column<T> = Column(id, columnTitle, columnType, fromField, hints)
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
    private var cells: Map<String, Cell>? = null
    private var hints: List<Hint>? = null
    lateinit var selector: RowSelector<T>

    fun hints(block: HintsBuilder<Hint>.() -> Unit) {
        hints = HintsBuilder<Hint>().apply(block)
    }

    fun cells(block: CellsBuilder.() -> Unit) {
        cells = CellsBuilder().apply(block)
    }

    fun build() : Row<T> = Row(selector, hints, cells)
}

class HintsBuilder<T : TableHint> : ArrayList<T>() {
    fun hint(hint: T) {
        add(hint)
    }
}

class CellsBuilder: HashMap<String,Cell>() {
    fun forColumn(id: String, block: CellBuilder.() -> Unit) {
        put(id, CellBuilder().apply(block).build())
    }
}

class CellBuilder {
    private var hints: List<Hint>? = null
    var value: Any? = null
    var type: CellType? = null

    fun hints(block: HintsBuilder<Hint>.() -> Unit) {
        hints = HintsBuilder<Hint>().apply(block)
    }

    fun build(): Cell = Cell(value, type, hints)
}

class DescriptionBuilder {
    lateinit var title: String
    var hints: List<Hint>? = null

    fun hints(block: HintsBuilder<Hint>.() -> Unit) {
        hints = HintsBuilder<Hint>().apply(block)
    }

    fun build(): Description = Description(title, hints)
}
