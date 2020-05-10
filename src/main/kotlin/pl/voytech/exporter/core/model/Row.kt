package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.hints.Hint


data class Row<T>(
    val selector: RowSelector<T>,
    val hints: List<Hint>?,
    val cells: Map<String, Cell>?
)
