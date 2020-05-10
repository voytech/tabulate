package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.hints.Hint

data class Cell(
   val value: Any?,
   val type: CellType?,
   val hints: List<Hint>?
)