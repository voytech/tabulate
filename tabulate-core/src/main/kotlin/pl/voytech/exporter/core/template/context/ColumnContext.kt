package pl.voytech.exporter.core.template.context

data class ColumnContext(
    val columnIndex: Int,
    val currentPhase: ColumnRenderPhase? = ColumnRenderPhase.BEFORE_FIRST_ROW,
) : ContextData<Unit>(), ColumnCoordinate {
    override fun getColumn(): Int = columnIndex
}

fun AttributedColumn.narrow(): ColumnContext =
    ColumnContext(columnIndex, currentPhase).also { it.additionalAttributes = additionalAttributes }
