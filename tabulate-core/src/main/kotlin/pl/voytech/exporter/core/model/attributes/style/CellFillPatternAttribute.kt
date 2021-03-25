package pl.voytech.exporter.core.model.attributes.style

import pl.voytech.exporter.core.model.attributes.style.enums.FillPattern

data class CellFillPatternAttribute(
    val pattern: FillPattern
): CellStyleAttribute<CellFillPatternAttribute>() {
    override fun mergeWith(other: CellFillPatternAttribute): CellFillPatternAttribute {
        TODO("Not yet implemented")
    }
}