package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.ColumnKey
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute

data class AttributedRow<T>(
    val rowAttributes: Set<RowAttribute>?,
    val rowCellValues: Map<ColumnKey<T>, AttributedCell>,
    val rowIndex: Int
): ContextData(), RowCoordinate {
    override fun getRow(): Int = rowIndex
}

object AttributedRowFactory {
    internal fun <T> createAttributedRow(
        rowIndex: Int,
        rowAttributes: Set<RowAttribute>,
        cells: Map<ColumnKey<T>, AttributedCell>,
        customAttributes: MutableMap<String, Any>
    ): AttributedRow<T> {
        return AttributedRow(rowIndex = rowIndex, rowAttributes = rowAttributes, rowCellValues = cells)
            .apply { additionalAttributes = customAttributes }
    }
}
