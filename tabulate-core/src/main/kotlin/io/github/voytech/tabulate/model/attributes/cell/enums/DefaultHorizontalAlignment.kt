package io.github.voytech.tabulate.model.attributes.cell.enums

import io.github.voytech.tabulate.model.attributes.cell.enums.contract.HorizontalAlignment

enum class DefaultHorizontalAlignment: HorizontalAlignment {
    LEFT,
    RIGHT,
    CENTER,
    JUSTIFY,
    FILL;

    override fun getHorizontalAlignmentId(): String = name

}