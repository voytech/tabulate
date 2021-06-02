package io.github.voytech.tabulate.template.context

data class ColumnContext(
    val columnIndex: Int,
    val currentPhase: ColumnRenderPhase? = ColumnRenderPhase.BEFORE_FIRST_ROW,
) : ContextData<Unit>(), ColumnCoordinate {
    override fun getColumn(): Int = columnIndex
}

fun AttributedColumn.narrow(): ColumnContext =
    ColumnContext(columnIndex, currentPhase).also { it.additionalAttributes = additionalAttributes }

enum class ColumnRenderPhase {
    BEFORE_FIRST_ROW,
    AFTER_LAST_ROW
}