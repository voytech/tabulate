package pl.voytech.exporter.core.model.attributes.style

import pl.voytech.exporter.core.api.builder.CellAttributeBuilder
import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.model.attributes.style.enums.BorderStyle

data class CellBordersAttribute(
    val leftBorderStyle: BorderStyle? = BorderStyle.NONE,
    val leftBorderColor: Color? = null,
    val rightBorderStyle: BorderStyle? = BorderStyle.NONE,
    val rightBorderColor: Color? = null,
    val topBorderStyle: BorderStyle? = BorderStyle.NONE,
    val topBorderColor: Color? = null,
    val bottomBorderStyle: BorderStyle? = BorderStyle.NONE,
    val bottomBorderColor: Color? = null
) : CellStyleAttribute() {

    class Builder : CellAttributeBuilder {
        var leftBorderStyle: BorderStyle? = BorderStyle.NONE
        var leftBorderColor: Color? = null
        var rightBorderStyle: BorderStyle? = BorderStyle.NONE
        var rightBorderColor: Color? = null
        var topBorderStyle: BorderStyle? = BorderStyle.NONE
        var topBorderColor: Color? = null
        var bottomBorderStyle: BorderStyle? = BorderStyle.NONE
        var bottomBorderColor: Color? = null

        override fun build(): CellAttribute = CellBordersAttribute(
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

    override fun mergeWith(other: CellAttribute): CellAttribute = CellBordersAttribute(
        leftBorderStyle = if (other is CellBordersAttribute) other.leftBorderStyle
            ?: this.leftBorderStyle else this.leftBorderStyle,
        leftBorderColor = if (other is CellBordersAttribute) other.leftBorderColor
            ?: this.leftBorderColor else this.leftBorderColor,
        rightBorderStyle = if (other is CellBordersAttribute) other.rightBorderStyle
            ?: this.rightBorderStyle else this.rightBorderStyle,
        rightBorderColor = if (other is CellBordersAttribute) other.rightBorderColor
            ?: this.rightBorderColor else this.rightBorderColor,
        topBorderStyle = if (other is CellBordersAttribute) other.topBorderStyle
            ?: this.rightBorderStyle else this.topBorderStyle,
        topBorderColor = if (other is CellBordersAttribute) other.topBorderColor
            ?: this.rightBorderColor else this.topBorderColor,
        bottomBorderStyle = if (other is CellBordersAttribute) other.bottomBorderStyle
            ?: this.bottomBorderStyle else this.bottomBorderStyle,
        bottomBorderColor = if (other is CellBordersAttribute) other.bottomBorderColor
            ?: this.bottomBorderColor else this.bottomBorderColor,
    )
}

fun borders(block: CellBordersAttribute.Builder.() -> Unit): CellAttribute =
    CellBordersAttribute.Builder().apply(block).build()