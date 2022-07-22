package io.github.voytech.tabulate.components.table.api.builder

import io.github.voytech.tabulate.api.builder.exception.BuilderException
import io.github.voytech.tabulate.components.table.model.*
import io.github.voytech.tabulate.core.api.builder.*
import io.github.voytech.tabulate.core.model.DataSourceBinding
import io.github.voytech.tabulate.core.reify
import kotlin.properties.Delegates
import kotlin.properties.Delegates.vetoable

/**
 * Top level builder state for creating table model.
 * Manages mutable state that is eventually materialized to table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class TableBuilderState<T : Any> : ModelBuilderState<Table<T>>, AttributesAwareBuilder<Table<T>>() {

    @get:JvmSynthetic
    internal val columnsBuilderState: ColumnBuilderStateCollection<T> = ColumnBuilderStateCollection(this)

    @get:JvmSynthetic
    internal val rowsBuilderState: RowBuilderStateCollection<T> = RowBuilderStateCollection(this)

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var name: String = "untitled table"

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var firstRow: Int? = 0

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var firstColumn: Int? = 0

    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var dataSource: DataSourceBinding<T>? = null

    @JvmSynthetic
    override fun build(): Table<T> = Table(
        name, firstRow, firstColumn,
        columnsBuilderState.build(), rowsBuilderState.build(),
        dataSource, attributes()
    )

    override fun modelClass(): Class<Table<T>> = reify()

}

internal class ColumnBuilderStateCollection<T : Any>(parentBuilder: TableBuilderState<T>) :
    ModelPartBuilderCollection<ColumnKey<T>, ColumnDef<T>, TableBuilderState<T>, ColumnBuilderState<T>>(
        parentBuilder, { list -> list.sortedBy { it.index } }
    ) {

    internal fun nextIndex() = entries().maxByOrNull { it.value.index }?.value?.index?.plus(1) ?: 0

    @JvmSynthetic
    fun addColumnBuilder(block: DslBlock<ColumnBuilderState<T>>? = null): ColumnBuilderState<T> =
        ensureBuilder(ColumnKey(name = "column-${nextIndex()}")).also {
            block?.invoke(it)
        }

    @JvmSynthetic
    fun ensureColumnBuilder(id: String, block: DslBlock<ColumnBuilderState<T>>? = null): ColumnBuilderState<T> =
        ensureBuilder(ColumnKey(name = id)).also {
            block?.invoke(it)
        }

    @JvmSynthetic
    fun ensureColumnBuilder(
        ref: PropertyReferenceColumnKey<T>,
        block: DslBlock<ColumnBuilderState<T>>? = null,
    ): ColumnBuilderState<T> = ensureBuilder(ColumnKey(property = ref)).also {
            block?.invoke(it)
    }

    @JvmSynthetic
    internal fun ensureColumnBuilder(key: ColumnKey<T>): ColumnBuilderState<T> = ensureBuilder(key)

    @JvmSynthetic
    fun ensureColumnBuilder(index: Int, block: DslBlock<ColumnBuilderState<T>>? = null): ColumnBuilderState<T> =
        ensureBuilder(index).also {
            block?.invoke(it)
        }

    @JvmSynthetic
    override fun createBuilder(index: Int): Pair<ColumnKey<T>,ColumnBuilderState<T>> =
        ColumnKey<T>("column-$index").let { key ->
            key to ColumnBuilderState(this, key).apply {
                this.index = index
            }
        }

    @JvmSynthetic
    override fun createBuilder(key: ColumnKey<T>): ColumnBuilderState<T> =
        ColumnBuilderState(this, key).apply {
            index = nextIndex()
        }
}

@JvmSynthetic
internal fun <T: Any> List<ColumnBuilderState<T>>.findByKey(key: ColumnKey<T>): ColumnBuilderState<T>? =
    find { it.id == key }


@JvmSynthetic
internal fun <T: Any, R> List<ColumnBuilderState<T>>.searchBackwardUntil(block: (col: ColumnBuilderState<T>) -> R?): R? =
    sortedBy { it.index }
        .reversed()
        .firstNotNullOfOrNull { block(it) }

@JvmSynthetic
internal fun <T: Any, R> List<ColumnBuilderState<T>>.searchBackwardStartingBefore(
    index: Int,
    block: (col: ColumnBuilderState<T>) -> R?,
): R? =
    searchBackwardUntil {
        if (it.index < index) block(it) else null
    }

@JvmSynthetic
internal fun <T: Any, R> List<ColumnBuilderState<T>>.searchForwardUntil(block: (col: ColumnBuilderState<T>) -> R?): R? =
    sortedBy { it.index }
        .firstNotNullOfOrNull { block(it) }

@JvmSynthetic
internal fun <T: Any, R> List<ColumnBuilderState<T>>.searchForwardStartingAfter(
    start: ColumnBuilderState<T>,
    block: (col: ColumnBuilderState<T>) -> R?,
): R? =
    searchForwardUntil {
        if (it.index > start.index) block(it) else null
    }

@JvmSynthetic
internal fun <T: Any, R> List<ColumnBuilderState<T>>.searchForwardStartingWith(
    start: ColumnBuilderState<T>,
    block: (col: ColumnBuilderState<T>) -> R?,
): R? =
    searchForwardUntil {
        if (it.index >= start.index) block(it) else null
    }

/**
 * Column builder state for creating single table column.
 * Manages mutable state that is eventually materialized as part of table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
internal class ColumnBuilderState<T: Any>(
    private val columnBuilderStates: ColumnBuilderStateCollection<T>,
    @get:JvmSynthetic
    internal val id: ColumnKey<T>,
) : AttributesAwareBuilder<ColumnDef<T>>() {

    @get:JvmSynthetic
    @set:JvmSynthetic
    var index: Int by vetoable(columnBuilderStates.nextIndex()) { _, _, newValue ->
        columnBuilderStates.moveAt(id, newValue)
    }

    override fun modelClass(): Class<ColumnDef<T>> = reify()

    @JvmSynthetic
    override fun build(): ColumnDef<T> = ColumnDef(
        id, index, attributes()
    )

}

/**
 * Build-time validation internal utility class.
 * Tracks row spans values per columns to perform validations when new cell is about to be added into builder state.
 * When existing row span constraints are violated - fails-fast during materialization of builder state.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
internal class RowSpans<T: Any> {

    private val rowSpans: MutableMap<ColumnKey<T>, MutableMap<RowIndexDef, Int>> = mutableMapOf()

    private fun Map.Entry<RowIndexDef, Int>.rowIndex(): RowIndexDef = key

    private fun Map.Entry<RowIndexDef, Int>.rowSpan(): Int = value - 1

    private fun Map.Entry<RowIndexDef, Int>.materializeRowSpan(): Set<RowIndexDef> =
        (rowIndex()..rowIndex() + rowSpan()).materialize()

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

/**
 * Rows builder state for creating table rows.
 * Manages mutable state that is eventually materialized as part of table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
internal class RowBuilderStateCollection<T : Any>(parentBuilder: TableBuilderState<T>) :
    ModelPartBuilderCollection<RowQualifier<T>, RowDef<T>, TableBuilderState<T>, RowBuilderState<T>>(
        parentBuilder
    ) {

    internal val rowSpans: RowSpans<T> = RowSpans()

    private var rowIndex: RowIndexDef = RowIndexDef(0)

    @JvmSynthetic
    fun addRowBuilder(block: DslBlock<RowBuilderState<T>>? = null): RowBuilderState<T> =
        ensureBuilder(RowQualifier(index = RowIndexPredicateLiteral(Eq(rowIndex)))).also {
            block?.invoke(it)
            rowIndex = it.qualifier.index?.lastIndex()?.plus(1) ?: rowIndex
        }

    @JvmSynthetic
    fun addRowBuilder(selector: RowPredicate<T>, block: DslBlock<RowBuilderState<T>>? = null): RowBuilderState<T> =
        ensureBuilder(RowQualifier(matching = selector)).also {
            block?.invoke(it)
        }

    @JvmSynthetic
    fun addRowBuilder(
        at: RowIndexPredicateLiteral<T>,
        block: DslBlock<RowBuilderState<T>>? = null,
    ): RowBuilderState<T> {
        rowIndex = at.lastIndex()
        return ensureBuilder(RowQualifier(index = at)).also {
            block?.invoke(it)
        }.also { rowIndex++ }
    }

    @JvmSynthetic
    fun addRowBuilder(at: RowIndexDef, block: DslBlock<RowBuilderState<T>>? = null): RowBuilderState<T> {
        rowIndex = at
        return ensureBuilder(RowQualifier(index = RowIndexPredicateLiteral(Eq(rowIndex++)))).also {
            block?.invoke(it)
        }
    }

    @JvmSynthetic
    fun addRowBuilder(step: Enum<*>, block: DslBlock<RowBuilderState<T>>? = null): RowBuilderState<T> {
        if (step != rowIndex.step) {
            rowIndex = RowIndexDef(index = 0, step = step)
        }
        return ensureBuilder(RowQualifier(index = RowIndexPredicateLiteral(Eq(rowIndex++)))).also {
            block?.invoke(it)
        }
    }

    @JvmSynthetic
    override fun createBuilder(key: RowQualifier<T>): RowBuilderState<T> =
        RowBuilderState(key, parentBuilder.columnsBuilderState, this)

}

/**
 * Row builder state for creating single table row.
 * Manages mutable state that is eventually materialized as part of table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
internal class RowBuilderState<T: Any>(
    @get:JvmSynthetic internal val qualifier: RowQualifier<T>,
    @get:JvmSynthetic internal val columnsBuilderState: ColumnBuilderStateCollection<T>,
    @get:JvmSynthetic internal val rowsBuilderState: RowBuilderStateCollection<T>,
) : AttributesAwareBuilder<RowDef<T>>() {

    @get:JvmSynthetic
    val cellBuilderStateCollection: CellBuilderStateCollection<T> = CellBuilderStateCollection(this)

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
                rowsBuilderState.rowSpans += cellBuilderStateCollection.currentColumn!!.id to it
            }
        }
    }

    override fun modelClass(): Class<RowDef<T>> = reify()

    @JvmSynthetic
    override fun build(): RowDef<T> = RowDef(
        qualifier,
        cellBuilderStateCollection.buildMap(),
        attributes()
    )

    internal fun isCellLockedByRowSpan(key: ColumnKey<T>): Boolean = with(rowsBuilderState.rowSpans) {
        isColumnLocked(key)
    }

}

/**
 * Cells builder state for creating table row cells.
 * Manages mutable state that is eventually materialized as part of table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
internal class CellBuilderStateCollection<T: Any>(
    private val rowBuilderState: RowBuilderState<T>,
    private val columnBuilders: ColumnBuilderStateCollection<T> = rowBuilderState.columnsBuilderState,
) : ModelPartBuilderCollection<ColumnKey<T>, CellDef<T>, RowBuilderState<T>, CellBuilderState<T>>(rowBuilderState) {

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
    fun addCellBuilder(ref: PropertyReferenceColumnKey<T>, block: DslBlock<CellBuilderState<T>>): CellBuilderState<T> =
        ensureCellBuilder(ColumnKey(property = ref)).apply(block)

    private fun ensureCellBuilder(key: ColumnKey<T>): CellBuilderState<T> =
        selectOrNull(key) ?: ensureBuilder(key)

    /*
    @JvmSynthetic
    override fun build(): Map<ColumnKey<T>, CellDef<T>> {
        return cells.map {
            validateCell(it.key, it.value)
            it.key to it.value.build()
        }.toMap()
    }
*/
    private fun validateCell(key: ColumnKey<T>, cellBuilder: CellBuilderState<T>) {
        if (cellBuilder.rowSpan > 1
            && cellBuilder.value == null
            && cellBuilder.expression == null
            && key.property != null
        ) {
            throw BuilderException("At least one cell addressed with property literal column key does not define custom value")
        }
    }

    private fun selectOrNull(key: ColumnKey<T>): CellBuilderState<T>? =
        this[key]?.also { selectColumn(key) }

    private fun findColumnOrThrow(key: ColumnKey<T>): ColumnBuilderState<T> =
        columnBuilders[key] ?: throw BuilderException("No column definition for : $key")

    private fun selectColumn(key: ColumnKey<T>): ColumnBuilderState<T>? =
        columnBuilders[key]?.also { currentColumn = it }

    private fun getPreviousCell(key: ColumnKey<T>): Pair<ColumnBuilderState<T>, CellBuilderState<T>>? {
        return columnBuilders[key]?.let { baseColumn ->
            columnBuilders.values().searchBackwardStartingBefore(baseColumn.index) { column ->
                this[column.id]?.let { column to it }
            }
        }
    }

    private fun isCellLockedByRowSpan(key: ColumnKey<T>): Boolean = rowBuilderState.isCellLockedByRowSpan(key)

    private fun isCellLockedByColSpan(key: ColumnKey<T>): Boolean =
        columnBuilders[key]?.let { isCellLockedByColSpan(it) } ?: false

    private fun isCellLockedByColSpan(column: ColumnBuilderState<T>): Boolean {
        val currentIndex = column.index
        return getPreviousCell(column.id)?.let {
            it.first.index + it.second.colSpan > currentIndex
        } ?: false
    }

    private fun columnBuilder(index: Int): ColumnBuilderState<T>? = columnBuilders.find { it.index == index }

    private fun findCurrentAvailableColumn(): ColumnBuilderState<T>? {
        val columnBuilderList = columnBuilders.values()
        return when (currentColumn) {
            null -> columnBuilderList.firstOrNull()?.let { first ->
                columnBuilderList.searchForwardStartingWith(first) {
                    if (!isCellLockedByRowSpan(it.id)) it else null
                }
            }
            else -> columnBuilderList.searchForwardStartingAfter(currentColumn!!) {
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

    override fun createBuilder(key: ColumnKey<T>): CellBuilderState<T> {
        if (isCellLockedByRowSpan(key)) throw BuilderException("Cannot create cell at $key due to 'rowSpan' lock.")
        if (isCellLockedByColSpan(key)) throw BuilderException("Cannot create cell at $key due to 'colSpan' lock.")
        return CellBuilderState(rowBuilderState).also {
            currentColumn = rowBuilderState.columnsBuilderState.ensureColumnBuilder(key)
        }
    }
}

/**
 * Cell builder state for creating single row cell.
 * Manages mutable state that is eventually materialized as part of table model.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
internal class CellBuilderState<T: Any>(
    private val rowBuilderState: RowBuilderState<T>,
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

    override fun modelClass(): Class<CellDef<T>> = reify()

    @JvmSynthetic
    override fun build(): CellDef<T> =
        CellDef(
            value, expression, colSpan, rowSpan, rowSpanStrategy,
            attributes()
        )

}