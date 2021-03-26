package pl.voytech.exporter.core.model.attributes.style.enums

import pl.voytech.exporter.core.model.attributes.style.enums.contract.WeightStyle

enum class BaseWeightStyle: WeightStyle {
    NORMAL,
    BOLD;

    override fun getAttributeId(): String = this.name
}