package io.github.voytech.tabulate.excel

import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.model.alignment.DefaultHorizontalAlignment
import io.github.voytech.tabulate.core.model.asHeight
import io.github.voytech.tabulate.core.model.asWidth
import io.github.voytech.tabulate.core.model.attributes.AlignmentAttribute
import io.github.voytech.tabulate.core.model.attributes.TextStylesAttribute
import io.github.voytech.tabulate.core.model.text.DefaultFonts
import io.github.voytech.tabulate.core.model.text.DefaultTextWrap
import io.github.voytech.tabulate.core.model.text.TextWrap
import io.github.voytech.tabulate.core.operation.*
import io.github.voytech.tabulate.core.operation.Nothing
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val SPACE_CHAR = ' '

class ApachePoiTextBox(
    private val text: String,
    private val boundingBox: RenderableBoundingBox,
    private val textStyles: TextStylesAttribute? = null,
    private val alignment: AlignmentAttribute? = null
) {

    private val topLeftX = boundingBox.absoluteX.value
    private val topLeftY = boundingBox.absoluteY.value

    inner class TextLine(
        val width: Float,
        var bottomLeftX: Float,
        val bottomLeftY: Float,
        val text: String,
        val lineIndex: Int,
    ) {

        private val words by lazy { text.split(SPACE_CHAR) }

        private val lineWidthComplement by lazy { with(metrics) { (measuredWidth - width) } }

        private val isLast
            get() = lineIndex == lines.size - 1

        private fun getAlignmentPadding(lineWidth: Float): Float = alignment?.horizontal?.let {
            when (it) {
                DefaultHorizontalAlignment.RIGHT -> measuredWidth - lineWidth
                DefaultHorizontalAlignment.CENTER -> measuredWidth / 2 - lineWidth / 2
                else -> 0F
            }
        } ?: 0F

        private fun justifyAndShowText() {
            val newWordGap = lineWidthComplement / (words.size - 1)
            //showTextPartsAtOffsets(words.indices.map { i ->
            // when text part is NEGATIVE then move text part to the RIGHT by value in text units.
            //    TextPosition((0F.takeIf { i == 0 } ?: (-spaceCharTextUnitsWidth - newWordGap)) to words[i])
            //})
        }

        fun render(): RenderingResult {
            val computedLeft = bottomLeftX + getAlignmentPadding(width)
            if (alignment?.horizontal == DefaultHorizontalAlignment.JUSTIFY && !isLast) {
            } else {
            }
            return Nothing.asResult()
        }

    }

    private var currentX = 0F
    private var currentY = 0F
    private var measuredWidth = 0F
    private var offset = 0

    private var textLineBreakOffset = -1
    private var currentLineBreakOffset = -1
    private var widthTillBreakLine = 0F
    //private val spaceCharTextUnitsWidth = with(measures) { font().getWidth(" ".codePointAt(0)) }

    private val maxWidth
        get() = resolveMaxWidth()
    private val maxHeight
        get() = resolveMaxHeight()

    private fun resolveMaxWidth(): Int = ((boundingBox.let {
        if (it.width != null) {
            min(it.maxWidth?.value ?: 0F, it.width?.value ?: 0F)
        } else {
            it.maxWidth?.value
        }
    } ?: 0f)).roundToInt()

    private fun resolveMaxHeight(): Int = ((boundingBox.let {
        if (it.height != null) {
            min(it.maxHeight?.value ?: 0F, it.height?.value ?: 0F)
        } else {
            it.maxHeight?.value
        }
    } ?: 0f)).roundToInt()

    private val fontHeightInPoints: Float = textStyles?.fontSize?.toFloat() ?: 12F

    private val fontName: String = textStyles?.fontFamily?.fontName ?: DefaultFonts.ARIAL.fontName

    private val font = Font(fontName, Font.PLAIN, fontHeightInPoints.toInt())
    private val graphics = graphics(font)
    private val metrics = graphics.fontMetrics
    private val fontHeight = metrics.ascent
    private val lineHeight = fontHeight * (textStyles?.lineSpacing ?: 1F)
    private val textWrap = resolveTextWrap()
    private val spaceCharTextUnitsWidth = with(metrics) { charWidth(" ".codePointAt(0)) }
    private val currentLine = StringBuilder()
    private val lines = mutableListOf<TextLine>()
    private fun graphics(font: Font): Graphics2D =
        BufferedImage(
            maxWidth.coerceAtMost(10000),
            maxHeight.coerceAtMost(10000),
            BufferedImage.TYPE_INT_ARGB
        ).createGraphics().also {
            it.font = font
        }

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

    private fun handleAndClearCurrentLine(lineWidth: Float) {
        val pdfBoxOriginY = (topLeftY + currentY + lineHeight)
        val pdfBoxOriginX = topLeftX
        TextLine(
            lineWidth, pdfBoxOriginX, pdfBoxOriginY + metrics.descent,
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

    fun measure(): RenderingResult {
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
                    handleAndClearCurrentLine(currentX)
                    moveToNextLine()
                }
                offset += Character.charCount(codePoint)
                continue
            }
            val charWidth = metrics.charWidth(codePoint)
            if ((currentX + charWidth).roundToInt() <= maxWidth) {
                markMaybeLineBreak(offset)
                currentX += charWidth
                measuredWidth = max(currentX, measuredWidth)
                currentLine.append(char)
                offset += Character.charCount(codePoint)
            } else {
                if (canBreakWord()) {
                    handleAndClearCurrentLine(currentX)
                    moveToNextLine()
                } else if (canBreakLine()) {
                    offset = textLineBreakOffset
                    currentLine.delete(currentLineBreakOffset, currentLine.length)
                    handleAndClearCurrentLine(widthTillBreakLine)
                    moveToNextLine()
                } else {
                    handleAndClearCurrentLine(currentX)
                    applySize(measuredWidth, currentY + lineHeight)
                    return RenderedPartly.asResult()
                }
            }
        }
        if (currentLine.isNotEmpty()) {
            handleAndClearCurrentLine(currentX)
        }
        applySize(measuredWidth, currentY + lineHeight)
        return Ok.asResult()
    }

    private fun applySize(measuredWidth: Float, measuredHeight: Float) {
        boundingBox.apply {
            width = width ?: (measuredWidth.asWidth())
            height = height ?: (measuredHeight.asHeight())
        }
    }
}

fun <E> E.measureText(): RenderingResult where E : HasValue<*>, E : Renderable<*> =
    if (!boundingBox.isDefined()) {
        ApachePoiTextBox(
            value.toString(),
            boundingBox,
            getModelAttribute<TextStylesAttribute>(),
            getModelAttribute<AlignmentAttribute>()
        ).measure()
    } else Nothing.asResult()