package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.ColumnHint

data class Column<T>(
   val id: String,
   val index: Int?,
   val columnTitle: Description?,
   val columnType: CellType?,
   val fromField: ((record: T) -> Any?)?,
   val columnHints: Set<ColumnHint>?,
   val cellHints: Set<CellHint>?
)