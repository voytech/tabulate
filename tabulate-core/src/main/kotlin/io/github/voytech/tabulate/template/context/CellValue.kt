package io.github.voytech.tabulate.template.context

data class CellValue(
    val value: Any,
    val colSpan: Int = 1,
    val rowSpan: Int = 1
)
