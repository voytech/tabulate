package pl.voytech.exporter.core.model.attributes.cell.enums

import pl.voytech.exporter.core.model.attributes.cell.enums.contract.WeightStyle

enum class DefaultWeightStyle: WeightStyle {
    NORMAL,
    BOLD;

    override fun getWeightStyleId(): String = name
}