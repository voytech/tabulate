package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.attributes.alias.CellAttribute

data class Cell<T> internal constructor(
    val value: Any?,
    val eval: RowCellEval<T>?,
    val type: CellType?,
    val colSpan: Int = 1,
    val rowSpan: Int = 1,
    val cellAttributes: Set<CellAttribute>?
) {
    fun colSpanOffset() = colSpan - 1

    fun rowSpanOffset() = rowSpan - 1

    fun resolveValue(context: SourceRow<T>) : Any? = eval?.invoke(context) ?: value

}
