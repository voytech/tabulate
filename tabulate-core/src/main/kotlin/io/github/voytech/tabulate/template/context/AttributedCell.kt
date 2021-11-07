package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute

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
