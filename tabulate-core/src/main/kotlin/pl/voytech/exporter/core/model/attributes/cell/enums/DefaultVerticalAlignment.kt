package pl.voytech.exporter.core.model.attributes.cell.enums

import pl.voytech.exporter.core.model.attributes.cell.enums.contract.VerticalAlignment

enum class DefaultVerticalAlignment: VerticalAlignment {
    BOTTOM,
    TOP,
    MIDDLE;

    override fun getVerticalAlignmentId(): String = name
}