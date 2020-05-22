package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.ColumnHint

data class Column<T>(
   val id: Key<T>,
   val index: Int?,
   val columnTitle: Description?,
   val columnType: CellType?,
   val columnHints: Set<ColumnHint>?,
   val cellHints: Set<CellHint>?
)