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

    @get:JvmSynthetic
    @set:JvmSynthetic
    var count: Int?
        get() = columnBuilderStates.size
        set(value) {
            resize(value)
        }

    @JvmSynthetic
    fun addColumnBuilder(id: String, block: DslBlock<ColumnBuilderState<T>>): ColumnBuilderState<T> =
        ensureColumnBuilder(ColumnKey(id = id)).also {
            block.invoke(it)
        }

    @JvmSynthetic
    fun addColumnBuilder(ref: PropertyBindingKey<T>, block: DslBlock<ColumnBuilderState<T>>): ColumnBuilderState<T> =
        ensureColumnBuilder(ColumnKey(ref = ref)).also {
            block.invoke(it)
        }

    private fun ensureColumnBuilder(key: ColumnKey<T>): ColumnBuilderState<T> =
        columnBuilderStates.find { it.id == key } ?: ColumnBuilderState(key, columnBuilderStates).apply {
            index = columnBuilderStates.lastOrNull()?.index?.plus(1) ?: 0
        }.also { columnBuilderStates.add(it) }

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
    index: Int,
    block: (col: ColumnBuilderState<T>) -> R?,
): R? =
    searchForwardUntil {
        if (it.index > index) block(it) else null
    }

@JvmSynthetic
internal fun <T, R> List<ColumnBuilderState<T>>.searchForwardStartingWith(
    index: Int,
    block: (col: ColumnBuilderState<T>) -> R?,
): R? =
    searchForwardUntil {
        if (it.index >= index) block(it) else null
    }

@JvmSynthetic
internal fun <T> List<ColumnBuilderState<T>>.lastIndex(): Int = lastOrNull()?.index ?: 0

internal class ColumnBuilderState<T>(
    internal val id: ColumnKey<T>,
    private val columnBuilderStates: List<ColumnBuilderState<T>>,
) : AttributesAwareBuilder<ColumnDef<T>>() {

    @get:JvmSynthetic
    @set:JvmSynthetic
    var columnType: CellType? = null

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
        id, index, columnType,
        getAttributesByClass(ColumnAttribute::class.java, transformerContainer),
        getAttributesByClass(CellAttribute::class.java, transformerContainer)
    )

}

internal class RowSpans<T> {

    private val rowSpans: MutableMap<ColumnKey<T>, MutableMap<RowIndexDef,Int>> = mutableMapOf()

    private fun Map.Entry<RowIndexDef,Int>.rowIndex(): RowIndexDef = key

    private fun Map.Entry<RowIndexDef,Int>.rowSpanIndexSpace(): Int = value - 1

    internal operator fun plusAssign(map: Pair<ColumnKey<T>, Map<RowIndexDef,Int>>) {
         rowSpans.getOrPut(map.first) { mutableMapOf() } += map.second
    }

    private fun applyRowSpanOffsets(column: ColumnKey<T>): Set<RowIndexDef> =
        rowSpans[column]?.entries?.map { it.rowIndex() + it.rowSpanIndexSpace() }?.toSet() ?: emptySet()

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
    fun addRowBuilder(block: DslBlock<RowBuilderState<T>>): RowBuilderState<T> =
        ensureRowBuilder(RowQualifier(index = RowIndexPredicateLiteral(Eq(rowIndex)))).also {
            block.invoke(it)
            rowIndex = it.qualifier.index?.lastIndex()?.plus(1) ?: rowIndex
        }

    @JvmSynthetic
    fun addRowBuilder(selector: RowPredicate<T>, block: DslBlock<RowBuilderState<T>>): RowBuilderState<T> =
        ensureRowBuilder(RowQualifier(matching = selector)).also {
            block.invoke(it)
        }

    @JvmSynthetic
    fun addRowBuilder(at: RowIndexDef, block: DslBlock<RowBuilderState<T>>): RowBuilderState<T> {
        rowIndex = at
        return ensureRowBuilder(RowQualifier(index = RowIndexPredicateLiteral(Eq(rowIndex++)))).also {
            block.invoke(it)
        }
    }

    @JvmSynthetic
    fun addRowBuilder(step: Enum<*>, block: DslBlock<RowBuilderState<T>>): RowBuilderState<T> {
        if (step != rowIndex.step) {
            rowIndex = RowIndexDef(index = 0, step = step)
        }
        return ensureRowBuilder(RowQualifier(index = RowIndexPredicateLiteral(Eq(rowIndex++)))).also {
            block.invoke(it)
        }
    }

    private fun ensureRowBuilder(rowQualifier: RowQualifier<T>): RowBuilderState<T> =
        rowBuilderStates.find { it.qualifier == rowQualifier } ?: newRowBuilder(rowQualifier)

    private fun newRowBuilder(rowQualifier: RowQualifier<T>): RowBuilderState<T> =
        RowBuilderState.new(columnsBuilderState, this).also {
            rowBuilderStates.add(it)
            it.qualifier = rowQualifier
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
    internal val columnsBuilderState: ColumnsBuilderState<T>,
    internal val rowsBuilderState: RowsBuilderState<T>,
) : AttributesAwareBuilder<RowDef<T>>() {

    @get:JvmSynthetic
    val cells: MutableMap<ColumnKey<T>, CellBuilderState<T>> = mutableMapOf()

    @get:JvmSynthetic
    val cellsBuilderState: CellsBuilderState<T> =
        CellsBuilderState(this, cells)

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal lateinit var qualifier: RowQualifier<T>

    internal fun getCellBuilder(key: ColumnKey<T>): CellBuilderState<T>? = cells[key]

    internal fun applyRowSpan(cellBuilder: CellBuilderState<T>) {
        if (qualifier.index != null) {
            qualifier.index!!.materialize().associateWith { cellBuilder.rowSpan }.let {
                rowsBuilderState.rowSpans += (cellsBuilderState.currentColumn.id to it)
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

    companion object {
        @JvmSynthetic
        internal fun <T> new(
            columnsBuilderState: ColumnsBuilderState<T>,
            rowsBuilderState: RowsBuilderState<T>,
        ): RowBuilderState<T> = RowBuilderState(columnsBuilderState, rowsBuilderState)
    }
}

internal class CellsBuilderState<T>(
    private val rowBuilderState: RowBuilderState<T>,
    private val cells: MutableMap<ColumnKey<T>, CellBuilderState<T>>,
    private val columnBuilders: MutableList<ColumnBuilderState<T>> = rowBuilderState.columnsBuilderState.columnBuilderStates,
) : InternalBuilder<Map<ColumnKey<T>, CellDef<T>>>() {

    private var finished: Boolean = false

    internal var currentColumn: ColumnBuilderState<T> = columnBuilders.first()

    @JvmSynthetic
    fun addCellBuilder(id: String, block: DslBlock<CellBuilderState<T>>): CellBuilderState<T> =
        ensureCellBuilder(ColumnKey(id = id)).apply(block)
            .also { setNextAvailableColumn() }

    @JvmSynthetic
    fun addCellBuilder(index: Int, block: DslBlock<CellBuilderState<T>>): CellBuilderState<T> =
        columnBuilder(index)?.let { column ->
            ensureCellBuilder(column.id).apply(block)
                .also { setNextAvailableColumn() }
        } ?: throw BuilderException("There is no column definition present at index $index")

    @JvmSynthetic
    fun addCellBuilder(block: DslBlock<CellBuilderState<T>>): CellBuilderState<T> =
        if (!finished) {
            findCurrentAvailableColumn()?.let {
                ensureCellBuilder(it.id).apply(block)
                    .also { setNextAvailableColumn() }
            } ?: throw BuilderException("Cannot create new cell. No more bindable column definition exists")
        } else throw BuilderException("Cannot create new cell. No more bindable column definition exists")

    @JvmSynthetic
    fun addCellBuilder(ref: PropertyBindingKey<T>, block: DslBlock<CellBuilderState<T>>): CellBuilderState<T> =
        ensureCellBuilder(ColumnKey(ref = ref)).apply(block)
            .also { setNextAvailableColumn() }

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
            && key.ref != null
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
            currentColumn = findColumnOrThrow(key)
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

    private fun isCellLockedByColSpan(key: ColumnKey<T>): Boolean {
        val currentIndex = findColumnOrNull(key)?.index ?: return false
        return getPreviousCell(key)?.let {
            it.first.index + it.second.colSpan > currentIndex
        } ?: false
    }

    private fun columnBuilder(index: Int): ColumnBuilderState<T>? = columnBuilders.findByIndex(index)

    private fun findNextAvailableColumnOrNull(): ColumnBuilderState<T>? {
        return columnBuilders.searchForwardStartingAfter(currentColumn.index) {
            if (!isCellLockedByRowSpan(it.id) && !isCellLockedByColSpan(it.id)) it.also { currentColumn = it } else null
        }
    }

    private fun setNextAvailableColumn() = findNextAvailableColumnOrNull() ?: run { finished = true }

    private fun findCurrentAvailableColumn(): ColumnBuilderState<T>? {
        return columnBuilders.searchForwardStartingWith(currentColumn.index) {
            if (!isCellLockedByRowSpan(it.id) && !isCellLockedByColSpan(it.id)) it.also { currentColumn = it } else null
        }
    }

}

internal class CellBuilderState<T>(
    internal val rowBuilderState : RowBuilderState<T>
) : AttributesAwareBuilder<CellDef<T>>() {

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
            value, expression, type, colSpan, rowSpan, rowSpanStrategy,
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



