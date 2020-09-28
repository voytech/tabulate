package pl.voytech.exporter.core.api.builder.fluent

import pl.voytech.exporter.core.api.builder.Builder
import pl.voytech.exporter.core.api.builder.ExtensionsAware
import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.extension.*


interface TopLevelBuilder<T> : Builder<Table<T>>

interface MidLevelBuilder<T, E : TopLevelBuilder<T>> : TopLevelBuilder<T> {

    @JvmSynthetic
    fun out(): E

    override fun build(): Table<T> = out().build()

}


class TableBuilder<T> : ExtensionsAware(), TopLevelBuilder<T> {
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

    fun rows() = RowsBuilder(this,columns)

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

class ColumnsBuilder<T>(private val tableBuilder: TableBuilder<T>) : MidLevelBuilder<T, TableBuilder<T>> {

    @set:JvmSynthetic
    private var columns: List<Column<T>> = emptyList()

    fun column(id: String) = ColumnBuilder(this).apply { id(ColumnKey(id = id)) }

    fun column(ref: ((record: T) -> Any?)) = ColumnBuilder(this).apply { id(ColumnKey(ref = ref)) }

    @JvmSynthetic
    internal fun addColumn(column: Column<T>) = apply {
        columns = columns + column
    }

    override fun out(): TableBuilder<T> {
        return tableBuilder.addColumns(*columns.toTypedArray())
    }

}

class ColumnBuilder<T>(private val columnsBuilder: ColumnsBuilder<T>) : ExtensionsAware(),
    MidLevelBuilder<T, ColumnsBuilder<T>> {
    @set:JvmSynthetic
    private lateinit var id: ColumnKey<T>

    @set:JvmSynthetic
    private var columnType: CellType? = null

    @set:JvmSynthetic
    private var index: Int? = null

    @set:JvmSynthetic
    private var dataFormatter: ((field: Any) -> Any)? = null

    fun id(id: ColumnKey<T>) = apply {
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
        return ColumnBuilder(out()).apply { id(ColumnKey(id = id)) }
    }

    fun column(ref: ((record: T) -> Any?)): ColumnBuilder<T> {
        return ColumnBuilder(out()).apply { id(ColumnKey(ref = ref)) }
    }

    fun rows() = out().out().rows()

    fun extension(vararg extension: Extension) = apply {
        extensions(*extension)
    }

    @JvmSynthetic
    override fun supportedExtensionClasses(): Set<Class<out Extension>> =
        setOf(ColumnExtension::class.java, CellExtension::class.java)

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

class RowsBuilder<T>(private val tableBuilder: TableBuilder<T>, private val columns: List<Column<T>>) : MidLevelBuilder<T, TableBuilder<T>> {

    @set:JvmSynthetic
    private var rows: List<Row<T>> = emptyList()

    fun row() = RowBuilder(this, columns)

    fun row(selector: RowSelector<T>) = RowBuilder(this, columns).apply { selector(selector) }

    fun row(at: Int) = RowBuilder(this, columns).apply { createAt(at) }

    @JvmSynthetic
    internal fun addRow(row: Row<T>) = apply {
        rows = rows + row
    }

    @JvmSynthetic
    override fun out(): TableBuilder<T> {
        return tableBuilder.addRows(*rows.toTypedArray())
    }

}

class RowBuilder<T>(private val rowsBuilder: RowsBuilder<T>, private val columns: List<Column<T>>) : ExtensionsAware(), MidLevelBuilder<T, RowsBuilder<T>> {
    @set:JvmSynthetic
    private var cells: Map<ColumnKey<T>, Cell<T>>? = null

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

    fun cells() = CellsBuilder(this, columns)

    fun row() = RowBuilder(out(), columns)

    fun row(selector: RowSelector<T>) = row().apply { selector(selector) }

    fun row(at: Int) = row().apply { createAt(at) }

    fun extension(vararg extension: Extension) = apply {
        extensions(*extension)
    }

    override fun supportedExtensionClasses(): Set<Class<out Extension>> =
        setOf(RowExtension::class.java, CellExtension::class.java)


    @JvmSynthetic
    internal fun addCells(cells: Map<ColumnKey<T>, Cell<T>>) = apply {
        this.cells = cells
    }

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

class CellsBuilder<T>(private val rowBuilder: RowBuilder<T>, private val columns: List<Column<T>>) : MidLevelBuilder<T, RowBuilder<T>> {

    @set:JvmSynthetic
    private var cells: Map<ColumnKey<T>, Cell<T>> = emptyMap()

    @set:JvmSynthetic
    private var cellIndex: Int = 0

    @JvmSynthetic
    internal fun addCell(columnKey: ColumnKey<T>, cell: Cell<T>) = apply {
        cells = cells + Pair(columnKey, cell)
    }

    fun forColumn(id: String) = CellBuilder(ColumnKey(id = id), this, columns)

    fun cell(index: Int): CellBuilder<T> {
        return columns[index].let {
            CellBuilder(ColumnKey(ref = it.id.ref, id = it.id.id), this, columns)
        }
    }

    fun cell(): CellBuilder<T> = cell(cellIndex++)

    fun forColumn(ref: ((record: T) -> Any?)) = CellBuilder(ColumnKey(ref = ref), this, columns)

    fun row() = RowBuilder(out().out(), columns)

    fun row(selector: RowSelector<T>) = row().apply { selector(selector) }

    fun row(at: Int) = row().apply { createAt(at) }

    @JvmSynthetic
    override fun out(): RowBuilder<T> = rowBuilder.addCells(cells)
}

class CellBuilder<T>(
    private val columnKey: ColumnKey<T>,
    private val cellsBuilder: CellsBuilder<T>,
    private val columns: List<Column<T>>
) : ExtensionsAware(), MidLevelBuilder<T, CellsBuilder<T>> {

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

    fun forColumn(id: String) = CellBuilder(ColumnKey(id = id), out(), columns)

    fun forColumn(ref: ((record: T) -> Any?)) = CellBuilder(ColumnKey(ref = ref), out(), columns)

    fun cell(index: Int): CellBuilder<T> {
        return columns[index].let {
            CellBuilder(ColumnKey(ref = it.id.ref, id = it.id.id), out(), columns)
        }
    }

    fun row() =  RowBuilder(out().out().out(), columns)

    fun row(selector: RowSelector<T>?) = row().apply { selector(selector) }

    fun row(at: Int) = row().apply { createAt(at) }

    override fun supportedExtensionClasses(): Set<Class<out Extension>> =
        setOf(RowExtension::class.java, CellExtension::class.java)

    @JvmSynthetic
    override fun out(): CellsBuilder<T> =
        cellsBuilder.addCell(
            columnKey,
            Cell(value, eval, type, colSpan, rowSpan, getExtensionsByClass(CellExtension::class.java))
        )

}