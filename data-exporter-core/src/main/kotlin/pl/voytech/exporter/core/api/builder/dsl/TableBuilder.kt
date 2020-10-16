package pl.voytech.exporter.core.api.builder.dsl

import pl.voytech.exporter.core.api.builder.Builder
import pl.voytech.exporter.core.api.builder.ExtensionsAwareBuilder
import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.extension.*

@DslMarker
annotation class TableMarker

@JvmSynthetic
fun <T> table(block: TableBuilder<T>.() -> Unit): Table<T> = TableBuilder.new<T>()
    .apply(block).build()

@TableMarker
class TableBuilder<T> private constructor() : ExtensionsAwareBuilder<Table<T>>() {

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

    init {
        NextId.reset()
    }

    @JvmSynthetic
    fun columns(block: ColumnsBuilder<T>.() -> Unit) {
        columns = columns + ColumnsBuilder.new<T>().apply(block).build()
    }

    @JvmSynthetic
    fun rows(block: RowsBuilder<T>.() -> Unit) {
        rows = (rows ?: emptyList()) + RowsBuilder.new<T>(columns).apply(block).build()
    }

    @JvmSynthetic
    override fun build(): Table<T> = Table(
        name, firstRow, firstColumn, columns, rows,
        getExtensionsByClass(TableExtension::class.java),
        getExtensionsByClass(CellExtension::class.java)
    )

    @JvmSynthetic
    override fun supportedExtensionClasses(): Set<Class<out Extension>> =
        setOf(TableExtension::class.java, CellExtension::class.java)

    companion object {
        @JvmSynthetic
        internal fun <T> new(): TableBuilder<T> = TableBuilder()
    }

}

@TableMarker
class ColumnsBuilder<T> private constructor() : Builder<List<Column<T>>> {

    @set:JvmSynthetic
    private var columns: List<Column<T>> = emptyList()

    @JvmSynthetic
    fun column(id: String) {
        columns = columns + (ColumnBuilder.new<T>().also { it.id = ColumnKey(id = id) }.build())
    }

    @JvmSynthetic
    fun column(id: String, block: ColumnBuilder<T>.() -> Unit) {
        columns = columns + (ColumnBuilder.new<T>().also { it.id = ColumnKey(id = id) }.apply(block).build())
    }

    @JvmSynthetic
    fun column(ref: ((record: T) -> Any?), block: ColumnBuilder<T>.() -> Unit) {
        columns = columns + (ColumnBuilder.new<T>().also { it.id = ColumnKey(ref = ref) }.apply(block).build())
    }

    @JvmSynthetic
    fun column(ref: ((record: T) -> Any?)) {
        columns = columns + (ColumnBuilder.new<T>().also { it.id = ColumnKey(ref = ref) }.build())
    }

    @JvmSynthetic
    override fun build(): List<Column<T>> = columns

    companion object {
        @JvmSynthetic
        internal fun <T> new(): ColumnsBuilder<T> = ColumnsBuilder()
    }
}

@TableMarker
class ColumnBuilder<T> private constructor() : ExtensionsAwareBuilder<Column<T>>() {
    @set:JvmSynthetic
    lateinit var id: ColumnKey<T>

    @set:JvmSynthetic
    var columnType: CellType? = null

    @set:JvmSynthetic
    var index: Int? = null

    @set:JvmSynthetic
    var dataFormatter: ((field: Any) -> Any)? = null


    @JvmSynthetic
    override fun build(): Column<T> = Column(
        id, index, columnType,
        getExtensionsByClass(ColumnExtension::class.java),
        getExtensionsByClass(CellExtension::class.java),
        dataFormatter
    )

    @JvmSynthetic
    override fun supportedExtensionClasses(): Set<Class<out Extension>> =
        setOf(ColumnExtension::class.java, CellExtension::class.java)

    companion object {
        @JvmSynthetic
        internal fun <T> new(): ColumnBuilder<T> = ColumnBuilder()
    }
}

@TableMarker
class RowsBuilder<T> private constructor(private val columns: List<Column<T>>) : Builder<List<Row<T>>> {

    @set:JvmSynthetic
    private var rows: List<Row<T>> = emptyList()

    private var rowIndex: Int = 0

    @JvmSynthetic
    fun row(block: RowBuilder<T>.() -> Unit) {
        rows = rows + (RowBuilder.new(columns).apply { createAt = this@RowsBuilder.rowIndex }.apply(block).build())
            .also { rowIndex = it.createAt?.plus(1) ?: rowIndex }
    }

    @JvmSynthetic
    fun row(selector: RowSelector<T>, block: RowBuilder<T>.() -> Unit) {
        rows = rows + RowBuilder.new(columns).apply { this.selector = selector }.apply(block).build()
    }

    @JvmSynthetic
    fun row(at: Int, block: RowBuilder<T>.() -> Unit) {
        rowIndex = at
        rows = rows + RowBuilder.new(columns).apply { createAt = this@RowsBuilder.rowIndex++ }.apply(block).build()
    }


    @JvmSynthetic
    override fun build(): List<Row<T>> = rows

    companion object {
        @JvmSynthetic
        internal fun <T> new(columns: List<Column<T>>): RowsBuilder<T> = RowsBuilder(columns)
    }
}

@TableMarker
class RowBuilder<T> private constructor(private val columns: List<Column<T>>) : ExtensionsAwareBuilder<Row<T>>() {

    @set:JvmSynthetic
    private var cells: Map<ColumnKey<T>, Cell<T>>? = null

    @set:JvmSynthetic
    private var _createAt: Int? = null

    @set:JvmSynthetic
    private var _selector: RowSelector<T>? = null

    @set:JvmSynthetic
    var selector: RowSelector<T>?
        get() {
            return _selector
        }
        set(value) {
            _selector = value
            _createAt = null
        }

    @set:JvmSynthetic
    var createAt: Int?
        get() {
            return _createAt
        }
        set(value) {
            _createAt = value
            _selector = null
        }

    @JvmSynthetic
    fun cells(block: CellsBuilder<T>.() -> Unit) {
        cells = (cells ?: emptyMap()) + CellsBuilder.new(columns).apply(block).build()
    }

    @JvmSynthetic
    override fun build(): Row<T> = Row(
        selector, createAt,
        getExtensionsByClass(RowExtension::class.java),
        getExtensionsByClass(CellExtension::class.java),
        cells
    )

    @JvmSynthetic
    override fun supportedExtensionClasses(): Set<Class<out Extension>> =
        setOf(RowExtension::class.java, CellExtension::class.java)

    companion object {
        @JvmSynthetic
        internal fun <T> new(columns: List<Column<T>>): RowBuilder<T> = RowBuilder(columns)
    }
}

@TableMarker
class CellsBuilder<T> private constructor(private val columns: List<Column<T>>) : Builder<Map<ColumnKey<T>, Cell<T>>> {

    @set:JvmSynthetic
    private var cells: Map<ColumnKey<T>, Cell<T>> = emptyMap()

    @set:JvmSynthetic
    private var cellIndex: Int = 0

    @JvmSynthetic
    fun forColumn(id: String, block: CellBuilder<T>.() -> Unit) {
        cells = cells + Pair(
            ColumnKey(id = id), CellBuilder.new<T>()
                .apply(block).build()
        )
    }

    @JvmSynthetic
    fun cell(index: Int, block: CellBuilder<T>.() -> Unit) {
        columns[index].let {
            cells = cells + Pair(
                ColumnKey(ref = it.id.ref, id = it.id.id), CellBuilder.new<T>()
                    .apply(block).build()
            )
        }
    }

    @JvmSynthetic
    fun cell(block: CellBuilder<T>.() -> Unit) {
        cell(index = cellIndex++, block)
    }

    @JvmSynthetic
    fun forColumn(ref: ((record: T) -> Any?), block: CellBuilder<T>.() -> Unit) {
        cells = cells + Pair(
            ColumnKey(ref = ref), CellBuilder.new<T>()
                .apply(block).build()
        )
    }

    @JvmSynthetic
    override fun build(): Map<ColumnKey<T>, Cell<T>> {
        return cells
    }

    companion object {
        @JvmSynthetic
        internal fun <T> new(columns: List<Column<T>>): CellsBuilder<T> = CellsBuilder(columns)
    }
}

@TableMarker
class CellBuilder<T> private constructor() : ExtensionsAwareBuilder<Cell<T>>() {

    @set:JvmSynthetic
    var value: Any? = null

    @set:JvmSynthetic
    var eval: RowCellEval<T>? = null

    @set:JvmSynthetic
    var type: CellType? = null

    @set:JvmSynthetic
    var colSpan: Int? = 1

    @set:JvmSynthetic
    var rowSpan: Int? = 1

    @JvmSynthetic
    override fun build(): Cell<T> =
        Cell(value, eval, type, colSpan, rowSpan, getExtensionsByClass(CellExtension::class.java))

    @JvmSynthetic
    override fun supportedExtensionClasses(): Set<Class<out Extension>> = setOf(CellExtension::class.java)

    companion object {
        @JvmSynthetic
        internal fun <T> new(): CellBuilder<T> = CellBuilder()
    }
}