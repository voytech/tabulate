package pl.voytech.exporter.core.model.extension.style

import pl.voytech.exporter.core.model.extension.style.enums.BorderStyle

data class CellBordersExtension(
    val leftBorderStyle: BorderStyle? = BorderStyle.NONE,
    val leftBorderColor: Color? = null,
    //val leftBorderWidth: Int? = null,
    val rightBorderStyle: BorderStyle? = BorderStyle.NONE,
    val rightBorderColor: Color? = null,
    val topBorderStyle: BorderStyle? = BorderStyle.NONE,
    val topBorderColor: Color? = null,
    val bottomBorderStyle: BorderStyle? = BorderStyle.NONE,
    val bottomBorderColor: Color? = null
) : CellStyleExtension()