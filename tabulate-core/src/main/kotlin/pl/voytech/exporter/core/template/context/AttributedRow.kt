package pl.voytech.exporter.core.template.context

import pl.voytech.exporter.core.model.ColumnKey
import pl.voytech.exporter.core.model.attributes.alias.RowAttribute

data class AttributedRow<T>(
    val rowAttributes: Set<RowAttribute>?,
    val rowCellValues: Map<ColumnKey<T>, AttributedCell>,
    val rowIndex: Int
): ContextData<T>(), RowCoordinate {
    override fun getRow(): Int = rowIndex
}
