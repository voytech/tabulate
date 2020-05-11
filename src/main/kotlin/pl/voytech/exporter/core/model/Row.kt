package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.hints.RowHint


data class Row<T>(
    val selector: RowSelector<T>,
    val rowHints: List<RowHint>?,
    val cells: Map<String, Cell>?
)
