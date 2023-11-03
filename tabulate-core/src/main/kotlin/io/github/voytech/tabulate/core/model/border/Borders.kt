package io.github.voytech.tabulate.core.model.border

import io.github.voytech.tabulate.core.model.Height
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.core.model.color.Color

interface Borders {
    val leftBorderStyle: BorderStyle?
    val leftBorderColor: Color?
    val leftBorderWidth: Width

    val rightBorderStyle: BorderStyle?
    val rightBorderColor: Color?
    val rightBorderWidth: Width

    val topBorderStyle: BorderStyle?
    val topBorderColor: Color?
    val topBorderHeight: Height

    val bottomBorderStyle: BorderStyle?
    val bottomBorderColor: Color?
    val bottomBorderHeight: Height
}