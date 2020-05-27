package pl.voytech.exporter.core.model.extension.style

import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.style.enums.BorderStyle

data class CellBordersExtension(
   val leftBorderStyle: BorderStyle? = null,
   val leftBorderColor: Color? = null,
   val rightBorderStyle: BorderStyle? = null,
   val rightBorderColor: Color? = null,
   val topBorderStyle: BorderStyle? = null,
   val topBorderColor: Color? = null,
   val bottomBorderStyle: BorderStyle? = null,
   val bottomBorderColor: Color? = null
): CellExtension()