package pl.voytech.exporter.core.api.builder.fluent

import pl.voytech.exporter.core.api.builder.Builder
import pl.voytech.exporter.core.api.builder.ExtensionsAwareBuilder
import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.extension.*

interface BuildPointSwitch<T> {
    fun out(): T
}

interface TopLevelSwitch<E : Builder<*>> {
    fun top(): E
}

class TableBuilder<T> : ExtensionsAwareBuilder<Table<T>>() {
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

    fun columns() = ColumnsBuilder(this)

    fun rows() = RowsBuilder(this)

    fun extension(vararg extension: Extension) = apply {
        extensions(*extension)
    }

    override fun build(): Table<T> = Table(
        name, firstRow, firstColumn, columns, rows,
        getExtensionsByClass(TableExtension::class.java),
        getExtensionsByClass(CellExtension::class.java)
    )

    @JvmSynthetic
    internal fun addColumns(vararg column: Column<T>): TableBuilder<T> = apply {
        columns = columns + column
    }

    @JvmSynthetic
    internal fun addRows(vararg row: Row<T>): TableBuilder<T> = apply {
        rows = (rows ?: emptyList()) + row
    }

    internal override fun supportedExtensionClasses(): Set<Class<out Extension>> =
        setOf(TableExtension::class.java, CellExtension::class.java)

}

class ColumnsBuilder<T>(private val tableBuilder: TableBuilder<T>) : Builder<Table<T>>,
    TopLevelSwitch<TableBuilder<T>> {

    @set:JvmSynthetic
    private var columns: List<Column<T>> = emptyList()

    fun column(id: String) = ColumnBuilder(this).apply { id(Key(id = id)) }

    fun column(ref: ((record: T) -> Any?)) = ColumnBuilder(this).apply { id(Key(ref = ref)) }

    @JvmSynthetic
    internal fun addColumn(column: Column<T>) = apply {
        columns = columns + column
    }

    override fun build(): Table<T> {
        return top().build()
    }

    override fun top(): TableBuilder<T> {
        return tableBuilder.addColumns(*columns.toTypedArray())
    }

}

class ColumnBuilder<T>(private val columnsBuilder: ColumnsBuilder<T>) : ExtensionsAwareBuilder<Table<T>>(),
    BuildPointSwitch<ColumnsBuilder<T>> {
    @set:JvmSynthetic
    private lateinit var id: Key<T>

    @set:JvmSynthetic
    private var columnType: CellType? = null

    @set:JvmSynthetic
    private var index: Int? = null

    @set:JvmSynthetic
    private var dataFormatter: ((field: Any) -> Any)? = null

    /**
     * JAVA style builder.
     **/

    fun id(id: Key<T>) = apply {
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

    fun column(id: String): ColumnBuilder<T> {
        return ColumnBuilder(out()).apply { id(Key(id = id)) }
    }

    fun column(ref: ((record: T) -> Any?)): ColumnBuilder<T> {
        return ColumnBuilder(out()).apply { id(Key(ref = ref)) }
    }

    fun extension(vararg extension: Extension) = apply {
        extensions(*extension)
    }

    fun rows() = out().top().rows()

    @JvmSynthetic
    override fun supportedExtensionClasses(): Set<Class<out Extension>> =
        setOf(ColumnExtension::class.java, CellExtension::class.java)

    @JvmSynthetic
    override fun build(): Table<T> {
        return out().build()
    }

    @JvmSynthetic
    override fun out(): ColumnsBuilder<T> {
        return columnsBuilder.addColumn(
            Column(
                id, index, columnType,
                getExtensionsByClass(ColumnExtension::class.java),
                getExtensionsByClass(CellExtension::class.java),
                dataFormatter
            )
        )
    }

}

class RowsBuilder<T>(private val tableBuilder: TableBuilder<T>) : Builder<Table<T>>, TopLevelSwitch<TableBuilder<T>> {

    @set:JvmSynthetic
    private var rows: List<Row<T>> = emptyList()

    fun row() = RowBuilder(this)

    fun row(selector: RowSelector<T>) = RowBuilder(this).apply { selector(selector) }

    fun row(at: Int) = RowBuilder(this).apply { createAt(at) }

    @JvmSynthetic
    internal fun addRow(row: Row<T>) = apply {
        rows = rows + row
    }

    override fun build(): Table<T> {
        return top().build()
    }

    @JvmSynthetic
    override fun top(): TableBuilder<T> {
        return tableBuilder.addRows(*rows.toTypedArray())
    }

}

class RowBuilder<T>(private val rowsBuilder: RowsBuilder<T>) : ExtensionsAwareBuilder<Table<T>>(),
    BuildPointSwitch<RowsBuilder<T>> {
    @set:JvmSynthetic
    private var cells: Map<Key<T>, Cell<T>>? = null

    @set:JvmSynthetic
    private var selector: RowSelector<T>? = null

    @set:JvmSynthetic
    private var createAt: Int? = null

    fun selector(selector: RowSelector<T>?) = apply {
        this.selector = selector
    }

    fun createAt(createAt: Int?) = apply {
        this.createAt = createAt
    }

    fun cells() = CellsBuilder(this)

    fun row() = RowBuilder(out())

    fun row(selector: RowSelector<T>) = RowBuilder(out()).apply { selector(selector) }

    fun row(at: Int) = RowBuilder(out()).apply { createAt(at) }

    fun extension(vararg extension: Extension) = apply {
        extensions(*extension)
    }

    override fun supportedExtensionClasses(): Set<Class<out Extension>> =
        setOf(RowExtension::class.java, CellExtension::class.java)


    @JvmSynthetic
    internal fun addCells(cells: Map<Key<T>, Cell<T>>) = apply {
        this.cells = cells
    }

    override fun build(): Table<T> = out().build()

    @JvmSynthetic
    override fun out(): RowsBuilder<T> {
        return rowsBuilder.addRow(
            Row(
                selector, createAt,
                getExtensionsByClass(RowExtension::class.java),
                getExtensionsByClass(CellExtension::class.java),
                cells
            )
        )
    }

}

class CellsBuilder<T>(private val rowBuilder: RowBuilder<T>) : Builder<Table<T>>, BuildPointSwitch<RowBuilder<T>> {

    @set:JvmSynthetic
    private var cells: Map<Key<T>, Cell<T>> = emptyMap()

    @JvmSynthetic
    internal fun addCell(key: Key<T>, cell: Cell<T>) = apply {
        cells = cells + Pair(key, cell)
    }

    fun forColumn(id: String) = CellBuilder(Key(id = id), this)

    fun forColumn(ref: ((record: T) -> Any?)) = CellBuilder(Key(ref = ref), this)

    fun row() = out()

    fun row(selector: RowSelector<T>) = out().apply { selector(selector) }

    fun row(at: Int) = out().apply { createAt(at) }

    override fun build(): Table<T> = out().build()

    @JvmSynthetic
    override fun out(): RowBuilder<T> = rowBuilder.addCells(cells)
}

class CellBuilder<T>(
    private val key: Key<T>,
    private val cellsBuilder: CellsBuilder<T>
) : ExtensionsAwareBuilder<Table<T>>(), BuildPointSwitch<CellsBuilder<T>> {

    @set:JvmSynthetic
    private var value: Any? = null

    @set:JvmSynthetic
    private var eval: RowCellEval<T>? = null

    @set:JvmSynthetic
    private var type: CellType? = null

    @set:JvmSynthetic
    private var colSpan: Int? = 1

    @set:JvmSynthetic
    private var rowSpan: Int? = 1

    fun value(value: Any?) = apply {
        this.value = value
    }

    fun eval(eval: RowCellEval<T>?) = apply {
        this.eval = eval
    }

    fun type(type: CellType?) = apply {
        this.type = type
    }

    fun colSpan(colSpan: Int?) = apply {
        this.colSpan = colSpan
    }

    fun rowSpan(rowSpan: Int?) = apply {
        this.rowSpan = rowSpan
    }

    fun forColumn(id: String) = CellBuilder(Key(id = id), out())

    fun forColumn(ref: ((record: T) -> Any?)) = CellBuilder(Key(ref = ref), out())

    fun row() = out()

    fun row(selector: RowSelector<T>) = out().out().apply { selector(selector) }

    fun row(at: Int) = out().out().apply { createAt(at) }

    override fun supportedExtensionClasses(): Set<Class<out Extension>> =
        setOf(RowExtension::class.java, CellExtension::class.java)

    override fun build(): Table<T> = out().build()

    @JvmSynthetic
    override fun out(): CellsBuilder<T> =
        cellsBuilder.addCell(key, Cell(value, eval, type, colSpan, rowSpan, getExtensionsByClass(CellExtension::class.java)))

}