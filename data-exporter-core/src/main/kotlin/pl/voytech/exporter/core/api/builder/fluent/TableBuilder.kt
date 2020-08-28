package pl.voytech.exporter.core.api.builder.fluent

import pl.voytech.exporter.core.api.builder.*
import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.extension.*

class TableBuilder<T> : Builder<Table<T>> {
    @set:JvmSynthetic
    private var name: String? = "untitled"
    @set:JvmSynthetic
    private var firstRow: Int? = 0
    @set:JvmSynthetic
    private var firstColumn: Int? = 0
    @set:JvmSynthetic
    private var columns: List<Column<T>> = emptyList()
    @set:JvmSynthetic
    private var rows: List<Row<T>>? = null
    @set:JvmSynthetic
    private var showHeader: Boolean? = false
    @set:JvmSynthetic
    private var showFooter: Boolean? = false
    @set:JvmSynthetic
    private var columnsDescription: Description? = null
    @set:JvmSynthetic
    private var rowsDescription: Description? = null
    @set:JvmSynthetic
    private var tableExtensions: Set<TableExtension>? = null
    @set:JvmSynthetic
    private var cellExtensions: Set<CellExtension>? = null

    init {
        NextId.reset()
    }

    fun name(name: String?) = apply {
        this.name = name
    }

    fun firstRow(firstRow: Int?) = apply {
        this.firstRow = firstRow
    }

    fun firstColumn(firstColumn: Int?) = apply {
        this.firstColumn = firstColumn
    }

    fun columns(vararg columnBuilders: ColumnBuilder<T>) = apply {
        columns = columns + columnBuilders.map { it.build() }
    }

    fun rows(vararg rowBuilders: RowBuilder<T>) = apply {
        rows = (rows ?: emptyList()) + rowBuilders.map { it.build() }
    }

    fun tableExtensions(vararg extensions: TableExtension) = apply {
        tableExtensions = (tableExtensions ?: emptySet()) + extensions.toHashSet()
    }

    fun cellExtensions(vararg extensions: CellExtension) = apply {
        cellExtensions = (cellExtensions ?: emptySet()) + extensions.toHashSet()
    }

    fun <T: TableExtensionBuilder> tableExtensions(vararg extensionBuilder: T) = apply {
        tableExtensions = (tableExtensions ?: emptySet()) + extensionBuilder.map { it.build() }
    }

    fun <T: CellExtensionBuilder> cellExtensions(vararg extensionBuilder: T) = apply  {
        cellExtensions = (cellExtensions ?: emptySet()) + extensionBuilder.map { it.build() }
    }

    override fun build(): Table<T> = Table(
        name, firstRow, firstColumn, columns, rows, showHeader,
        showFooter, columnsDescription, rowsDescription,
        tableExtensions, cellExtensions
    )

}

fun <T> Table<T>.builder() = TableBuilder<T>()

class ColumnsBuilder<T> : InternalBuilder<List<Column<T>>>() {

    @set:JvmSynthetic
    private var columns: List<Column<T>> = emptyList()

    fun column(id: String, builder: ColumnBuilder<T>) = apply {
        columns = columns + builder.also { it.id(Key(id = id)) }.build()
    }

    fun column(ref: ((record: T) -> Any?), builder: ColumnBuilder<T>) = apply {
        columns = columns + builder.also { it.id(Key(ref = ref)) }.build()
    }

    fun column(id: String) = apply {
        columns = columns + (ColumnBuilder<T>()
            .also { it.id(Key(id = id)) }.build())
    }

    fun column(ref: ((record: T) -> Any?)) = apply {
        columns = columns + (ColumnBuilder<T>()
            .also { it.id(Key(ref = ref)) }.build())
    }

    override fun build(): List<Column<T>> = columns

}

class ColumnBuilder<T> : InternalBuilder<Column<T>>() {
    @set:JvmSynthetic
    private lateinit var id: Key<T>
    @set:JvmSynthetic
    private var columnType: CellType? = null
    @set:JvmSynthetic
    private var index: Int? = null
    @set:JvmSynthetic
    private var columnExtensions: Set<ColumnExtension>? = null
    @set:JvmSynthetic
    private var cellExtensions: Set<CellExtension>? = null
    @set:JvmSynthetic
    private var dataFormatter: ((field: Any) -> Any)? = null

    /**
     * JAVA style builder.
     **/

    fun id(id : Key<T>) = apply {
        this.id = id
    }

    fun dataFormatter(dataFormatter: ((field: Any) -> Any)?) = apply {
        this.dataFormatter = dataFormatter
    }

    fun index(index: Int?) = apply {
        this.index = index
    }

    fun columnType(columnType: CellType?) = apply {
        this.columnType = columnType
    }

    fun cellExtensions(vararg extensions: CellExtension) = apply {
        cellExtensions = (cellExtensions ?: emptySet()) + extensions.toHashSet()
    }

    fun columnExtensions(vararg extensions: ColumnExtension) = apply {
        columnExtensions = (columnExtensions ?: emptySet()) + extensions.toHashSet()
    }

    fun <T: ColumnExtensionBuilder> columnExtensions(vararg extensionBuilder: T) = apply {
        columnExtensions = (columnExtensions ?: emptySet()) + extensionBuilder.map { it.build() }
    }

    fun <T: CellExtensionBuilder> cellExtensions(vararg  extensionBuilder: T) = apply {
        cellExtensions = (cellExtensions ?: emptySet()) + extensionBuilder.map { it.build() }
    }

    override fun build(): Column<T> = Column(id, index, columnType, columnExtensions, cellExtensions, dataFormatter)

}

class RowsBuilder<T> : InternalBuilder<List<Row<T>>>() {

    @set:JvmSynthetic
    private var rows: List<Row<T>> = emptyList()

    fun row(builder: RowBuilder<T>) = apply {
        rows = rows + builder.build()
    }

    fun row(selector: RowSelector<T>, builder: RowBuilder<T>) = apply {
        rows = rows + builder.selector(selector).build()
    }

    fun row(at: Int, builder: RowBuilder<T>) {
        rows = rows + builder.createAt(at).build()
    }

    override fun build(): List<Row<T>> = rows

}

class RowBuilder<T> : InternalBuilder<Row<T>>() {
    @set:JvmSynthetic
    private var cells: Map<Key<T>, Cell<T>>? = null
    @set:JvmSynthetic
    private var rowExtensions: Set<RowExtension>? = null
    @set:JvmSynthetic
    private var cellExtensions: Set<CellExtension>? = null
    @set:JvmSynthetic
    private var selector: RowSelector<T>? = null
    @set:JvmSynthetic
    private var createAt: Int? = null

    fun selector(selector: RowSelector<T>?) = apply {
        this.selector = selector
    }

    fun createAt(createAt : Int?) = apply {
        this.createAt = createAt
    }

    fun rowExtensions(vararg extensions: RowExtension) = apply {
        rowExtensions = (rowExtensions ?: emptySet()) + extensions.toHashSet()
    }

    fun cellExtensions(vararg extensions: CellExtension) = apply {
        cellExtensions = (cellExtensions ?: emptySet()) + extensions.toHashSet()
    }

    fun <T: RowExtensionBuilder> rowExtensions(vararg extensionBuilder: T) = apply {
        rowExtensions = (rowExtensions ?: emptySet()) + extensionBuilder.map { it.build() }
    }

    fun <T: CellExtensionBuilder> cellExtensions(vararg extensionBuilder: T) = apply {
        cellExtensions = (cellExtensions ?: emptySet()) + extensionBuilder.map { it.build() }
    }

    fun cells(cellsBuilder: CellsBuilder<T>) = apply {
        cells = (cells ?: emptyMap()) + cellsBuilder.build()
    }

    override fun build(): Row<T> = Row(selector, createAt, rowExtensions, cellExtensions, cells)

}

class CellsBuilder<T> : InternalBuilder<Map<Key<T>, Cell<T>>>() {

    @set:JvmSynthetic
    private var cells: Map<Key<T>, Cell<T>> = emptyMap()

    fun forColumn(id: String, builder: CellBuilder<T>) {
        cells = cells  + Pair(Key(id), builder.build())
    }

    fun forColumn(ref: ((record: T) -> Any?), builder: CellBuilder<T>) {
        cells = cells + Pair(Key(ref = ref), builder.build())
    }

    override fun build(): Map<Key<T>, Cell<T>> {
        return cells
    }

}

class CellBuilder<T> : InternalBuilder<Cell<T>>() {
    @set:JvmSynthetic
    private var cellExtensions: Set<CellExtension>? = null

    @set:JvmSynthetic
    private var value: Any? = null

    @set:JvmSynthetic
    private var eval: RowCellEval<T>? = null

    @set:JvmSynthetic
    private var type: CellType? = null

    fun cellExtensions(vararg extensions: CellExtension) = apply {
        cellExtensions = (cellExtensions ?: emptySet()) + extensions.toHashSet()
    }

    fun <T : CellExtensionBuilder> cellExtensions(vararg extensionBuilder: T) = apply {
        cellExtensions = (cellExtensions ?: emptySet()) + extensionBuilder.map { it.build() }
    }

    fun value(value: Any?) = apply {
        this.value = value
    }

    fun eval(eval: RowCellEval<T>?) = apply {
        this.eval = eval
    }

    fun type(type: CellType?) = apply {
        this.type = type
    }

    override fun build(): Cell<T> = Cell(value, eval, type, cellExtensions)

}