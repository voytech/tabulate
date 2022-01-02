package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.*
import io.github.voytech.tabulate.model.attributes.Attribute
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.model.attributes.alias.TableAttribute
import io.github.voytech.tabulate.model.attributes.overrideAttributesLeftToRight
import io.github.voytech.tabulate.template.resolvers.SyntheticRow

sealed class AttributedModel<A : Attribute<*>>(open val attributes: Set<A>?) : ContextData()

data class AttributedTable(
    override val attributes: Set<TableAttribute>?,
) : AttributedModel<TableAttribute>(attributes)

internal fun <T> Table<T>.createContext(customAttributes: MutableMap<String, Any>): AttributedTable =
    AttributedTable(tableAttributes).apply { additionalAttributes = customAttributes }

open class AttributedRow(
    override val attributes: Set<RowAttribute>?,
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

data class AttributedRowWithCells<T>(
    override val attributes: Set<RowAttribute>?,
    val rowCellValues: Map<ColumnKey<T>, AttributedCell>,
    override val rowIndex: Int
) : AttributedRow(attributes, rowIndex)

fun <T> AttributedRow.withCells(rowCellValues: Map<ColumnKey<T>, AttributedCell>): AttributedRowWithCells<T> =
    AttributedRowWithCells(
        rowIndex = this@withCells.rowIndex,
        attributes = this@withCells.attributes ?: emptySet(),
        rowCellValues = rowCellValues
    ).apply { additionalAttributes = this@withCells.additionalAttributes }

data class AttributedColumn(
    override val attributes: Set<ColumnAttribute>? = null,
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
        attributes = overrideAttributesLeftToRight(
            columnAttributes.orEmpty() + column.columnAttributes.orEmpty()
        ).toSet(),
    ).apply { additionalAttributes = customAttributes }

data class AttributedCell(
    val value: CellValue,
    override val attributes: Set<CellAttribute>?,
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
