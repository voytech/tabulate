package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.RowHint


data class Row<T>(
    val selector: RowSelector<T>,
    val rowHints: List<RowHint>?,
    val cellHints: List<CellHint>?,
    val cells: Map<String, Cell<T>>?
)
