package pl.voytech.exporter.core.api.builder

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.extension.*

@DslMarker
annotation class TableMarker

@JvmSynthetic
fun <T> table(block: TableBuilder<T>.() -> Unit): Table<T> = TableBuilder<T>().apply(block).build()

@TableMarker
class TableBuilder<T> : Builder<Table<T>>{
    @set:JvmSynthetic
    var name: String? = "untitled"
    @set:JvmSynthetic
    var firstRow: Int? = 0
    @set:JvmSynthetic
    var firstColumn: Int? = 0
    @set:JvmSynthetic
    private var columns: List<Column<T>> = emptyList()
    @set:JvmSynthetic
    private var rows: List<Row<T>>? = null
    @set:JvmSynthetic
    var showHeader: Boolean? = false
    @set:JvmSynthetic
    var showFooter: Boolean? = false
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

    @JvmSynthetic
    fun columns(block: ColumnsBuilder<T>.() -> Unit) {
        columns = columns + ColumnsBuilder<T>().apply(block).build()
    }

    @JvmSynthetic
    fun rows(block: RowsBuilder<T>.() -> Unit) {
        rows = (rows ?: emptyList()) + RowsBuilder<T>().apply(block).build()
    }

    @JvmSynthetic
    fun columnsDescription(block: DescriptionBuilder.() -> Unit) {
        columnsDescription = DescriptionBuilder().apply(block).build()
    }

    @JvmSynthetic
    fun rowsDescription(block: DescriptionBuilder.() -> Unit) {
        rowsDescription = DescriptionBuilder().apply(block).build()
    }

    /**
     * JAVA style builder.
     **/

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

@TableMarker
class ColumnsBuilder<T> : InternalBuilder<List<Column<T>>>() {

    @set:JvmSynthetic
    private var columns: List<Column<T>> = emptyList()

    @JvmSynthetic
    fun column(id: String, block: ColumnBuilder<T>.() -> Unit) {
        columns = columns + (ColumnBuilder<T>().also { it.id = Key(id = id) }.apply(block).build())
    }

    @JvmSynthetic
    fun column(ref: ((record: T) -> Any?), block: ColumnBuilder<T>.() -> Unit) {
        columns = columns + (ColumnBuilder<T>().also { it.id = Key(ref = ref) }.apply(block).build())
    }

    /**
     * JAVA style builder.
     **/

    fun column(id: String, builder: ColumnBuilder<T>) = apply {
        columns = columns + builder.also { it.id = Key(id = id) }.build()
    }

    fun column(ref: ((record: T) -> Any?), builder: ColumnBuilder<T>) = apply {
        columns = columns + builder.also { it.id = Key(ref = ref) }.build()
    }

    fun column(id: String) = apply {
        columns = columns + (ColumnBuilder<T>().also { it.id = Key(id = id) }.build())
    }

    fun column(ref: ((record: T) -> Any?)) = apply {
        columns = columns + (ColumnBuilder<T>().also { it.id = Key(ref = ref) }.build())
    }

    override fun build(): List<Column<T>> = columns
}

@TableMarker
class ColumnBuilder<T> : InternalBuilder<Column<T>>() {
    @set:JvmSynthetic
    lateinit var id: Key<T>
    @set:JvmSynthetic
    var columnType: CellType? = null
    @set:JvmSynthetic
    var index: Int? = null
    @set:JvmSynthetic
    private var columnExtensions: Set<ColumnExtension>? = null
    @set:JvmSynthetic
    private var cellExtensions: Set<CellExtension>? = null
    @set:JvmSynthetic
    var dataFormatter: ((field: Any) -> Any)? = null

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

@TableMarker
class RowsBuilder<T> : InternalBuilder<List<Row<T>>>() {

    @set:JvmSynthetic
    private var rows: List<Row<T>> = emptyList()

    @JvmSynthetic
    fun row(block: RowBuilder<T>.() -> Unit) {
        rows = rows + (RowBuilder<T>().apply(block).build())
    }

    @JvmSynthetic
    fun row(selector: RowSelector<T>, block: RowBuilder<T>.() -> Unit) {
        val builder = RowBuilder<T>()
        builder.selector = selector
        rows = rows + builder.apply(block).build()
    }

    @JvmSynthetic
    fun row(at: Int, block: RowBuilder<T>.() -> Unit) {
        val builder = RowBuilder<T>()
        builder.createAt = at
        rows = rows + builder.apply(block).build()
    }

    /**
     * JAVA style builder.
     **/

    fun row(builder: RowBuilder<T>) = apply {
        rows = rows + builder.build()
    }

    fun row(selector: RowSelector<T>, builder: RowBuilder<T>) = apply {
        builder.selector = selector
        rows = rows + builder.build()
    }

    fun row(at: Int, builder: RowBuilder<T>) {
        builder.createAt = at
        rows = rows + builder.build()
    }

    override fun build(): List<Row<T>> = rows

}

@TableMarker
class RowBuilder<T> : InternalBuilder<Row<T>>() {
    @set:JvmSynthetic
    private var cells: Map<Key<T>, Cell<T>>? = null
    @set:JvmSynthetic
    private var rowExtensions: Set<RowExtension>? = null
    @set:JvmSynthetic
    private var cellExtensions: Set<CellExtension>? = null
    @set:JvmSynthetic
    var selector: RowSelector<T>? = null
    @set:JvmSynthetic
    var createAt: Int? = null

    @JvmSynthetic
    fun cells(block: CellsBuilder<T>.() -> Unit) {
        cells = (cells ?: emptyMap()) + CellsBuilder<T>().apply(block).build()
    }

    /**
     * JAVA style builder.
     **/

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

@TableMarker
class CellsBuilder<T> : InternalBuilder<Map<Key<T>, Cell<T>>>() {

    @set:JvmSynthetic
    private var cells: Map<Key<T>, Cell<T>> = emptyMap()

    @JvmSynthetic
    fun forColumn(id: String, block: CellBuilder<T>.() -> Unit) {
        cells = cells  + Pair(Key(id), CellBuilder<T>().apply(block).build())
    }

    @JvmSynthetic
    fun forColumn(ref: ((record: T) -> Any?), block: CellBuilder<T>.() -> Unit) {
        cells = cells + Pair(Key(ref = ref), CellBuilder<T>().apply(block).build())
    }

    /**
     * JAVA style builder.
     **/

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

@TableMarker
class CellBuilder<T> : InternalBuilder<Cell<T>>() {
    @set:JvmSynthetic
    private var cellExtensions: Set<CellExtension>? = null
    @set:JvmSynthetic
    var value: Any? = null
    @set:JvmSynthetic
    var eval: RowCellEval<T>? = null
    @set:JvmSynthetic
    var type: CellType? = null

    /**
     * JAVA style builder.
     **/

    fun cellExtensions(vararg extensions: CellExtension) = apply {
        cellExtensions = (cellExtensions ?: emptySet()) + extensions.toHashSet()
    }

    fun <T: CellExtensionBuilder> cellExtensions(vararg extensionBuilder: T) = apply {
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

@TableMarker
class DescriptionBuilder {
    lateinit var title: String
    private var extensions: Set<Extension>? = null

    @JvmSynthetic
    fun extensions(vararg extensions: Extension) {
        this.extensions = (this.extensions ?: emptySet()) + extensions.toHashSet()
    }

    @JvmSynthetic
    fun build(): Description = Description(title, extensions)
}
