package io.github.voytech.tabulate.pdf

import io.github.voytech.tabulate.ImageIndex
import io.github.voytech.tabulate.core.model.attributes.BordersAttribute
import io.github.voytech.tabulate.core.model.border.Borders
import io.github.voytech.tabulate.core.model.color.Color
import io.github.voytech.tabulate.core.HavingViewportSize
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.operation.AttributedContext
import io.github.voytech.tabulate.core.operation.Renderable
import io.github.voytech.tabulate.core.operation.boundingBox
import io.github.voytech.tabulate.core.result.OutputBinding
import io.github.voytech.tabulate.core.result.OutputStreamOutputBinding
import io.github.voytech.tabulate.core.spi.DocumentFormat
import io.github.voytech.tabulate.core.spi.OutputBindingsProvider
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
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
    var xTextOffset: Float = 0F
    var yTextOffset: Float = 0F

    fun addPage() = PDPage().apply {
        if (this@PdfBoxRenderingContext::pageContentStream.isInitialized) pageContentStream.close()
        document.addPage(this)
        pageContentStream = createContent()
        currentPage = this
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
            getCurrentContentStream().setFont(defaultFont, 10F)
        }
        getCurrentContentStream().showText(text)
    }

    fun endText() {
        textDrawing = false
        fontSet = false
        textPositionSet = false
        xTextOffset = 0F
        yTextOffset = 0F
        getCurrentContentStream().setStrokingColor(AwtColor.BLACK)
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

    fun Renderable<*>.renderImageFromURI(uri: String) {
        with(boxLayout(this, getModelAttribute<BordersAttribute>())) {
            loadImage(uri).showImage(innerX, innerY, inner.width?.value, inner.height?.value)
        }
    }

    fun Renderable<*>.resolveUriImageBoundingBox(uri: String) {
        boundingBox()?.let { bbox ->
            if (!bbox.isDefined()) {
                val image = loadImage(uri)
                bbox.apply {
                    height = height ?: Height(image.height.toFloat(), UnitsOfMeasure.PT)
                    width = width ?: Width(image.width.toFloat(), UnitsOfMeasure.PT)
                }
            }
        }
    }

    fun <R> Renderable<*>.withinRect(type: BoxLayoutBboxType, action: (RenderableBoundingBox) -> R): R {
        val bbox = boxLayout(this, getModelAttribute<BordersAttribute>())
        return (bbox.inner.takeIf { type == BoxLayoutBboxType.INNER } ?: bbox.outer).let { rect ->
            with(getCurrentContentStream()) {
                action(rect)
            }
        }
    }

    fun RenderableBoundingBox.setCuttingEdge() {
        getCurrentContentStream().addRect(
            absoluteX.value, absoluteY.value, width?.value ?: 0f, height?.value ?: 0f
        )
        getCurrentContentStream().clip()
    }

    fun RenderableBoundingBox.drawRect(color: Color? = null) {
        with(getCurrentContentStream()) {
            saveGraphicsState()
            setNonStrokingColor(color.awtColor())
            addRect(absoluteX.value, absoluteY.value, width?.value ?: 0f, height?.value ?: 0f)
            fill()
            restoreGraphicsState()
        }
    }

    fun drawRect(x: Float, y: Float, width: Float, height: Float, color: Color? = null) {
        with(getCurrentContentStream()) {
            saveGraphicsState()
            setNonStrokingColor(color.awtColor())
            addRect(x, y, width, height)
            fill()
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
            setNonStrokingColor(color.awtColor())
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
    fun <A : AttributedContext> boxLayout(context: A, borders: Borders?): BoxLayout =
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

    fun Position.pdfBoxOrigin(): Position {
        require(y.unit == UnitsOfMeasure.PT)
        return copy(y = Y(y.value.intoPdfBoxOrigin(), y.unit))
    }

    override fun getWidth(): Width = if (this@PdfBoxRenderingContext::currentPage.isInitialized) {
        Width(currentPage.mediaBox.width, UnitsOfMeasure.PT)
    } else Width(PDRectangle.LETTER.width, UnitsOfMeasure.PT)


    override fun getHeight(): Height = if (this@PdfBoxRenderingContext::currentPage.isInitialized) {
        Height(currentPage.mediaBox.height, UnitsOfMeasure.PT)
    } else Height(PDRectangle.LETTER.height, UnitsOfMeasure.PT)

}

enum class BoxLayoutBboxType {
    INNER,
    OUTER;
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
    private val borders: Borders?,
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

    private fun computeContentBoundingBox(): RenderableBoundingBox =
        if (borders != null) {
            val left = if (borders.leftBorderStyle.hasBorder()) {
                (borders.leftBorderWidth.switchUnitOfMeasure(UnitsOfMeasure.PT).value)
            } else 0F
            val top = if (borders.topBorderStyle.hasBorder()) {
                (borders.topBorderWidth.switchUnitOfMeasure(UnitsOfMeasure.PT).value)
            } else 0F
            val right = if (borders.rightBorderStyle.hasBorder()) {
                (borders.rightBorderWidth.switchUnitOfMeasure(UnitsOfMeasure.PT).value)
            } else 0F
            val bottom = if (borders.bottomBorderStyle.hasBorder()) {
                (borders.bottomBorderWidth.switchUnitOfMeasure(UnitsOfMeasure.PT).value)
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

fun Color?.awtColor(): AwtColor =
    this?.let { AwtColor(it.r, it.g, it.b) } ?: AwtColor.BLACK
