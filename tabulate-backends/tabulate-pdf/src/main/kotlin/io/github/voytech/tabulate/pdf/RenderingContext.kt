package io.github.voytech.tabulate.pdf

import io.github.voytech.tabulate.ImageIndex
import io.github.voytech.tabulate.core.HavingViewportSize
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.model.border.Borders
import io.github.voytech.tabulate.core.model.color.Color
import io.github.voytech.tabulate.core.operation.AttributedEntity
import io.github.voytech.tabulate.core.operation.RenderableEntity
import io.github.voytech.tabulate.core.operation.boundingBox
import io.github.voytech.tabulate.core.result.OutputBinding
import io.github.voytech.tabulate.core.result.OutputStreamOutputBinding
import io.github.voytech.tabulate.core.spi.DocumentFormat
import io.github.voytech.tabulate.core.spi.OutputBindingsProvider
import io.github.voytech.tabulate.round1
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.IOException
import java.io.OutputStream
import java.awt.Color as AwtColor


class PdfBoxOutputBindingsFactory : OutputBindingsProvider<PdfBoxRenderingContext> {

    override fun createOutputBindings(): List<OutputBinding<PdfBoxRenderingContext, *>> = listOf(
        PdfBoxOutputStreamOutputBinding()
    )

    override fun getDocumentFormat(): DocumentFormat<PdfBoxRenderingContext> =
        DocumentFormat.format("pdf", "pdfbox")

}

class PdfBoxOutputStreamOutputBinding : OutputStreamOutputBinding<PdfBoxRenderingContext>() {
    override fun flush(output: OutputStream) {
        renderingContext.closeContents()
        with(renderingContext.document) {
            save(output)
            close()
        }
    }
}

class PdfBoxRenderingContext(
    val document: PDDocument = PDDocument(),
    private val images: ImageIndex = ImageIndex(),
) : RenderingContext, HavingViewportSize {

    private lateinit var pageContentStream: PDPageContentStream
    private lateinit var currentPage: PDPage
    private var textDrawing: Boolean = false
    private var fontSet: Boolean = false
    private var textPositionSet: Boolean = false

    fun createNextPage() = PDPage().apply {
        if (this@PdfBoxRenderingContext::pageContentStream.isInitialized) pageContentStream.close()
        document.addPage(this)
        pageContentStream = createContent()
        currentPage = this
    }

    fun createPageIfMissing() {
        if (!this::currentPage.isInitialized) {
            createNextPage()
        }
    }

    fun beginText() {
        if (!textDrawing) {
            getCurrentContentStream().beginText()
            textDrawing = true
        }
    }

    fun setFont(font: PDFont, fontSize: Float) {
        getCurrentContentStream().setFont(font, fontSize)
        fontSet = true
    }

    fun setTextPosition(tx: Float, ty: Float, forceSet: Boolean = false) {
        if (!textPositionSet || forceSet) {
            getCurrentContentStream().newLineAtOffset(tx, ty)
            textPositionSet = true
        }
    }

    fun showText(text: String) {
        if (!fontSet) {
            getCurrentContentStream().setFont(defaultFont, defaultFontSize.toFloat())
        }
        getCurrentContentStream().showText(text)
    }

    fun showTextPartsAtOffsets(textPositions: List<TextPosition>) {
        if (!fontSet) {
            getCurrentContentStream().setFont(defaultFont, defaultFontSize.toFloat())
        }
        getCurrentContentStream().showTextWithPositioning(Array<Any>(textPositions.size * 2) { i ->
            textPositions[i / 2].xPositionedText.let { pair ->
                pair.first.takeIf { i.mod(2) == 0 } ?: pair.second
            }
        })
    }

    fun endText() {
        textDrawing = false
        fontSet = false
        textPositionSet = false
        getCurrentContentStream().setFont(defaultFont, defaultFontSize.toFloat())
        getCurrentContentStream().setStrokingColor(AwtColor.BLACK)
        getCurrentContentStream().setNonStrokingColor(AwtColor.BLACK)
        getCurrentContentStream().endText()
    }

    fun loadImage(filePath: String): PDImageXObject = with(images) {
        createImage(filePath.cacheImageAsByteArray())
    }

    private fun createImage(binary: ByteArray, name: String = "noname"): PDImageXObject =
        PDImageXObject.createFromByteArray(document, binary, name)

    fun PDImageXObject.showImage(x: Float, y: Float, width: Float?, height: Float?) {
        if (width != null && height != null) {
            getCurrentContentStream().drawImage(this, x, y, width, height)
        } else
            getCurrentContentStream().drawImage(this, x, y)
    }

    fun renderClipped(bbox: RenderableBoundingBox, render: () -> Unit) = with(getCurrentContentStream()) {
        if (bbox.isDefined()) {
            val height = requireNotNull(bbox.height)
            val width = requireNotNull(bbox.width)
            val bottomY = bbox.absoluteY + height
            val rightX = bbox.absoluteX + width
            val maxRightBottom = bbox.maxRightBottom
            if (bottomY >= maxRightBottom.y || rightX >= maxRightBottom.x) {
                val yLimit = bottomY.value.coerceAtMost(maxRightBottom.y.value)
                val effectiveHeight = yLimit - bbox.absoluteY.value
                val xLimit = rightX.value.coerceAtMost(maxRightBottom.x.value)
                val effectiveWidth = xLimit - bbox.absoluteX.value
                val y = bbox.absoluteY.value.intoPdfBoxOrigin()
                saveGraphicsState()
                getCurrentContentStream().addRect(
                    bbox.absoluteX.value, y-effectiveHeight, effectiveWidth, effectiveHeight
                )
                getCurrentContentStream().clip()
                render()
                restoreGraphicsState()
            } else {
                render()
            }
        }
    }

    fun <CTX: AttributedEntity> renderClipped(context: CTX, render: () -> Unit) =
        renderClipped((context as RenderableEntity<*>).boundingBox,render)

    fun BoxLayout.fillRect(color: Color? = null) {
        with(getCurrentContentStream()) {
            saveGraphicsState()
            setNonStrokingColor(color.awtColorOrDefault())
            moveTo(outerLeftTopX + leftTopCornerRadiusOrZero, outerLeftTopY)
            lineTo(outerRightTopX - rightTopCornerRadiusOrZero, outerRightTopY)
            curveTo(outerRightTopX, outerRightTopY, outerRightTopX, outerRightTopY, outerRightTopX, outerRightTopY - rightTopCornerRadiusOrZero)
            lineTo(outerRightBottomX, outerRightBottomY + rightBottomCornerRadiusOrZero)
            curveTo(outerRightBottomX, outerRightBottomY, outerRightTopX, outerRightBottomY, outerRightBottomX - rightBottomCornerRadiusOrZero, outerRightBottomY)
            lineTo(outerLeftBottomX + leftBottomCornerRadiusOrZero, outerLeftBottomY)
            curveTo(outerLeftBottomX, outerLeftBottomY, outerLeftBottomX, outerLeftBottomY, outerLeftBottomX, outerLeftBottomY + leftBottomCornerRadiusOrZero)
            lineTo(outerLeftTopX, outerLeftTopY - leftTopCornerRadiusOrZero)
            curveTo(outerLeftTopX, outerLeftTopY, outerLeftTopX, outerLeftTopY, outerLeftTopX + leftTopCornerRadiusOrZero, outerLeftTopY)
            closePath()
            fill()
            restoreGraphicsState()
        }
    }

    // TODO all closed shapes needs to be rendered as lines - convert from drawRect to drawLine invocations for drawing background and borders.
    @Throws(IOException::class)
    fun drawRoundedRectangle(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float
    ) {
        with(getCurrentContentStream()) {
            saveGraphicsState()
            val b = y - height
            val right = x + width
            val top = y
            moveTo(x + radius, y)
            lineTo(right - radius, y)
            curveTo(right, y, right, y, right, y - radius)
            lineTo(right, b + radius)
            curveTo(right, b, right, b, right - radius, b)
            lineTo(x + radius, b)
            curveTo(x, b, x, b, x, b + radius)
            lineTo(x, y - radius)
            curveTo(x, y, x, y, x + radius, y)
            closePath()
            stroke()
            restoreGraphicsState()
        }
    }

    fun RenderableBoundingBox.drawRect(color: Color? = null) {
        drawRect(absoluteX.value, absoluteY.value, width?.value ?: 0f, height?.value ?: 0f,color)
    }

    fun drawRect(x: Float, y: Float, width: Float, height: Float,color: Color? = null) {
        with(getCurrentContentStream()) {
            saveGraphicsState()
            setStrokingColor(color.awtColorOrDefault())
            addRect(x, y, width, height)
            stroke()
            restoreGraphicsState()
        }
    }

    fun fillPolygon(
        x: Float,
        y: Float,
        x2: Float,
        y2: Float,
        x3: Float,
        y3: Float,
        x4: Float,
        y4: Float,
        color: Color? = null,
    ) {
        with(getCurrentContentStream()) {
            saveGraphicsState()
            setNonStrokingColor(color.awtColorOrDefault())
            moveTo(x, y)
            lineTo(x2, y2)
            lineTo(x3, y3)
            lineTo(x4, y4)
            lineTo(x, y)
            fill()
            restoreGraphicsState()
        }
    }

    //TODO put it into shared attribute map for caching at first boxLayout access.
    fun <A : AttributedEntity> boxLayout(context: A, borders: Borders?): BoxLayout =
        context.boundingBox()?.let { bbox ->
            BoxLayout(this, bbox, borders)
        } ?: error("Engine must provide bbox")

    fun closeContents() {
        if (this@PdfBoxRenderingContext::pageContentStream.isInitialized) pageContentStream.close()
    }

    fun getCurrentContentStream(): PDPageContentStream = pageContentStream

    fun getCurrentPage(): PDPage = currentPage

    private fun PDPage.createContent(): PDPageContentStream = PDPageContentStream(document, this)

    fun Float.intoPdfBoxOrigin(): Float = currentPage.mediaBox.height - this

    override fun getWidth(): Width = if (this@PdfBoxRenderingContext::currentPage.isInitialized) {
        Width(currentPage.mediaBox.width, UnitsOfMeasure.PT)
    } else Width(PDRectangle.LETTER.width, UnitsOfMeasure.PT)


    override fun getHeight(): Height = if (this@PdfBoxRenderingContext::currentPage.isInitialized) {
        Height(currentPage.mediaBox.height, UnitsOfMeasure.PT)
    } else Height(PDRectangle.LETTER.height, UnitsOfMeasure.PT)

}

/**
 * BoxLayout moves absolute layout coordinates from model origin into PDFBox library origins which are upside-down.
 * In PDFBox page Y coordinates starts from bottom of the screen and are growing to top of the screen.
 * When drawing bounding boxes, x1 and y1 coordinates are forming bottom left corner not top left one.
 * @author Wojciech Mąka
 * @since 0.*.0
 */
data class BoxLayout(
    private val context: PdfBoxRenderingContext,
    val source: RenderableBoundingBox,
    val borders: Borders?,
) {
    private val pageHeight = Height(context.getCurrentPage().mediaBox.height, UnitsOfMeasure.PT)
    val outer: RenderableBoundingBox by lazy {
        // move to PDFBox origin. X,Y is bottomLeft corner not topLeft one.
        source.copy(absoluteY = pageHeight.asY() - (source.absoluteY.value.plus(source.height!!.value)))
    }
    val inner: RenderableBoundingBox by lazy {
        computeContentBoundingBox()
    }
    val innerX: Float = inner.absoluteX.value
    val innerY: Float = inner.absoluteY.value
    val outerX: Float = outer.absoluteX.value
    val outerY: Float = outer.absoluteY.value

    val leftBorderHalfWidth = borders?.let{ (it.leftBorderWidth.value / 2).round1()} ?: 0F
    val topBorderHalfHeight = borders?.let{ (it.topBorderHeight.value / 2).round1()} ?: 0F
    val rightBorderHalfWidth = borders?.let{ (it.rightBorderWidth.value / 2).round1()} ?: 0F
    val bottomBorderHalfHeight = borders?.let{ (it.bottomBorderHeight.value / 2).round1()} ?: 0F

    val leftTopCornerHalfWidth = borders?.let{ (it.leftTopBorderCornerWidth.value / 2).round1()} ?: 0F
    val rightTopCornerHalfWidth = borders?.let{ (it.rightTopBorderCornerWidth.value / 2).round1()} ?: 0F
    val rightBottomCornerHalfWidth = borders?.let{ (it.rightBottomBorderCornerWidth.value / 2).round1()} ?: 0F
    val leftBottomCornerHalfWidth = borders?.let{ (it.leftBottomBorderCornerWidth.value / 2).round1()} ?: 0F

    val leftTopCornerRadiusOrZero = borders?.let{ it.leftTopBorderCornerRadius.value } ?: 0F
    val rightTopCornerRadiusOrZero = borders?.let{ it.rightTopBorderCornerRadius.value } ?: 0F
    val rightBottomCornerRadiusOrZero = borders?.let{ it.rightBottomBorderCornerRadius.value } ?: 0F
    val leftBottomCornerRadiusOrZero = borders?.let{ it.leftBottomBorderCornerRadius.value } ?: 0F

    val outerLeftTopX: Float = outerX
    val outerLeftTopY: Float = outerY + (outer.height?.value ?: 0F)

    val outerRightTopX: Float = outerX + (outer.width?.value ?: 0F)
    val outerRightTopY: Float = outerY + (outer.height?.value ?: 0F)

    val outerLeftBottomX: Float = outerX
    val outerLeftBottomY: Float = outerY

    val outerRightBottomX: Float = outerX + (outer.width?.value ?: 0F)
    val outerRightBottomY: Float = outerY


    private fun computeContentBoundingBox(): RenderableBoundingBox =
        if (borders != null) {
            val left = if (borders.leftBorderStyle.hasBorder()) {
                (borders.leftBorderWidth.switchUnitOfMeasure(UnitsOfMeasure.PT).value)
            } else 0F
            val top = if (borders.topBorderStyle.hasBorder()) {
                (borders.topBorderHeight.switchUnitOfMeasure(UnitsOfMeasure.PT).value)
            } else 0F
            val right = if (borders.rightBorderStyle.hasBorder()) {
                (borders.rightBorderWidth.switchUnitOfMeasure(UnitsOfMeasure.PT).value)
            } else 0F
            val bottom = if (borders.bottomBorderStyle.hasBorder()) {
                (borders.bottomBorderHeight.switchUnitOfMeasure(UnitsOfMeasure.PT).value)
            } else 0F
            if (left != 0F || right != 0F || top != 0F || bottom != 0F) {
                outer.copy(
                    absoluteX = outer.absoluteX + X(left, UnitsOfMeasure.PT),
                    absoluteY = outer.absoluteY + Y(bottom, UnitsOfMeasure.PT),
                    width = Width(outer.width?.value?.minus(left + right) ?: 0F, UnitsOfMeasure.PT),
                    height = Height(outer.height?.value?.minus(top + bottom) ?: 0F, UnitsOfMeasure.PT)
                )
            } else outer
        } else outer

}

@JvmInline
value class TextPosition(val xPositionedText: Pair<Float, String>)

fun Color?.awtColorOrDefault(): AwtColor =
    this?.let { AwtColor(it.r, it.g, it.b) } ?: AwtColor.BLACK
