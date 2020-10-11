package pl.voytech.exporter.core.model.extension.style

import pl.voytech.exporter.core.api.builder.CellExtensionBuilder
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.style.enums.BorderStyle

data class CellBordersExtension(
    val leftBorderStyle: BorderStyle? = BorderStyle.NONE,
    val leftBorderColor: Color? = null,
    val rightBorderStyle: BorderStyle? = BorderStyle.NONE,
    val rightBorderColor: Color? = null,
    val topBorderStyle: BorderStyle? = BorderStyle.NONE,
    val topBorderColor: Color? = null,
    val bottomBorderStyle: BorderStyle? = BorderStyle.NONE,
    val bottomBorderColor: Color? = null
) : CellStyleExtension() {

    class Builder : CellExtensionBuilder {
        var leftBorderStyle: BorderStyle? = BorderStyle.NONE
        var leftBorderColor: Color? = null
        var rightBorderStyle: BorderStyle? = BorderStyle.NONE
        var rightBorderColor: Color? = null
        var topBorderStyle: BorderStyle? = BorderStyle.NONE
        var topBorderColor: Color? = null
        var bottomBorderStyle: BorderStyle? = BorderStyle.NONE
        var bottomBorderColor: Color? = null

        override fun build(): CellExtension = CellBordersExtension(
            leftBorderStyle,
            leftBorderColor,
            rightBorderStyle,
            rightBorderColor,
            topBorderStyle,
            topBorderColor,
            bottomBorderStyle,
            bottomBorderColor
        )
    }

    override fun mergeWith(other: CellExtension): CellExtension = CellBordersExtension(
        leftBorderStyle = if (other is CellBordersExtension) other.leftBorderStyle
            ?: this.leftBorderStyle else this.leftBorderStyle,
        leftBorderColor = if (other is CellBordersExtension) other.leftBorderColor
            ?: this.leftBorderColor else this.leftBorderColor,
        rightBorderStyle = if (other is CellBordersExtension) other.rightBorderStyle
            ?: this.rightBorderStyle else this.rightBorderStyle,
        rightBorderColor = if (other is CellBordersExtension) other.rightBorderColor
            ?: this.rightBorderColor else this.rightBorderColor,
        topBorderStyle = if (other is CellBordersExtension) other.topBorderStyle
            ?: this.rightBorderStyle else this.topBorderStyle,
        topBorderColor = if (other is CellBordersExtension) other.topBorderColor
            ?: this.rightBorderColor else this.topBorderColor,
        bottomBorderStyle = if (other is CellBordersExtension) other.bottomBorderStyle
            ?: this.bottomBorderStyle else this.bottomBorderStyle,
        bottomBorderColor = if (other is CellBordersExtension) other.bottomBorderColor
            ?: this.bottomBorderColor else this.bottomBorderColor,
    )
}

fun borders(block: CellBordersExtension.Builder.() -> Unit): CellExtension =
    CellBordersExtension.Builder().apply(block).build()