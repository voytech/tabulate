package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.ColumnDef
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute

data class AttributedColumn(
    val columnAttributes: Set<ColumnAttribute>? = null,
    val columnIndex: Int,
    val currentPhase: ColumnRenderPhase? = ColumnRenderPhase.BEFORE_FIRST_ROW
) : ContextData<Unit>(), ColumnCoordinate {
    override fun getColumn(): Int = columnIndex
}

internal fun <T> TableExportingState<T>.createAttributedColumn(
    indexedColumn: IndexedValue<ColumnDef<T>>,
    phase: ColumnRenderPhase,
): AttributedColumn {
    return AttributedColumn(
        columnIndex = (firstColumn ?: 0) + indexedColumn.index,
        currentPhase = phase,
        columnAttributes = indexedColumn.value.columnAttributes?.filter { ext ->
            ((ColumnRenderPhase.BEFORE_FIRST_ROW == phase) && ext.beforeFirstRow()) ||
                    ((ColumnRenderPhase.AFTER_LAST_ROW == phase) && ext.afterLastRow())
        }?.toSet(),
    ).apply { additionalAttributes = getCustomAttributes() }
}
