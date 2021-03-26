package pl.voytech.exporter.core.model.attributes.cell.enums

import pl.voytech.exporter.core.model.attributes.cell.enums.contract.HorizontalAlignment

enum class DefaultHorizontalAlignment: HorizontalAlignment {
    LEFT,
    RIGHT,
    CENTER,
    JUSTIFY,
    FILL;

    override fun getHorizontalAlignmentId(): String = name

}