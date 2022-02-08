package io.github.voytech.tabulate.excel.model

import io.github.voytech.tabulate.model.attributes.cell.enums.contract.BorderStyle

/**
 * Excel format specific border styles
 * @author Wojciech Mąka
 * @since 0.1.0
 */
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


