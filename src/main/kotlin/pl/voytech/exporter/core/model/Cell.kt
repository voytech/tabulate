package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.hints.CellHint

data class Cell<T>(
   val value: Any?,
   val eval: RowCellEval<T>?,
   val type: CellType?,
   val cellHints: List<CellHint>?
)