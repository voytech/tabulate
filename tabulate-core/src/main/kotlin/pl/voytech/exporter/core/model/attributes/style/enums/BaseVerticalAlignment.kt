package pl.voytech.exporter.core.model.attributes.style.enums

import pl.voytech.exporter.core.model.attributes.style.enums.contract.VerticalAlignment

enum class BaseVerticalAlignment: VerticalAlignment {
    BOTTOM,
    TOP,
    MIDDLE;

    override fun getAttributeId(): String = this.name
}