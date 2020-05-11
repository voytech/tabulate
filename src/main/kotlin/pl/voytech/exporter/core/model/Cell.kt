package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.hints.CellHint

data class Cell(
   val value: Any?,
   val type: CellType?,
   val cellHints: List<CellHint>?
)