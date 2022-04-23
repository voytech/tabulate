package io.github.voytech.tabulate.components.table.operation

import io.github.voytech.tabulate.components.table.model.*
import io.github.voytech.tabulate.components.table.model.attributes.CellAttribute
import io.github.voytech.tabulate.components.table.model.attributes.ColumnAttribute
import io.github.voytech.tabulate.components.table.model.attributes.RowAttribute
import io.github.voytech.tabulate.components.table.model.attributes.TableAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.TypeHintAttribute
import io.github.voytech.tabulate.components.table.template.SyntheticRow
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.orEmpty
import io.github.voytech.tabulate.core.template.operation.AttributedModel
import io.github.voytech.tabulate.core.template.operation.Context


/**
 * Basic interface providing custom attributes that are shared throughout entire exporting process.
 * @author Wojciech Mąka
 * @since 0.1.0
 */


fun Context.getSheetName(): String {
    return (getContextAttributes()?.get("_sheetName") ?: error("")) as String
}

/**
 * CellValue representing cell associated data exposed by row cell operation.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class CellValue(
    val value: Any,
    val colSpan: Int = 1,
    val rowSpan: Int = 1
)

/**
 * Row coordinates of single cell
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface RowCoordinate {
    fun getRow(): Int
}

/**
 * Column coordinates of single cell
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface ColumnCoordinate {
    fun getColumn(): Int
}

/**
 * Row and column coordinates of single cell
 * @author Wojciech Mąka
 * @since 0.1.0
 */
interface RowCellCoordinate : RowCoordinate, ColumnCoordinate

data class Coordinates(
    val tableName: String
) {
    var rowIndex: Int = 0
        internal set
    var columnIndex: Int = 0
        internal set

    constructor(tableName: String, rowIdx: Int, columnIdx: Int) : this(tableName) {
        rowIndex = rowIdx
        columnIndex = columnIdx
    }
}

internal fun <T> Table<T>.getRowIndex(rowIndex: Int) = (firstRow ?: 0) + rowIndex

internal fun <T> Table<T>.getColumnIndex(columnIndex: Int) = (firstColumn ?: 0) + columnIndex

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
 * @since 0.2.0
 */
sealed class TableContext(
    attributes: Attributes<TableAttribute<*>>?,
) : AttributedModel<TableAttribute<*>>(attributes)

/**
 * Column operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
sealed class ColumnContext(
    attributes: Attributes<ColumnAttribute<*>>?,
) : AttributedModel<ColumnAttribute<*>>(attributes)

/**
 * Row operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
sealed class RowContext(
    attributes: Attributes<RowAttribute<*>>?,
) : AttributedModel<RowAttribute<*>>(attributes)

/**
 * Table operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class TableOpeningContext(
    attributes: Attributes<TableAttribute<*>>?,
) : TableContext(attributes)

internal fun <T> Table<T>.createContext(customAttributes: MutableMap<String, Any>): TableOpeningContext =
    TableOpeningContext(tableAttributes).apply { additionalAttributes = customAttributes }

/**
 * Table operation context with additional model attributes applicable on table level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class TableClosingContext(
    attributes: Attributes<TableAttribute<*>>?,
) : TableContext(attributes)

internal fun <T> Table<T>.createClosingContext(customAttributes: MutableMap<String, Any>): TableClosingContext =
    TableClosingContext(tableAttributes).apply { additionalAttributes = customAttributes }

/**
 * Row operation context with additional model attributes applicable on row level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class RowOpeningContext(
    attributes: Attributes<RowAttribute<*>>?,
    val rowIndex: Int
) : RowContext(attributes), RowCoordinate {
    override fun getRow(): Int = rowIndex
}

internal fun <T> SyntheticRow<T>.createRowOpening(
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
class RowClosingContext<T>(
    attributes: Attributes<RowAttribute<*>>?,
    val rowCellValues: Map<ColumnKey<T>, CellContext>,
    val rowIndex: Int
) :  RowContext(attributes), RowCoordinate {
    override fun getRow(): Int = rowIndex

    fun getCells(): Map<ColumnKey<T>, CellContext> = rowCellValues
}

fun <T> RowOpeningContext.asRowClosing(rowCellValues: Map<ColumnKey<T>, CellContext>): RowClosingContext<T> =
    RowClosingContext(
        rowIndex = this@asRowClosing.rowIndex,
        attributes = this@asRowClosing.attributes ?: Attributes(attributeCategory = RowAttribute::class.java),
        rowCellValues = rowCellValues
    ).apply { additionalAttributes = this@asRowClosing.additionalAttributes }

/**
 * Column operation context with additional model attributes applicable on column level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class ColumnOpeningContext(
    attributes: Attributes<ColumnAttribute<*>>? = null,
    val columnIndex: Int
) : ColumnContext(attributes), ColumnCoordinate {
    val currentPhase: ColumnRenderPhase = ColumnRenderPhase.BEFORE_FIRST_ROW
    override fun getColumn(): Int = columnIndex
}

internal fun <T> Table<T>.createColumnOpening(
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

class ColumnClosingContext(
    attributes: Attributes<ColumnAttribute<*>>? = null,
    val columnIndex: Int,
) : ColumnContext(attributes), ColumnCoordinate {
    val currentPhase: ColumnRenderPhase = ColumnRenderPhase.AFTER_LAST_ROW
    override fun getColumn(): Int = columnIndex
}

internal fun <T> Table<T>.createColumnClosing(
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
class CellContext(
    val value: CellValue,
    attributes: Attributes<CellAttribute<*>>?,
    val rowIndex: Int,
    val columnIndex: Int,
    val rawValue: Any = value.value
) : AttributedModel<CellAttribute<*>>(attributes), RowCellCoordinate {
    override fun getRow(): Int = rowIndex
    override fun getColumn(): Int = columnIndex
}

internal fun <T> SyntheticRow<T>.createCellContext(
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