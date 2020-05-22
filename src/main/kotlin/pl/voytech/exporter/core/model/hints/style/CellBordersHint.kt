package pl.voytech.exporter.core.model.hints.style

import pl.voytech.exporter.core.model.hints.CellHint
import pl.voytech.exporter.core.model.hints.style.enums.BorderStyle

data class CellBordersHint(
   val leftBorderStyle: BorderStyle? = null,
   val leftBorderColor: Color? = null,
   val rightBorderStyle: BorderStyle? = null,
   val rightBorderColor: Color? = null,
   val topBorderStyle: BorderStyle? = null,
   val topBorderColor: Color? = null,
   val bottomBorderStyle: BorderStyle? = null,
   val bottomBorderColor: Color? = null
): CellHint()