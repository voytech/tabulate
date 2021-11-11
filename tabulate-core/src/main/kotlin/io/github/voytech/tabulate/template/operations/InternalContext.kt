package io.github.voytech.tabulate.template.operations

import io.github.voytech.tabulate.model.ColumnKey
import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.model.attributes.Attribute
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.model.attributes.alias.TableAttribute


sealed class AttributedModel<A: Attribute<*>>(open val attributes: Set<A>?) : ContextData()

data class AttributedTable(
        override val attributes: Set<TableAttribute>?,
) : AttributedModel<TableAttribute>(attributes)

internal fun <T> Table<T>.createContext(customAttributes: MutableMap<String, Any>): AttributedTable =
        AttributedTable(tableAttributes).apply { additionalAttributes = customAttributes }

open class AttributedRow<T>(
        override val attributes: Set<RowAttribute>?,
        open val rowIndex: Int
): AttributedModel<RowAttribute>(attributes), RowCoordinate {
    override fun getRow(): Int = rowIndex
}

data class AttributedRowWithCells<T>(
        override val attributes: Set<RowAttribute>?,
        val rowCellValues: Map<ColumnKey<T>, AttributedCell>,
        override val rowIndex: Int
): AttributedRow<T>(attributes, rowIndex)

fun <T> AttributedRow<T>.withCells(rowCellValues: Map<ColumnKey<T>, AttributedCell>): AttributedRowWithCells<T> =
        AttributedRowFactory.createAttributedRow(
                rowIndex,
                attributes ?: emptySet(),
                rowCellValues,
                additionalAttributes ?: mutableMapOf()
        )

object AttributedRowFactory {

    internal fun <T> createAttributedRow(
            rowIndex: Int,
            rowAttributes: Set<RowAttribute>,
            customAttributes: MutableMap<String, Any>
    ): AttributedRow<T> {
        return AttributedRow<T>(rowIndex = rowIndex, attributes = rowAttributes)
                .apply { additionalAttributes = customAttributes }
    }

    internal fun <T> createAttributedRow(
            rowIndex: Int,
            rowAttributes: Set<RowAttribute>,
            cells: Map<ColumnKey<T>, AttributedCell>,
            customAttributes: MutableMap<String, Any>
    ): AttributedRowWithCells<T> {
        return AttributedRowWithCells(rowIndex = rowIndex, attributes = rowAttributes, rowCellValues = cells)
                .apply { additionalAttributes = customAttributes }
    }
}

data class AttributedColumn(
        override val attributes: Set<ColumnAttribute>? = null,
        val columnIndex: Int,
        val currentPhase: ColumnRenderPhase? = ColumnRenderPhase.BEFORE_FIRST_ROW
) : AttributedModel<ColumnAttribute>(attributes), ColumnCoordinate {
    override fun getColumn(): Int = columnIndex
}

object AttributedColumnFactory {
    internal fun createAttributedColumn(
            columnIndex: Int,
            phase: ColumnRenderPhase,
            attributes: Set<ColumnAttribute>? = null,
            customAttributes: MutableMap<String, Any>
    ): AttributedColumn {
        return AttributedColumn(
                columnIndex = columnIndex,
                currentPhase = phase,
                attributes = attributes?.filter { ext ->
                    ((ColumnRenderPhase.BEFORE_FIRST_ROW == phase) && ext.beforeFirstRow()) ||
                            ((ColumnRenderPhase.AFTER_LAST_ROW == phase) && ext.afterLastRow())
                }?.toSet(),
        ).apply { additionalAttributes = customAttributes }
    }
}

data class AttributedCell(
        val value: CellValue,
        override val attributes: Set<CellAttribute>?,
        val rowIndex: Int,
        val columnIndex: Int,
): AttributedModel<CellAttribute>(attributes), RowCellCoordinate {
    override fun getRow(): Int = rowIndex
    override fun getColumn(): Int = columnIndex
}

object AttributedCellFactory {
    internal fun createAttributedCell(
            rowIndex: Int,
            columnIndex: Int,
            value: CellValue,
            attributes: Set<CellAttribute>,
            customAttributes: MutableMap<String, Any>
    ): AttributedCell {
        return AttributedCell(value = value, attributes = attributes, rowIndex = rowIndex, columnIndex = columnIndex)
                .apply { additionalAttributes = customAttributes }
    }
}


