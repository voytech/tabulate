package pl.voytech.exporter.core.model.attributes.style

import pl.voytech.exporter.core.api.builder.CellAttributeBuilder
import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.model.attributes.style.enums.WeightStyle

data class CellFontAttribute(
    val fontFamily: String? = null,
    val fontSize: Int? = 10,
    val weight: WeightStyle? = WeightStyle.NORMAL,
    val italic: Boolean? = false,
    val strikeout: Boolean? = false,
    val underline: Boolean? = false,
    val fontColor: Color? = Color(0, 0, 0)
) : CellStyleAttribute() {

    class Builder : CellAttributeBuilder {
        var fontFamily: String? = null
        var fontSize: Int? = 10
        var weight: WeightStyle? = WeightStyle.NORMAL
        var italic: Boolean? = false
        var strikeout: Boolean? = false
        var underline: Boolean? = false
        var fontColor: Color? = Color(0, 0, 0)
        override fun build(): CellAttribute =
            CellFontAttribute(fontFamily, fontSize, weight, italic, strikeout, underline, fontColor)
    }

    override fun mergeWith(other: CellAttribute): CellAttribute = CellFontAttribute(
        fontFamily = if (other is CellFontAttribute) other.fontFamily
            ?: this.fontFamily else this.fontFamily,
        fontSize = if (other is CellFontAttribute) other.fontSize
            ?: this.fontSize else this.fontSize,
        weight = if (other is CellFontAttribute) other.weight
            ?: this.weight else this.weight,
        italic = if (other is CellFontAttribute) other.italic
            ?: this.italic else this.italic,
        strikeout = if (other is CellFontAttribute) other.strikeout
            ?: this.strikeout else this.strikeout,
        underline = if (other is CellFontAttribute) other.underline
            ?: this.underline else this.underline,
        fontColor = if (other is CellFontAttribute) other.fontColor
            ?: this.fontColor else this.fontColor
    )
}

fun font(block: CellFontAttribute.Builder.() -> Unit): CellAttribute = CellFontAttribute.Builder().apply(block).build()
