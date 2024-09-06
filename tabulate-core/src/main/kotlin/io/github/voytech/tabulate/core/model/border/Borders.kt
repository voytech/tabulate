package io.github.voytech.tabulate.core.model.border

import io.github.voytech.tabulate.core.model.Height
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.core.model.color.Color

interface Borders {
    val leftBorderStyle: BorderStyle
    val leftBorderColor: Color?
    val leftBorderWidth: Width

    val rightBorderStyle: BorderStyle
    val rightBorderColor: Color?
    val rightBorderWidth: Width

    val topBorderStyle: BorderStyle
    val topBorderColor: Color?
    val topBorderHeight: Height

    val bottomBorderStyle: BorderStyle
    val bottomBorderColor: Color?
    val bottomBorderHeight: Height

    val leftTopBorderCornerRadius: Width
    val leftTopBorderCornerWidth: Width
    val leftTopBorderCornerColor: Color?
    val leftTopBorderCornerStyle: BorderStyle

    val rightTopBorderCornerRadius: Width
    val rightTopBorderCornerWidth: Width
    val rightTopBorderCornerColor: Color?
    val rightTopBorderCornerStyle: BorderStyle

    val leftBottomBorderCornerRadius: Width
    val leftBottomBorderCornerWidth: Width
    val leftBottomBorderCornerColor: Color?
    val leftBottomBorderCornerStyle: BorderStyle

    val rightBottomBorderCornerRadius: Width
    val rightBottomBorderCornerWidth: Width
    val rightBottomBorderCornerColor: Color?
    val rightBottomBorderCornerStyle: BorderStyle
}
