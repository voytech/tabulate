package pl.voytech.exporter.core.model.attributes.style.enums

import pl.voytech.exporter.core.model.attributes.style.enums.contract.HorizontalAlignment

enum class BaseHorizontalAlignment: HorizontalAlignment {
    LEFT,
    RIGHT,
    CENTER,
    JUSTIFY,
    FILL;

    override fun getAttributeId(): String = this.name

}