package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.extension.CellExtension

data class Cell<T> internal constructor(
    val value: Any?,
    val eval: RowCellEval<T>?,
    val type: CellType?,
    val colSpan: Int? = 1,
    val rowSpan: Int? = 1,
    val cellExtensions: Set<CellExtension>?
)