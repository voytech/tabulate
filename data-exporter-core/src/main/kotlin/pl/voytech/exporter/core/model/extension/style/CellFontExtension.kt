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
        override fun build(): CellExtension = CellFontExtension(fontFamily,fontSize, weight, italic, strikeout, underline, fontColor)
    }
}

fun font(block: CellFontExtension.Builder.() -> Unit): CellExtension = CellFontExtension.Builder().apply(block).build()
