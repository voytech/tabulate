package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.*
import io.github.voytech.tabulate.model.attributes.Attribute
import io.github.voytech.tabulate.model.attributes.Attributes
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.model.attributes.alias.TableAttribute
import io.github.voytech.tabulate.model.attributes.orEmpty
import io.github.voytech.tabulate.template.resolvers.SyntheticRow

/**
 * A base class for all operation context, where each includes additional model attributes for table appearance
 * customisation.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
sealed class AttributedModel<A : Attribute<*>>(open val attributes: Attributes<A>?) : ContextData()

/**
 * Table operation context with additional model attributes applicable on table level.
 * To be used by method `createTable` of [AttributedContextExportOperations]
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class AttributedTable(
    override val attributes: Attributes<TableAttribute>?,
) : AttributedModel<TableAttribute>(attributes)

internal fun <T> Table<T>.createContext(customAttributes: MutableMap<String, Any>): AttributedTable =
    AttributedTable(tableAttributes).apply { additionalAttributes = customAttributes }

/**
 * Row operation context with additional model attributes applicable on row level.
 * To be used by method `beginRow` of [AttributedContextExportOperations]
 * @author Wojciech Mąka
 * @since 0.1.0
 */
open class AttributedRow(
    override val attributes: Attributes<RowAttribute>?,
    open val rowIndex: Int
) : AttributedModel<RowAttribute>(attributes), RowCoordinate {
    override fun getRow(): Int = rowIndex
}

internal fun <T> SyntheticRow<T>.createAttributedRow(
    rowIndex: Int,
    customAttributes: MutableMap<String, Any>
): AttributedRow {
    return AttributedRow(rowIndex = table.getRowIndex(rowIndex), attributes = rowAttributes)
        .apply { additionalAttributes = customAttributes }
}

/**
 * Row operation context with additional model attributes applicable on row level.
 * Additionally it contains also all resolved cell operation context for each contained cell.
 * To be used by method `endRow` of [AttributedContextExportOperations]
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class AttributedRowWithCells<T>(
    override val attributes: Attributes<RowAttribute>?,
    val rowCellValues: Map<ColumnKey<T>, AttributedCell>,
    override val rowIndex: Int
) : AttributedRow(attributes, rowIndex)

fun <T> AttributedRow.withCells(rowCellValues: Map<ColumnKey<T>, AttributedCell>): AttributedRowWithCells<T> =
    AttributedRowWithCells(
        rowIndex = this@withCells.rowIndex,
        attributes = this@withCells.attributes ?: Attributes(),
        rowCellValues = rowCellValues
    ).apply { additionalAttributes = this@withCells.additionalAttributes }

/**
 * Column operation context with additional model attributes applicable on column level.
 * To be used by method `renderColumn` of [AttributedContextExportOperations]
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class AttributedColumn(
    override val attributes: Attributes<ColumnAttribute>? = null,
    val columnIndex: Int,
    val currentPhase: ColumnRenderPhase? = ColumnRenderPhase.BEFORE_FIRST_ROW
) : AttributedModel<ColumnAttribute>(attributes), ColumnCoordinate {
    override fun getColumn(): Int = columnIndex
}

internal fun <T> Table<T>.createAttributedColumn(
    column: ColumnDef<T>,
    phase: ColumnRenderPhase,
    customAttributes: MutableMap<String, Any>
) = AttributedColumn(
        columnIndex = getColumnIndex(column.index),
        currentPhase = phase,
        attributes = columnAttributes.orEmpty() + column.columnAttributes.orEmpty()
    ).apply { additionalAttributes = customAttributes }

/**
 * Cell operation context with additional model attributes applicable on cell level.
 * To be used by method `renderRowCell` of [AttributedContextExportOperations]
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class AttributedCell(
    val value: CellValue,
    override val attributes: Attributes<CellAttribute>?,
    val rowIndex: Int,
    val columnIndex: Int,
) : AttributedModel<CellAttribute>(attributes), RowCellCoordinate {
    override fun getRow(): Int = rowIndex
    override fun getColumn(): Int = columnIndex
}

internal fun <T> SyntheticRow<T>.createAttributedCell(
    row: SourceRow<T>,
    column: ColumnDef<T>,
    customAttributes: MutableMap<String, Any>
): AttributedCell? =
    cellDefinitions.resolveCellValue(column, row)?.let { value ->
        AttributedCell(
            value = value,
            attributes = cellAttributes[column],
            rowIndex = table.getRowIndex(row.rowIndexValue()),
            columnIndex = table.getColumnIndex(column.index)
        ).apply { additionalAttributes = customAttributes }
    }
