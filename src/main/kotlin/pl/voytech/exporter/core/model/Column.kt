package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.hints.ColumnHint

data class Column<T>(
   val columnTitle: String?,
   val fromField: (record: T) -> Any?,
   val hints: List<ColumnHint>? = emptyList(),
   val cells: Map<Row, Cell>? = emptyMap()
)