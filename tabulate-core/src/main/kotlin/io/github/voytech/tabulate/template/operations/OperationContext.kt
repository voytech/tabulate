package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.*
import io.github.voytech.tabulate.model.attributes.*
import io.github.voytech.tabulate.model.attributes.cell.TypeHintAttribute
import io.github.voytech.tabulate.template.resolvers.SyntheticRow

/**
 * A base class for all operation context, where each includes additional model attributes for table appearance
 * customisation.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
sealed class AttributedModel<A : Attribute<*>>(@JvmSynthetic internal open val attributes: Attributes<A>?) : ContextData(), AttributeCategoryAware<A> {
    @Suppress("UNCHECKED_CAST")
    fun <T : A> getModelAttribute(clazz: Class<T>): T? =
        attributes?.get(clazz)

    override fun getAttributeCategoryClass() : Class<A> = attributes!!.getAttributeCategoryClass()
}

@JvmName("getCellModelAttributes")
inline fun <reified T: CellAttribute<T>> AttributedModel<CellAttribute<*>>.getModelAttribute(): T?  =
    getModelAttribute(T::class.java)

@JvmName("getColumnModelAttributes")
inline fun <reified T: ColumnAttribute<T>> AttributedModel<ColumnAttribute<*>>.getModelAttribute(): T? =
    getModelAttribute(T::class.java)

@JvmName("getRowModelAttributes")
inline fun <reified T: RowAttribute<T>> AttributedModel<RowAttribute<*>>.getModelAttribute(): T? =
    getModelAttribute(T::class.java)

@JvmName("getTableModelAttributes")
inline fun <reified T: TableAttribute<T>> AttributedModel<TableAttribute<*>>.getModelAttribute(): T? =
    getModelAttribute(T::class.java)

/**
 * Table operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class TableOpeningContext(
    override val attributes: Attributes<TableAttribute<*>>?,
) : AttributedModel<TableAttribute<*>>(attributes)

internal fun <T> Table<T>.createContext(customAttributes: MutableMap<String, Any>): TableOpeningContext =
    TableOpeningContext(tableAttributes).apply { additionalAttributes = customAttributes }

/**
 * Table operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class TableClosingContext(
    override val attributes: Attributes<TableAttribute<*>>?,
) : AttributedModel<TableAttribute<*>>(attributes)

internal fun <T> Table<T>.createClosingContext(customAttributes: MutableMap<String, Any>): TableClosingContext =
    TableClosingContext(tableAttributes).apply { additionalAttributes = customAttributes }

/**
 * Row operation context with additional model attributes applicable on row level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
open class RowOpeningContext(
    override val attributes: Attributes<RowAttribute<*>>?,
    open val rowIndex: Int
) : AttributedModel<RowAttribute<*>>(attributes), RowCoordinate {
    override fun getRow(): Int = rowIndex
}

internal fun <T> SyntheticRow<T>.openAttributedRow(
    rowIndex: Int,
    customAttributes: MutableMap<String, Any>
): RowOpeningContext {
    return RowOpeningContext(rowIndex = table.getRowIndex(rowIndex), attributes = rowAttributes)
        .apply { additionalAttributes = customAttributes }
}

/**
 * Row operation context with additional model attributes applicable on row level.
 * Additionally it contains also all resolved cell operation context for each contained cell.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class RowClosingContext<T>(
    override val attributes: Attributes<RowAttribute<*>>?,
    val rowCellValues: Map<ColumnKey<T>, CellContext>,
    override val rowIndex: Int
) : RowOpeningContext(attributes, rowIndex) {
    fun getCells(): Map<ColumnKey<T>, CellContext> = rowCellValues
}

fun <T> RowOpeningContext.close(rowCellValues: Map<ColumnKey<T>, CellContext>): RowClosingContext<T> =
    RowClosingContext(
        rowIndex = this@close.rowIndex,
        attributes = this@close.attributes ?: Attributes(attributeCategory = RowAttribute::class.java),
        rowCellValues = rowCellValues
    ).apply { additionalAttributes = this@close.additionalAttributes }

/**
 * Column operation context with additional model attributes applicable on column level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class ColumnOpeningContext(
    override val attributes: Attributes<ColumnAttribute<*>>? = null,
    val columnIndex: Int
) : AttributedModel<ColumnAttribute<*>>(attributes), ColumnCoordinate {
    val currentPhase: ColumnRenderPhase = ColumnRenderPhase.BEFORE_FIRST_ROW
    override fun getColumn(): Int = columnIndex
}

internal fun <T> Table<T>.openAttributedColumn(
    column: ColumnDef<T>,
    customAttributes: MutableMap<String, Any>
) = ColumnOpeningContext(
        columnIndex = getColumnIndex(column.index),
        attributes = columnAttributes.orEmpty() + column.columnAttributes.orEmpty()
    ).apply { additionalAttributes = customAttributes }

/**
 * Column operation context with additional model attributes applicable on column level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */

enum class ColumnRenderPhase {
    BEFORE_FIRST_ROW,
    AFTER_LAST_ROW
}

data class ColumnClosingContext(
    override val attributes: Attributes<ColumnAttribute<*>>? = null,
    val columnIndex: Int,
) : AttributedModel<ColumnAttribute<*>>(attributes), ColumnCoordinate {
    val currentPhase: ColumnRenderPhase = ColumnRenderPhase.AFTER_LAST_ROW
    override fun getColumn(): Int = columnIndex
}

internal fun <T> Table<T>.closeAttributedColumn(
    column: ColumnDef<T>,
    customAttributes: MutableMap<String, Any>
) = ColumnClosingContext(
    columnIndex = getColumnIndex(column.index),
    attributes = columnAttributes.orEmpty() + column.columnAttributes.orEmpty()
).apply { additionalAttributes = customAttributes }

/**
 * Cell operation context with additional model attributes applicable on cell level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class CellContext(
    val value: CellValue,
    override val attributes: Attributes<CellAttribute<*>>?,
    val rowIndex: Int,
    val columnIndex: Int,
    val rawValue: Any = value.value
) : AttributedModel<CellAttribute<*>>(attributes), RowCellCoordinate {
    override fun getRow(): Int = rowIndex
    override fun getColumn(): Int = columnIndex
}

internal fun <T> SyntheticRow<T>.createAttributedCell(
    row: SourceRow<T>,
    column: ColumnDef<T>,
    customAttributes: MutableMap<String, Any>
): CellContext? =
    cellDefinitions.resolveCellValue(column, row)?.let { value ->
        CellContext(
            value = value,
            attributes = cellAttributes[column],
            rowIndex = table.getRowIndex(row.rowIndexValue()),
            columnIndex = table.getColumnIndex(column.index)
        ).apply { additionalAttributes = customAttributes }
    }

fun CellContext.getTypeHint(): TypeHintAttribute? = getModelAttribute<TypeHintAttribute>()