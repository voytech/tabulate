package pl.voytech.exporter.core.model.attributes.style

import pl.voytech.exporter.core.api.builder.CellAttributeBuilder
import pl.voytech.exporter.core.model.attributes.style.enums.WeightStyle

data class CellFontAttribute(
    val fontFamily: String? = null,
    val fontSize: Int? = 10,
    val weight: WeightStyle? = WeightStyle.NORMAL,
    val italic: Boolean? = false,
    val strikeout: Boolean? = false,
    val underline: Boolean? = false,
    val fontColor: Color? = Color(0, 0, 0)
) : CellStyleAttribute<CellFontAttribute>() {

    class Builder : CellAttributeBuilder<CellFontAttribute> {
        var fontFamily: String? = null
        var fontSize: Int? = 10
        var weight: WeightStyle? = WeightStyle.NORMAL
        var italic: Boolean? = false
        var strikeout: Boolean? = false
        var underline: Boolean? = false
        var fontColor: Color? = Color(0, 0, 0)
        override fun build(): CellFontAttribute =
            CellFontAttribute(fontFamily, fontSize, weight, italic, strikeout, underline, fontColor)
    }

    override fun mergeWith(other: CellFontAttribute): CellFontAttribute = CellFontAttribute(
        fontFamily = other.fontFamily ?: this.fontFamily,
        fontSize = other.fontSize ?: this.fontSize,
        weight = other.weight ?: this.weight,
        italic = other.italic ?: this.italic,
        strikeout = other.strikeout ?: this.strikeout,
        underline = other.underline ?: this.underline,
        fontColor = other.fontColor ?: this.fontColor
    )
}

fun font(block: CellFontAttribute.Builder.() -> Unit): CellFontAttribute = CellFontAttribute.Builder().apply(block).build()
