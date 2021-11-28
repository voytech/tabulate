package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.ColumnKey
import io.github.voytech.tabulate.model.attributes.*
import io.github.voytech.tabulate.model.attributes.cell.TypeHintAttribute

/**
 * Class encapsulates shared, generic logic for accessing attribute instance of selected attribute class.
 * Class is intended to be derived by all classes representing attribute-less contexts like:
 * [RowCellContext], [ColumnContext], [RowContext], [RowContextWithCells], [TableContext]
 * @author Wojciech Mąka
 */
sealed class ModelAttributeAccessor<A : Attribute<*>>(attributedContext: AttributedModel<A>) {
    private val attributeMap: Map<Class<out A>,List<A>> by lazy {
        attributedContext.attributes?.groupBy { it.javaClass } ?: emptyMap()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : A> getModelAttributes(clazz: Class<T>): List<T> =
            if (attributeMap.containsKey(clazz)) {
                attributeMap[clazz] as List<T>
            } else emptyList()
}

@JvmName("getCellModelAttributes")
inline fun <reified T: CellAttribute<T>> ModelAttributeAccessor<CellAttribute<*>>.getModelAttributes(): List<T> =
        getModelAttributes(T::class.java)

@JvmName("getColumnModelAttributes")
inline fun <reified T: ColumnAttribute<T>> ModelAttributeAccessor<ColumnAttribute<*>>.getModelAttributes(): List<T> =
        getModelAttributes(T::class.java)

@JvmName("getRowModelAttributes")
inline fun <reified T: RowAttribute<T>> ModelAttributeAccessor<RowAttribute<*>>.getModelAttributes(): List<T> =
        getModelAttributes(T::class.java)

@JvmName("getTableModelAttributes")
inline fun <reified T: TableAttribute<T>> ModelAttributeAccessor<TableAttribute<*>>.getModelAttributes(): List<T> =
        getModelAttributes(T::class.java)

inline fun <reified T: CellAttribute<T>> ModelAttributeAccessor<CellAttribute<*>>.getFirstAttributeOrNull(): T? =
        getModelAttributes(T::class.java).firstOrNull()

inline fun <reified T: ColumnAttribute<T>> ModelAttributeAccessor<ColumnAttribute<*>>.getFirstAttributeOrNull(): T? =
        getModelAttributes(T::class.java).firstOrNull()

inline fun <reified T: RowAttribute<T>> ModelAttributeAccessor<RowAttribute<*>>.getFirstAttributeOrNull(): T? =
        getModelAttributes(T::class.java).firstOrNull()

inline fun <reified T: TableAttribute<T>> ModelAttributeAccessor<TableAttribute<*>>.getFirstAttributeOrNull(): T? =
        getModelAttributes(T::class.java).firstOrNull()

class TableContext(private val attributedContext: AttributedTable) :
        Context by attributedContext,
        ModelAttributeAccessor<TableAttribute<*>>(attributedContext)

fun AttributedTable.crop(): TableContext = TableContext(this)

class ColumnContext(private val attributedContext: AttributedColumn):
        Context by attributedContext,
        ColumnCoordinate by attributedContext,
        ModelAttributeAccessor<ColumnAttribute<*>>(attributedContext) {
    val currentPhase: ColumnRenderPhase? = attributedContext.currentPhase
}

fun AttributedColumn.crop(): ColumnContext = ColumnContext(this)

enum class ColumnRenderPhase {
    BEFORE_FIRST_ROW,
    AFTER_LAST_ROW
}

open class RowContext<T>(private val attributedContext: AttributedRow<T>) :
        Context by attributedContext,
        RowCoordinate by attributedContext,
        ModelAttributeAccessor<RowAttribute<*>>(attributedContext)

class RowContextWithCells<T>(private val attributedContext: AttributedRowWithCells<T>) : RowContext<T>(attributedContext) {
    fun getCells(): Map<ColumnKey<T>, RowCellContext> = attributedContext.rowCellValues.crop()
}

fun <T> AttributedRowWithCells<T>.crop(): RowContextWithCells<T> = RowContextWithCells(this)

fun <T> AttributedRow<T>.crop(): RowContext<T> = RowContext(this)

private fun <T> Map<ColumnKey<T>, AttributedCell>.crop(): Map<ColumnKey<T>, RowCellContext> {
    return entries.associate {
        it.key to it.value.crop()
    }
}

class RowCellContext(private val attributedContext: AttributedCell) :
        Context by attributedContext,
        RowCellCoordinate by attributedContext,
        ModelAttributeAccessor<CellAttribute<*>>(attributedContext) {

    val value: CellValue by attributedContext::value

    val rawValue: Any by value::value
}

@Suppress("UNCHECKED_CAST")
fun AttributedCell.crop(): RowCellContext = RowCellContext(this)

fun RowCellContext.getTypeHint(): TypeHintAttribute? = getFirstAttributeOrNull<TypeHintAttribute>()