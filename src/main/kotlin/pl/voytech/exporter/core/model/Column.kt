package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.hints.Hint

data class Column<T>(
   val rowRanges: List<LongRange> = listOf(infinite()),
   val columnTitle: String?,
   val fromField: (record: T) -> Any?,
   val hints: List<Hint>? = null,
   val cells: Map<Row, Cell>? = emptyMap()
)