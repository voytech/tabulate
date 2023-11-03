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

private const val SPACE_CHAR = ' '

@Suppress("MemberVisibilityCanBePrivate")
internal class PdfBoxText(
    private val text: String,
    boundingBox: RenderableBoundingBox,
    private val measures: FontMeasurements,
    paddings: Paddings,
    private val textStyles: TextStylesAttribute? = null,
    alignment: AlignmentAttribute? = null
) : PdfBoxElement(boundingBox, paddings, alignment), PdfBoxRenderable, PdfBoxMeasurable {

    inner class TextLine(
        val width: Float,
        var bottomLeftX: Float,
        val bottomLeftY: Float,
        val text: String,
        val lineIndex: Int,
    ) : PdfBoxRenderable {

        private val words by lazy { text.split(SPACE_CHAR) }

        private val lineWidthComplement by lazy { with(measures) { (measuredWidth - width).toTextUnits() } }

        private val isLast
            get() = lineIndex == lines.size - 1

        private fun getAlignmentPadding(lineWidth: Float): Float = alignment?.horizontal?.let {
            when (it) {
                DefaultHorizontalAlignment.RIGHT -> measuredWidth - lineWidth
                DefaultHorizontalAlignment.CENTER -> measuredWidth / 2 - lineWidth / 2
                else -> 0F
            }
        } ?: 0F

        private fun PdfBoxRenderingContext.justifyAndShowText() {
            val newWordGap = lineWidthComplement / (words.size - 1)
            showTextPartsAtOffsets(words.indices.map { i ->
                // when text part is NEGATIVE then move text part to the RIGHT by value in text units.
                TextPosition((0F.takeIf { i == 0 } ?: (-spaceCharTextUnitsWidth - newWordGap)) to words[i])
            })
        }

        override fun render(renderer: PdfBoxRenderingContext): RenderingResult {
            val computedLeft = bottomLeftX + getAlignmentPadding(width)
            renderer.getCurrentContentStream().setTextMatrix(Matrix.getTranslateInstance(computedLeft, bottomLeftY))
            if (alignment?.horizontal == DefaultHorizontalAlignment.JUSTIFY && !isLast) {
                renderer.justifyAndShowText()
            } else {
                renderer.showText(text)
            }
            return Nothing.asResult()
        }

    }

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

    private var textLineBreakOffset = -1
    private var currentLineBreakOffset = -1
    private var widthTillBreakLine = 0F
    private val spaceCharTextUnitsWidth = with(measures) { font().getWidth(" ".codePointAt(0)) }

    private fun resolveTextWrap(): TextWrap =
        textStyles?.textWrap ?: DefaultTextWrap.NO_WRAP

    private fun canBreakLine(): Boolean =
        textWrap.getId() == DefaultTextWrap.BREAK_LINES.getId() && currentLineBreakOffset != -1 && textLineBreakOffset != -1

    private fun canBreakWord(): Boolean =
        textWrap.getId() == DefaultTextWrap.BREAK_WORDS.getId()

    private fun isLF(char: Char): Boolean = char == '\n'

    private fun isRF(char: Char): Boolean = char == '\r'

    private fun markMaybeLineBreak(index: Int) {
        if (text[index] == SPACE_CHAR && text.length > index + 1) {
            // Position current line and whole text offset to swallow trailing space.
            // When breaking line on space char, space char should vanish from resulting line and
            // line length should not include length of trailing space char.
            textLineBreakOffset = index + 1
            widthTillBreakLine = currentX
            currentLineBreakOffset = currentLine.length
        }
    }

    private fun PdfBoxRenderingContext.handleAndClearCurrentLine(lineWidth: Float) {
        val pdfBoxOriginY = (topLeftY + currentY + lineHeight).intoPdfBoxOrigin()
        val pdfBoxOriginX = topLeftX
        TextLine(
            lineWidth, pdfBoxOriginX, pdfBoxOriginY + measures.descender(),
            currentLine.toString(), lines.size
        ).also {
            lines.add(it)
            currentLine.clear()
        }
    }

    private fun moveToNextLine() {
        currentY += lineHeight
        currentX = 0F
        currentLineBreakOffset = -1
        textLineBreakOffset = -1
        widthTillBreakLine = 0F
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
                        markMaybeLineBreak(offset)
                        currentX += charWidth
                        measuredWidth = max(currentX, measuredWidth)
                        currentLine.append(char)
                        offset += Character.charCount(codePoint)
                    } else {
                        if (canBreakWord()) {
                            renderer.handleAndClearCurrentLine(currentX)
                            moveToNextLine()
                        } else if (canBreakLine()) {
                            offset = textLineBreakOffset
                            currentLine.delete(currentLineBreakOffset, currentLine.length)
                            renderer.handleAndClearCurrentLine(widthTillBreakLine)
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


    override fun render(renderer: PdfBoxRenderingContext): RenderingResult {
        if (measuringResult == null) {
            measuringResult = measure(renderer)
        }
        renderer.beginText()
        lines.forEach { it.render(renderer) }
        renderer.endText()
        return requireNotNull(measuringResult)
    }
}
