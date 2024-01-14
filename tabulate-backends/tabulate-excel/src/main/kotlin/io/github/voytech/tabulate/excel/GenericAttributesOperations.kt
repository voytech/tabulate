package io.github.voytech.tabulate.excel

import io.github.voytech.tabulate.components.table.rendering.CellRenderable
import io.github.voytech.tabulate.components.table.rendering.getSheetName
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.alignment.DefaultVerticalAlignment
import io.github.voytech.tabulate.core.model.attributes.*
import io.github.voytech.tabulate.core.model.border.DefaultBorderStyle
import io.github.voytech.tabulate.core.model.text.DefaultTextWrap
import io.github.voytech.tabulate.core.model.text.DefaultWeightStyle
import io.github.voytech.tabulate.core.operation.AttributeOperation
import io.github.voytech.tabulate.core.operation.AttributedContext
import io.github.voytech.tabulate.core.operation.cacheOnAttributeSet
import org.apache.poi.hssf.usermodel.HSSFTextbox
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FontUnderline
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.TextDirection
import org.apache.poi.xssf.usermodel.XSSFFont

class XSSFShapeAlignmentAttributeRenderOperation<CTX> :
    AttributeOperation<ApachePoiRenderingContext, AlignmentAttribute, CTX> where CTX : AttributedContext {

    override fun invoke(
        renderingContext: ApachePoiRenderingContext,
        context: CTX,
        attribute: AlignmentAttribute,
    ) = with(renderingContext) {
        renderingContext.provideSheet(context.getSheetName()).let {
            context.shape<SimpleShapeWrapper>().let { shape ->
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
            context.shape<SimpleShapeWrapper>().let { text ->
                text.applyFont(renderingContext) { font -> font.configureWith(attribute) }
                attribute.textWrap?.let {
                    when (it.getId()) {
                        DefaultTextWrap.BREAK_WORDS.getId(),
                        DefaultTextWrap.BREAK_LINES.getId() -> text.wordWrap = true
                        DefaultTextWrap.NO_WRAP.getId() -> text.wordWrap = false
                    }
                }
                text.textDirection = attribute.rotation?.mod(360)?.let {
                    when (it) {
                        in (0..45), in (136..225), in (316..360) -> TextDirection.HORIZONTAL
                        in (46..135) -> TextDirection.VERTICAL
                        in (226..315) -> TextDirection.VERTICAL_270
                        else -> TextDirection.HORIZONTAL
                    }
                } ?: TextDirection.HORIZONTAL
                text.leftInset
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
            context.shape<SimpleShapeWrapper>().let { shape ->
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
            context.shape<SimpleShapeWrapper>().let { shape ->
                if (attribute.areAllEqual()) {
                    attribute.color()?.let { shape.setLineStyleColor(it.r, it.g, it.b) }
                    attribute.style()?.let {
                        shape.setLineStyle(
                            when (it) {
                                DefaultBorderStyle.SOLID -> HSSFTextbox.LINESTYLE_SOLID
                                DefaultBorderStyle.DOUBLE -> HSSFTextbox.LINESTYLE_SOLID
                                DefaultBorderStyle.DASHED -> HSSFTextbox.LINESTYLE_DASHSYS
                                DefaultBorderStyle.DOTTED -> HSSFTextbox.LINESTYLE_DOTSYS
                                DefaultBorderStyle.NONE -> HSSFTextbox.LINESTYLE_NONE
                                else -> HSSFTextbox.LINESTYLE_DEFAULT
                            }
                        )
                    }
                    attribute.width().let {
                        shape.setLineWidth(it.switchUnitOfMeasure(UnitsOfMeasure.PT).value.toDouble())
                    }
                }
            }
        }
    }
}

private const val CELL_STYLE_CACHE_KEY: String = "cellStyle"

private const val CELL_FONT_CACHE_KEY: String = "font"

fun ApachePoiRenderingContext.getCachedStyle(context: CellRenderable): CellStyle {
    return context.cacheOnAttributeSet(CELL_STYLE_CACHE_KEY, this::createCellStyle)  as CellStyle
}

fun ApachePoiRenderingContext.getCachedFont(context: AttributedContext): XSSFFont {
    return context.cacheOnAttributeSet(CELL_FONT_CACHE_KEY, this::createFont) as XSSFFont
}

fun ApachePoiRenderingContext.createFontFrom(attributedContext: AttributedContext, attribute: TextStylesAttribute): XSSFFont =
    getCachedFont(attributedContext).configureWith(attribute)

fun XSSFFont.configureWith(attribute: TextStylesAttribute): XSSFFont = apply {
    attribute.fontFamily?.run { this@apply.fontName = this.fontName }
    attribute.fontColor?.run { setColor(ApachePoiRenderingContext.color(this)) }
    attribute.fontSize?.run { fontHeightInPoints = toShort() }
    attribute.italic?.run { italic = this }
    attribute.strikeout?.run { strikeout = this }
    attribute.underline?.run { setUnderline(if (this) FontUnderline.SINGLE else FontUnderline.NONE) }
    attribute.weight?.run { bold = this == DefaultWeightStyle.BOLD }
}
