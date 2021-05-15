package io.github.voytech.tabulate.core.template.context

import io.github.voytech.tabulate.core.model.CellType

data class CellValue(
    val value: Any,
    val type: CellType?,
    val colSpan: Int = 1,
    val rowSpan: Int = 1
)
