package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute

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