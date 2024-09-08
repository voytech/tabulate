package io.github.voytech.tabulate.pdf

import io.github.voytech.tabulate.core.model.Measure
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.attributes.BackgroundAttribute
import io.github.voytech.tabulate.core.model.attributes.BordersAttribute
import io.github.voytech.tabulate.core.model.attributes.TextStylesAttribute
import io.github.voytech.tabulate.core.model.border.BorderStyle
import io.github.voytech.tabulate.core.model.border.Borders
import io.github.voytech.tabulate.core.model.border.DefaultBorderStyle
import io.github.voytech.tabulate.core.model.color.Color
import io.github.voytech.tabulate.core.model.color.darken
import io.github.voytech.tabulate.core.model.text.DefaultWeightStyle
import io.github.voytech.tabulate.core.operation.AttributeOperation
import io.github.voytech.tabulate.core.operation.AttributedEntity
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts

val defaultFont: PDFont = PDType1Font(Standard14Fonts.FontName.HELVETICA)
const val defaultFontSize: Int = 10

fun TextStylesAttribute.default(): PDFont =
    (if (weight == DefaultWeightStyle.BOLD) {
        Standard14Fonts.FontName.HELVETICA_BOLD_OBLIQUE.takeIf { italic == true }
            ?: Standard14Fonts.FontName.HELVETICA_BOLD
    } else if (italic == true) {
        Standard14Fonts.FontName.HELVETICA_OBLIQUE
    } else {
        Standard14Fonts.FontName.HELVETICA
    }).let { PDType1Font(it) }

fun TextStylesAttribute.pdFont(): PDFont =
    if (fontFamily != null) {
        when (fontFamily!!.getFontId()) {
            "TIMES_NEW_ROMAN", "TIMES_ROMAN" -> {
                (if (weight == DefaultWeightStyle.BOLD) {
                    Standard14Fonts.FontName.TIMES_BOLD_ITALIC.takeIf { italic == true }
                        ?: Standard14Fonts.FontName.TIMES_BOLD
                } else if (italic == true) {
                    Standard14Fonts.FontName.TIMES_ITALIC
                } else {
                    Standard14Fonts.FontName.TIMES_ROMAN
                }).let { PDType1Font(it) }
            }

            "COURIER", "COURIER_NEW" -> {
                (if (weight == DefaultWeightStyle.BOLD) {
                    Standard14Fonts.FontName.COURIER_BOLD_OBLIQUE.takeIf { italic == true }
                        ?: Standard14Fonts.FontName.COURIER_BOLD
                } else if (italic == true) {
                    Standard14Fonts.FontName.COURIER_OBLIQUE
                } else {
                    Standard14Fonts.FontName.COURIER
                }).let { PDType1Font(it) }
            }

            "HELVETICA" -> default()
            else -> default()
        }
    } else default()

class BackgroundAttributeRenderOperation<CTX : AttributedEntity> :
    AttributeOperation<PdfBoxRenderingContext, BackgroundAttribute, CTX> {
    override fun invoke(
        renderingContext: PdfBoxRenderingContext,
        context: CTX,
        attribute: BackgroundAttribute,
    ): Unit = with(renderingContext) {
        renderClipped(context) {
            val bbox = boxLayout(context, context.getModelAttribute<BordersAttribute>())
            bbox.fillRect(attribute.color)
        }
    }
}

class BordersAttributeRenderOperation<CTX : AttributedEntity> :
    AttributeOperation<PdfBoxRenderingContext, BordersAttribute, CTX> {
    override fun invoke(
        renderingContext: PdfBoxRenderingContext,
        context: CTX,
        attribute: BordersAttribute,
    ): Unit = with(renderingContext) {
        renderClipped(context) {
            drawBorders(context, attribute)
        }
    }
}


fun <A : AttributedEntity> PdfBoxRenderingContext.drawBorders(context: A, borders: Borders) {
    boxLayout(context, borders).let { box ->
        getCurrentContentStream().saveGraphicsState()
        val leftPrimaryColor = borders.leftBorderStyle.leftTopPrimaryColor(borders.leftBorderColor)
        val leftSecondaryColor = borders.leftBorderStyle.leftTopSecondaryColor(borders.leftBorderColor)
        leftBorder(box, borders, leftPrimaryColor, leftSecondaryColor)
        leftTopRoundCorner(box, borders, true)
        leftBottomRoundCorner(box, borders, true)

        val topPrimaryColor = borders.topBorderStyle.leftTopPrimaryColor(borders.topBorderColor)
        val topSecondaryColor = borders.topBorderStyle.leftTopSecondaryColor(borders.topBorderColor)
        topBorder(box, borders, topPrimaryColor, topSecondaryColor)
        leftTopRoundCorner(box, borders, false)
        rightTopRoundCorner(box, borders, false)

        val rightPrimaryColor = borders.rightBorderStyle.rightBottomPrimaryColor(borders.rightBorderColor)
        val rightSecondaryColor = borders.rightBorderStyle.rightBottomSecondaryColor(borders.rightBorderColor)
        // rightBorder(box, borders, rightPrimaryColor, rightSecondaryColor)
        rightTopRoundCorner(box, borders, true)
        rightBottomRoundCorner(box, borders, true)


        val bottomPrimaryColor = borders.bottomBorderStyle.rightBottomPrimaryColor(borders.bottomBorderColor)
        val bottomSecondaryColor = borders.bottomBorderStyle.rightBottomSecondaryColor(borders.bottomBorderColor)
        bottomBorder(box, borders, bottomPrimaryColor, bottomSecondaryColor)
        leftBottomRoundCorner(box, borders, false)
        rightBottomRoundCorner(box, borders, false)

        getCurrentContentStream().restoreGraphicsState()
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

fun PdfBoxRenderingContext.leftTopRoundCorner(
    box: BoxLayout,
    borders: Borders,
    isLeftBorderStyle: Boolean
) {
    val color = if (isLeftBorderStyle) borders.leftBorderColor else borders.topBorderColor
    val style = if (isLeftBorderStyle) borders.leftBorderStyle else borders.topBorderStyle
    val width = if (isLeftBorderStyle) borders.leftBorderWidth else borders.topBorderHeight
    val radius = borders.leftTopBorderCornerRadius
    val baseTop = box.outerLeftTopY + box.topBorderHalfThickness
    val baseLeft = box.outerLeftTopX - box.leftBorderHalfThickness
    pathClipped(
        baseLeft, baseTop - box.outerHalfHeight,
        baseLeft + box.outerHalfWidth, baseTop - box.outerHalfHeight,
        baseLeft + box.outerHalfWidth, baseTop,
        baseLeft, baseTop,
        baseLeft, baseTop - box.outerHalfHeight,
    ) {
        setCornerStyle(style, color, width)
        drawLeftTopCorner(
            box.outerLeftTopX,
            box.outerLeftTopY,
            radius.value
        )
    }
}

fun PdfBoxRenderingContext.rightTopRoundCorner(
    box: BoxLayout,
    borders: Borders,
    isRightBorderStyle: Boolean
) {
    val color = if (isRightBorderStyle) borders.rightBorderColor else borders.topBorderColor
    val style = if (isRightBorderStyle) borders.rightBorderStyle else borders.topBorderStyle
    val width = if (isRightBorderStyle) borders.rightBorderWidth else borders.topBorderHeight
    val radius = borders.rightTopBorderCornerRadius
    val baseTop = box.outerRightTopY + box.topBorderHalfThickness
    val baseRight = box.outerRightTopX + box.rightBorderHalfThickness
    pathClipped(
        baseRight - box.outerHalfWidth, baseTop - box.outerHalfHeight,
        baseRight, baseTop - box.outerHalfHeight,
        baseRight, baseTop,
        baseRight - box.outerHalfWidth, baseTop,
        baseRight - box.outerHalfWidth, baseTop - box.outerHalfHeight,
    ) {
        setCornerStyle(style, color, width)
        drawRightTopCorner(box.outerRightTopX, box.outerRightTopY, radius.value)
    }
}

fun PdfBoxRenderingContext.rightBottomRoundCorner(
    box: BoxLayout,
    borders: Borders,
    isRightBorderStyle: Boolean
) {
    val color = if (isRightBorderStyle) borders.rightBorderColor else borders.bottomBorderColor
    val style = if (isRightBorderStyle) borders.rightBorderStyle else borders.bottomBorderStyle
    val width = if (isRightBorderStyle) borders.rightBorderWidth else borders.bottomBorderHeight
    val radius = borders.rightBottomBorderCornerRadius
    val baseBottom = box.outerRightBottomY - box.bottomBorderHalfThickness
    val baseRight = box.outerRightBottomX + box.rightBorderHalfThickness
    pathClipped(
        baseRight - box.outerHalfWidth, baseBottom,
        baseRight, baseBottom,
        baseRight, baseBottom + box.outerHalfHeight,
        baseRight - box.outerHalfWidth, baseBottom + box.outerHalfHeight,
        baseRight - box.outerHalfWidth, baseBottom,
    ) {
        setCornerStyle(style, color, width)
        drawRightBottomCorner(box.outerRightBottomX, box.outerRightBottomY, radius.value)
    }
}

fun PdfBoxRenderingContext.leftBottomRoundCorner(
    box: BoxLayout,
    borders: Borders,
    isLeftBorderStyle: Boolean
) {
    val color = if (isLeftBorderStyle) borders.leftBorderColor else borders.bottomBorderColor
    val style = if (isLeftBorderStyle) borders.leftBorderStyle else borders.bottomBorderStyle
    val width = if (isLeftBorderStyle) borders.leftBorderWidth else borders.bottomBorderHeight
    val radius = borders.leftBottomBorderCornerRadius
    val baseBottom = box.outerLeftBottomY - box.bottomBorderHalfThickness
    val baseLeft = box.outerLeftBottomX - box.leftBorderHalfThickness
    pathClipped(
        baseLeft, baseBottom,
        baseLeft + box.outerHalfWidth, baseBottom,
        baseLeft + box.outerHalfWidth, baseBottom + box.outerHalfHeight,
        baseLeft, baseBottom + box.outerHalfHeight,
        baseLeft, baseBottom,
    ) {
        setCornerStyle(style, color, width)
        drawLeftBottomCorner(box.outerLeftBottomX, box.outerLeftBottomY, radius.value)
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

private fun PdfBoxRenderingContext.drawLine(x1: Float, y1: Float, x2: Float, y2: Float) {
    with(getCurrentContentStream()) {
        moveTo(x1, y1)
        lineTo(x2, y2)
        stroke()
    }
}

private fun PdfBoxRenderingContext.drawRightBottomCorner(x: Float, y: Float, radius: Float) {
    with(getCurrentContentStream()) {
        moveTo(x - radius, y)
        curveTo(x, y, x, y, x, y + radius)
        stroke()
    }
}

private fun PdfBoxRenderingContext.drawRightTopCorner(x: Float, y: Float, radius: Float) {
    with(getCurrentContentStream()) {
        moveTo(x - radius, y)
        curveTo(x, y, x, y, x, y - radius)
        stroke()
    }
}

private fun PdfBoxRenderingContext.drawLeftTopCorner(x: Float, y: Float, radius: Float) {
    with(getCurrentContentStream()) {
        moveTo(x + radius, y)
        curveTo(x, y, x, y, x, y - radius)
        stroke()
    }
}

private fun PdfBoxRenderingContext.drawLeftBottomCorner(x: Float, y: Float, radius: Float) {
    with(getCurrentContentStream()) {
        moveTo(x + radius, y)
        curveTo(x, y, x, y, x, y + radius)
        stroke()
    }
}

private fun PdfBoxRenderingContext.setCornerStyle(style: BorderStyle?, color: Color?, width: Measure<*>) {
    with(getCurrentContentStream()) {
        setStrokingColor(color.awtColorOrDefault())
        style?.applyOn(this)
        setLineWidth(width.switchUnitOfMeasure(UnitsOfMeasure.PT).value)
    }
}

private fun PdfBoxRenderingContext.setBorderStyle(style: BorderStyle?, color: Color?, width: Measure<*>) {
    with(getCurrentContentStream()) {
        setStrokingColor(color.awtColorOrDefault())
        style?.applyOn(this)
        setLineWidth(width.switchUnitOfMeasure(UnitsOfMeasure.PT).value)
    }
}

private fun BorderStyle?.subLineWidth(width: Measure<*>): Float =
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

private fun BorderStyle?.subLine(width: Measure<*>): SubLineWidthAndOffset =
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

private fun PdfBoxRenderingContext.topCompositeBorder(
    boxLayout: BoxLayout,
    borders: Borders,
    primaryColor: Color? = null,
    secondaryColor: Color? = null,
) {
    val (effectiveWidth, effectiveOffset) = borders.topBorderStyle.subLine(borders.topBorderHeight)
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

private fun PdfBoxRenderingContext.topBorder(
    box: BoxLayout,
    borders: Borders,
    primaryColor: Color? = null,
    secondaryColor: Color? = null,
) {
    if (borders.topBorderStyle.hasBorder()) {
        box.let {
            if (borders.topBorderStyle.hasComplexBorder()) {
                topCompositeBorder(it, borders, primaryColor, secondaryColor)
            } else {
                setBorderStyle(borders.topBorderStyle, borders.topBorderColor, borders.topBorderHeight)
                drawLine(
                    box.outerLeftTopX + box.leftTopCornerRadiusOrZero,
                    box.outerLeftTopY,
                    box.outerRightTopX - box.rightTopCornerRadiusOrZero,
                    box.outerRightTopY
                )
            }
        }
    }
}

private fun PdfBoxRenderingContext.bottomCompositeBorder(
    boxLayout: BoxLayout,
    borders: Borders,
    primaryColor: Color? = null,
    secondaryColor: Color? = null,
) {
    val (effectiveWidth, effectiveOffset) = borders.bottomBorderStyle.subLine(borders.bottomBorderHeight)
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

private fun PdfBoxRenderingContext.bottomBorder(
    box: BoxLayout,
    borders: Borders,
    primaryColor: Color? = null,
    secondaryColor: Color? = null,
) {
    if (borders.bottomBorderStyle.hasBorder()) {
        box.let {
            if (borders.bottomBorderStyle.hasComplexBorder()) {
                bottomCompositeBorder(it, borders, primaryColor, secondaryColor)
            } else {
                setBorderStyle(borders.bottomBorderStyle, borders.bottomBorderColor, borders.bottomBorderHeight)
                drawLine(
                    box.outerLeftBottomX + box.leftBottomCornerRadiusOrZero,
                    box.outerLeftBottomY,
                    box.outerRightBottomX - box.rightBottomCornerRadiusOrZero,
                    box.outerRightBottomY
                )
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
    val minTopBorderWidth = borders.topBorderStyle.subLineWidth(borders.topBorderHeight)
    val minBottomBorderWidth = borders.bottomBorderStyle.subLineWidth(borders.bottomBorderHeight)

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

private fun PdfBoxRenderingContext.leftBorder(
    box: BoxLayout,
    borders: Borders,
    primaryColor: Color? = null,
    secondaryColor: Color? = null,
) {
    if (borders.leftBorderStyle.hasBorder()) {
        box.let {
            if (borders.leftBorderStyle.hasComplexBorder()) {
                leftCompositeBorder(it, borders, primaryColor, secondaryColor)
            } else {
                setBorderStyle(borders.leftBorderStyle, borders.leftBorderColor, borders.leftBorderWidth)
                drawLine(
                    box.outerLeftTopX,
                    box.outerLeftTopY - box.leftTopCornerRadiusOrZero,
                    box.outerLeftBottomX,
                    box.outerLeftBottomY + box.leftBottomCornerRadiusOrZero
                )
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
    val minTopBorderWidth = borders.topBorderStyle.subLineWidth(borders.topBorderHeight)
    val minBottomBorderWidth = borders.bottomBorderStyle.subLineWidth(borders.bottomBorderHeight)

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

private fun PdfBoxRenderingContext.rightBorder(
    box: BoxLayout,
    borders: Borders,
    primaryColor: Color? = null,
    secondaryColor: Color? = null,
) {
    if (borders.rightBorderStyle.hasBorder()) {
        box.let {
            if (borders.rightBorderStyle.hasComplexBorder()) {
                rightCompositeBorder(it, borders, primaryColor, secondaryColor)
            } else {
                setBorderStyle(borders.rightBorderStyle, borders.rightBorderColor, borders.rightBorderWidth)
                drawLine(
                    box.outerRightTopX,
                    (box.outerRightTopY - box.leftTopCornerRadiusOrZero),
                    box.outerRightBottomX,
                    (box.outerRightBottomY + box.leftBottomCornerRadiusOrZero)
                )
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

