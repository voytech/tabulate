package pl.voytech.exporter.core.api.builder

import pl.voytech.exporter.core.model.*
import pl.voytech.exporter.core.model.attributes.*

interface Builder<T> {
    fun build(): T
    fun afterDslApplied() { }
}

typealias DslBlock<T> = (T) -> Unit

abstract class AttributesAware {
    private var attributes: MutableMap<Class<out Attribute>, Set<Attribute>> = mutableMapOf()

    @JvmSynthetic
    open fun attributes(vararg attributes: Attribute) {
        applyAttributes(attributes.asList())
    }

    @JvmSynthetic
    open fun attributes(attributes: Collection<Attribute>) {
        applyAttributes(attributes)
    }

    @JvmSynthetic
    open fun attributes(vararg builders: AttributeBuilder<out Attribute>) {
        attributes(*(builders.map { it.build() }).toTypedArray())
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Attribute> visit(clazz : Class<T>, visitor: ((current: Set<T>?) -> Set<T>?)) {
        this.attributes[clazz] = visitor.invoke(this.attributes[clazz] as Set<T>?) ?: emptySet()
    }

    @Suppress("UNCHECKED_CAST")
    @JvmSynthetic
    internal fun <C : Attribute> getAttributesByClass(clazz: Class<C>): Set<C>? = attributes[clazz] as Set<C>?

    @JvmSynthetic
    internal abstract fun supportedAttributeClasses(): Set<Class<out Attribute>>

    private fun applyAttributes(attributes: Collection<Attribute>) {
        attributes.forEach {
            supportedAttributeClasses().find { clazz -> clazz.isAssignableFrom(it.javaClass) }
                ?.let { baseClass ->
                    this.attributes[baseClass] = this.attributes[baseClass]?.let { extensionSet -> extensionSet + it } ?: setOf(it)
                }
        }
    }
}

abstract class AttributesAwareBuilder<T> : AttributesAware(), Builder<T>

class TableBuilder<T> : AttributesAwareBuilder<Table<T>>() {

    @JvmSynthetic
    val columnsBuilder: ColumnsBuilder<T> = ColumnsBuilder()

    @JvmSynthetic
    val rowsBuilder: RowsBuilder<T> = RowsBuilder(columnsBuilder)

    @JvmSynthetic
    var name: String? = "untitled"

    @JvmSynthetic
    var firstRow: Int? = 0

    @JvmSynthetic
    var firstColumn: Int? = 0

    init {
        NextId.reset()
    }

    @JvmSynthetic
    override fun build(): Table<T> = Table(
        name, firstRow, firstColumn,
        columnsBuilder.build(), rowsBuilder.build(),
        getAttributesByClass(TableAttribute::class.java),
        getAttributesByClass(CellAttribute::class.java)
    )

    @JvmSynthetic
    override fun supportedAttributeClasses(): Set<Class<out Attribute>> =
        setOf(TableAttribute::class.java, CellAttribute::class.java)

}

class ColumnsBuilder<T> internal constructor() : Builder<List<Column<T>>> {

    @JvmSynthetic
    val columnBuilders: MutableList<ColumnBuilder<T>> = mutableListOf()

    var count: Int?
        get() {
            return columnBuilders.size
        }
        set(value) {
            columnBuilders.clear()
            (1..value!!).forEach { addColumnBuilder("column-$it") }
        }

    @JvmSynthetic
    fun addColumnBuilder(id: String): ColumnBuilder<T> =
        ColumnBuilder.new<T>().let {
            columnBuilders.add(it.apply { it.id = ColumnKey(id = id) })
            return it
        }

    @JvmSynthetic
    fun addColumnBuilder(ref: ((record: T) -> Any?)):  ColumnBuilder<T> =
        ColumnBuilder.new<T>().let {
            columnBuilders.add(it.apply { it.id = ColumnKey(ref = ref) })
            return it
        }


    @JvmSynthetic
    override fun build(): List<Column<T>> {
        if (columnBuilders.isEmpty() && count != null) {
            (1..count!!).forEach { addColumnBuilder("column-$it") }
        }
        return columnBuilders.map { it.build() }
    }

    companion object {
        @JvmSynthetic
        internal fun <T> new(): ColumnsBuilder<T> = ColumnsBuilder()
    }
}

class ColumnBuilder<T> internal constructor() : AttributesAwareBuilder<Column<T>>() {

    @JvmSynthetic
    lateinit var id: ColumnKey<T>

    @JvmSynthetic
    var columnType: CellType? = null

    @JvmSynthetic
    var index: Int? = null

    @JvmSynthetic
    var dataFormatter: ((field: Any) -> Any)? = null

    @JvmSynthetic
    override fun build(): Column<T> = Column(
        id, index, columnType,
        getAttributesByClass(ColumnAttribute::class.java),
        getAttributesByClass(CellAttribute::class.java),
        dataFormatter
    )

    @JvmSynthetic
    override fun supportedAttributeClasses(): Set<Class<out Attribute>> =
        setOf(ColumnAttribute::class.java, CellAttribute::class.java)

    companion object {
        @JvmSynthetic
        internal fun <T> new(): ColumnBuilder<T> = ColumnBuilder()
    }
}

class RowsBuilder<T> internal constructor(private val columnsBuilder: ColumnsBuilder<T>) : Builder<List<Row<T>>> {

    @JvmSynthetic
    val rowBuilders: MutableList<RowBuilder<T>> = mutableListOf()

    private var rowIndex: Int = 0

    private val interceptedRowSpans: MutableMap<ColumnKey<T>, Int> = mutableMapOf()

    @JvmSynthetic
    fun addRowBuilder(block: DslBlock<RowBuilder<T>>) =
        RowBuilder.new(columnsBuilder, interceptedRowSpans).let {
            rowBuilders.add(it)
            it.createAt = rowIndex
            block.invoke(it)
            rowIndex = it.createAt?.plus(1) ?: rowIndex
            interceptRowSpans()
        }

    @JvmSynthetic
    fun addRowBuilder(selector: RowSelector<T>, block: DslBlock<RowBuilder<T>>)  =
        RowBuilder.new(columnsBuilder, interceptedRowSpans).let {
            rowBuilders.add(it)
            it.selector = selector
            block.invoke(it)
            interceptRowSpans()
        }

    @JvmSynthetic
    fun addRowBuilder(at: Int, block: DslBlock<RowBuilder<T>>) =
        RowBuilder.new(columnsBuilder, interceptedRowSpans).let {
            rowIndex = at
            rowBuilders.add(it)
            it.createAt = rowIndex ++
            block.invoke(it)
            interceptRowSpans()
        }

    @JvmSynthetic
    override fun build(): List<Row<T>> {
        return sortedNullsLast().map { it.build() }
    }

    private fun interceptRowSpans() {
        sortedCustomRows().let { customRows ->
            if (customRows.isNotEmpty()) {
                customRows.last().let { lastRow ->
                    columnsBuilder.columnBuilders.forEach { columnBuilder ->
                        val cellBuilder: CellBuilder<T>? = lastRow.cellsBuilder.cells[columnBuilder.id]
                        interceptedRowSpans[columnBuilder.id] = if (cellBuilder != null) {
                            (cellBuilder.rowSpan ?: 1) - 1
                        } else {
                            (interceptedRowSpans[columnBuilder.id]?.let { it - 1 } ?: 0).coerceAtLeast(0)
                        }
                    }
                }
            }
        }
    }

    private inline fun sortedNullsLast(): List<RowBuilder<T>> {
        return rowBuilders.sortedWith(compareBy(nullsLast()) { it.createAt })
    }

    private inline fun sortedCustomRows(): List<RowBuilder<T>> {
        return sortedNullsLast().filter { it.createAt != null }
    }

    companion object {
        @JvmSynthetic
        internal fun <T> new(columnsBuilder: ColumnsBuilder<T>): RowsBuilder<T> = RowsBuilder(columnsBuilder)
    }
}

class RowBuilder<T> private constructor(
    columnsBuilder: ColumnsBuilder<T>,
    interceptedRowSpans: MutableMap<ColumnKey<T>, Int>
) : AttributesAwareBuilder<Row<T>>() {

    @JvmSynthetic
    val cellsBuilder: CellsBuilder<T> = CellsBuilder.new(columnsBuilder, interceptedRowSpans)

    @JvmSynthetic
    private var _createAt: Int? = null

    @JvmSynthetic
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
    override fun build(): Row<T> = Row(
        selector, createAt,
        getAttributesByClass(RowAttribute::class.java),
        getAttributesByClass(CellAttribute::class.java),
        cellsBuilder.build()
    )

    @JvmSynthetic
    override fun supportedAttributeClasses(): Set<Class<out Attribute>> =
        setOf(RowAttribute::class.java, CellAttribute::class.java)

    companion object {
        @JvmSynthetic
        internal fun <T> new(
            columnsBuilder: ColumnsBuilder<T>,
            interceptedRowSpans: MutableMap<ColumnKey<T>, Int>
        ): RowBuilder<T> = RowBuilder(columnsBuilder, interceptedRowSpans)
    }
}

class CellsBuilder<T> private constructor(
    private val columnsBuilder: ColumnsBuilder<T>,
    private val interceptedRowSpans: MutableMap<ColumnKey<T>, Int>
) : Builder<Map<ColumnKey<T>, Cell<T>>> {

    @JvmSynthetic
    val cells: MutableMap<ColumnKey<T>, CellBuilder<T>> = mutableMapOf()

    @JvmSynthetic
    private var cellIndex: Int = 0

    private var activeColSpanOffset: Int = 0

    @JvmSynthetic
    fun addCellBuilder(id: String, block: DslBlock<CellBuilder<T>>) =
        CellBuilder.new<T>().let {
            cells[ColumnKey(id = id)] = it
            block.invoke(it)
        }

    @JvmSynthetic
    fun addCellBuilder(index: Int, block: DslBlock<CellBuilder<T>>) =
        CellBuilder.new<T>().let {
            columnsBuilder.columnBuilders[index].let { column ->
                cells[ColumnKey(ref = column.id.ref, id = column.id.id)] = it
                block.invoke(it)
            }
        }


    @JvmSynthetic
    fun addCellBuilder(block: DslBlock<CellBuilder<T>>) =
        addCellBuilder(index = cellIndex(), block).also {
            nextCellIndex()
        }


    @JvmSynthetic
    fun addCellBuilder(ref: ((record: T) -> Any?), block: DslBlock<CellBuilder<T>>) =
        CellBuilder.new<T>().let {
            cells[ColumnKey(ref = ref)] = it
            block.invoke(it)
        }

    @JvmSynthetic
    override fun build(): Map<ColumnKey<T>, Cell<T>> {
        return cells.map { it.key to it.value.build() }.toMap()
    }

    private fun cellIndex(): Int {
        if (activeColSpanOffset > 0)
            cellIndex += activeColSpanOffset
        while ((interceptedRowSpans[columnsBuilder.columnBuilders[cellIndex].id] ?: 0) > 0 && cellIndex < columnsBuilder.columnBuilders.size - 1) {
            cellIndex++
        }
        return cellIndex
    }

    private fun nextCellIndex() {
        activeColSpanOffset = (cells[columnsBuilder.columnBuilders[cellIndex++].id]?.let { it.colSpan?.minus(1) ?: 0 } ?: 0).coerceAtLeast(0)
    }

    companion object {
        @JvmSynthetic
        internal fun <T> new(
            columnsBuilder: ColumnsBuilder<T>,
            interceptedRowSpans: MutableMap<ColumnKey<T>, Int>
        ): CellsBuilder<T> = CellsBuilder(columnsBuilder, interceptedRowSpans)
    }
}

class CellBuilder<T> private constructor() : AttributesAwareBuilder<Cell<T>>() {

    @JvmSynthetic
    var value: Any? = null

    @JvmSynthetic
    var eval: RowCellEval<T>? = null

    @JvmSynthetic
    var type: CellType? = null

    @JvmSynthetic
    var colSpan: Int? = 1

    @JvmSynthetic
    var rowSpan: Int? = 1

    @JvmSynthetic
    override fun build(): Cell<T> =
        Cell(value, eval, type, colSpan, rowSpan, getAttributesByClass(CellAttribute::class.java))

    @JvmSynthetic
    override fun supportedAttributeClasses(): Set<Class<out Attribute>> = setOf(CellAttribute::class.java)

    companion object {
        @JvmSynthetic
        internal fun <T> new(): CellBuilder<T> = CellBuilder()
    }
}

interface TableAttributeBuilder : AttributeBuilder<TableAttribute>

interface AttributeBuilder<T : Attribute> : Builder<T>

interface CellAttributeBuilder<T : CellAttribute<T>> : AttributeBuilder<T>

interface RowAttributeBuilder : AttributeBuilder<RowAttribute>

interface ColumnAttributeBuilder : AttributeBuilder<ColumnAttribute>



