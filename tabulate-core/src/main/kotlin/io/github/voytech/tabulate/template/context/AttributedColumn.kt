package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute

data class AttributedColumn(
    val columnAttributes: Set<ColumnAttribute>? = null,
    val columnIndex: Int,
    val currentPhase: ColumnRenderPhase? = ColumnRenderPhase.BEFORE_FIRST_ROW
) : ContextData<Unit>(), ColumnCoordinate {
    override fun getColumn(): Int = columnIndex
}
