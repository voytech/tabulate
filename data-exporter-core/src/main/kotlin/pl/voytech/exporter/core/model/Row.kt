package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.model.attributes.RowAttribute

data class Row<T> internal constructor(
    val selector: RowSelector<T>? = null,
    val createAt: Int? = null,
    val rowAttributes: Set<RowAttribute>?,
    val cellAttributes: Set<CellAttribute>?,
    val cells: Map<ColumnKey<T>, Cell<T>>?
)

