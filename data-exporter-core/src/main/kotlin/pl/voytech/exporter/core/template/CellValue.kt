package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.CellType

data class CellValue(
    val value: Any,
    val type: CellType?
)