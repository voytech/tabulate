package io.github.voytech.tabulate.pdf

import io.github.voytech.tabulate.core.model.Height
import io.github.voytech.tabulate.core.model.Size
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.core.model.alignment.DefaultHorizontalAlignment
import io.github.voytech.tabulate.core.model.alignment.DefaultVerticalAlignment
import io.github.voytech.tabulate.core.model.alignment.HorizontalAlignment
import io.github.voytech.tabulate.core.model.alignment.VerticalAlignment
import io.github.voytech.tabulate.core.model.attributes.AlignmentAttribute
import io.github.voytech.tabulate.core.model.attributes.BackgroundAttribute
import io.github.voytech.tabulate.core.model.attributes.BordersAttribute
import io.github.voytech.tabulate.core.model.attributes.TextStylesAttribute
import io.github.voytech.tabulate.core.model.border.BorderStyle
import io.github.voytech.tabulate.core.model.border.Borders
import io.github.voytech.tabulate.core.model.border.DefaultBorderStyle
import io.github.voytech.tabulate.core.model.color.Color
import io.github.voytech.tabulate.core.model.color.darken
import io.github.voytech.tabulate.core.model.text.DefaultWeightStyle
import io.github.voytech.tabulate.core.template.layout.LayoutElementBoundingBox
import io.github.voytech.tabulate.core.template.operation.*
import org.apache.pdfbox.pdmodel.PDPageContentStream
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

class AlignmentAttributeRenderOperation<CTX> :
    AttributeOperation<PdfBoxRenderingContext, AlignmentAttribute, CTX> where CTX : AttributedContext, CTX : HasValue<*> {


    private fun PdfBoxRenderingContext.applyTextAlignment(
        context: CTX,
        vertical: VerticalAlignment? = DefaultVerticalAlignment.MIDDLE,
        horizontal: HorizontalAlignment? = DefaultHorizontalAlignment.CENTER,
    ) {
        val bbox = boxLayout(context, context.getModelAttribute<BordersAttribute>())
        val fontAndSize = context.fontSize()
        var xOffset = 0.0F
        var yOffset = 0.0F
        if (vertical != null) {
            val textHeight = fontAndSize.measureTextHeight()
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
            val textWidth = fontAndSize.measureTextWidth(context.value.toString())
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

class TextStylesAttributeRenderOperation<CTX : AttributedContext> :
    AttributeOperation<PdfBoxRenderingContext, TextStylesAttribute, CTX> {

    override operator fun invoke(
        renderingContext: PdfBoxRenderingContext,
        context: CTX,
        attribute: TextStylesAttribute,
    ) = with(renderingContext) {
        getCurrentContentStream().let { content ->
            beginText()
            context.fontSize().let { fontAndSize ->
                setFont(fontAndSize.font(), fontAndSize.size().toFloat())
                content.setNonStrokingColor(attribute.fontColor.awtColor())
                val ident: Int = attribute.ident?.toInt() ?: 0
                val identWidth =
                    if (ident > 0) fontAndSize.measureTextWidth((1..ident).fold("") { agg, _ -> "$agg " }) else 0F
                xTextOffset += identWidth
            }
        }
    }
}

class BackgroundAttributeRenderOperation<CTX : AttributedContext> :
    AttributeOperation<PdfBoxRenderingContext, BackgroundAttribute, CTX> {
    override fun invoke(
        renderingContext: PdfBoxRenderingContext,
        context: CTX,
        attribute: BackgroundAttribute,
    ): Unit = with(renderingContext) {
        val bbox = boxLayout(context, context.getModelAttribute<BordersAttribute>())
        bbox.outer.drawRect(attribute.color)
    }
}

fun BorderStyle?.leftTopPrimaryColor(original: Color?): Color? =
    when (this?.getBorderStyleId()) {
        DefaultBorderStyle.INSET.name,
        DefaultBorderStyle.GROOVE.name,
        -> original?.darken(1.2F)

        else -> original
    }

fun BorderStyle?.rightBottomPrimaryColor(original: Color?): Color? =
    when (this?.getBorderStyleId()) {
        DefaultBorderStyle.OUTSET.name -> original?.darken(1.2F)
        else -> original
    }

fun BorderStyle?.leftTopSecondaryColor(original: Color?): Color? =
    when (this?.getBorderStyleId()) {
        DefaultBorderStyle.INSET.name -> original?.darken(1.2F)
        else -> original
    }

fun BorderStyle?.rightBottomSecondaryColor(original: Color?): Color? =
    when (this?.getBorderStyleId()) {
        DefaultBorderStyle.OUTSET.name,
        DefaultBorderStyle.GROOVE.name,
        -> original?.darken(1.2F)

        else -> original
    }

fun <A : AttributedContext> PdfBoxRenderingContext.drawBorders(context: A, borders: Borders) {
    topBorder(
        context, borders,
        borders.topBorderStyle?.leftTopPrimaryColor(borders.topBorderColor),
        borders.topBorderStyle?.leftTopSecondaryColor(borders.topBorderColor),
    )
    leftBorder(
        context, borders,
        borders.leftBorderStyle?.leftTopPrimaryColor(borders.leftBorderColor),
        borders.leftBorderStyle?.leftTopSecondaryColor(borders.leftBorderColor),
    )
    rightBorder(
        context, borders,
        borders.rightBorderStyle?.rightBottomPrimaryColor(borders.rightBorderColor),
        borders.rightBorderStyle?.rightBottomSecondaryColor(borders.rightBorderColor),
    )
    bottomBorder(
        context, borders,
        borders.bottomBorderStyle?.rightBottomPrimaryColor(borders.bottomBorderColor),
        borders.bottomBorderStyle?.rightBottomSecondaryColor(borders.bottomBorderColor),
    )
}

class BordersAttributeRenderOperation<CTX : AttributedContext> :
    AttributeOperation<PdfBoxRenderingContext, BordersAttribute, CTX> {
    override fun invoke(
        renderingContext: PdfBoxRenderingContext,
        context: CTX,
        attribute: BordersAttribute,
    ): Unit = with(renderingContext) {
        drawBorders(context, attribute)
    }
}

typealias LineDashPattern = Pair<FloatArray, Float>

private fun BorderStyle.resolveLineDashPattern(): LineDashPattern =
    when (getBorderStyleId()) {
        DefaultBorderStyle.DASHED.name -> floatArrayOf(3.0F) to 0F
        DefaultBorderStyle.DOTTED.name -> floatArrayOf(1.0F) to 0F
        else -> floatArrayOf() to 0F
    }

private fun BorderStyle.applyOn(contentStream: PDPageContentStream) {
    if (getBorderStyleId() in arrayOf(
            DefaultBorderStyle.DASHED.name,
            DefaultBorderStyle.DOTTED.name,
            DefaultBorderStyle.SOLID.name
        )
    ) {
        resolveLineDashPattern().let {
            contentStream.setLineDashPattern(it.first, it.second)
        }
    }
}

private fun PdfBoxRenderingContext.drawLine(
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
) {
    with(getCurrentContentStream()) {
        moveTo(x1, y1)
        lineTo(x2, y2)
        stroke()
        restoreGraphicsState()
    }
}

private fun PdfBoxRenderingContext.setBorderStyle(style: BorderStyle?, color: Color?, width: Width) {
    with(getCurrentContentStream()) {
        saveGraphicsState()
        setStrokingColor(color.awtColor())
        style?.applyOn(this)
        setLineWidth(width.switchUnitOfMeasure(UnitsOfMeasure.PT).value)
    }
}

private fun BorderStyle?.subLineWidth(width: Width): Float =
    if (this != null && hasBorder()) {
        when (getBorderStyleId()) {
            DefaultBorderStyle.DOUBLE.name -> width.value / 3
            DefaultBorderStyle.INSET.name,
            DefaultBorderStyle.OUTSET.name,
            DefaultBorderStyle.GROOVE.name,
            -> width.value / 2

            else -> width.value
        }
    } else 0F

typealias SubLineWidthAndOffset = Pair<Float, Float>

private fun BorderStyle?.subLine(width: Width): SubLineWidthAndOffset =
    if (this != null && hasBorder()) {
        when (getBorderStyleId()) {
            DefaultBorderStyle.DOUBLE.name -> (width.value / 3).let { it to (it * 2) }
            DefaultBorderStyle.INSET.name,
            DefaultBorderStyle.OUTSET.name,
            DefaultBorderStyle.GROOVE.name,
            -> (width.value / 2).let { it to it }

            else -> width.value to 0F
        }
    } else 0F to 0F

private fun <A : AttributedContext> PdfBoxRenderingContext.topCompositeBorder(
    boxLayout: BoxLayout,
    context: A,
    borders: Borders,
    primaryColor: Color? = null,
    secondaryColor: Color? = null,
) {
    val (effectiveWidth, effectiveOffset) = borders.topBorderStyle.subLine(borders.topBorderWidth)
    val minLeftBorderWidth = borders.leftBorderStyle.subLineWidth(borders.leftBorderWidth)
    val minRightBorderWidth = borders.rightBorderStyle.subLineWidth(borders.rightBorderWidth)

    val x1 = boxLayout.outerX
    val y1 = boxLayout.outerY + (boxLayout.outer.height?.value ?: 0f)
    val x2 = boxLayout.outerX + (boxLayout.outer.width?.value ?: 0f)
    //drawLine(x1, y1, x2, y1)
    fillPolygon(
        x1, y1, x2, y1,
        x2 - minRightBorderWidth, y1 - effectiveWidth,
        x1 + minLeftBorderWidth, y1 - effectiveWidth,
        primaryColor ?: borders.topBorderColor
    )

    val lx1 = if (borders.leftBorderStyle.hasBorder()) (boxLayout.innerX - minLeftBorderWidth) else boxLayout.outerX
    val ly1 = y1 - effectiveOffset
    val lx2 = lx1 + if (borders.rightBorderStyle.hasBorder()) (boxLayout.inner.width?.value ?: 0f) +
            (minLeftBorderWidth + minRightBorderWidth) else (boxLayout.inner.width?.value ?: 0f)

    fillPolygon(
        lx1, ly1, lx2, ly1,
        lx2 - minRightBorderWidth, ly1 - effectiveWidth,
        lx1 + minLeftBorderWidth, ly1 - effectiveWidth,
        secondaryColor ?: borders.topBorderColor
    )
}

private fun <A : AttributedContext> PdfBoxRenderingContext.topBorder(
    context: A,
    borders: Borders,
    primaryColor: Color? = null,
    secondaryColor: Color? = null,
) {
    if (borders.topBorderStyle.hasBorder()) {
        boxLayout(context, borders).let {
            if (borders.topBorderStyle.hasComplexBorder()) {
                topCompositeBorder(it, context, borders, primaryColor, secondaryColor)
            } else {
                setBorderStyle(borders.topBorderStyle, borders.topBorderColor, borders.topBorderWidth)
                val x1 = it.outerX
                val y1 = it.outerY + (it.outer.height?.value ?: 0f) - borders.topBorderWidth.value / 2
                val x2 = it.outerX + (it.outer.width?.value ?: 0f)
                drawLine(x1, y1, x2, y1)
            }
        }
    }
}

private fun <A : AttributedContext> PdfBoxRenderingContext.bottomCompositeBorder(
    boxLayout: BoxLayout,
    context: A,
    borders: Borders,
    primaryColor: Color? = null,
    secondaryColor: Color? = null,
) {
    val (effectiveWidth, effectiveOffset) = borders.bottomBorderStyle.subLine(borders.bottomBorderWidth)
    val minLeftBorderWidth = borders.leftBorderStyle.subLineWidth(borders.leftBorderWidth)
    val minRightBorderWidth = borders.rightBorderStyle.subLineWidth(borders.rightBorderWidth)

    val x1 = boxLayout.outerX
    val y1 = boxLayout.outerY //+ effectiveWidth / 2
    val x2 = x1 + (boxLayout.outer.width?.value ?: 0f)
    fillPolygon(
        x1, y1, x2, y1,
        x2 - minRightBorderWidth, y1 + effectiveWidth,
        x1 + minLeftBorderWidth, y1 + effectiveWidth,
        primaryColor ?: borders.bottomBorderColor
    )

    val lx1 = if (borders.leftBorderStyle.hasBorder()) (boxLayout.innerX - minLeftBorderWidth) else boxLayout.outerX
    val ly1 = y1 + effectiveOffset
    val lx2 = lx1 + if (borders.rightBorderStyle.hasBorder()) (boxLayout.inner.width?.value ?: 0f) +
            (minLeftBorderWidth + minRightBorderWidth) else (boxLayout.inner.width?.value ?: 0f)

    fillPolygon(
        lx1, ly1, lx2, ly1,
        lx2 - minRightBorderWidth, ly1 + effectiveWidth,
        lx1 + minLeftBorderWidth, ly1 + effectiveWidth,
        secondaryColor ?: borders.bottomBorderColor
    )
}

private fun <A : AttributedContext> PdfBoxRenderingContext.bottomBorder(
    context: A,
    borders: Borders,
    primaryColor: Color? = null,
    secondaryColor: Color? = null,
) {
    if (borders.bottomBorderStyle.hasBorder()) {
        boxLayout(context, borders).let {
            if (borders.bottomBorderStyle.hasComplexBorder()) {
                bottomCompositeBorder(it, context, borders, primaryColor, secondaryColor)
            } else {
                setBorderStyle(borders.bottomBorderStyle, borders.bottomBorderColor, borders.bottomBorderWidth)
                val x1 = it.outerX
                val y1 = it.outerY + borders.bottomBorderWidth.value / 2
                val x2 = x1 + (it.outer.width?.value ?: 0f)
                drawLine(x1, y1, x2, y1)
            }
        }
    }
}

private fun PdfBoxRenderingContext.leftCompositeBorder(
    boxLayout: BoxLayout,
    borders: Borders,
    primaryColor: Color? = null,
    secondaryColor: Color? = null,
) {
    val (effectiveWidth, effectiveOffset) = borders.leftBorderStyle.subLine(borders.leftBorderWidth)
    val minTopBorderWidth = borders.topBorderStyle.subLineWidth(borders.topBorderWidth)
    val minBottomBorderWidth = borders.bottomBorderStyle.subLineWidth(borders.bottomBorderWidth)

    val x1 = boxLayout.outerX
    val y1 = boxLayout.outerY
    val y2 = y1 + (boxLayout.outer.height?.value ?: 0F)
    fillPolygon(
        x1, y1, x1, y2,
        x1 + effectiveWidth, y2 - minTopBorderWidth,
        x1 + effectiveWidth, y1 + minBottomBorderWidth,
        primaryColor ?: borders.leftBorderColor
    )

    val lx1 = x1 + effectiveOffset
    val ly1 = if (borders.bottomBorderStyle.hasBorder()) boxLayout.innerY - minBottomBorderWidth else boxLayout.outerY
    val ly2 = ly1 + if (borders.topBorderStyle.hasBorder()) (boxLayout.inner.height?.value
        ?: 0F) + minBottomBorderWidth + minTopBorderWidth
    else boxLayout.inner.height?.value ?: 0F

    fillPolygon(
        lx1, ly1, lx1, ly2,
        lx1 + effectiveWidth, ly2 - minTopBorderWidth,
        lx1 + effectiveWidth, ly1 + minBottomBorderWidth,
        secondaryColor ?: borders.leftBorderColor
    )
}

private fun <A : AttributedContext> PdfBoxRenderingContext.leftBorder(
    context: A,
    borders: Borders,
    primaryColor: Color? = null,
    secondaryColor: Color? = null,
) {
    if (borders.leftBorderStyle.hasBorder()) {
        boxLayout(context, borders).let {
            if (borders.leftBorderStyle.hasComplexBorder()) {
                leftCompositeBorder(it, borders, primaryColor, secondaryColor)
            } else {
                setBorderStyle(borders.leftBorderStyle, borders.leftBorderColor, borders.leftBorderWidth)
                val x1 = it.outerX + borders.leftBorderWidth.value / 2
                val y1 = it.outerY
                val y2 = y1 + (it.outer.height?.value ?: 0F)
                drawLine(x1, y1, x1, y2)
            }
        }
    }
}

private fun PdfBoxRenderingContext.rightCompositeBorder(
    boxLayout: BoxLayout,
    borders: Borders,
    primaryColor: Color? = null,
    secondaryColor: Color? = null,
) {
    val (effectiveWidth, effectiveOffset) = borders.rightBorderStyle.subLine(borders.rightBorderWidth)
    val minTopBorderWidth = borders.topBorderStyle.subLineWidth(borders.topBorderWidth)
    val minBottomBorderWidth = borders.bottomBorderStyle.subLineWidth(borders.bottomBorderWidth)

    val x1 = boxLayout.outerX + (boxLayout.outer.width?.value ?: 0f) //- effectiveWidth / 2
    val y1 = boxLayout.outerY
    val y2 = y1 + (boxLayout.outer.height?.value ?: 0F)
    fillPolygon(
        x1, y1, x1, y2,
        x1 - effectiveWidth, y2 - minTopBorderWidth,
        x1 - effectiveWidth, y1 + minBottomBorderWidth,
        primaryColor ?: borders.leftBorderColor
    )

    val lx1 = x1 - effectiveOffset
    val ly1 = if (borders.bottomBorderStyle.hasBorder()) boxLayout.innerY - minBottomBorderWidth else boxLayout.outerY
    val ly2 = ly1 + if (borders.topBorderStyle.hasBorder()) (boxLayout.inner.height?.value
        ?: 0F) + minBottomBorderWidth + minTopBorderWidth else boxLayout.inner.height?.value ?: 0F

    fillPolygon(
        lx1, ly1, lx1, ly2,
        lx1 - effectiveWidth, ly2 - minTopBorderWidth,
        lx1 - effectiveWidth, ly1 + minBottomBorderWidth,
        secondaryColor ?: borders.leftBorderColor
    )
}

private fun <A : AttributedContext> PdfBoxRenderingContext.rightBorder(
    context: A,
    borders: Borders,
    primaryColor: Color? = null,
    secondaryColor: Color? = null,
) {
    if (borders.rightBorderStyle.hasBorder()) {
        boxLayout(context, borders).let {
            if (borders.rightBorderStyle.hasComplexBorder()) {
                rightCompositeBorder(it, borders, primaryColor, secondaryColor)
            } else {
                setBorderStyle(borders.rightBorderStyle, borders.rightBorderColor, borders.rightBorderWidth)
                val x1 = it.outerX + (it.outer.width?.value ?: 0f) - borders.rightBorderWidth.value / 2
                val y1 = it.outerY
                val y2 = y1 + (it.outer.height?.value ?: 0F)
                drawLine(x1, y1, x1, y2)
            }
        }
    }
}

fun BorderStyle?.hasBorder(): Boolean = this != null && getBorderStyleId() != DefaultBorderStyle.NONE.name

fun BorderStyle?.hasComplexBorder(): Boolean = this != null && (
        getBorderStyleId() == DefaultBorderStyle.DOUBLE.name ||
                getBorderStyleId() == DefaultBorderStyle.INSET.name ||
                getBorderStyleId() == DefaultBorderStyle.OUTSET.name ||
                getBorderStyleId() == DefaultBorderStyle.GROOVE.name
        )

fun <A, V> A.resolveTextBoundingBox(): LayoutElementBoundingBox? where A : AttributedContext, A : HasValue<V> {
    return boundingBox()?.apply {
        if (!isDefined()) {
            (this@resolveTextBoundingBox.measureText(unitsOfMeasure()) +
                    this@resolveTextBoundingBox.measureBorders(unitsOfMeasure())).let { measured ->
                height = height ?: measured.height
                width = width ?: measured.width
            }
        }
    }
}


@JvmInline
value class FontMeasurements(private val inner: Pair<PDFont, Int>) {

    fun font(): PDFont = inner.first

    fun size(): Int = inner.second

    private fun capHeight(): Float = font().fontDescriptor.capHeight

    private fun descent(): Float = font().fontDescriptor.descent * -1

    fun descender(): Float = descent() / 1000 * size()

    fun measureTextHeight(): Float =
        (capHeight() + (2 * descent())) / 1000 * size()

    fun measureTextWidth(string: String): Float =
        width(string) / 1000 * size()

    private fun width(string: String): Float = font().getStringWidth(string)

}

fun <A : AttributedContext> A.fontSize(): FontMeasurements =
    getModelAttribute<TextStylesAttribute>().let {
        FontMeasurements((it?.pdFont() ?: PDType1Font.HELVETICA) to (it?.fontSize ?: 10))
    }

fun <A, V> A.measureText(uom: UnitsOfMeasure): Size where A : AttributedContext, A : HasValue<V> =
    fontSize().let {
        Size(
            Width(it.measureTextWidth(this.value.toString()), UnitsOfMeasure.PT),
            Height(it.measureTextHeight(), UnitsOfMeasure.PT) // ???
        )
    }

fun <A : AttributedContext> A.measureBorders(uom: UnitsOfMeasure): Size =
    getModelAttribute<BordersAttribute>()?.let { borders ->
        Size(
            (borders.leftBorderWidth + borders.rightBorderWidth).switchUnitOfMeasure(uom),
            Height((borders.bottomBorderWidth + borders.topBorderWidth).switchUnitOfMeasure(uom).value, uom)
        )
    } ?: Size(Width.zero(uom), Height.zero(uom))
