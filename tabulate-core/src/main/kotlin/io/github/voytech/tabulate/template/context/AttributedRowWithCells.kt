package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.ColumnKey
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.template.context.AttributedRowFactory.createAttributedRow

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
    createAttributedRow(
        rowIndex,
        attributes?: emptySet(),
        rowCellValues,
        additionalAttributes?: mutableMapOf()
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
