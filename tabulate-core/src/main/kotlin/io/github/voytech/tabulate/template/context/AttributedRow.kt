package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.ColumnKey
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute

data class AttributedRow<T>(
    val rowAttributes: Set<RowAttribute>?,
    val rowCellValues: Map<ColumnKey<T>, AttributedCell>,
    val rowIndex: Int
): ContextData<T>(), RowCoordinate {
    override fun getRow(): Int = rowIndex
}
