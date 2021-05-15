package io.github.voytech.tabulate.core.model.attributes.cell.enums

import io.github.voytech.tabulate.core.model.attributes.cell.enums.contract.WeightStyle

enum class DefaultWeightStyle: WeightStyle {
    NORMAL,
    BOLD;

    override fun getWeightStyleId(): String = name
}