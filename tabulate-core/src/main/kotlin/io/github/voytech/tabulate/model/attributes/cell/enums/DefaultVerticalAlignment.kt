package io.github.voytech.tabulate.model.attributes.cell.enums

import io.github.voytech.tabulate.model.attributes.cell.enums.contract.VerticalAlignment

enum class DefaultVerticalAlignment: VerticalAlignment {
    BOTTOM,
    TOP,
    MIDDLE;

    override fun getVerticalAlignmentId(): String = name
}