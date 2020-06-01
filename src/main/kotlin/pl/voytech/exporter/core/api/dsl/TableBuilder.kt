package pl.voytech.exporter.core.api.dsl

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.extension.*

fun <T> table(block: TableBuilder<T>.() -> Unit): Table<T> = TableBuilder<T>().apply(block).build()

class TableBuilder<T> {
    var name: String? = "untitled"
    var firstRow: Int? = 0
    var firstColumn: Int? = 0
    private lateinit var columns: List<Column<T>>
    private var rows: List<Row<T>>? = null
    private var showHeader: Boolean? = false
    private var showFooter: Boolean? = false
    private var columnsDescription: Description? = null
    private var rowsDescription: Description? = null
    private var tableExtensions: Set<TableExtension>? = null
    private var cellExtensions: Set<CellExtension>? = null

    init {
        NextId.reset()
    }

    fun columns(block: ColumnsBuilder<T>.() -> Unit) {
        columns = ColumnsBuilder<T>().apply(block)
    }

    fun rows(block: RowsBuilder<T>.() -> Unit) {
        rows = RowsBuilder<T>().apply(block)
    }

    fun tableExtensions(block: ExtensionsBuilder<TableExtension>.() -> Unit) {
        tableExtensions = ExtensionsBuilder<TableExtension>().apply(block)
    }

    fun cellExtensions(block: ExtensionsBuilder<CellExtension>.() -> Unit) {
        cellExtensions = ExtensionsBuilder<CellExtension>().apply(block)
    }

    fun columnsDescription(block: DescriptionBuilder.() -> Unit) {
        columnsDescription = DescriptionBuilder().apply(block).build()
    }

    fun rowsDescription(block: DescriptionBuilder.() -> Unit) {
        rowsDescription = DescriptionBuilder().apply(block).build()
    }

    fun build(): Table<T> = Table(
        name, firstRow, firstColumn, columns, rows, showHeader,
        showFooter, columnsDescription, rowsDescription,
        tableExtensions, cellExtensions
    )
}

class ColumnsBuilder<T> : ArrayList<Column<T>>() {

    fun column(id: String, block: ColumnBuilder<T>.() -> Unit) {
        add(ColumnBuilder<T>(id = Key(id)).apply(block).build())
    }

    fun column(ref: ((record: T) -> Any?), block: ColumnBuilder<T>.() -> Unit) {
        add(ColumnBuilder(id = Key(ref = ref)).apply(block).build())
    }

    fun column(id: String) {
        add(ColumnBuilder<T>(id = Key(id)).build())
    }

    fun column(ref: ((record: T) -> Any?)) {
        add(ColumnBuilder(id = Key(ref = ref)).build())
    }
}

class ColumnBuilder<T> {
    lateinit var id: Key<T>
    private var columnTitle: Description? = null
    var columnType: CellType? = null
    var index: Int? = null
    private var columnExtensions: Set<ColumnExtension>? = null
    private var cellExtensions: Set<CellExtension>? = null
    var dataFormatter: ((field: Any) -> Any)? = null

    constructor()

    constructor(id: Key<T>) {
        this.id = id
    }

    fun columnTitle(block: DescriptionBuilder.() -> Unit) {
        columnTitle = DescriptionBuilder().apply(block).build()
    }

    fun columnExtensions(block: ExtensionsBuilder<ColumnExtension>.() -> Unit) {
        columnExtensions = ExtensionsBuilder<ColumnExtension>().apply(block)
    }

    fun columnExtensions(vararg extensions: ColumnExtension) {
        columnExtensions = extensions.toHashSet()
    }

    fun cellExtensions(block: ExtensionsBuilder<CellExtension>.() -> Unit) {
        cellExtensions = ExtensionsBuilder<CellExtension>().apply(block)
    }

    fun cellExtensions(vararg extensions: CellExtension) {
        cellExtensions = extensions.toHashSet()
    }

    fun build(): Column<T> = Column(id, index, columnTitle, columnType, columnExtensions, cellExtensions, dataFormatter)
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

    fun row(at: Int, block: RowBuilder<T>.() -> Unit) {
        val builder = RowBuilder<T>()
        builder.createAt = at
        add(builder.apply(block).build())
    }
}

class RowBuilder<T> {
    private var cells: Map<Key<T>, Cell<T>>? = null
    private var rowExtensions: Set<RowExtension>? = null
    private var cellExtensions: Set<CellExtension>? = null
    var selector: RowSelector<T>? = null
    var createAt: Int? = null

    fun rowExtensions(block: ExtensionsBuilder<RowExtension>.() -> Unit) {
        rowExtensions = ExtensionsBuilder<RowExtension>().apply(block)
    }

    fun rowExtensions(vararg extensions: RowExtension) {
        rowExtensions = extensions.toHashSet()
    }

    fun cellExtensions(block: ExtensionsBuilder<CellExtension>.() -> Unit) {
        cellExtensions = ExtensionsBuilder<CellExtension>().apply(block)
    }

    fun cellExtensions(vararg extensions: CellExtension) {
        cellExtensions = extensions.toHashSet()
    }

    fun cells(block: CellsBuilder<T>.() -> Unit) {
        cells = CellsBuilder<T>().apply(block)
    }

    fun build(): Row<T> = Row(selector, createAt, rowExtensions, cellExtensions, cells)
}

class ExtensionsBuilder<T> : HashSet<T>() {
    fun extend(extension: T) {
        add(extension)
    }
}

class CellsBuilder<T> : HashMap<Key<T>, Cell<T>>() {
    fun forColumn(id: String, block: CellBuilder<T>.() -> Unit) {
        put(Key(id), CellBuilder<T>().apply(block).build())
    }

    fun forColumn(ref: ((record: T) -> Any?), block: CellBuilder<T>.() -> Unit) {
        put(Key(ref = ref), CellBuilder<T>().apply(block).build())
    }
}

class CellBuilder<T> {
    private var cellExtensions: Set<CellExtension>? = null
    var value: Any? = null
    var eval: RowCellEval<T>? = null
    var type: CellType? = null

    fun cellExtensions(block: ExtensionsBuilder<CellExtension>.() -> Unit) {
        cellExtensions = ExtensionsBuilder<CellExtension>().apply(block)
    }

    fun cellExtensions(vararg extensions: CellExtension) {
        cellExtensions = extensions.toHashSet()
    }

    fun build(): Cell<T> = Cell(value, eval, type, cellExtensions)
}

class DescriptionBuilder {
    lateinit var title: String
    var extensions: Set<Extension>? = null

    fun extensions(block: ExtensionsBuilder<Extension>.() -> Unit) {
        extensions = ExtensionsBuilder<Extension>().apply(block)
    }

    fun extensions(vararg extensions: Extension) {
        this.extensions = extensions.toHashSet()
    }

    fun build(): Description = Description(title, extensions)
}
