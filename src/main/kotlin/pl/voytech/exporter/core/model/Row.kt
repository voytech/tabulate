package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.RowExtension


data class Row<T>(
    val selector: RowSelector<T>,
    val rowExtensions: Set<RowExtension>?,
    val cellExtensions: Set<CellExtension>?,
    val cells: Map<Key<T>, Cell<T>>?
)
