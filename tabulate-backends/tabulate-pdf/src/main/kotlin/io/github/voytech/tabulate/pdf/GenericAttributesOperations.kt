package io.github.voytech.tabulate.pdf

import io.github.voytech.tabulate.core.model.Measure
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.attributes.*
import io.github.voytech.tabulate.core.model.border.BorderStyle
import io.github.voytech.tabulate.core.model.border.Borders
import io.github.voytech.tabulate.core.model.border.DefaultBorderStyle
import io.github.voytech.tabulate.core.model.color.Color
import io.github.voytech.tabulate.core.model.text.DefaultWeightStyle
import io.github.voytech.tabulate.core.operation.AttributeOperation
import io.github.voytech.tabulate.core.operation.AttributedEntity
import io.github.voytech.tabulate.round3
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
        leftBorderClippingPath(box) {
            renderBorder(box, borders, BorderType.LEFT)
            leftTopRoundCorner(box, borders, true)
            leftBottomRoundCorner(box, borders, true)
        }

        topBorderClippingPath(box) {
            renderBorder(box, borders, BorderType.TOP)
            leftTopRoundCorner(box, borders, false)
            rightTopRoundCorner(box, borders, false)
        }

        rightBorderClippingPath(box) {
            renderBorder(box, borders, BorderType.RIGHT)
            rightTopRoundCorner(box, borders, true)
            rightBottomRoundCorner(box, borders, true)

        }

        bottomBorderClippingPath(box) {
            renderBorder(box, borders, BorderType.BOTTOM)
            leftBottomRoundCorner(box, borders, false)
            rightBottomRoundCorner(box, borders, false)
        }
        getCurrentContentStream().restoreGraphicsState()
    }
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
    val baseTop = box.outerLeftTopY - box.topBorderHalfThickness
    val baseLeft = box.outerLeftTopX + box.leftBorderHalfThickness
    pathClipped(
        box.outerLeftTopX, box.outerLeftTopY - box.outerHalfHeight,
        box.outerLeftTopX + box.outerHalfWidth, box.outerLeftTopY - box.outerHalfHeight,
        box.outerLeftTopX + box.outerHalfWidth, box.outerLeftTopY,
        box.outerLeftTopX, box.outerLeftTopY,
        box.outerLeftTopX, box.outerLeftTopY - box.outerHalfHeight,
    ) {
        setLineStyle(style, color, width)
        drawArcCorner(baseLeft + radius.value, baseTop - radius.value, baseLeft, baseTop)
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
    val baseTop = box.outerRightTopY - box.topBorderHalfThickness
    val baseRight = box.outerRightTopX - box.rightBorderHalfThickness
    pathClipped(
        box.outerRightTopX - box.outerHalfWidth, box.outerRightTopY - box.outerHalfHeight,
        box.outerRightTopX, box.outerRightTopY - box.outerHalfHeight,
        box.outerRightTopX, box.outerRightTopY,
        box.outerRightTopX - box.outerHalfWidth, box.outerRightTopY,
        box.outerRightTopX - box.outerHalfWidth, box.outerRightTopY - box.outerHalfHeight,
    ) {
        when (style.getBorderStyleId()) {
            DefaultBorderStyle.DOUBLE.name -> {
                val oneThirdThickness =
                    if (isRightBorderStyle) box.rightBorderOneThirdThickness else box.topBorderOneThirdThickness
                setLineStyle(style, color, width)
                drawArcCorner(
                    baseRight + oneThirdThickness - radius.value, baseTop + oneThirdThickness - radius.value,
                    baseRight + oneThirdThickness, baseTop + oneThirdThickness
                )
                drawArcCorner(
                    baseRight - oneThirdThickness - radius.value, baseTop - oneThirdThickness - radius.value,
                    baseRight - oneThirdThickness, baseTop - oneThirdThickness
                )
            }

            DefaultBorderStyle.GROOVE.name -> {}

            else -> {
                setLineStyle(style, color, width)
                drawArcCorner(baseRight - radius.value, baseTop - radius.value, baseRight, baseTop)
            }
        }
        setLineStyle(style, color, width)

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
    val baseBottom = box.outerRightBottomY + box.bottomBorderHalfThickness
    val baseRight = box.outerRightBottomX - box.rightBorderHalfThickness
    pathClipped(
        box.outerRightBottomX - box.outerHalfWidth, box.outerRightBottomY + box.outerHalfHeight,
        box.outerRightBottomX, box.outerRightBottomY + box.outerHalfHeight,
        box.outerRightBottomX, box.outerRightBottomY,
        box.outerRightBottomX - box.outerHalfWidth, box.outerRightBottomY,
        box.outerRightBottomX - box.outerHalfWidth, box.outerRightBottomY + box.outerHalfHeight,
    ) {
        setLineStyle(style, color, width)
        drawArcCorner(baseRight - radius.value, baseBottom + radius.value, baseRight, baseBottom)
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
    val baseBottom = box.outerLeftBottomY + box.bottomBorderHalfThickness
    val baseLeft = box.outerLeftBottomX + box.leftBorderHalfThickness
    pathClipped(
        box.outerLeftBottomX, box.outerLeftBottomY + box.outerHalfHeight,
        box.outerLeftBottomX + box.outerHalfWidth, box.outerLeftBottomY + box.outerHalfHeight,
        box.outerLeftBottomX + box.outerHalfWidth, box.outerLeftBottomY,
        box.outerLeftBottomX, box.outerLeftBottomY,
        box.outerLeftBottomX, box.outerLeftBottomY + box.outerHalfHeight,
    ) {
        setLineStyle(style, color, width)
        drawArcCorner(baseLeft + radius.value, baseBottom + radius.value, baseLeft, baseBottom)
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

private fun PdfBoxRenderingContext.drawArcCorner(xs: Float, ye: Float, x: Float, y: Float) {
    with(getCurrentContentStream()) {
        moveTo(xs, y)
        curveTo(x, y, x, y, x, ye)
        stroke()
    }
}

private fun PdfBoxRenderingContext.setLineStyle(style: BorderStyle?, color: Color?, width: Measure<*>) {
    setLineStyle(style, color, width.switchUnitOfMeasure(UnitsOfMeasure.PT).value)
}

private fun PdfBoxRenderingContext.setLineStyle(style: BorderStyle?, color: Color?, width: Float) {
    with(getCurrentContentStream()) {
        setStrokingColor(color.awtColorOrDefault())
        style?.applyOn(this)
        setLineWidth(width)
    }
}

private fun PdfBoxRenderingContext.topBorderClippingPath(box: BoxLayout, block: () -> Unit) {
    box.borders?.let {
        val x1 = box.outerLeftTopX
        val y1 = box.outerLeftTopY
        val x2 = x1 + maxOf(it.leftBorderWidth.value, box.leftTopCornerRadiusOrZero)
        val y2 = y1 - maxOf(it.topBorderHeight.value, box.leftTopCornerRadiusOrZero)
        val x3 = box.outerRightTopX - maxOf(it.rightBorderWidth.value, box.rightTopCornerRadiusOrZero)
        val x4 = box.outerRightTopX
        val y4 = box.outerRightTopY
        pathClipped(x1, y1, x2, y2, x3, y2, x4, y4, x1, y1) { block() }
    }
}

@Suppress("DuplicatedCode")
private fun PdfBoxRenderingContext.bottomBorderClippingPath(box: BoxLayout, block: () -> Unit) {
    box.borders?.let {
        val x1 = box.outerLeftBottomX
        val y1 = box.outerLeftBottomY
        val x2 = x1 + maxOf(it.leftBorderWidth.value, box.leftBottomCornerRadiusOrZero)
        val y2 = y1 + maxOf(it.bottomBorderHeight.value, box.leftBottomCornerRadiusOrZero)
        val x3 = box.outerRightBottomX - maxOf(it.rightBorderWidth.value, box.rightBottomCornerRadiusOrZero)
        val y3 = box.outerRightBottomY + maxOf(it.bottomBorderHeight.value, box.rightBottomCornerRadiusOrZero)
        val x4 = box.outerRightBottomX
        val y4 = box.outerRightBottomY
        pathClipped(x1, y1, x2, y2, x3, y3, x4, y4, x1, y1) { block() }
    }
}

@Suppress("DuplicatedCode")
private fun PdfBoxRenderingContext.leftBorderClippingPath(box: BoxLayout, block: () -> Unit) {
    box.borders?.let {
        val x1 = box.outerLeftTopX
        val y1 = box.outerLeftTopY
        val x2 = x1 + maxOf(it.leftBorderWidth.value, box.leftTopCornerRadiusOrZero)
        val y2 = y1 - maxOf(it.topBorderHeight.value, box.leftTopCornerRadiusOrZero)
        val y3 = box.outerLeftBottomY + maxOf(it.bottomBorderHeight.value, box.leftBottomCornerRadiusOrZero)
        val x4 = box.outerLeftBottomX
        val y4 = box.outerLeftBottomY
        pathClipped(x1, y1, x2, y2, x2, y3, x4, y4, x1, y1) { block() }
    }
}

@Suppress("DuplicatedCode")
private fun PdfBoxRenderingContext.rightBorderClippingPath(box: BoxLayout, block: () -> Unit) {
    box.borders?.let {
        val x1 = box.outerRightTopX
        val y1 = box.outerRightTopY
        val x2 = x1 - maxOf(it.rightBorderWidth.value, box.rightTopCornerRadiusOrZero)
        val y2 = y1 - maxOf(it.topBorderHeight.value, box.rightTopCornerRadiusOrZero)
        val y3 = box.outerRightBottomY + maxOf(it.bottomBorderHeight.value, box.rightBottomCornerRadiusOrZero)
        val x4 = box.outerRightBottomX
        val y4 = box.outerRightBottomY
        pathClipped(x1, y1, x2, y2, x2, y3, x4, y4, x1, y1) { block() }
    }
}

private fun drawPointOffsetModifier(borderType: BorderType): Int {
    return when (borderType) {
        BorderType.LEFT -> 1
        BorderType.TOP -> -1
        BorderType.RIGHT -> -1
        BorderType.BOTTOM -> 1
    }
}

private fun PdfBoxRenderingContext.renderBorder(box: BoxLayout, borders: Borders, borderType: BorderType) {
    val style = borders.getStyle(borderType)
    if (style.hasBorder()) {
        val color = borders.getColor(borderType)
        val width = borders.getWidth(borderType)
        val halfThickness = (width.value / 2).round3()
        val dPointMod = drawPointOffsetModifier(borderType)
        var startX: Float = -1F
        var startY: Float = -1F
        var endX: Float = -1F
        var endY: Float = -1F
        if (isHorizontal(borderType)) {
            val xMiddle = box.getOuterX1(borderType) + (box.getOuterX2(borderType) - box.getOuterX1(borderType)) / 2
            startX = (box.getOuterX1(borderType) + borders.getRadius(borderType).first.value)
                .coerceAtMost(xMiddle)
            endX = (box.getOuterX2(borderType) - borders.getRadius(borderType).second.value)
                .coerceAtLeast(xMiddle)
        } else {
            val yMiddle = box.getOuterY1(borderType) - (box.getOuterY1(borderType) - box.getOuterY2(borderType)) / 2
            startY = (box.getOuterY1(borderType) - borders.getRadius(borderType).first.value)
                .coerceAtLeast(yMiddle)
            endY = (box.getOuterY2(borderType) + borders.getRadius(borderType).second.value)
                .coerceAtMost(yMiddle)
        }
        when (style.getBorderStyleId()) {
            DefaultBorderStyle.DOUBLE.name -> {
                val oneThirdThickness = (width.value / 3).round3()
                setLineStyle(style, color, oneThirdThickness)
                if (isHorizontal(borderType)) {
                    drawLine(
                        startX, box.getOuterY1(borderType) + halfThickness * dPointMod - oneThirdThickness,
                        endX, box.getOuterY2(borderType) + halfThickness * dPointMod - oneThirdThickness
                    )
                    drawLine(
                        startX, box.getOuterY1(borderType) + halfThickness * dPointMod + oneThirdThickness,
                        endX, box.getOuterY2(borderType) + halfThickness * dPointMod + oneThirdThickness
                    )
                } else {
                    drawLine(
                        box.getOuterX1(borderType) + halfThickness * dPointMod - oneThirdThickness, startY,
                        box.getOuterX2(borderType) + halfThickness * dPointMod - oneThirdThickness, endY
                    )
                    drawLine(
                        box.getOuterX1(borderType) + halfThickness * dPointMod + oneThirdThickness, startY,
                        box.getOuterX2(borderType) + halfThickness * dPointMod + oneThirdThickness, endY
                    )
                }
            }

            DefaultBorderStyle.GROOVE.name -> {}

            else -> {
                setLineStyle(style, color, width)
                if (isHorizontal(borderType)) {
                    drawLine(
                        startX, box.getOuterY1(borderType) + halfThickness * dPointMod,
                        endX, box.getOuterY2(borderType) + halfThickness * dPointMod
                    )
                } else {
                    drawLine(
                        box.getOuterX1(borderType) + halfThickness * dPointMod, startY,
                        box.getOuterX2(borderType) + halfThickness * dPointMod, endY
                    )
                }

            }
        }
    }
}

fun BorderStyle?.hasBorder(): Boolean = this != null && getBorderStyleId() != DefaultBorderStyle.NONE.name