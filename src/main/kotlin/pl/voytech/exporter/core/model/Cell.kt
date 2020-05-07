package pl.voytech.exporter.core.model

import pl.voytech.exporter.core.model.hints.Hint

data class Cell(
   val hints: List<Hint>?,
   val value: Any
)