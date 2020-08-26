package pl.voytech.exporter.core.api.dsl

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.extension.*

@DslMarker
annotation class TableMarker

@JvmSynthetic
fun <T> table(block: TableBuilder<T>.() -> Unit): Table<T> = TableBuilder<T>().apply(block).build()

@TableMarker
class TableBuilder<T> {
    var name: String? = "untitled"
    var firstRow: Int? = 0
    var firstColumn: Int? = 0
    private var columns: List<Column<T>> = emptyList()
    private var rows: List<Row<T>>? = null
    var showHeader: Boolean? = false
    var showFooter: Boolean? = false
    private var columnsDescription: Description? = null
    private var rowsDescription: Description? = null
    private var tableExtensions: Set<TableExtension>? = null
    private var cellExtensions: Set<CellExtension>? = null

    init {
        NextId.reset()
    }

    @JvmSynthetic
    fun columns(block: ColumnsBuilder<T>.() -> Unit) {
        columns = columns + ColumnsBuilder<T>().apply(block)
    }

    @JvmSynthetic
    fun rows(block: RowsBuilder<T>.() -> Unit) {
        rows = (rows ?: emptyList()) + RowsBuilder<T>().apply(block)
    }

    @JvmSynthetic
    fun tableExtensions(block: ExtensionsBuilder<TableExtension>.() -> Unit) {
        tableExtensions = (tableExtensions ?: emptySet()) + ExtensionsBuilder<TableExtension>().apply(block)
    }

    @JvmSynthetic
    fun tableExtensions(vararg extensions: TableExtension) {
        tableExtensions = (tableExtensions ?: emptySet()) + extensions.toHashSet()
    }

    @JvmSynthetic
    fun cellExtensions(block: ExtensionsBuilder<CellExtension>.() -> Unit) {
        cellExtensions = (cellExtensions ?: emptySet()) + ExtensionsBuilder<CellExtension>().apply(block)
    }

    @JvmSynthetic
    fun cellExtensions(vararg extensions: CellExtension) {
        cellExtensions = (cellExtensions ?: emptySet()) + extensions.toHashSet()
    }

    @JvmSynthetic
    fun columnsDescription(block: DescriptionBuilder.() -> Unit) {
        columnsDescription = DescriptionBuilder().apply(block).build()
    }

    @JvmSynthetic
    fun rowsDescription(block: DescriptionBuilder.() -> Unit) {
        rowsDescription = DescriptionBuilder().apply(block).build()
    }

    @JvmSynthetic
    fun build(): Table<T> = Table(
        name, firstRow, firstColumn, columns, rows, showHeader,
        showFooter, columnsDescription, rowsDescription,
        tableExtensions, cellExtensions
    )
}

@TableMarker
class ColumnsBuilder<T> : ArrayList<Column<T>>() {

    @JvmSynthetic
    fun column(id: String, block: ColumnBuilder<T>.() -> Unit) {
        add(ColumnBuilder<T>(id = Key(id)).apply(block).build())
    }

    @JvmSynthetic
    fun column(ref: ((record: T) -> Any?), block: ColumnBuilder<T>.() -> Unit) {
        add(ColumnBuilder(id = Key(ref = ref)).apply(block).build())
    }

    @JvmSynthetic
    fun column(id: String) {
        add(ColumnBuilder<T>(id = Key(id)).build())
    }

    @JvmSynthetic
    fun column(ref: ((record: T) -> Any?)) {
        add(ColumnBuilder(id = Key(ref = ref)).build())
    }
}

@TableMarker
class ColumnBuilder<T>(val id: Key<T>) {
    var columnType: CellType? = null
    var index: Int? = null
    private var columnExtensions: Set<ColumnExtension>? = null
    private var cellExtensions: Set<CellExtension>? = null
    var dataFormatter: ((field: Any) -> Any)? = null

    @JvmSynthetic
    fun columnExtensions(block: ExtensionsBuilder<ColumnExtension>.() -> Unit) {
        columnExtensions = (columnExtensions ?: emptySet()) + ExtensionsBuilder<ColumnExtension>().apply(block)
    }

    @JvmSynthetic
    fun columnExtensions(vararg extensions: ColumnExtension) {
        columnExtensions = (columnExtensions ?: emptySet()) + extensions.toHashSet()
    }

    @JvmSynthetic
    fun cellExtensions(block: ExtensionsBuilder<CellExtension>.() -> Unit) {
        cellExtensions = (cellExtensions ?: emptySet()) + ExtensionsBuilder<CellExtension>().apply(block)
    }

    @JvmSynthetic
    fun cellExtensions(vararg extensions: CellExtension) {
        cellExtensions = (cellExtensions ?: emptySet()) + extensions.toHashSet()
    }

    @JvmSynthetic
    fun build(): Column<T> = Column(id, index, columnType, columnExtensions, cellExtensions, dataFormatter)
}

@TableMarker
class RowsBuilder<T> : ArrayList<Row<T>>() {

    @JvmSynthetic
    fun row(block: RowBuilder<T>.() -> Unit) {
        add(RowBuilder<T>().apply(block).build())
    }

    @JvmSynthetic
    fun row(selector: RowSelector<T>, block: RowBuilder<T>.() -> Unit) {
        val builder = RowBuilder<T>()
        builder.selector = selector
        add(builder.apply(block).build())
    }

    @JvmSynthetic
    fun row(at: Int, block: RowBuilder<T>.() -> Unit) {
        val builder = RowBuilder<T>()
        builder.createAt = at
        add(builder.apply(block).build())
    }
}

@TableMarker
class RowBuilder<T> {
    private var cells: Map<Key<T>, Cell<T>>? = null
    private var rowExtensions: Set<RowExtension>? = null
    private var cellExtensions: Set<CellExtension>? = null
    var selector: RowSelector<T>? = null
    var createAt: Int? = null

    @JvmSynthetic
    fun rowExtensions(block: ExtensionsBuilder<RowExtension>.() -> Unit) {
        rowExtensions = (rowExtensions ?: emptySet()) + ExtensionsBuilder<RowExtension>().apply(block)
    }

    @JvmSynthetic
    fun rowExtensions(vararg extensions: RowExtension) {
        rowExtensions = (rowExtensions ?: emptySet()) + extensions.toHashSet()
    }

    @JvmSynthetic
    fun cellExtensions(block: ExtensionsBuilder<CellExtension>.() -> Unit) {
        cellExtensions = (cellExtensions ?: emptySet()) + ExtensionsBuilder<CellExtension>().apply(block)
    }

    @JvmSynthetic
    fun cellExtensions(vararg extensions: CellExtension) {
        cellExtensions = (cellExtensions ?: emptySet()) + extensions.toHashSet()
    }

    @JvmSynthetic
    fun cells(block: CellsBuilder<T>.() -> Unit) {
        cells = (cells ?: emptyMap()) + CellsBuilder<T>().apply(block)
    }

    @JvmSynthetic
    fun build(): Row<T> = Row(selector, createAt, rowExtensions, cellExtensions, cells)
}

@TableMarker
class ExtensionsBuilder<T> : HashSet<T>() {
    fun extend(extension: T) {
        add(extension)
    }
}

@TableMarker
class CellsBuilder<T> : HashMap<Key<T>, Cell<T>>() {

    @JvmSynthetic
    fun forColumn(id: String, block: CellBuilder<T>.() -> Unit) {
        put(Key(id), CellBuilder<T>().apply(block).build())
    }

    @JvmSynthetic
    fun forColumn(ref: ((record: T) -> Any?), block: CellBuilder<T>.() -> Unit) {
        put(Key(ref = ref), CellBuilder<T>().apply(block).build())
    }
}

@TableMarker
class CellBuilder<T> {
    private var cellExtensions: Set<CellExtension>? = null
    var value: Any? = null
    var eval: RowCellEval<T>? = null
    var type: CellType? = null

    @JvmSynthetic
    fun cellExtensions(block: ExtensionsBuilder<CellExtension>.() -> Unit) {
        cellExtensions = (cellExtensions ?: emptySet()) + ExtensionsBuilder<CellExtension>().apply(block)
    }

    @JvmSynthetic
    fun cellExtensions(vararg extensions: CellExtension) {
        cellExtensions = (cellExtensions ?: emptySet()) + extensions.toHashSet()
    }

    @JvmSynthetic
    fun build(): Cell<T> = Cell(value, eval, type, cellExtensions)
}

@TableMarker
class DescriptionBuilder {
    lateinit var title: String
    var extensions: Set<Extension>? = null

    @JvmSynthetic
    fun extensions(block: ExtensionsBuilder<Extension>.() -> Unit) {
        extensions = (extensions ?: emptySet()) + ExtensionsBuilder<Extension>().apply(block)
    }

    @JvmSynthetic
    fun extensions(vararg extensions: Extension) {
        this.extensions = (this.extensions ?: emptySet()) + extensions.toHashSet()
    }

    @JvmSynthetic
    fun build(): Description = Description(title, extensions)
}
