package pl.voytech.exporter.core.model.extension.style

import pl.voytech.exporter.core.api.builder.CellExtensionBuilder
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.model.extension.style.enums.WeightStyle

data class CellFontExtension(
    val fontFamily: String? = null,
    val fontSize: Int? = 10,
    val weight: WeightStyle? = WeightStyle.NORMAL,
    val italic: Boolean? = false,
    val strikeout: Boolean? = false,
    val underline: Boolean? = false,
    val fontColor: Color? = Color(0, 0, 0)
) : CellStyleExtension() {

    class Builder : CellExtensionBuilder {
        var fontFamily: String? = null
        var fontSize: Int? = 10
        var weight: WeightStyle? = WeightStyle.NORMAL
        var italic: Boolean? = false
        var strikeout: Boolean? = false
        var underline: Boolean? = false
        var fontColor: Color? = Color(0, 0, 0)
        override fun build(): CellExtension =
            CellFontExtension(fontFamily, fontSize, weight, italic, strikeout, underline, fontColor)
    }

    override fun mergeWith(other: CellExtension): CellExtension = CellFontExtension(
        fontFamily = if (other is CellFontExtension) other.fontFamily
            ?: this.fontFamily else this.fontFamily,
        fontSize = if (other is CellFontExtension) other.fontSize
            ?: this.fontSize else this.fontSize,
        weight = if (other is CellFontExtension) other.weight
            ?: this.weight else this.weight,
        italic = if (other is CellFontExtension) other.italic
            ?: this.italic else this.italic,
        strikeout = if (other is CellFontExtension) other.strikeout
            ?: this.strikeout else this.strikeout,
        underline = if (other is CellFontExtension) other.underline
            ?: this.underline else this.underline,
        fontColor = if (other is CellFontExtension) other.fontColor
            ?: this.fontColor else this.fontColor
    )
}

fun font(block: CellFontExtension.Builder.() -> Unit): CellExtension = CellFontExtension.Builder().apply(block).build()
