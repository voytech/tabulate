package pl.voytech.exporter.core.model.attributes.cell.enums

import pl.voytech.exporter.core.model.attributes.cell.enums.contract.CellFill

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