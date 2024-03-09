package io.github.voytech.tabulate.pdf

import io.github.voytech.tabulate.core.layout.Axis
import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.model.alignment.DefaultHorizontalAlignment
import io.github.voytech.tabulate.core.model.alignment.DefaultVerticalAlignment
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

    private val requiredBboxWidth
        get() = bboxWidth?.toFloat() ?: measuredWidth

    private val requiredBboxHeight
        get() = requireNotNull(bboxHeight)

    inner class TextLine(
        val width: Float,
        var bottomLeftX: Float,
        val bottomLeftY: Float,
        val text: String,
        val lineIndex: Int
    ) : PdfBoxRenderable {

        private val words by lazy { text.split(SPACE_CHAR) }

        private val lineWidthComplement by lazy { with(measures) { (measuredWidth - width).toTextUnits() } }

        private val isLast
            get() = lineIndex == lines.size - 1

        private fun PdfBoxRenderingContext.justifyAndShowText() {
            val newWordGap = lineWidthComplement / (words.size - 1)
            showTextPartsAtOffsets(words.indices.map { i ->
                // when text part is NEGATIVE then move text part to the RIGHT by value in text units.
                TextPosition((0F.takeIf { i == 0 } ?: (-spaceCharTextUnitsWidth - newWordGap)) to words[i])
            })
        }

        private fun getHorizontalAlignmentPadding(lineWidth: Float): Float = alignment?.horizontal?.let {
            when (it) {
                DefaultHorizontalAlignment.RIGHT -> (requiredBboxWidth - lineWidth).coerceAtLeast(0F)
                DefaultHorizontalAlignment.CENTER -> (requiredBboxWidth / 2 - lineWidth / 2).coerceAtLeast(0F)
                else -> 0F
            }
        } ?: 0F

        private fun getVerticalAlignmentPadding(): Float = alignment?.vertical?.let {
            when (it) {
                DefaultVerticalAlignment.TOP -> (requiredBboxHeight - lineHeight).coerceAtLeast(0F)
                DefaultVerticalAlignment.MIDDLE -> (requiredBboxHeight / 2 - lineHeight / 2).coerceAtLeast(0F)
                else -> 0F
            }
        } ?: 0F

        override fun render(renderer: PdfBoxRenderingContext): RenderingResult {
            val computedLeftX = bottomLeftX + getHorizontalAlignmentPadding(width)
            val computedLeftY = bottomLeftY - getVerticalAlignmentPadding()
            renderer.getCurrentContentStream().setTextMatrix(Matrix.getTranslateInstance(computedLeftX, computedLeftY))
            if (alignment?.horizontal == DefaultHorizontalAlignment.JUSTIFY && !isLast) {
                renderer.justifyAndShowText()
            } else {
                renderer.showText(text)
            }
            return Nothing.asResult()
        }

    }

    // Immutable properties
    private val fontHeight = measures.fontHeight()
    private val lineHeight = fontHeight * (textStyles?.lineSpacing ?: 1F)
    private val textWrap = resolveTextWrap()
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
    private val fontColor = textStyles?.fontColor.awtColorOrDefault()

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
                    if (currentY > 0F && (currentY + lineHeight).roundToInt() > maxHeight) {
                        adjustRenderableBoundingBox(measuredWidth, currentY + lineHeight)
                        return RenderedPartly(Axis.Y).asResult()
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
                            val effectiveMeasuredWidth = if (measuredWidth==0F) charWidth else measuredWidth
                            adjustRenderableBoundingBox(effectiveMeasuredWidth, currentY + lineHeight)
                            return RenderedPartly(Axis.X).asResult()
                        }
                    }
                }
                if (currentLine.isNotEmpty()) {
                    renderer.handleAndClearCurrentLine(currentX)
                }
                adjustRenderableBoundingBox(measuredWidth, currentY + lineHeight)
                return Ok.asResult()
            }
        }
    }


    override fun render(renderer: PdfBoxRenderingContext): RenderingResult {
        if (measuringResult == null) {
            measuringResult = measure(renderer)
        }
        renderer.renderClipped(boundingBox) {
            renderer.beginText()
            renderer.setFont(measures.font(), measures.fontSize().toFloat())
            renderer.getCurrentContentStream().setNonStrokingColor(fontColor)
            lines.forEach { it.render(renderer) }
            renderer.endText()
        }
        return requireNotNull(measuringResult)
    }
}
