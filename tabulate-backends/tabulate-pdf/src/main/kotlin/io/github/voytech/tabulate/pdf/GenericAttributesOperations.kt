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
            bbox.outer.fillRect(attribute.color)
        }
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

fun <A : AttributedEntity> PdfBoxRenderingContext.drawBorders(context: A, borders: Borders) {
    val leftPrimaryColor = borders.leftBorderStyle?.leftTopPrimaryColor(borders.leftBorderColor)
    val leftSecondaryColor = borders.leftBorderStyle?.leftTopSecondaryColor(borders.leftBorderColor)
    leftBorder(
        context, borders,
        borders.leftBorderStyle?.leftTopPrimaryColor(borders.leftBorderColor),
        borders.leftBorderStyle?.leftTopSecondaryColor(borders.leftBorderColor),
    )
    leftTopRoundCorner(context, borders)
    val topPrimaryColor = borders.topBorderStyle?.leftTopPrimaryColor(borders.topBorderColor)
    val topSecondaryColor = borders.topBorderStyle?.leftTopSecondaryColor(borders.topBorderColor)
    topBorder(
        context, borders,
        borders.topBorderStyle?.leftTopPrimaryColor(borders.topBorderColor),
        borders.topBorderStyle?.leftTopSecondaryColor(borders.topBorderColor),
    )
    rightTopRoundCorner(context, borders)
    val rightPrimaryColor = borders.rightBorderStyle?.rightBottomPrimaryColor(borders.rightBorderColor)
    val rightSecondaryColor = borders.rightBorderStyle?.rightBottomSecondaryColor(borders.rightBorderColor)
    rightBorder(
        context, borders,
        borders.rightBorderStyle?.rightBottomPrimaryColor(borders.rightBorderColor),
        borders.rightBorderStyle?.rightBottomSecondaryColor(borders.rightBorderColor),
    )
    rightBottomRoundCorner(context, borders)
    val bottomPrimaryColor = borders.bottomBorderStyle?.rightBottomPrimaryColor(borders.bottomBorderColor)
    val bottomSecondaryColor = borders.bottomBorderStyle?.rightBottomSecondaryColor(borders.bottomBorderColor)
    bottomBorder(
        context, borders,
        borders.bottomBorderStyle?.rightBottomPrimaryColor(borders.bottomBorderColor),
        borders.bottomBorderStyle?.rightBottomSecondaryColor(borders.bottomBorderColor),
    )
    leftBottomRoundCorner(context, borders)
}

fun <A : AttributedEntity> leftTopRoundCorner(
    context: A,
    borders: Borders,
    primaryColor: Color? = null,
    secondaryColor: Color? = null,
) {
    TODO("Not yet implemented")
}

fun <A : AttributedEntity> rightTopRoundCorner(
    context: A,
    borders: Borders,
    primaryColor: Color? = null,
    secondaryColor: Color? = null,
) {
    TODO("Not yet implemented")
}

fun <A : AttributedEntity> rightBottomRoundCorner(
    context: A,
    borders: Borders,
    primaryColor: Color? = null,
    secondaryColor: Color? = null,
) {
    TODO("Not yet implemented")
}

fun <A : AttributedEntity> leftBottomRoundCorner(
    context: A,
    borders: Borders,
    primaryColor: Color? = null,
    secondaryColor: Color? = null,
) {
    TODO("Not yet implemented")
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

private fun PdfBoxRenderingContext.setBorderStyle(style: BorderStyle?, color: Color?, width: Measure<*>) {
    with(getCurrentContentStream()) {
        saveGraphicsState()
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

private fun <A : AttributedEntity> PdfBoxRenderingContext.topCompositeBorder(
    boxLayout: BoxLayout,
    context: A,
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

private fun <A : AttributedEntity> PdfBoxRenderingContext.topBorder(
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
                setBorderStyle(borders.topBorderStyle, borders.topBorderColor, borders.topBorderHeight)
                val x1 = it.outerX
                val y1 = it.outerY + (it.outer.height?.value ?: 0f) - borders.topBorderHeight.value / 2
                val x2 = it.outerX + (it.outer.width?.value ?: 0f)
                drawLine(x1, y1, x2, y1)
            }
        }
    }
}

private fun <A : AttributedEntity> PdfBoxRenderingContext.bottomCompositeBorder(
    boxLayout: BoxLayout,
    context: A,
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

private fun <A : AttributedEntity> PdfBoxRenderingContext.bottomBorder(
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
                setBorderStyle(borders.bottomBorderStyle, borders.bottomBorderColor, borders.bottomBorderHeight)
                val x1 = it.outerX
                val y1 = it.outerY + borders.bottomBorderHeight.value / 2
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

private fun <A : AttributedEntity> PdfBoxRenderingContext.leftBorder(
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

private fun <A : AttributedEntity> PdfBoxRenderingContext.rightBorder(
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

