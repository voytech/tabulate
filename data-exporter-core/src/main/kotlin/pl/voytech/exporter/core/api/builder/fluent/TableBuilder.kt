package pl.voytech.exporter.core.api.builder.fluent

import pl.voytech.exporter.core.api.builder.*
import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.extension.*

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

    override fun build(): Table<T> = Table(
        name, firstRow, firstColumn, columns, rows,
        getExtensionsByClass(TableExtension::class.java),
        getExtensionsByClass(CellExtension::class.java)
    )

    override fun supportedExtensionClasses(): Set<Class<out Extension>> =
        setOf(TableExtension::class.java, CellExtension::class.java)

}

class ColumnsBuilder<T>(private val tableBuilder: TableBuilder<T>) : Builder<TableBuilder<T>> {

    @set:JvmSynthetic
    private var columns: List<Column<T>> = emptyList()

    fun column(id: String) = ColumnBuilder(this)

    fun column(ref: ((record: T) -> Any?)) = ColumnBuilder(this)

    override fun build(): TableBuilder<T> = tableBuilder

}

class ColumnBuilder<T>(private val columnsBuilder: ColumnsBuilder<T>) : ExtensionsAwareBuilder<ColumnsBuilder<T>>() {
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

    override fun supportedExtensionClasses(): Set<Class<out Extension>> =
        setOf(ColumnExtension::class.java, CellExtension::class.java)

    override fun build(): ColumnsBuilder<T> {
        return Column(id, index, columnType,
            getExtensionsByClass(ColumnExtension::class.java),
            getExtensionsByClass(CellExtension::class.java),
            dataFormatter
        ).let {
            columnsBuilder
        }
    }

}

class RowsBuilder<T>(private val tableBuilder: TableBuilder<T>) : Builder<TableBuilder<T>> {

    @set:JvmSynthetic
    private var rows: List<Row<T>> = emptyList()

    fun row() = RowBuilder(this)

    fun row(selector: RowSelector<T>) = RowBuilder(this).apply { selector(selector) }

    fun row(at: Int) = RowBuilder(this).apply { createAt(at) }

    override fun build(): TableBuilder<T> {
        return tableBuilder
    }

}

class RowBuilder<T>(private val rowsBuilder: RowsBuilder<T>) : ExtensionsAwareBuilder<RowsBuilder<T>>() {
    @set:JvmSynthetic
    private var cells: Map<Key<T>, Cell<T>>? = null
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

    fun cells() = CellsBuilder(this)

    override fun supportedExtensionClasses(): Set<Class<out Extension>> =
        setOf(RowExtension::class.java, CellExtension::class.java)

    override fun build(): RowsBuilder<T> {
        return Row(selector, createAt,
            getExtensionsByClass(RowExtension::class.java),
            getExtensionsByClass(CellExtension::class.java),
            cells
        ).let {
            rowsBuilder
        }
    }

}

class CellsBuilder<T>(private val rowBuilder: RowBuilder<T>) : Builder<RowBuilder<T>> {

    @set:JvmSynthetic
    private var cells: Map<Key<T>, Cell<T>> = emptyMap()

    fun forColumn(id: String) = CellBuilder(id, null, this)

    fun forColumn(ref: ((record: T) -> Any?)) = CellBuilder(null, ref, this)

    override fun build(): RowBuilder<T> {
      return rowBuilder
    }
}

class CellBuilder<T>(
    private val id: String?,
    private val ref: ((record: T) -> Any?)?,
    private val cellsBuilder: CellsBuilder<T>
) : ExtensionsAwareBuilder<CellsBuilder<T>>() {

    @set:JvmSynthetic
    private var value: Any? = null

    @set:JvmSynthetic
    private var eval: RowCellEval<T>? = null

    @set:JvmSynthetic
    private var type: CellType? = null

    fun value(value: Any?) = apply {
        this.value = value
    }

    fun eval(eval: RowCellEval<T>?) = apply {
        this.eval = eval
    }

    fun type(type: CellType?) = apply {
        this.type = type
    }

    override fun supportedExtensionClasses(): Set<Class<out Extension>> =
        setOf(RowExtension::class.java, CellExtension::class.java)

    override fun build(): CellsBuilder<T> {
        return Cell(value, eval, type, getExtensionsByClass(CellExtension::class.java)).let {
           cellsBuilder
        }
    }

}