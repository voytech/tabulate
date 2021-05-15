package io.github.voytech.tabulate.model.attributes.cell.enums

import io.github.voytech.tabulate.model.attributes.cell.enums.contract.CellFill

enum class DefaultCellFill : CellFill {
    WIDE_DOTS,
    LARGE_SPOTS,
    BRICKS,
    DIAMONDS,
    SMALL_DOTS,
    SOLID,
    SQUARES;

    override fun getCellFillId() = name
}