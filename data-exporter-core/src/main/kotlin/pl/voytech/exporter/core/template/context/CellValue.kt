package pl.voytech.exporter.core.template.context

import pl.voytech.exporter.core.model.CellType

data class CellValue(
    val value: Any,
    val type: CellType?,
    val colSpan: Int = 1,
    val rowSpan: Int = 1
)
