package io.github.voytech.tabulate.impl.template.model

import io.github.voytech.tabulate.core.model.attributes.cell.enums.contract.BorderStyle

enum class ExcelBorderStyle : BorderStyle {
    THIN,
    THICK,
    DASH_DOT,
    DASH_DOT_DOT,
    HAIR,
    MEDIUM,
    MEDIUM_DASHED,
    MEDIUM_DASH_DOT,
    MEDIUM_DASH_DOT_DOT,
    MEDIUM_SLANTED;
    override fun getBorderStyleId() = name
}


