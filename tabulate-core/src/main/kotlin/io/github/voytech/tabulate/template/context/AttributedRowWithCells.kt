package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.ColumnKey
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.template.context.AttributedRowFactory.createAttributedRow

open class AttributedRow<T>(
    open val rowAttributes: Set<RowAttribute>?,
    open val rowIndex: Int
): ContextData(), RowCoordinate {
    override fun getRow(): Int = rowIndex
}

data class AttributedRowWithCells<T>(
    override val rowAttributes: Set<RowAttribute>?,
    val rowCellValues: Map<ColumnKey<T>, AttributedCell>,
    override val rowIndex: Int
): AttributedRow<T>(rowAttributes, rowIndex)

fun <T> AttributedRow<T>.withCells(rowCellValues: Map<ColumnKey<T>, AttributedCell>): AttributedRowWithCells<T> =
    createAttributedRow(
        rowIndex,
        rowAttributes?: emptySet(),
        rowCellValues,
        additionalAttributes?: mutableMapOf()
    )

object AttributedRowFactory {

    internal fun <T> createAttributedRow(
        rowIndex: Int,
        rowAttributes: Set<RowAttribute>,
        customAttributes: MutableMap<String, Any>
    ): AttributedRow<T> {
        return AttributedRow<T>(rowIndex = rowIndex, rowAttributes = rowAttributes)
            .apply { additionalAttributes = customAttributes }
    }

    internal fun <T> createAttributedRow(
        rowIndex: Int,
        rowAttributes: Set<RowAttribute>,
        cells: Map<ColumnKey<T>, AttributedCell>,
        customAttributes: MutableMap<String, Any>
    ): AttributedRowWithCells<T> {
        return AttributedRowWithCells(rowIndex = rowIndex, rowAttributes = rowAttributes, rowCellValues = cells)
            .apply { additionalAttributes = customAttributes }
    }
}
