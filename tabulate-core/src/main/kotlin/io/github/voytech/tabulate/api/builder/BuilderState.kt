package io.github.voytech.tabulate.api.builder

import io.github.voytech.tabulate.api.builder.exception.BuilderException
import io.github.voytech.tabulate.model.*
import io.github.voytech.tabulate.model.attributes.*
import kotlin.properties.Delegates
import kotlin.properties.Delegates.vetoable
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

typealias DslBlock<T> = (T) -> Unit

abstract class Builder<T> {
    internal abstract fun build(): T
}

abstract class InternalBuilder<T> {
    internal abstract fun build(transformerContainer: AttributeTransformerContainer? = null): T
}

fun interface AttributeSetTransformer<C : Attribute<*>> {
    fun transform(input: Set<C>): Set<C>
}

internal class AttributeTransformersChain<C : Attribute<*>>(
    private val attributeTransformers: List<AttributeSetTransformer<C>>,
) : AttributeSetTransformer<C> {
    override fun transform(input: Set<C>): Set<C> =
        attributeTransformers.fold(input) { set, transformer -> transformer.transform(set) }
}

internal class AttributeTransformerContainer(
    private val attributeSetTransformers: MutableMap<Class<out Attribute<*>>, AttributeTransformersChain<Attribute<*>>> = mutableMapOf(),
) {
    @Suppress("UNCHECKED_CAST")
    internal fun <C : Attribute<*>> transform(clazz: Class<C>, attributes: Set<C>): Set<C> =
        attributeSetTransformers[clazz]?.transform(attributes) as Set<C>

    @Suppress("UNCHECKED_CAST")
    internal fun <C : Attribute<*>> set(clazz: Class<C>, attributeTransformers: List<AttributeSetTransformer<C>>) {
        attributeSetTransformers[clazz] =
            AttributeTransformersChain(attributeTransformers) as AttributeTransformersChain<Attribute<*>>
    }
}

sealed class AttributesAwareBuilder<T> : InternalBuilder<T>() {

    private var attributes: MutableMap<Class<out Attribute<*>>, Set<Attribute<*>>> = mutableMapOf()

    @JvmSynthetic
    protected open fun <A : Attribute<A>, B : AttributeBuilder<A>> attribute(builder: B) {
        applyAttribute(builder.build())
    }

    @Suppress("UNCHECKED_CAST")
    @JvmSynthetic
    internal fun <C : Attribute<*>> getAttributesByClass(
        clazz: Class<C>,
        transformerContainer: AttributeTransformerContainer?,
    ): Set<C>? {
        return (attributes[clazz] as Set<C>?)?.let {
            transformerContainer?.transform(clazz, it) ?: it
        }
    }

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
    internal val columnsBuilderState: ColumnsBuilderState<T> = ColumnsBuilderState()

    @get:JvmSynthetic
    internal val rowsBuilderState: RowsBuilderState<T> = RowsBuilderState(columnsBuilderState)

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
    override fun build(transformerContainer: AttributeTransformerContainer?): Table<T> = Table(
        name, firstRow, firstColumn,
        columnsBuilderState.build(transformerContainer), rowsBuilderState.build(transformerContainer),
        getAttributesByClass(TableAttribute::class.java, transformerContainer),
        getAttributesByClass(CellAttribute::class.java, transformerContainer),
        getAttributesByClass(ColumnAttribute::class.java, transformerContainer),
        getAttributesByClass(RowAttribute::class.java, transformerContainer)
    )

    @JvmSynthetic
    public override fun <A : Attribute<A>, B : AttributeBuilder<A>> attribute(builder: B) {
        super.attribute(builder)
    }

}

internal class ColumnsBuilderState<T> : InternalBuilder<List<ColumnDef<T>>>() {

    @get:JvmSynthetic
    internal val columnBuilderStates: MutableList<ColumnBuilderState<T>> = mutableListOf()

    @JvmSynthetic
    fun addColumnBuilder(block: DslBlock<ColumnBuilderState<T>>? = null): ColumnBuilderState<T> =
        ensureColumnBuilder(ColumnKey(name = "column-${columnBuilderStates.findLastOrNull()?.index?.plus(1) ?: 0}"))
            .also {
                block?.invoke(it)
            }

    @JvmSynthetic
    fun ensureColumnBuilder(id: String, block: DslBlock<ColumnBuilderState<T>>? = null): ColumnBuilderState<T> =
        ensureColumnBuilder(ColumnKey(name = id)).also {
            block?.invoke(it)
        }

    @JvmSynthetic
    fun ensureColumnBuilder(ref: PropertyBindingKey<T>, block: DslBlock<ColumnBuilderState<T>>? = null): ColumnBuilderState<T> =
        ensureColumnBuilder(ColumnKey(property = ref)).also {
            block?.invoke(it)
        }

    @JvmSynthetic
    fun ensureColumnBuilder(index: Int, block: DslBlock<ColumnBuilderState<T>>? = null): ColumnBuilderState<T> =
        ensureColumnBuilder(index).also {
            block?.invoke(it)
        }

    @JvmSynthetic
    internal fun ensureColumnBuilder(index: Int): ColumnBuilderState<T> =
        columnBuilderStates.find { it.index == index } ?: ColumnBuilderState(columnBuilderStates).apply {
            this.id = ColumnKey("column-$index")
            this.index = index
        }.also { columnBuilderStates.add(it) }

    @JvmSynthetic
    internal fun ensureColumnBuilder(key: ColumnKey<T>): ColumnBuilderState<T> =
        columnBuilderStates.find { it.id == key } ?: ColumnBuilderState(columnBuilderStates).apply {
            id = key
            index = columnBuilderStates.findLastOrNull()?.index?.plus(1) ?: 0
        }.also { columnBuilderStates.add(it) }

    @JvmSynthetic
    override fun build(transformerContainer: AttributeTransformerContainer?): List<ColumnDef<T>> {
        return columnBuilderStates.map { it.build(transformerContainer) }
    }
}


@JvmSynthetic
internal fun <T> List<ColumnBuilderState<T>>.findByKey(key: ColumnKey<T>): ColumnBuilderState<T>? =
    find { it.id == key }

@JvmSynthetic
internal fun <T> List<ColumnBuilderState<T>>.findByIndex(index: Int): ColumnBuilderState<T>? =
    find { it.index == index }

@JvmSynthetic
internal fun <T> List<ColumnBuilderState<T>>.findPrevious(index: Int): ColumnBuilderState<T>? {
    return sortedBy { it.index }.findLast { it.index < index }
}

@JvmSynthetic
internal fun <T> List<ColumnBuilderState<T>>.findPrevious(key: ColumnKey<T>): ColumnBuilderState<T>? {
    return findByKey(key)?.let { column ->
        sortedBy { it.index }.findLast { it.index < column.index }
    }
}

@JvmSynthetic
internal fun <T> List<ColumnBuilderState<T>>.findNext(key: ColumnKey<T>): ColumnBuilderState<T>? {
    return findByKey(key)?.let { column ->
        sortedBy { it.index }.find { it.index > column.index }
    }
}

@JvmSynthetic
internal fun <T> List<ColumnBuilderState<T>>.findLastOrNull(): ColumnBuilderState<T>? = maxByOrNull { it.index }


@JvmSynthetic
internal fun <T, R> List<ColumnBuilderState<T>>.searchBackwardUntil(block: (col: ColumnBuilderState<T>) -> R?): R? =
    sortedBy { it.index }
        .reversed()
        .asSequence()
        .firstNotNullOfOrNull { block(it) }

@JvmSynthetic
internal fun <T, R> List<ColumnBuilderState<T>>.searchBackwardStartingBefore(
    index: Int,
    block: (col: ColumnBuilderState<T>) -> R?,
): R? =
    searchBackwardUntil {
        if (it.index < index) block(it) else null
    }

@JvmSynthetic
internal fun <T, R> List<ColumnBuilderState<T>>.searchForwardUntil(block: (col: ColumnBuilderState<T>) -> R?): R? =
    sortedBy { it.index }
        .asSequence()
        .firstNotNullOfOrNull { block(it) }

@JvmSynthetic
internal fun <T, R> List<ColumnBuilderState<T>>.searchForwardStartingAfter(
    start: ColumnBuilderState<T>,
    block: (col: ColumnBuilderState<T>) -> R?,
): R? =
    searchForwardUntil {
        if (it.index > start.index) block(it) else null
    }

@JvmSynthetic
internal fun <T, R> List<ColumnBuilderState<T>>.searchForwardStartingWith(
    start: ColumnBuilderState<T>,
    block: (col: ColumnBuilderState<T>) -> R?,
): R? =
    searchForwardUntil {
        if (it.index >= start.index) block(it) else null
    }

internal class ColumnBuilderState<T>(private val columnBuilderStates: List<ColumnBuilderState<T>>) :
    AttributesAwareBuilder<ColumnDef<T>>() {

    @get:JvmSynthetic
    @set:JvmSynthetic
    var id: ColumnKey<T> by vetoable(ColumnKey("?")) { _, _, newValue ->
        columnBuilderStates.findByKey(newValue)?.let {
            if (it === this) true
            else throw BuilderException("Could not set column id $newValue because this id is in use by another column.")
        } ?: true
    }

    @get:JvmSynthetic
    @set:JvmSynthetic
    var index: Int by vetoable(columnBuilderStates.lastOrNull()?.index?.plus(1) ?: 0) { _, _, newValue ->
        columnBuilderStates.findByIndex(newValue)?.let {
            if (it === this) true
            else throw BuilderException("Could not set column index $newValue because index is in use by another column.")
        } ?: true
    }

    @JvmSynthetic
    fun <A : ColumnAttribute<A>, B : ColumnAttributeBuilder<A>> attribute(builder: B): Unit = super.attribute(builder)

    @JvmSynthetic
    fun <A : CellAttribute<A>, B : CellAttributeBuilder<A>> attribute(builder: B): Unit = super.attribute(builder)

    @JvmSynthetic
    override fun build(transformerContainer: AttributeTransformerContainer?): ColumnDef<T> = ColumnDef(
        id, index,
        getAttributesByClass(ColumnAttribute::class.java, transformerContainer),
        getAttributesByClass(CellAttribute::class.java, transformerContainer)
    )

}

internal class RowSpans<T> {

    private val rowSpans: MutableMap<ColumnKey<T>, MutableMap<RowIndexDef, Int>> = mutableMapOf()

    private fun Map.Entry<RowIndexDef, Int>.rowIndex(): RowIndexDef = key

    private fun Map.Entry<RowIndexDef, Int>.rowSpan(): Int = value - 1

    private fun Map.Entry<RowIndexDef, Int>.materializeRowSpan(): Set<RowIndexDef> =
        (rowIndex()..(rowIndex() + rowSpan())).materialize()

    internal operator fun plusAssign(spansByColumn: Pair<ColumnKey<T>, Map<RowIndexDef, Int>>) {
        rowSpans.getOrPut(spansByColumn.first) { mutableMapOf() } += spansByColumn.second
    }

    private fun applyRowSpanOffsets(column: ColumnKey<T>): Set<RowIndexDef> =
        rowSpans[column]?.entries?.flatMap { it.materializeRowSpan() }?.toSet() ?: emptySet()

    internal fun RowBuilderState<T>.isColumnLocked(column: ColumnKey<T>): Boolean {
        return qualifier.index?.materialize()?.let { rowIndices ->
            rowIndices intersect applyRowSpanOffsets(column)
        }?.isNotEmpty() ?: false
    }
}

internal class RowsBuilderState<T>(private val columnsBuilderState: ColumnsBuilderState<T>) :
    InternalBuilder<List<RowDef<T>>>() {

    @get:JvmSynthetic
    val rowBuilderStates: MutableList<RowBuilderState<T>> = mutableListOf()

    internal val rowSpans: RowSpans<T> = RowSpans()

    private var rowIndex: RowIndexDef = RowIndexDef(0)

    @JvmSynthetic
    fun addRowBuilder(block: DslBlock<RowBuilderState<T>>? = null): RowBuilderState<T> =
        ensureRowBuilder(RowQualifier(index = RowIndexPredicateLiteral(Eq(rowIndex)))).also {
            block?.invoke(it)
            rowIndex = it.qualifier.index?.lastIndex()?.plus(1) ?: rowIndex
        }

    @JvmSynthetic
    fun addRowBuilder(selector: RowPredicate<T>, block: DslBlock<RowBuilderState<T>>? = null): RowBuilderState<T> =
        ensureRowBuilder(RowQualifier(matching = selector)).also {
            block?.invoke(it)
        }

    @JvmSynthetic
    fun addRowBuilder(
        at: RowIndexPredicateLiteral<T>,
        block: DslBlock<RowBuilderState<T>>? = null,
    ): RowBuilderState<T> {
        rowIndex = at.lastIndex()
        return ensureRowBuilder(RowQualifier(index = at)).also {
            block?.invoke(it)
        }.also { rowIndex++ }
    }

    @JvmSynthetic
    fun addRowBuilder(at: RowIndexDef, block: DslBlock<RowBuilderState<T>>? = null): RowBuilderState<T> {
        rowIndex = at
        return ensureRowBuilder(RowQualifier(index = RowIndexPredicateLiteral(Eq(rowIndex++)))).also {
            block?.invoke(it)
        }
    }

    @JvmSynthetic
    fun addRowBuilder(step: Enum<*>, block: DslBlock<RowBuilderState<T>>? = null): RowBuilderState<T> {
        if (step != rowIndex.step) {
            rowIndex = RowIndexDef(index = 0, step = step)
        }
        return ensureRowBuilder(RowQualifier(index = RowIndexPredicateLiteral(Eq(rowIndex++)))).also {
            block?.invoke(it)
        }
    }

    private fun ensureRowBuilder(rowQualifier: RowQualifier<T>): RowBuilderState<T> =
        rowBuilderStates.find { it.qualifier == rowQualifier } ?: newRowBuilder(rowQualifier)

    private fun newRowBuilder(rowQualifier: RowQualifier<T>): RowBuilderState<T> =
        RowBuilderState(rowQualifier, columnsBuilderState, this).also {
            rowBuilderStates.add(it)
        }

    @JvmSynthetic
    override fun build(transformerContainer: AttributeTransformerContainer?): List<RowDef<T>> {
        return sortedNullsLast().map { it.build(transformerContainer) }
    }

    private fun sortedNullsLast(): List<RowBuilderState<T>> {
        return rowBuilderStates.sortedWith(compareBy(nullsLast()) {
            it.qualifier.index?.computeRanges()?.last()?.endInclusive
        })
    }

}

internal class RowBuilderState<T>(
    @get:JvmSynthetic internal val qualifier: RowQualifier<T>,
    @get:JvmSynthetic internal val columnsBuilderState: ColumnsBuilderState<T>,
    @get:JvmSynthetic internal val rowsBuilderState: RowsBuilderState<T>,
) : AttributesAwareBuilder<RowDef<T>>() {

    @get:JvmSynthetic
    val cells: MutableMap<ColumnKey<T>, CellBuilderState<T>> = mutableMapOf()

    @get:JvmSynthetic
    val cellsBuilderState: CellsBuilderState<T> =
        CellsBuilderState(this, cells)

    internal fun applyRowSpan(cellBuilder: CellBuilderState<T>) {
        if (qualifier.index != null) {
            val step = qualifier.index.firstIndex().step
            mutableMapOf<RowIndexDef, Int>().also {
                qualifier.index.computeRanges().forEach { range ->
                    for (i in range.progression() step cellBuilder.rowSpan) {
                        it[RowIndexDef(i, step)] = cellBuilder.rowSpan
                    }
                }
            }.also {
                rowsBuilderState.rowSpans += (cellsBuilderState.currentColumn!!.id to it)
            }
        }
    }

    @JvmSynthetic
    override fun build(transformerContainer: AttributeTransformerContainer?): RowDef<T> = RowDef(
        qualifier,
        getAttributesByClass(RowAttribute::class.java, transformerContainer),
        getAttributesByClass(CellAttribute::class.java, transformerContainer),
        cellsBuilderState.build(transformerContainer)
    )

    @JvmSynthetic
    fun <A : RowAttribute<A>, B : RowAttributeBuilder<A>> attribute(builder: B): Unit = super.attribute(builder)

    @JvmSynthetic
    fun <A : CellAttribute<A>, B : CellAttributeBuilder<A>> attribute(builder: B): Unit = super.attribute(builder)

    internal fun isCellLockedByRowSpan(key: ColumnKey<T>): Boolean = with(rowsBuilderState.rowSpans) {
        isColumnLocked(key)
    }

}

internal class CellsBuilderState<T>(
    private val rowBuilderState: RowBuilderState<T>,
    private val cells: MutableMap<ColumnKey<T>, CellBuilderState<T>>,
    private val columnBuilders: MutableList<ColumnBuilderState<T>> = rowBuilderState.columnsBuilderState.columnBuilderStates,
) : InternalBuilder<Map<ColumnKey<T>, CellDef<T>>>() {

    internal var currentColumn: ColumnBuilderState<T>? = null

    @JvmSynthetic
    fun addCellBuilder(id: String, block: DslBlock<CellBuilderState<T>>): CellBuilderState<T> =
        ensureCellBuilder(ColumnKey(name = id)).apply(block)

    @JvmSynthetic
    fun addCellBuilder(index: Int, block: DslBlock<CellBuilderState<T>>): CellBuilderState<T> =
        columnBuilder(index)?.let { column ->
            ensureCellBuilder(column.id).apply(block)
        } ?: throw BuilderException("There is no column definition present at index $index")

    @JvmSynthetic
    fun addCellBuilder(block: DslBlock<CellBuilderState<T>>): CellBuilderState<T> =
        setCurrentAvailableColumn().let {
            ensureCellBuilder(it.id).apply(block)
        }

    @JvmSynthetic
    fun addCellBuilder(ref: PropertyBindingKey<T>, block: DslBlock<CellBuilderState<T>>): CellBuilderState<T> =
        ensureCellBuilder(ColumnKey(property = ref)).apply(block)

    @JvmSynthetic
    override fun build(transformerContainer: AttributeTransformerContainer?): Map<ColumnKey<T>, CellDef<T>> {
        return cells.map {
            validateCell(it.key, it.value)
            it.key to it.value.build(transformerContainer)
        }.toMap()
    }

    private fun validateCell(key: ColumnKey<T>, cellBuilder: CellBuilderState<T>) {
        if (cellBuilder.rowSpan > 1
            && cellBuilder.value == null
            && cellBuilder.expression == null
            && key.property != null
        ) {
            throw BuilderException("At least one cell addressed with property literal column key does not define custom value")
        }
    }

    private fun ensureCellBuilder(key: ColumnKey<T>): CellBuilderState<T> =
        cells.selectOrNull(key) ?: newCellBuilder(key)

    private fun MutableMap<ColumnKey<T>, CellBuilderState<T>>.findOrNull(key: ColumnKey<T>): CellBuilderState<T>? =
        this.entries.find { it.key == key }?.value

    private fun MutableMap<ColumnKey<T>, CellBuilderState<T>>.selectOrNull(key: ColumnKey<T>): CellBuilderState<T>? =
        findOrNull(key)?.also { selectColumn(key) }

    private fun newCellBuilder(key: ColumnKey<T>): CellBuilderState<T> {
        if (isCellLockedByRowSpan(key)) throw BuilderException("Cannot create cell at $key due to 'rowSpan' lock.")
        if (isCellLockedByColSpan(key)) throw BuilderException("Cannot create cell at $key due to 'colSpan' lock.")
        return CellBuilderState(rowBuilderState).also {
            currentColumn = rowBuilderState.columnsBuilderState.ensureColumnBuilder(key)
            cells[key] = it
        }
    }

    private fun findColumnOrNull(key: ColumnKey<T>): ColumnBuilderState<T>? = columnBuilders.find { it.id == key }

    private fun findColumnOrThrow(key: ColumnKey<T>): ColumnBuilderState<T> =
        findColumnOrNull(key) ?: throw BuilderException("No column definition for : $key")

    private fun selectColumn(key: ColumnKey<T>): ColumnBuilderState<T>? =
        findColumnOrNull(key)?.also { currentColumn = it }

    private fun getPreviousCell(key: ColumnKey<T>): Pair<ColumnBuilderState<T>, CellBuilderState<T>>? {
        return findColumnOrNull(key)?.let { baseColumn ->
            columnBuilders.searchBackwardStartingBefore(baseColumn.index) { column ->
                cells[column.id]?.let { column to it }
            }
        }
    }

    private fun isCellLockedByRowSpan(key: ColumnKey<T>): Boolean = rowBuilderState.isCellLockedByRowSpan(key)

    private fun isCellLockedByColSpan(key: ColumnKey<T>): Boolean =
        findColumnOrNull(key)?.let { isCellLockedByColSpan(it) } ?: false

    private fun isCellLockedByColSpan(column: ColumnBuilderState<T>): Boolean {
        val currentIndex = column.index
        return getPreviousCell(column.id)?.let {
            it.first.index + it.second.colSpan > currentIndex
        } ?: false
    }

    private fun columnBuilder(index: Int): ColumnBuilderState<T>? = columnBuilders.findByIndex(index)

    private fun findCurrentAvailableColumn(): ColumnBuilderState<T>? {
        return when (currentColumn) {
            null -> columnBuilders.firstOrNull()?.let { first ->
                columnBuilders.searchForwardStartingWith(first) {
                    if (!isCellLockedByRowSpan(it.id)) it else null
                }
            }
            else -> columnBuilders.searchForwardStartingAfter(currentColumn!!) {
                if (!isCellLockedByRowSpan(it.id) && !isCellLockedByColSpan(it)) it else null
            }
        }
    }

    private fun addColumns(): ColumnBuilderState<T> {
        var column: ColumnBuilderState<T>
        do {
            column = rowBuilderState.columnsBuilderState.addColumnBuilder()
        } while (isCellLockedByColSpan(column))
        return column
    }

    private fun ensureCurrentAvailableColumn(): ColumnBuilderState<T> {
        return findCurrentAvailableColumn() ?: addColumns()
    }

    private fun setCurrentAvailableColumn() = ensureCurrentAvailableColumn().also { currentColumn = it }

}

internal class CellBuilderState<T>(
    internal val rowBuilderState: RowBuilderState<T>,
) : AttributesAwareBuilder<CellDef<T>>() {

    @get:JvmSynthetic
    @set:JvmSynthetic
    var value: Any? = null

    @get:JvmSynthetic
    @set:JvmSynthetic
    var expression: RowCellExpression<T>? = null

    @get:JvmSynthetic
    @set:JvmSynthetic
    var colSpan: Int by vetoable(1) { _, _, newValue ->
        if (newValue < 1) {
            throw BuilderException("Minimum value for colSpan is 1")
        } else true
    }

    @get:JvmSynthetic
    @set:JvmSynthetic
    var rowSpan: Int by Delegates.observable(1) { _, _, newValue ->
        if (newValue < 1) {
            throw BuilderException("Minimum value for rowSpan is 1")
        } else {
            rowBuilderState.applyRowSpan(this)
        }
    }

    @get:JvmSynthetic
    @set:JvmSynthetic
    var rowSpanStrategy: CollidingRowSpanStrategy = CollidingRowSpanStrategy.SHADOW

    @JvmSynthetic
    override fun build(transformerContainer: AttributeTransformerContainer?): CellDef<T> =
        CellDef(
            value, expression, colSpan, rowSpan, rowSpanStrategy,
            getAttributesByClass(CellAttribute::class.java, transformerContainer)
        )

    @JvmSynthetic
    fun <A : CellAttribute<A>, B : CellAttributeBuilder<A>> attribute(builder: B): Unit = super.attribute(builder)
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

abstract class TableAttributeBuilder<T : TableAttribute<T>> : AttributeBuilder<T>()

abstract class CellAttributeBuilder<T : CellAttribute<T>> : AttributeBuilder<T>()

abstract class RowAttributeBuilder<T : RowAttribute<T>> : AttributeBuilder<T>()

abstract class ColumnAttributeBuilder<T : ColumnAttribute<T>> : AttributeBuilder<T>()


