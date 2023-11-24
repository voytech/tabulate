package io.github.voytech.tabulate.excel

import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.model.Height
import io.github.voytech.tabulate.core.model.Size
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.core.model.attributes.TextStylesAttribute
import io.github.voytech.tabulate.core.model.text.DefaultFonts
import io.github.voytech.tabulate.core.model.text.DefaultWeightStyle
import io.github.voytech.tabulate.core.operation.*
import io.github.voytech.tabulate.core.operation.Nothing
import java.awt.font.FontRenderContext
import java.awt.font.TextAttribute
import java.awt.font.TextLayout
import java.text.AttributedString
import kotlin.Double
import kotlin.Float
import kotlin.String
import kotlin.let


class TextMeasurements(
    private val text: String,
    private val boundingBox: RenderableBoundingBox,
    private val textStyles: TextStylesAttribute? = null,
) {

    private val fontHeightInPoints: Float = textStyles?.fontSize?.toFloat() ?: 12F

    private val fontName: String = textStyles?.fontFamily?.fontName ?: DefaultFonts.ARIAL.fontName

    /*
        str.addAttribute(TextAttribute.FAMILY, font.getFontName(), startIdx, endIdx);
        str.addAttribute(TextAttribute.SIZE, (float)font.getFontHeightInPoints());
        if (font.getBold()) str.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, startIdx, endIdx);
        if (font.getItalic() ) str.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE, startIdx, endIdx);
        if (font.getUnderline() == Font.U_SINGLE ) str.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, startIdx, endIdx);
     */
    private fun createAttributedString(): AttributedString {
        val attributedString = AttributedString(text)
        textStyles?.let {
            attributedString.addAttribute(TextAttribute.FAMILY, fontName, 0, text.length)
            attributedString.addAttribute(TextAttribute.SIZE, fontHeightInPoints)
            if (it.italic == true) {
                attributedString.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE, 0, text.length)
            }
            if (it.weight?.getWeightStyleId() == DefaultWeightStyle.BOLD.getWeightStyleId()) {
                attributedString.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, 0, text.length)
            }
        }
        return attributedString
    }

    private fun AttributedString.measure(): Size {
        val fontRenderContext = FontRenderContext(null, true, true)
        val layout = TextLayout(iterator, fontRenderContext)
        val bounds = layout.bounds
        // frameWidth accounts for leading spaces which is excluded from bounds.getWidth()
        val frameWidth: Double = bounds.x + bounds.width
        return Size(
            width = Width(frameWidth.toFloat(), UnitsOfMeasure.PT),
            height = Height(layout.ascent + layout.descent, UnitsOfMeasure.PT)
        )
    }

    fun measure(): RenderingResult {
        createAttributedString().measure().let {
            boundingBox.width = boundingBox.width ?: it.width
            boundingBox.height = boundingBox.height ?: it.height
        }
        return Ok.asResult()
    }
}

fun <E> E.measureText(): RenderingResult where E : HasValue<*>, E : Renderable<*> =
    if (!boundingBox.isDefined()) {
        TextMeasurements(value.toString(), boundingBox, getModelAttribute<TextStylesAttribute>()).measure()
    } else Nothing.asResult()