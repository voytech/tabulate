package io.github.voytech.tabulate.excel

import io.github.voytech.tabulate.components.table.operation.getSheetName
import io.github.voytech.tabulate.core.model.alignment.DefaultVerticalAlignment
import io.github.voytech.tabulate.core.model.attributes.AlignmentAttribute
import io.github.voytech.tabulate.core.model.attributes.BackgroundAttribute
import io.github.voytech.tabulate.core.model.attributes.BordersAttribute
import io.github.voytech.tabulate.core.model.attributes.TextStylesAttribute
import io.github.voytech.tabulate.core.model.text.DefaultWeightStyle
import io.github.voytech.tabulate.core.template.operation.AttributeOperation
import io.github.voytech.tabulate.core.template.operation.AttributedContext
import org.apache.poi.ss.usermodel.FontUnderline
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFFont

class XSSFShapeAlignmentAttributeRenderOperation<CTX> :
    AttributeOperation<ApachePoiRenderingContext, AlignmentAttribute, CTX> where CTX : AttributedContext {

    override fun invoke(
        renderingContext: ApachePoiRenderingContext,
        context: CTX,
        attribute: AlignmentAttribute,
    ) = with(renderingContext) {
        renderingContext.provideSheet(context.getSheetName()).let {
            context.shape<RichTextBox>().let { shape ->
                when (attribute.vertical) {
                    DefaultVerticalAlignment.MIDDLE -> shape.verticalAlignment = VerticalAlignment.CENTER
                    null, DefaultVerticalAlignment.BOTTOM -> shape.verticalAlignment = VerticalAlignment.BOTTOM
                    DefaultVerticalAlignment.TOP -> shape.verticalAlignment = VerticalAlignment.TOP
                }
            }
        }
    }
}

class XSSFShapeTextStylesAttributeRenderOperation<CTX : AttributedContext> :
    AttributeOperation<ApachePoiRenderingContext, TextStylesAttribute, CTX> {

    override operator fun invoke(
        renderingContext: ApachePoiRenderingContext,
        context: CTX,
        attribute: TextStylesAttribute,
    ): Unit = with(renderingContext) {
        renderingContext.provideSheet(context.getSheetName()).let {
            context.shape<RichTextBox>().let { text ->
                text.applyFont(renderingContext) { font -> font.configureWith(attribute) }
            }
        }
    }
}

class XSSFShapeBackgroundAttributeRenderOperation<CTX : AttributedContext> :
    AttributeOperation<ApachePoiRenderingContext, BackgroundAttribute, CTX> {
    override fun invoke(
        renderingContext: ApachePoiRenderingContext,
        context: CTX,
        attribute: BackgroundAttribute,
    ): Unit = with(renderingContext) {
        renderingContext.provideSheet(context.getSheetName()).let {
            context.shape<RichTextBox>().let { shape ->
                attribute.color?.let { color ->
                    shape.setFillColor(color.r, color.g, color.b)
                } ?: run { shape.isNoFill = true }
            }
        }
    }
}

class XSSFBorderAttributeRenderOperation<CTX : AttributedContext> :
    AttributeOperation<ApachePoiRenderingContext, BordersAttribute, CTX> {
    override fun invoke(
        renderingContext: ApachePoiRenderingContext,
        context: CTX,
        attribute: BordersAttribute,
    ): Unit = with(renderingContext) {
        renderingContext.provideSheet(context.getSheetName()).let {
            context.shape<RichTextBox>().let { shape ->
                //attribute.?.let { color ->
                //textBox.setLineStyleColor(0, 0, 0)
                //textBox.setLineWidth(2.0)
                //}
            }
        }
    }
}

fun ApachePoiRenderingContext.createFontFrom(attribute: TextStylesAttribute): XSSFFont =
    (workbook().createFont() as XSSFFont).configureWith(attribute)

fun XSSFFont.configureWith(attribute: TextStylesAttribute): XSSFFont = apply {
    attribute.fontFamily?.run { this@apply.fontName = this.fontName }
    attribute.fontColor?.run { setColor(ApachePoiRenderingContext.color(this)) }
    attribute.fontSize?.run { fontHeightInPoints = toShort() }
    attribute.italic?.run { italic = this }
    attribute.strikeout?.run { strikeout = this }
    attribute.underline?.run { setUnderline(if (this) FontUnderline.SINGLE else FontUnderline.NONE) }
    attribute.weight?.run { bold = this == DefaultWeightStyle.BOLD }
}
