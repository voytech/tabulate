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
sealed class ModelAttributeAccessor<A : Attribute<*>>(private val attributedContext: AttributedModel<A>) {

    @Suppress("UNCHECKED_CAST")
    fun <T : A> getModelAttributes(clazz: Class<T>): T? =
        attributedContext.attributes?.get(clazz) as T?
}

@JvmName("getCellModelAttributes")
inline fun <reified T: CellAttribute<T>> ModelAttributeAccessor<CellAttribute<*>>.getModelAttributes(): T?  =
        getModelAttributes(T::class.java)

@JvmName("getColumnModelAttributes")
inline fun <reified T: ColumnAttribute<T>> ModelAttributeAccessor<ColumnAttribute<*>>.getModelAttributes(): T? =
        getModelAttributes(T::class.java)

@JvmName("getRowModelAttributes")
inline fun <reified T: RowAttribute<T>> ModelAttributeAccessor<RowAttribute<*>>.getModelAttributes(): T? =
        getModelAttributes(T::class.java)

@JvmName("getTableModelAttributes")
inline fun <reified T: TableAttribute<T>> ModelAttributeAccessor<TableAttribute<*>>.getModelAttributes(): T? =
        getModelAttributes(T::class.java)


class TableContext(private val attributedContext: AttributedTable) :
        Context by attributedContext,
        ModelAttributeAccessor<TableAttribute<*>>(attributedContext)

fun AttributedTable.skipAttributes(): TableContext = TableContext(this)

class ColumnContext(private val attributedContext: AttributedColumn):
        Context by attributedContext,
        ColumnCoordinate by attributedContext,
        ModelAttributeAccessor<ColumnAttribute<*>>(attributedContext) {
    val currentPhase: ColumnRenderPhase? = attributedContext.currentPhase
}

fun AttributedColumn.skipAttributes(): ColumnContext = ColumnContext(this)

enum class ColumnRenderPhase {
    BEFORE_FIRST_ROW,
    AFTER_LAST_ROW
}

open class RowContext(private val attributedContext: AttributedRow) :
        Context by attributedContext,
        RowCoordinate by attributedContext,
        ModelAttributeAccessor<RowAttribute<*>>(attributedContext)

class RowContextWithCells<T>(private val attributedContext: AttributedRowWithCells<T>) : RowContext(attributedContext) {
    fun getCells(): Map<ColumnKey<T>, RowCellContext> = attributedContext.rowCellValues.skipAttributes()
}

fun <T> AttributedRowWithCells<T>.skipAttributes(): RowContextWithCells<T> = RowContextWithCells(this)

fun AttributedRow.skipAttributes(): RowContext = RowContext(this)

private fun <T> Map<ColumnKey<T>, AttributedCell>.skipAttributes(): Map<ColumnKey<T>, RowCellContext> {
    return entries.associate {
        it.key to it.value.skipAttributes()
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
fun AttributedCell.skipAttributes(): RowCellContext = RowCellContext(this)

fun RowCellContext.getTypeHint(): TypeHintAttribute? = getModelAttributes<TypeHintAttribute>()