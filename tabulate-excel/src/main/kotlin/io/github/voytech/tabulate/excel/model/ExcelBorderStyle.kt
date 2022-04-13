package io.github.voytech.tabulate.excel.model

import io.github.voytech.tabulate.model.attributes.cell.enums.DefaultBorderStyle
import io.github.voytech.tabulate.model.attributes.cell.enums.contract.BorderStyle
import org.apache.poi.ss.usermodel.BorderStyle as POIBorderStyle
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

fun BorderStyle.resolveBorderStyle(): POIBorderStyle {
    return when (getBorderStyleId()) {
        DefaultBorderStyle.DASHED.name -> POIBorderStyle.DASHED
        DefaultBorderStyle.DOTTED.name -> POIBorderStyle.DOTTED
        DefaultBorderStyle.SOLID.name -> POIBorderStyle.THIN
        DefaultBorderStyle.DOUBLE.name -> POIBorderStyle.DOUBLE
        else -> try {
            POIBorderStyle.valueOf(getBorderStyleId())
        } catch (e: IllegalArgumentException) {
            POIBorderStyle.NONE
        }
    }
}
