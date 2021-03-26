package pl.voytech.exporter.core.model.attributes.style

import pl.voytech.exporter.core.api.builder.CellAttributeBuilder
import pl.voytech.exporter.core.model.attributes.style.enums.BaseBorderStyle
import pl.voytech.exporter.core.model.attributes.style.enums.contract.BorderStyle

data class CellBordersAttribute(
    val leftBorderStyle: BorderStyle? = BaseBorderStyle.NONE,
    val leftBorderColor: Color? = null,
    val rightBorderStyle: BorderStyle? = BaseBorderStyle.NONE,
    val rightBorderColor: Color? = null,
    val topBorderStyle: BorderStyle? = BaseBorderStyle.NONE,
    val topBorderColor: Color? = null,
    val bottomBorderStyle: BorderStyle? = BaseBorderStyle.NONE,
    val bottomBorderColor: Color? = null
) : CellStyleAttribute<CellBordersAttribute>() {

    class Builder : CellAttributeBuilder<CellBordersAttribute> {
        var leftBorderStyle: BorderStyle? = BaseBorderStyle.NONE
        var leftBorderColor: Color? = null
        var rightBorderStyle: BorderStyle? = BaseBorderStyle.NONE
        var rightBorderColor: Color? = null
        var topBorderStyle: BorderStyle? = BaseBorderStyle.NONE
        var topBorderColor: Color? = null
        var bottomBorderStyle: BorderStyle? = BaseBorderStyle.NONE
        var bottomBorderColor: Color? = null

        override fun build(): CellBordersAttribute = CellBordersAttribute(
            leftBorderStyle,
            leftBorderColor,
            rightBorderStyle,
            rightBorderColor,
            topBorderStyle,
            topBorderColor,
            bottomBorderStyle,
            bottomBorderColor,
        )
    }

    override fun mergeWith(other: CellBordersAttribute): CellBordersAttribute = CellBordersAttribute(
        leftBorderStyle = other.leftBorderStyle ?: this.leftBorderStyle,
        leftBorderColor = other.leftBorderColor ?: this.leftBorderColor,
        rightBorderStyle = other.rightBorderStyle ?: this.rightBorderStyle,
        rightBorderColor = other.rightBorderColor ?: this.rightBorderColor,
        topBorderStyle = other.topBorderStyle ?: this.topBorderStyle,
        topBorderColor = other.topBorderColor ?: this.topBorderColor,
        bottomBorderStyle = other.bottomBorderStyle ?: this.bottomBorderStyle,
        bottomBorderColor = other.bottomBorderColor ?: this.bottomBorderColor,
    )
}

fun borders(block: CellBordersAttribute.Builder.() -> Unit): CellBordersAttribute =
    CellBordersAttribute.Builder().apply(block).build()
