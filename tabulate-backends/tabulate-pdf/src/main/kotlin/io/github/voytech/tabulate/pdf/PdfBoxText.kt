package io.github.voytech.tabulate.pdf

import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.model.alignment.DefaultHorizontalAlignment
import io.github.voytech.tabulate.core.model.attributes.AlignmentAttribute
import io.github.voytech.tabulate.core.model.attributes.TextStylesAttribute
import io.github.voytech.tabulate.core.model.text.DefaultTextWrap
import io.github.voytech.tabulate.core.model.text.TextWrap
import io.github.voytech.tabulate.core.operation.*
import io.github.voytech.tabulate.core.operation.Nothing
import org.apache.pdfbox.util.Matrix
import kotlin.math.max
import kotlin.math.roundToInt

private class TextLine(
    val width: Float,
    var bottomLeftX: Float,
    val bottomLeftY: Float,
    val text: String,
) : PdfBoxRenderable {

    override fun render(renderer: PdfBoxRenderingContext): RenderingResult {
        renderer.getCurrentContentStream().setTextMatrix(Matrix.getTranslateInstance(bottomLeftX, bottomLeftY))
        renderer.showText(text)
        return Nothing.asResult()
    }

}

@Suppress("MemberVisibilityCanBePrivate")
internal class PdfBoxText(
    private val text: String,
    boundingBox: RenderableBoundingBox,
    private val measures: FontMeasurements,
    paddings: Paddings,
    private val textStyles: TextStylesAttribute? = null,
    alignment: AlignmentAttribute? = null
) : PdfBoxElement(boundingBox, paddings, alignment), PdfBoxRenderable, PdfBoxMeasurable {

    // Immutable properties
    val fontHeight = measures.fontHeight()
    private val lineHeight = fontHeight * (textStyles?.lineSpacing ?: 1F)

    val textWrap = resolveTextWrap()

    private var measuringResult: RenderingResult? = null

    private val currentLine = StringBuilder()
    private val lines = mutableListOf<TextLine>()

    // Mutable state properties
    private var currentX = 0F
    private var currentY = 0F
    private var measuredWidth = 0F
    private var offset = 0

    private var breakpointIndex = -1
    private var lineBreakpointIndex = -1
    private var breakpointWidth = 0F

    private fun resolveTextWrap(): TextWrap =
        textStyles?.textWrap ?: DefaultTextWrap.NO_WRAP

    private fun canBreakLine(): Boolean =
        textWrap.getId() == DefaultTextWrap.BREAK_LINES.getId() && lineBreakpointIndex != -1 && breakpointIndex != -1

    private fun canBreakWord(): Boolean =
        textWrap.getId() == DefaultTextWrap.BREAK_WORDS.getId()

    private fun isLF(char: Char): Boolean = char == '\n'

    private fun isRF(char: Char): Boolean = char == '\r'

    private fun markMaybeLineBreak(index: Int) {
        if (text[index] == ' ' && text.length > index + 1) {
            breakpointIndex = index + 1
            breakpointWidth = currentX
            lineBreakpointIndex = currentLine.length - 1
        }
    }

    private fun PdfBoxRenderingContext.handleAndClearCurrentLine(lineWidth: Float) {
        val pdfBoxOriginY = (topLeftY + currentY + lineHeight).intoPdfBoxOrigin()
        val pdfBoxOriginX = topLeftX
        TextLine(
            lineWidth, pdfBoxOriginX, pdfBoxOriginY + measures.descender(), currentLine.toString()
        ).also {
            lines.add(it)
            currentLine.clear()
        }
    }

    private fun moveToNextLine() {
        currentY += lineHeight
        currentX = 0F
        lineBreakpointIndex = -1
        breakpointIndex = -1
        breakpointWidth = 0F
    }

    override fun measure(renderer: PdfBoxRenderingContext): RenderingResult {
        return measuringResult ?: run<RenderingResult> {
            with(measures) {
                while (offset < text.length) {
                    if ((currentY + lineHeight).roundToInt() > maxHeight) {
                        applySize(measuredWidth, currentY)
                        return RenderedPartly.asResult()
                    }
                    val codePoint: Int = text.codePointAt(offset)
                    val char = text[offset]
                    val rf = isRF(char)
                    val lf = isLF(char)
                    if (rf || lf) {
                        if (lf) {
                            renderer.handleAndClearCurrentLine(currentX)
                            moveToNextLine()
                        }
                        offset += Character.charCount(codePoint)
                        continue
                    }
                    val charWidth = font().getWidth(codePoint).toPoints()
                    if ((currentX + charWidth).roundToInt() <= maxWidth) {
                        currentX += charWidth
                        measuredWidth = max(currentX, measuredWidth)
                        currentLine.append(char)
                        markMaybeLineBreak(offset)
                        offset += Character.charCount(codePoint)
                    } else {
                        if (canBreakWord()) {
                            renderer.handleAndClearCurrentLine(currentX)
                            moveToNextLine()
                        } else if (canBreakLine()) {
                            offset = breakpointIndex
                            currentLine.delete(lineBreakpointIndex, currentLine.length)
                            renderer.handleAndClearCurrentLine(breakpointWidth)
                            moveToNextLine()
                        } else {
                            renderer.handleAndClearCurrentLine(currentX)
                            applySize(measuredWidth, currentY + lineHeight)
                            return RenderedPartly.asResult()
                        }
                    }
                }
                if (currentLine.isNotEmpty()) {
                    renderer.handleAndClearCurrentLine(currentX)
                }
                applySize(measuredWidth, currentY + lineHeight)
                return Ok.asResult()
            }
        }
    }

    private fun getAlignmentPadding(lineWidth: Float): Float = alignment?.horizontal?.let {
        when (it) {
            DefaultHorizontalAlignment.RIGHT -> measuredWidth - lineWidth
            DefaultHorizontalAlignment.CENTER -> measuredWidth / 2 - lineWidth / 2
            else -> 0F
        }
    } ?: 0F

    override fun render(renderer: PdfBoxRenderingContext): RenderingResult {
        if (measuringResult == null) {
            measuringResult = measure(renderer)
        }
        renderer.beginText()
        lines.forEach {
            it.bottomLeftX = it.bottomLeftX + getAlignmentPadding(it.width)
            it.render(renderer)
        }
        renderer.endText()
        return requireNotNull(measuringResult)
    }
}
