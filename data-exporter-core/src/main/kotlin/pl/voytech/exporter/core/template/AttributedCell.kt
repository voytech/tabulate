package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.attributes.CellAttribute

data class AttributedCell(
    val value: CellValue,
    val attributes: Set<CellAttribute>?
)