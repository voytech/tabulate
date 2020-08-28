package pl.voytech.exporter.core.api.builder.dsl

import pl.voytech.exporter.core.api.builder.*
import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.extension.*

@DslMarker
annotation class TableMarker

@JvmSynthetic
fun <T> table(block: TableBuilder<T>.() -> Unit): Table<T> = TableBuilder.new<T>()
    .apply(block).build()

@TableMarker
class TableBuilder<T> private constructor() : Builder<Table<T>> {
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
        columns = columns + ColumnsBuilder.new<T>()
            .apply(block).build()
    }

    @JvmSynthetic
    fun rows(block: RowsBuilder<T>.() -> Unit) {
        rows = (rows ?: emptyList()) + RowsBuilder.new<T>()
            .apply(block).build()
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
    fun tableExtensions(vararg extensions: TableExtension) {
        tableExtensions = (tableExtensions ?: emptySet()) + extensions.toHashSet()
    }

    @JvmSynthetic
    fun cellExtensions(vararg extensions: CellExtension) {
        cellExtensions = (cellExtensions ?: emptySet()) + extensions.toHashSet()
    }

    @JvmSynthetic
    fun <T : TableExtensionBuilder> tableExtensions(vararg extensionBuilder: T) {
        tableExtensions = (tableExtensions ?: emptySet()) + extensionBuilder.map { it.build() }
    }

    @JvmSynthetic
    fun <T : CellExtensionBuilder> cellExtensions(vararg extensionBuilder: T) {
        cellExtensions = (cellExtensions ?: emptySet()) + extensionBuilder.map { it.build() }
    }

    @JvmSynthetic
    override fun build(): Table<T> = Table(
        name, firstRow, firstColumn, columns, rows, showHeader,
        showFooter, columnsDescription, rowsDescription,
        tableExtensions, cellExtensions
    )

    companion object {
        @JvmSynthetic
        internal fun <T> new(): TableBuilder<T> =
            TableBuilder()
    }
}

@TableMarker
class ColumnsBuilder<T> private constructor() : InternalBuilder<List<Column<T>>>() {

    @set:JvmSynthetic
    private var columns: List<Column<T>> = emptyList()

    @JvmSynthetic
    fun column(id: String) {
        columns = columns + (ColumnBuilder.new<T>().also { it.id = Key(id = id) }.build())
    }

    @JvmSynthetic
    fun column(id: String, block: ColumnBuilder<T>.() -> Unit) {
        columns = columns + (ColumnBuilder.new<T>().also { it.id = Key(id = id) }.apply(block).build())
    }

    @JvmSynthetic
    fun column(ref: ((record: T) -> Any?), block: ColumnBuilder<T>.() -> Unit) {
        columns = columns + (ColumnBuilder.new<T>().also { it.id = Key(ref = ref) }.apply(block).build())
    }

    @JvmSynthetic
    fun column(ref: ((record: T) -> Any?)) {
        columns = columns + (ColumnBuilder.new<T>().also { it.id = Key(ref = ref) }.build())
    }

    @JvmSynthetic
    override fun build(): List<Column<T>> = columns

    companion object {
        @JvmSynthetic
        internal fun <T> new(): ColumnsBuilder<T> =
            ColumnsBuilder()
    }
}

@TableMarker
class ColumnBuilder<T> private constructor() : InternalBuilder<Column<T>>() {
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

    @JvmSynthetic
    fun cellExtensions(vararg extensions: CellExtension) {
        cellExtensions = (cellExtensions ?: emptySet()) + extensions.toHashSet()
    }

    @JvmSynthetic
    fun columnExtensions(vararg extensions: ColumnExtension) {
        columnExtensions = (columnExtensions ?: emptySet()) + extensions.toHashSet()
    }

    @JvmSynthetic
    fun <T : ColumnExtensionBuilder> columnExtensions(vararg extensionBuilder: T) {
        columnExtensions = (columnExtensions ?: emptySet()) + extensionBuilder.map { it.build() }
    }

    @JvmSynthetic
    fun <T : CellExtensionBuilder> cellExtensions(vararg extensionBuilder: T) {
        cellExtensions = (cellExtensions ?: emptySet()) + extensionBuilder.map { it.build() }
    }

    @JvmSynthetic
    override fun build(): Column<T> = Column(id, index, columnType, columnExtensions, cellExtensions, dataFormatter)

    companion object {
        @JvmSynthetic
        internal fun <T> new(): ColumnBuilder<T> =
            ColumnBuilder()
    }
}

@TableMarker
class RowsBuilder<T> private constructor() : InternalBuilder<List<Row<T>>>() {

    @set:JvmSynthetic
    private var rows: List<Row<T>> = emptyList()

    @JvmSynthetic
    fun row(block: RowBuilder<T>.() -> Unit) {
        rows = rows + (RowBuilder.new<T>().apply(block).build())
    }

    @JvmSynthetic
    fun row(selector: RowSelector<T>, block: RowBuilder<T>.() -> Unit) {
        val builder = RowBuilder.new<T>()
        builder.selector = selector
        rows = rows + builder.apply(block).build()
    }

    @JvmSynthetic
    fun row(at: Int, block: RowBuilder<T>.() -> Unit) {
        val builder = RowBuilder.new<T>()
        builder.createAt = at
        rows = rows + builder.apply(block).build()
    }

    @JvmSynthetic
    override fun build(): List<Row<T>> = rows

    companion object {
        @JvmSynthetic
        internal fun <T> new(): RowsBuilder<T> =
            RowsBuilder()
    }
}

@TableMarker
class RowBuilder<T> private constructor() : InternalBuilder<Row<T>>() {
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
        cells = (cells ?: emptyMap()) + CellsBuilder.new<T>()
            .apply(block).build()
    }

    @JvmSynthetic
    fun rowExtensions(vararg extensions: RowExtension) {
        rowExtensions = (rowExtensions ?: emptySet()) + extensions.toHashSet()
    }

    @JvmSynthetic
    fun cellExtensions(vararg extensions: CellExtension) {
        cellExtensions = (cellExtensions ?: emptySet()) + extensions.toHashSet()
    }

    @JvmSynthetic
    fun <T : RowExtensionBuilder> rowExtensions(vararg extensionBuilder: T) {
        rowExtensions = (rowExtensions ?: emptySet()) + extensionBuilder.map { it.build() }
    }

    @JvmSynthetic
    fun <T : CellExtensionBuilder> cellExtensions(vararg extensionBuilder: T) {
        cellExtensions = (cellExtensions ?: emptySet()) + extensionBuilder.map { it.build() }
    }

    @JvmSynthetic
    override fun build(): Row<T> = Row(selector, createAt, rowExtensions, cellExtensions, cells)

    companion object {
        @JvmSynthetic
        internal fun <T> new(): RowBuilder<T> =
            RowBuilder()
    }
}

@TableMarker
class CellsBuilder<T> private constructor() : InternalBuilder<Map<Key<T>, Cell<T>>>() {

    @set:JvmSynthetic
    private var cells: Map<Key<T>, Cell<T>> = emptyMap()

    @JvmSynthetic
    fun forColumn(id: String, block: CellBuilder<T>.() -> Unit) {
        cells = cells + Pair(
            Key(id), CellBuilder.new<T>()
                .apply(block).build()
        )
    }

    @JvmSynthetic
    fun forColumn(ref: ((record: T) -> Any?), block: CellBuilder<T>.() -> Unit) {
        cells = cells + Pair(
            Key(ref = ref), CellBuilder.new<T>()
                .apply(block).build()
        )
    }

    @JvmSynthetic
    override fun build(): Map<Key<T>, Cell<T>> {
        return cells
    }

    companion object {
        @JvmSynthetic
        internal fun <T> new(): CellsBuilder<T> =
            CellsBuilder()
    }
}

@TableMarker
class CellBuilder<T> private constructor() : InternalBuilder<Cell<T>>() {
    @set:JvmSynthetic
    private var cellExtensions: Set<CellExtension>? = null

    @set:JvmSynthetic
    var value: Any? = null

    @set:JvmSynthetic
    var eval: RowCellEval<T>? = null

    @set:JvmSynthetic
    var type: CellType? = null

    @JvmSynthetic
    fun cellExtensions(vararg extensions: CellExtension) {
        cellExtensions = (cellExtensions ?: emptySet()) + extensions.toHashSet()
    }

    @JvmSynthetic
    fun <T : CellExtensionBuilder> cellExtensions(vararg extensionBuilder: T) {
        cellExtensions = (cellExtensions ?: emptySet()) + extensionBuilder.map { it.build() }
    }

    @JvmSynthetic
    override fun build(): Cell<T> = Cell(value, eval, type, cellExtensions)

    companion object {
        @JvmSynthetic
        internal fun <T> new(): CellBuilder<T> =
            CellBuilder()
    }
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
