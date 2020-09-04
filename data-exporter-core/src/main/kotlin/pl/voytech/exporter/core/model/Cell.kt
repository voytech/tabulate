package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.api.builder.fluent.CellBuilder
import pl.voytech.exporter.core.model.extension.CellExtension

data class Cell<T> internal constructor(
    val value: Any?,
    val eval: RowCellEval<T>?,
    val type: CellType?,
    val cellExtensions: Set<CellExtension>?
)