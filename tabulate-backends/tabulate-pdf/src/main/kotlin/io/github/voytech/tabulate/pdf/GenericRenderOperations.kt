package io.github.voytech.tabulate.pdf

import io.github.voytech.tabulate.core.model.alignment.DefaultHorizontalAlignment
import io.github.voytech.tabulate.core.model.alignment.DefaultVerticalAlignment
import io.github.voytech.tabulate.core.model.alignment.HorizontalAlignment
import io.github.voytech.tabulate.core.model.alignment.VerticalAlignment
import io.github.voytech.tabulate.core.model.attributes.AlignmentAttribute
import io.github.voytech.tabulate.core.model.attributes.BordersAttribute
import io.github.voytech.tabulate.core.model.attributes.TextStylesAttribute
import io.github.voytech.tabulate.core.model.text.DefaultWeightStyle
import io.github.voytech.tabulate.core.template.operation.AttributeOperation
import io.github.voytech.tabulate.core.template.operation.AttributedContext
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font

fun TextStylesAttribute.default(): PDFont =
    if (weight == DefaultWeightStyle.BOLD) {
        PDType1Font.HELVETICA_BOLD_OBLIQUE.takeIf { italic == true } ?: PDType1Font.HELVETICA_BOLD
    } else if (italic == true) {
        PDType1Font.HELVETICA_OBLIQUE
    } else {
        PDType1Font.HELVETICA
    }

fun TextStylesAttribute.pdFont(): PDFont =
    if (fontFamily != null) {
        when (fontFamily!!.getFontId()) {
            "TIMES_NEW_ROMAN", "TIMES_ROMAN" -> {
                if (weight == DefaultWeightStyle.BOLD) {
                    PDType1Font.TIMES_BOLD_ITALIC.takeIf { italic == true } ?: PDType1Font.TIMES_BOLD
                } else if (italic == true) {
                    PDType1Font.TIMES_ITALIC
                } else {
                    PDType1Font.TIMES_ROMAN
                }
            }

            "COURIER", "COURIER_NEW" -> {
                if (weight == DefaultWeightStyle.BOLD) {
                    PDType1Font.COURIER_BOLD_OBLIQUE.takeIf { italic == true } ?: PDType1Font.COURIER_BOLD
                } else if (italic == true) {
                    PDType1Font.COURIER_OBLIQUE
                } else {
                    PDType1Font.COURIER
                }
            }

            "HELVETICA" -> default()
            else -> default()
        }
    } else default()

class AlignmentAttributeRenderOperation<CTX: AttributedContext> :
    AttributeOperation<PdfBoxRenderingContext, AlignmentAttribute, CTX> {

    private fun fontAndSize(context: CTX): Pair<PDFont, Int> =
        context.getModelAttribute<TextStylesAttribute>().let {
            val font = it?.pdFont() ?: PDType1Font.HELVETICA
            val size: Int = it?.fontSize ?: 16
            return font to size
        }

    private fun PdfBoxRenderingContext.applyTextAlignment(
        context: CTX,
        vertical: VerticalAlignment? = DefaultVerticalAlignment.MIDDLE,
        horizontal: HorizontalAlignment? = DefaultHorizontalAlignment.CENTER,
    ) {
        val bbox = boxLayout(context, context.getModelAttribute<BordersAttribute>())
        var xOffset = 0.0F
        var yOffset = 0.0F
        val params = fontAndSize(context)
        if (vertical != null) {
            val textHeight = params.measureTextHeight()
            when (vertical) {
                DefaultVerticalAlignment.TOP -> {
                    yOffset += (bbox.inner.height?.value ?: 0f) - textHeight
                }

                DefaultVerticalAlignment.BOTTOM -> {}
                DefaultVerticalAlignment.MIDDLE -> {
                    yOffset += (bbox.inner.height?.value?.div(2) ?: 0f) - textHeight / 2
                }
            }
        }
        if (horizontal != null) {
            val textWidth = params.measureTextWidth(" ") // Here all applicable context that is AttributedContext : TextAttributedContext
            when (horizontal) {
                DefaultHorizontalAlignment.LEFT -> {}
                DefaultHorizontalAlignment.CENTER -> {
                    xOffset += ((bbox.inner.width?.value?.div(2)) ?: 0f) - textWidth / 2
                }

                DefaultHorizontalAlignment.RIGHT -> {
                    xOffset += (bbox.inner.width?.value ?: 0f) - textWidth
                }
            }
        }
        xTextOffset += xOffset
        yTextOffset += yOffset
    }

    override fun invoke(
        renderingContext: PdfBoxRenderingContext,
        context: CTX,
        attribute: AlignmentAttribute,
    ) = with(renderingContext) {
        beginText()
        applyTextAlignment(context, attribute.vertical, attribute.horizontal)
    }

}