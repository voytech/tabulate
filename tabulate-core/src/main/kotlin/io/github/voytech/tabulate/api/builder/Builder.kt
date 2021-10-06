package io.github.voytech.tabulate.api.builder

import io.github.voytech.tabulate.api.builder.exception.BuilderException
import io.github.voytech.tabulate.model.*
import io.github.voytech.tabulate.model.attributes.*
import io.github.voytech.tabulate.template.context.DefaultSteps
import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class Builder<T> {
    internal abstract fun build(): T
}

fun interface BuilderTransformer<T, B: Builder<T>> {
    fun transform(builder: B): B
}

internal fun interface TableBuilderTransformer<T>: BuilderTransformer<Table<T>, TableBuilderState<T>>

typealias DslBlock<T> = (T) -> Unit

sealed class AttributesAwareBuilder<T>: Builder<T>() {

    private var attributes: MutableMap<Class<out Attribute<*>>, Set<Attribute<*>>> = mutableMapOf()

    @JvmSynthetic
    protected open fun <A : Attribute<A>, B: AttributeBuilder<A>> attribute(builder: B) {
        applyAttribute(builder.build())
    }

    @Suppress("UNCHECKED_CAST")
    fun <A : Attribute<*>> visit(clazz: Class<A>, visitor: ((current: Set<A>) -> Set<A>)) {
        attributes[clazz]?.let {
            visitor.invoke(it as Set<A>)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @JvmSynthetic
    protected fun <C : Attribute<*>> getAttributesByClass(clazz: Class<C>): Set<C>? = attributes[clazz] as Set<C>?

    private fun applyAttribute(attribute: Attribute<*>) {
        supportedSuperClass(attribute).let { clazz ->
            (attributes[clazz]?.plus(attribute) ?: setOf(attribute)).run {
                attributes[clazz] = this
            }
        }
    }

    companion object {
        private fun supportedSuperClass(attribute: Attribute<*>): Class<out Attribute<*>> {
            return when {
                CellAttribute::class.java.isAssignableFrom(attribute.javaClass) -> CellAttribute::class.java
                ColumnAttribute::class.java.isAssignableFrom(attribute.javaClass) -> ColumnAttribute::class.java
                TableAttribute::class.java.isAssignableFrom(attribute.javaClass) -> TableAttribute::class.java
                RowAttribute::class.java.isAssignableFrom(attribute.javaClass) -> RowAttribute::class.java
                else -> throw BuilderException("Unsupported attribute class.")
            }
        }
    }
}

internal class TableBuilderState<T> : AttributesAwareBuilder<Table<T>>() {

    @get:JvmSynthetic
    internal val columnsBuilderState: ColumnsBuilderState<T> = ColumnsBuilderState.new()

    @get:JvmSynthetic
    internal val rowsBuilderState: RowsBuilderState<T> = RowsBuilderState.new(columnsBuilderState)

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var name: String = "untitled table"

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var firstRow: Int? = 0

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var firstColumn: Int? = 0

    @JvmSynthetic
    override fun build(): Table<T> = Table(
        name, firstRow, firstColumn,
        columnsBuilderState.build(), rowsBuilderState.build(),
        getAttributesByClass(TableAttribute::class.java),
        getAttributesByClass(CellAttribute::class.java),
        getAttributesByClass(ColumnAttribute::class.java),
        getAttributesByClass(RowAttribute::class.java)
    )

    @JvmSynthetic
    public override fun <A : Attribute<A>, B: AttributeBuilder<A>> attribute(builder: B) {
        super.attribute(builder)
    }

}

internal class ColumnsBuilderState<T> private constructor() : Builder<List<ColumnDef<T>>>() {

    @get:JvmSynthetic
    val columnBuilderStates: MutableList<ColumnBuilderState<T>> = mutableListOf()

    @get:JvmSynthetic
    @set:JvmSynthetic
    var count: Int?
        get() {
            return columnBuilderStates.size
        }
        set(value) {
            resize(value)
        }

    @JvmSynthetic
    fun addColumnBuilder(id: String, block: DslBlock<ColumnBuilderState<T>>): ColumnBuilderState<T> =
        ensureColumnBuilder(ColumnKey(id = id)).let {
            block.invoke(it)
            it
        }

    @JvmSynthetic
    fun addColumnBuilder(ref: ColRefId<T>, block: DslBlock<ColumnBuilderState<T>>): ColumnBuilderState<T> =
        ensureColumnBuilder(ColumnKey(ref = ref)).let {
            block.invoke(it)
            it
        }

    private fun ensureColumnBuilder(key: ColumnKey<T>): ColumnBuilderState<T> =
        columnBuilderStates.find { it.id == key } ?: ColumnBuilderState.new<T>().let {
            columnBuilderStates.add(it.apply { it.id = key })
            it
        }

    private fun addColumnBuilder(id: String): ColumnBuilderState<T> = ensureColumnBuilder(ColumnKey(id = id))

    private fun resize(newSize: Int?) {
        if (newSize ?: 0 < columnBuilderStates.size) {
            columnBuilderStates.take(newSize ?: 0).also {
                columnBuilderStates.retainAll(it)
            }
        } else if (newSize ?: 0 > columnBuilderStates.size) {
            (columnBuilderStates.size + 1..(newSize ?: 0)).forEach { addColumnBuilder("column-$it") }
        }
    }

    @JvmSynthetic
    override fun build(): List<ColumnDef<T>> {
        return columnBuilderStates.map { it.build() }
    }

    companion object {
        @JvmSynthetic
        internal fun <T> new(): ColumnsBuilderState<T> = ColumnsBuilderState()
    }
}

internal class ColumnBuilderState<T> private constructor() : AttributesAwareBuilder<ColumnDef<T>>() {

    @get:JvmSynthetic
    @set:JvmSynthetic
    lateinit var id: ColumnKey<T>

    @get:JvmSynthetic
    @set:JvmSynthetic
    var columnType: CellType? = null

    @get:JvmSynthetic
    @set:JvmSynthetic
    var index: Int? = null

    @JvmSynthetic
    fun <A : ColumnAttribute<A>, B: ColumnAttributeBuilder<A>> attribute(builder: B): Unit = super.attribute(builder)

    @JvmSynthetic
    fun <A : CellAttribute<A>, B: CellAttributeBuilder<A>> attribute(builder: B): Unit = super.attribute(builder)

    @JvmSynthetic
    override fun build(): ColumnDef<T> = ColumnDef(
        id, index, columnType,
        getAttributesByClass(ColumnAttribute::class.java),
        getAttributesByClass(CellAttribute::class.java)
    )

    companion object {
        @JvmSynthetic
        internal fun <T> new(): ColumnBuilderState<T> = ColumnBuilderState()
    }
}

internal class RowsBuilderState<T> private constructor(private val columnsBuilderState: ColumnsBuilderState<T>) : Builder<List<RowDef<T>>>() {

    @get:JvmSynthetic
    val rowBuilderStates: MutableList<RowBuilderState<T>> = mutableListOf()

    private var rowIndex: RowIndexDef = RowIndexDef(0)

    private val interceptedRowSpans: MutableMap<ColumnKey<T>, Int> = mutableMapOf()

    @JvmSynthetic
    fun addRowBuilder(block: DslBlock<RowBuilderState<T>>): RowBuilderState<T> =
        ensureRowBuilder(RowQualifier(createAt = rowIndex)).let {
            block.invoke(it)
            rowIndex = it.qualifier.createAt?.plus(1) ?: rowIndex
            refreshRowSpans(it)
            it
        }

    @JvmSynthetic
    fun addRowBuilder(selector: RowPredicate<T>, block: DslBlock<RowBuilderState<T>>): RowBuilderState<T> =
        ensureRowBuilder(RowQualifier(applyWhen = selector)).let {
            block.invoke(it)
            it
        }

    @JvmSynthetic
    fun addRowBuilder(at: RowIndexDef, block: DslBlock<RowBuilderState<T>>): RowBuilderState<T> {
        rowIndex = at
        return ensureRowBuilder(RowQualifier(createAt = rowIndex++)).let {
            block.invoke(it)
            refreshRowSpans(it)
            it
        }
    }

    @JvmSynthetic
    fun addRowBuilder(label: DefaultSteps, block: DslBlock<RowBuilderState<T>>): RowBuilderState<T> {
        if (label.name != rowIndex.offsetLabel) {
            rowIndex = RowIndexDef(index = 0, offsetLabel = label.name)
        }
        return ensureRowBuilder(RowQualifier(createAt = rowIndex++)).let {
            block.invoke(it)
            refreshRowSpans(it)
            it
        }
    }

    private fun ensureRowBuilder(rowQualifier: RowQualifier<T>): RowBuilderState<T> =
        rowBuilderStates.find { it.qualifier == rowQualifier } ?: RowBuilderState.new(columnsBuilderState, interceptedRowSpans).let {
            rowBuilderStates.add(it)
            it.qualifier = rowQualifier
            it
        }

    @JvmSynthetic
    override fun build(): List<RowDef<T>> {
        return sortedNullsLast().map { it.build() }
    }

    private fun decreaseRowSpan(key: ColumnKey<T>): Int =
        (interceptedRowSpans[key]?.let { it - 1 } ?: 0).coerceAtLeast(0)

    private fun refreshRowSpans(rowBuilderState: RowBuilderState<T>) {
        columnsBuilderState.columnBuilderStates.forEach { columnBuilder ->
            rowBuilderState.getCellBuilder(columnBuilder.id).let {
                interceptedRowSpans[columnBuilder.id] =
                    if (it != null) it.rowSpan - 1 else decreaseRowSpan(columnBuilder.id)
            }
        }
    }

    private fun sortedNullsLast(): List<RowBuilderState<T>> {
        return rowBuilderStates.sortedWith(compareBy(nullsLast()) { it.qualifier.createAt })
    }

    companion object {
        @JvmSynthetic
        internal fun <T> new(columnsBuilderState: ColumnsBuilderState<T>): RowsBuilderState<T> = RowsBuilderState(columnsBuilderState)
    }
}

internal class RowBuilderState<T> private constructor(
    columnsBuilderState: ColumnsBuilderState<T>,
    interceptedRowSpans: MutableMap<ColumnKey<T>, Int>,
) : AttributesAwareBuilder<RowDef<T>>() {

    @get:JvmSynthetic
    val cells: MutableMap<ColumnKey<T>, CellBuilderState<T>> = mutableMapOf()

    @get:JvmSynthetic
    private val cellIndex: AtomicInteger = AtomicInteger(0)

    @get:JvmSynthetic
    val cellsBuilderState: CellsBuilderState<T> = CellsBuilderState.new(columnsBuilderState, interceptedRowSpans, cellIndex, cells)

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal lateinit var qualifier: RowQualifier<T>

    internal fun getCellBuilder(key: ColumnKey<T>): CellBuilderState<T>? = cells[key]

    @JvmSynthetic
    override fun build(): RowDef<T> = RowDef(
        qualifier,
        getAttributesByClass(RowAttribute::class.java),
        getAttributesByClass(CellAttribute::class.java),
        cellsBuilderState.build()
    )

    @JvmSynthetic
    fun <A : RowAttribute<A>, B: RowAttributeBuilder<A>> attribute(builder: B): Unit = super.attribute(builder)

    @JvmSynthetic
    fun <A : CellAttribute<A>, B: CellAttributeBuilder<A>> attribute(builder: B): Unit = super.attribute(builder)

    companion object {
        @JvmSynthetic
        internal fun <T> new(
            columnsBuilderState: ColumnsBuilderState<T>,
            interceptedRowSpans: MutableMap<ColumnKey<T>, Int>,
        ): RowBuilderState<T> = RowBuilderState(columnsBuilderState, interceptedRowSpans)
    }
}

internal class CellsBuilderState<T> private constructor(
    private val columnsBuilderState: ColumnsBuilderState<T>,
    private val interceptedRowSpans: MutableMap<ColumnKey<T>, Int>,
    private var cellIndex: AtomicInteger,
    private val cells: MutableMap<ColumnKey<T>, CellBuilderState<T>>,
) : Builder<Map<ColumnKey<T>, CellDef<T>>>() {

    @JvmSynthetic
    fun addCellBuilder(id: String, block: DslBlock<CellBuilderState<T>>): CellBuilderState<T> =
        ensureCellBuilder(ColumnKey(id = id)).apply(block)
            .also { nextCellIndex() }

    @JvmSynthetic
    fun addCellBuilder(index: Int, block: DslBlock<CellBuilderState<T>>): CellBuilderState<T> =
        columnsBuilderState.columnBuilderStates[index].let { column ->
            ensureCellBuilder(column.id).apply(block)
                .also {
                    cellIndex.set(index)
                    nextCellIndex()
                }
        }

    @JvmSynthetic
    fun addCellBuilder(block: DslBlock<CellBuilderState<T>>): CellBuilderState<T> =
        addCellBuilder(index = currCellIndex(), block)


    @JvmSynthetic
    fun addCellBuilder(ref: ColRefId<T>, block: DslBlock<CellBuilderState<T>>): CellBuilderState<T> =
        ensureCellBuilder(ColumnKey(ref = ref)).apply(block)
            .also { nextCellIndex() }


    @JvmSynthetic
    override fun build(): Map<ColumnKey<T>, CellDef<T>> {
        return cells.map { it.key to it.value.build() }.toMap()
    }

    private fun ensureCellBuilder(key: ColumnKey<T>): CellBuilderState<T> =
        cells.entries.find { it.key == key }?.value ?: CellBuilderState.new<T>().let {
            cellIndex.set(cells.entries.size)
            cells[key] = it
            it
        }

    private fun columnIdByIndex(index: Int): ColumnKey<T> = columnsBuilderState.columnBuilderStates[index].id

    private fun columnSpanByIndex(index: Int): Int = cells[columnIdByIndex(index)]?.colSpan ?: 1

    private fun rowSpanOffsetByIndex(index: Int): Int = interceptedRowSpans[columnIdByIndex(index)] ?: 0

    private fun currCellIndex(): Int {
        while (rowSpanOffsetByIndex(cellIndex.get()) > 0 && cellIndex.get() < columnsBuilderState.columnBuilderStates.size - 1) {
            cellIndex.getAndIncrement()
        }
        return cellIndex.get()
    }

    private fun nextCellIndex() {
        cellIndex.addAndGet(columnSpanByIndex(cellIndex.get()))
    }

    companion object {
        @JvmSynthetic
        internal fun <T> new(
            columnsBuilderState: ColumnsBuilderState<T>,
            interceptedRowSpans: MutableMap<ColumnKey<T>, Int>,
            cellIndex: AtomicInteger,
            cells: MutableMap<ColumnKey<T>, CellBuilderState<T>>,
        ): CellsBuilderState<T> = CellsBuilderState(columnsBuilderState, interceptedRowSpans, cellIndex, cells)
    }
}

internal class CellBuilderState<T> private constructor() : AttributesAwareBuilder<CellDef<T>>() {

    @get:JvmSynthetic
    @set:JvmSynthetic
    var value: Any? = null

    @get:JvmSynthetic
    @set:JvmSynthetic
    var expression: RowCellExpression<T>? = null

    @get:JvmSynthetic
    @set:JvmSynthetic
    var type: CellType? = null

    @get:JvmSynthetic
    @set:JvmSynthetic
    var colSpan: Int = 1

    @get:JvmSynthetic
    @set:JvmSynthetic
    var rowSpan: Int = 1

    @JvmSynthetic
    override fun build(): CellDef<T> =
        CellDef(value, expression, type, colSpan, rowSpan, getAttributesByClass(CellAttribute::class.java))

    @JvmSynthetic
    fun <A : CellAttribute<A>, B: CellAttributeBuilder<A>> attribute(builder: B): Unit = super.attribute(builder)

    companion object {
        @JvmSynthetic
        internal fun <T> new(): CellBuilderState<T> = CellBuilderState()
    }

}

abstract class AttributeBuilder<T : Attribute<*>> : Builder<T>() {
    private val propertyChanges = mutableSetOf<KProperty<*>>()
    private val mappings = mutableMapOf<String, String>()
    fun <F> observable(initialValue: F, fieldMapping: Pair<String, String>? = null): ReadWriteProperty<Any?, F> {
        if (fieldMapping != null) {
            mappings[fieldMapping.first] = fieldMapping.second
        }
        return object : ObservableProperty<F>(initialValue) {
            override fun afterChange(property: KProperty<*>, oldValue: F, newValue: F) {
                propertyChanges.add(property)
            }
        }
    }

    protected abstract fun provide(): T

    @JvmSynthetic
    final override fun build(): T {
        return provide().apply {
            nonDefaultProps = propertyChanges.map {
                if (mappings.containsKey(it.name)) mappings[it.name]!! else it.name
            }.toSet()
        }
    }
}

abstract class TableAttributeBuilder<T: TableAttribute<T>> : AttributeBuilder<T>()

abstract class CellAttributeBuilder<T : CellAttribute<T>> : AttributeBuilder<T>()

abstract class RowAttributeBuilder<T : RowAttribute<T>> : AttributeBuilder<T>()

abstract class ColumnAttributeBuilder<T: ColumnAttribute<T>> : AttributeBuilder<T>()



