package io.github.voytech.tabulate.pdf

import io.github.voytech.tabulate.core.model.attributes.TextStylesAttribute
import io.github.voytech.tabulate.core.operation.AttributedContext
import org.apache.pdfbox.pdmodel.font.PDFont


class FontMeasurements(private val inner: Pair<PDFont, Int>) {

    fun font(): PDFont = inner.first

    fun fontSize(): Int = inner.second

    private fun capHeight(): Float = font().fontDescriptor.capHeight

    private fun descent(): Float = font().fontDescriptor.descent * -1

    fun descender(): Float = descent().toPoints()

    fun fontHeight(): Float =
        (capHeight() + (2 * descent())).toPoints()

    fun Float.toPoints(): Float = this / 1000 * fontSize()

    fun Float.toTextUnits(): Float = this * 1000 / fontSize()

}

fun <A : AttributedContext> A.textMeasures(): FontMeasurements =
    getModelAttribute<TextStylesAttribute>().let {
        FontMeasurements((it?.pdFont() ?: defaultFont) to (it?.fontSize ?: defaultFontSize))
    }

