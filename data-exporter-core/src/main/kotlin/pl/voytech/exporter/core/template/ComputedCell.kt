package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.extension.CellExtension

data class ComputedCell(
    val value: CellValue,
    val extensions: Set<CellExtension>?
)