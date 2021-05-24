package io.github.voytech.tabulate.api.builder

import io.github.voytech.tabulate.model.*
import io.github.voytech.tabulate.model.attributes.*

interface Builder<T> {
    fun build(): T
}

typealias DslBlock<T> = (T) -> Unit

abstract class AttributesAware {
    private var attributes: MutableMap<Class<out Attribute<*>>, Set<Attribute<*>>> = mutableMapOf()

    @JvmSynthetic
    fun attributes(vararg attributes: Attribute<*>) {
        applyAttributes(attributes.asList())
    }

    @JvmSynthetic
    fun attributes(attributes: Collection<Attribute<*>>) {
        applyAttributes(attributes)
    }

    @JvmSynthetic
    protected fun attributes(vararg builders: AttributeBuilder<out Attribute<*>>) {
        attributes(*(builders.map { it.build() }).toTypedArray())
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Attribute<*>> visit(clazz: Class<T>, visitor: ((current: Set<T>?) -> Set<T>?)) {
        this.attributes[clazz] = visitor.invoke(this.attributes[clazz] as Set<T>?) ?: emptySet()
    }

    @Suppress("UNCHECKED_CAST")
    @JvmSynthetic
    protected fun <C : Attribute<*>> getAttributesByClass(clazz: Class<C>): Set<C>? = attributes[clazz] as Set<C>?

    @JvmSynthetic
    protected abstract fun supportedAttributeClasses(): Set<Class<out Attribute<*>>>

    private fun applyAttributes(attributes: Collection<Attribute<*>>) {
        attributes.forEach { attribute ->
            supportedAttributeClasses().find { clazz -> clazz.isAssignableFrom(attribute.javaClass) }
                ?.let { baseClass ->
                    this.attributes[baseClass] =
                        this.attributes[baseClass]
                            ?.filter { it.javaClass != attribute.javaClass }
                            ?.toSet()
                            ?.let { it + attribute } ?: setOf(attribute)
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
    override fun supportedAttributeClasses(): Set<Class<out Attribute<*>>> =
        setOf(TableAttribute::class.java, CellAttribute::class.java)

}

class ColumnsBuilder<T> internal constructor() : Builder<List<ColumnDef<T>>> {

    @JvmSynthetic
    val columnBuilders: MutableList<ColumnBuilder<T>> = mutableListOf()

    var count: Int?
        get() {
            return columnBuilders.size
        }
        set(value) {
            resize(value)
        }

    @JvmSynthetic
    fun addColumnBuilder(id: String, block: DslBlock<ColumnBuilder<T>>) : ColumnBuilder<T> =
        ensureColumnBuilder(ColumnKey(id = id)).let {
            block.invoke(it)
            it
        }

    @JvmSynthetic
    fun addColumnBuilder(ref: ((record: T) -> Any?), block: DslBlock<ColumnBuilder<T>>): ColumnBuilder<T> =
        ensureColumnBuilder(ColumnKey(ref = ref)).let {
            block.invoke(it)
            it
        }

    @JvmSynthetic
    private fun ensureColumnBuilder(key: ColumnKey<T>) : ColumnBuilder<T> =
        columnBuilders.find { it.id == key } ?:
        ColumnBuilder.new<T>().let {
            columnBuilders.add(it.apply { it.id = key })
            it
        }

    @JvmSynthetic
    private fun addColumnBuilder(id: String) : ColumnBuilder<T> = ensureColumnBuilder(ColumnKey(id = id))

    private fun resize(newSize: Int?) {
        if (newSize ?: 0 < columnBuilders.size) {
            columnBuilders.take(newSize ?: 0).also {
                columnBuilders.retainAll(it)
            }
        } else if (newSize ?: 0 > columnBuilders.size) {
            (columnBuilders.size + 1..(newSize ?: 0)).forEach { addColumnBuilder("column-$it") }
        }
    }

    @JvmSynthetic
    override fun build(): List<ColumnDef<T>> {
        return columnBuilders.map { it.build() }
    }

    companion object {
        @JvmSynthetic
        internal fun <T> new(): ColumnsBuilder<T> = ColumnsBuilder()
    }
}

class ColumnBuilder<T> internal constructor() : AttributesAwareBuilder<ColumnDef<T>>() {

    @JvmSynthetic
    lateinit var id: ColumnKey<T>

    @JvmSynthetic
    var columnType: CellType? = null

    @JvmSynthetic
    var index: Int? = null

    @JvmSynthetic
    var dataFormatter: ((field: Any) -> Any)? = null

    @JvmSynthetic
    override fun build(): ColumnDef<T> = ColumnDef(
        id, index, columnType,
        getAttributesByClass(ColumnAttribute::class.java),
        getAttributesByClass(CellAttribute::class.java)
    )

    @JvmSynthetic
    override fun supportedAttributeClasses(): Set<Class<out Attribute<*>>> =
        setOf(ColumnAttribute::class.java, CellAttribute::class.java)

    companion object {
        @JvmSynthetic
        internal fun <T> new(): ColumnBuilder<T> = ColumnBuilder()
    }
}

class RowsBuilder<T> internal constructor(private val columnsBuilder: ColumnsBuilder<T>) : Builder<List<RowDef<T>>> {

    @JvmSynthetic
    val rowBuilders: MutableList<RowBuilder<T>> = mutableListOf()

    private var rowIndex: Int = 0

    private val interceptedRowSpans: MutableMap<ColumnKey<T>, Int> = mutableMapOf()

    @JvmSynthetic
    fun addRowBuilder(block: DslBlock<RowBuilder<T>>): RowBuilder<T> =
        ensureRowBuilder(RowQualifier(createAt = rowIndex)).let {
            block.invoke(it)
            rowIndex = it.qualifier.createAt?.plus(1) ?: rowIndex
            refreshRowSpans(it)
            it
        }

    @JvmSynthetic
    fun addRowBuilder(selector: RowPredicate<T>, block: DslBlock<RowBuilder<T>>): RowBuilder<T> =
        ensureRowBuilder(RowQualifier(applyWhen = selector)).let {
            block.invoke(it)
            it
        }

    @JvmSynthetic
    fun addRowBuilder(at: Int, block: DslBlock<RowBuilder<T>>): RowBuilder<T> {
        rowIndex = at
        return ensureRowBuilder(RowQualifier(createAt = rowIndex++)).let {
            block.invoke(it)
            refreshRowSpans(it)
            it
        }
    }

    @JvmSynthetic
    private fun ensureRowBuilder(rowQualifier: RowQualifier<T>): RowBuilder<T> =
        rowBuilders.find { it.qualifier == rowQualifier } ?:
        RowBuilder.new(columnsBuilder, interceptedRowSpans).let {
            rowBuilders.add(it)
            it.qualifier = rowQualifier
            it
        }

    @JvmSynthetic
    override fun build(): List<RowDef<T>> {
        return sortedNullsLast().map { it.build() }
    }

    private fun decreaseRowSpan(key: ColumnKey<T>): Int =
        (interceptedRowSpans[key]?.let { it - 1 } ?: 0).coerceAtLeast(0)

    private fun refreshRowSpans(rowBuilder: RowBuilder<T>) {
        columnsBuilder.columnBuilders.forEach { columnBuilder ->
            rowBuilder.getCellBuilder(columnBuilder.id).let {
                interceptedRowSpans[columnBuilder.id] =
                    if (it != null) it.rowSpan - 1 else decreaseRowSpan(columnBuilder.id)
            }
        }
    }

    private fun sortedNullsLast(): List<RowBuilder<T>> {
        return rowBuilders.sortedWith(compareBy(nullsLast()) { it.qualifier.createAt })
    }

    companion object {
        @JvmSynthetic
        internal fun <T> new(columnsBuilder: ColumnsBuilder<T>): RowsBuilder<T> = RowsBuilder(columnsBuilder)
    }
}

class RowBuilder<T> private constructor(
    columnsBuilder: ColumnsBuilder<T>,
    interceptedRowSpans: MutableMap<ColumnKey<T>, Int>
) : AttributesAwareBuilder<RowDef<T>>() {

    @JvmSynthetic
    val cellsBuilder: CellsBuilder<T> = CellsBuilder.new(columnsBuilder, interceptedRowSpans)

    @JvmSynthetic
    internal lateinit var qualifier: RowQualifier<T>

    internal fun getCellBuilder(key: ColumnKey<T>): CellBuilder<T>? = cellsBuilder.cells[key]

    @JvmSynthetic
    override fun build(): RowDef<T> = RowDef(
        qualifier,
        getAttributesByClass(RowAttribute::class.java),
        getAttributesByClass(CellAttribute::class.java),
        cellsBuilder.build()
    )

    @JvmSynthetic
    override fun supportedAttributeClasses(): Set<Class<out Attribute<*>>> =
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
) : Builder<Map<ColumnKey<T>, CellDef<T>>> {

    @JvmSynthetic
    val cells: MutableMap<ColumnKey<T>, CellBuilder<T>> = mutableMapOf()

    @JvmSynthetic
    private var cellIndex: Int = 0

    @JvmSynthetic
    fun addCellBuilder(id: String, block: DslBlock<CellBuilder<T>>): CellBuilder<T> =
        ensureCellBuilder(ColumnKey(id = id)).apply(block)

    @JvmSynthetic
    fun addCellBuilder(index: Int, block: DslBlock<CellBuilder<T>>): CellBuilder<T> =
        columnsBuilder.columnBuilders[index].let { column ->
            ensureCellBuilder(column.id).let { cell ->
                block.invoke(cell)
                cellIndex = index
                nextCellIndex()
                cell
            }
        }

    @JvmSynthetic
    fun addCellBuilder(block: DslBlock<CellBuilder<T>>): CellBuilder<T> =
        addCellBuilder(index = currCellIndex(), block)


    @JvmSynthetic
    fun addCellBuilder(ref: ((record: T) -> Any?), block: DslBlock<CellBuilder<T>>): CellBuilder<T> =
        ensureCellBuilder(ColumnKey(ref = ref)).apply(block)


    @JvmSynthetic
    override fun build(): Map<ColumnKey<T>, CellDef<T>> {
        return cells.map { it.key to it.value.build() }.toMap()
    }

    @JvmSynthetic
    private fun ensureCellBuilder(key: ColumnKey<T>): CellBuilder<T> =
        cells.entries.find { it.key == key }?.value ?:
        CellBuilder.new<T>().let {
            cells[key] = it
            it
        }

    private fun columnIdByIndex(index: Int): ColumnKey<T> = columnsBuilder.columnBuilders[index].id

    private fun columnSpanByIndex(index: Int): Int = cells[columnIdByIndex(index)]?.colSpan ?: 1

    private fun rowSpanOffsetByIndex(index: Int): Int = interceptedRowSpans[columnIdByIndex(index)] ?: 0

    private fun currCellIndex(): Int {
        while (rowSpanOffsetByIndex(cellIndex) > 0 && cellIndex < columnsBuilder.columnBuilders.size - 1) {
            cellIndex++
        }
        return cellIndex
    }

    private fun nextCellIndex() {
        cellIndex += columnSpanByIndex(cellIndex)
    }

    companion object {
        @JvmSynthetic
        internal fun <T> new(
            columnsBuilder: ColumnsBuilder<T>,
            interceptedRowSpans: MutableMap<ColumnKey<T>, Int>
        ): CellsBuilder<T> = CellsBuilder(columnsBuilder, interceptedRowSpans)
    }
}

class CellBuilder<T> private constructor() : AttributesAwareBuilder<CellDef<T>>() {

    @JvmSynthetic
    var value: Any? = null

    @JvmSynthetic
    var expression: RowCellExpression<T>? = null

    @JvmSynthetic
    var type: CellType? = null

    @JvmSynthetic
    var colSpan: Int = 1

    @JvmSynthetic
    var rowSpan: Int = 1

    @JvmSynthetic
    override fun build(): CellDef<T> =
        CellDef(value, expression, type, colSpan, rowSpan, getAttributesByClass(CellAttribute::class.java))

    @JvmSynthetic
    override fun supportedAttributeClasses(): Set<Class<out Attribute<*>>> = setOf(CellAttribute::class.java)

    companion object {
        @JvmSynthetic
        internal fun <T> new(): CellBuilder<T> = CellBuilder()
    }

}

interface TableAttributeBuilder : AttributeBuilder<TableAttribute<*>>

interface AttributeBuilder<T : Attribute<*>> : Builder<T>

interface CellAttributeBuilder<T : CellAttribute<T>> : AttributeBuilder<T>

interface RowAttributeBuilder : AttributeBuilder<RowAttribute<*>>

interface ColumnAttributeBuilder : AttributeBuilder<ColumnAttribute<*>>



