package io.github.voytech.tabulate.excel.template

import io.github.voytech.tabulate.excel.model.attributes.CellExcelDataFormatAttribute
import io.github.voytech.tabulate.excel.model.attributes.FilterAndSortTableAttribute
import io.github.voytech.tabulate.excel.template.PoiExcelExportOperationsFactory.Companion.getCachedStyle
import io.github.voytech.tabulate.excel.template.poi.ApachePoiUtils
import io.github.voytech.tabulate.model.attributes.cell.CellAlignmentAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBackgroundAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.model.attributes.cell.enums.*
import io.github.voytech.tabulate.model.attributes.cell.enums.contract.BorderStyle
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.template.operations.*
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.FontUnderline
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import java.io.FileInputStream
import org.apache.poi.ss.usermodel.BorderStyle as PoiBorderStyle

/**
 * Apache POI [CellTextStylesAttribute] renderer.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class CellTextStylesAttributeRenderOperation: CellAttributeRenderOperation<ApachePoiRenderingContext, CellTextStylesAttribute> {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java

    override fun attributeType(): Class<CellTextStylesAttribute> = CellTextStylesAttribute::class.java

    override fun renderAttribute(renderingContext: ApachePoiRenderingContext, context: CellContext, attribute: CellTextStylesAttribute) {
        renderingContext.provideCellStyle(
            sheetName = context.getTableId(),
            rowIndex = context.getRow(),
            columnIndex = context.getColumn(),
            provideCellStyle = { getCachedStyle(renderingContext, context) }
        ).let {
            val font: XSSFFont = renderingContext.workbook().createFont() as XSSFFont
            attribute.fontFamily?.run { font.fontName = this }
            attribute.fontColor?.run { font.setColor(ApachePoiRenderingContext.color(this)) }
            attribute.fontSize?.run { font.fontHeightInPoints = toShort() }
            attribute.italic?.run { font.italic = this }
            attribute.strikeout?.run { font.strikeout = this }
            attribute.underline?.run { font.setUnderline(if (this) FontUnderline.SINGLE else FontUnderline.NONE) }
            attribute.weight?.run { font.bold = this == DefaultWeightStyle.BOLD }
            it.setFont(font)
            it.indention = attribute.ident ?: 0
            it.wrapText = attribute.wrapText ?: false
            it.rotation = attribute.rotation ?: 0
        }
    }
}

/**
 * Apache POI [CellBackgroundAttribute] renderer.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class CellBackgroundAttributeRenderOperation: CellAttributeRenderOperation<ApachePoiRenderingContext, CellBackgroundAttribute> {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java

    override fun attributeType(): Class<CellBackgroundAttribute> = CellBackgroundAttribute::class.java

    override fun renderAttribute(
        renderingContext: ApachePoiRenderingContext,
        context: CellContext,
        attribute: CellBackgroundAttribute,
    ) {
        renderingContext.provideCellStyle(
            sheetName = context.getTableId(),
            rowIndex = context.getRow(),
            columnIndex = context.getColumn(),
            provideCellStyle = { getCachedStyle(renderingContext, context) }
        ).let {
            if (attribute.color != null) {
                (it as XSSFCellStyle).setFillForegroundColor(ApachePoiRenderingContext.color(attribute.color!!))
            }
            when (attribute.fill) {
                DefaultCellFill.SOLID -> it.fillPattern = FillPatternType.SOLID_FOREGROUND
                DefaultCellFill.BRICKS -> it.fillPattern = FillPatternType.BRICKS
                DefaultCellFill.WIDE_DOTS -> it.fillPattern = FillPatternType.ALT_BARS
                DefaultCellFill.DIAMONDS -> it.fillPattern = FillPatternType.DIAMONDS
                DefaultCellFill.SMALL_DOTS -> it.fillPattern = FillPatternType.FINE_DOTS
                DefaultCellFill.SQUARES -> it.fillPattern = FillPatternType.SQUARES
                DefaultCellFill.LARGE_SPOTS -> it.fillPattern = FillPatternType.BIG_SPOTS
                else -> it.fillPattern = resolveFillPatternByEnumName(attribute)
            }
        }
    }

    private fun resolveFillPatternByEnumName(background: CellBackgroundAttribute): FillPatternType {
        return try {
            FillPatternType.valueOf(background.fill.getCellFillId())
        } catch (e: IllegalArgumentException) {
            FillPatternType.NO_FILL
        }
    }
}

/**
 * Apache POI [CellBordersAttribute] renderer.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class CellBordersAttributeRenderOperation: CellAttributeRenderOperation<ApachePoiRenderingContext, CellBordersAttribute> {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java

    override fun attributeType(): Class<CellBordersAttribute> = CellBordersAttribute::class.java

    override fun renderAttribute(renderingContext: ApachePoiRenderingContext, context: CellContext, attribute: CellBordersAttribute) {
        renderingContext.provideCellStyle(
            sheetName = context.getTableId(),
            rowIndex = context.getRow(),
            columnIndex = context.getColumn(),
            provideCellStyle = { getCachedStyle(renderingContext, context) }
        ).let {
            attribute.leftBorderColor?.run { (it as XSSFCellStyle).setLeftBorderColor(ApachePoiRenderingContext.color(this)) }
            attribute.rightBorderColor?.run { (it as XSSFCellStyle).setRightBorderColor(ApachePoiRenderingContext.color(this)) }
            attribute.topBorderColor?.run { (it as XSSFCellStyle).setTopBorderColor(ApachePoiRenderingContext.color(this)) }
            attribute.bottomBorderColor?.run {
                (it as XSSFCellStyle).setBottomBorderColor(
                    ApachePoiRenderingContext.color(
                        this
                    )
                )
            }
            attribute.leftBorderStyle?.run { it.borderLeft = resolveBorderStyle(this) }
            attribute.rightBorderStyle?.run { it.borderRight = resolveBorderStyle(this) }
            attribute.topBorderStyle?.run { it.borderTop = resolveBorderStyle(this) }
            attribute.bottomBorderStyle?.run { it.borderBottom = resolveBorderStyle(this) }
        }
    }

    private fun resolveBorderStyle(style: BorderStyle): PoiBorderStyle {
        return when (style.getBorderStyleId()) {
            DefaultBorderStyle.DASHED.name -> PoiBorderStyle.DASHED
            DefaultBorderStyle.DOTTED.name -> PoiBorderStyle.DOTTED
            DefaultBorderStyle.SOLID.name -> PoiBorderStyle.THIN
            DefaultBorderStyle.DOUBLE.name -> PoiBorderStyle.DOUBLE
            else -> try {
                PoiBorderStyle.valueOf(style.getBorderStyleId())
            } catch (e: IllegalArgumentException) {
                PoiBorderStyle.NONE
            }
        }
    }
}

/**
 * Apache POI [CellAlignmentAttribute] renderer.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class CellAlignmentAttributeRenderOperation: CellAttributeRenderOperation<ApachePoiRenderingContext, CellAlignmentAttribute> {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java

    override fun attributeType(): Class<CellAlignmentAttribute> = CellAlignmentAttribute::class.java

    override fun renderAttribute(renderingContext: ApachePoiRenderingContext, context: CellContext, attribute: CellAlignmentAttribute) {
        renderingContext.provideCellStyle(
            sheetName = context.getTableId(),
            rowIndex = context.getRow(),
            columnIndex = context.getColumn(),
            provideCellStyle = { getCachedStyle(renderingContext, context) }
        ).let {
            with(attribute.horizontal) {
                it.alignment =
                    when (this) {
                        DefaultHorizontalAlignment.CENTER -> org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER
                        DefaultHorizontalAlignment.LEFT -> org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT
                        DefaultHorizontalAlignment.RIGHT -> org.apache.poi.ss.usermodel.HorizontalAlignment.RIGHT
                        DefaultHorizontalAlignment.JUSTIFY -> org.apache.poi.ss.usermodel.HorizontalAlignment.JUSTIFY
                        DefaultHorizontalAlignment.FILL -> org.apache.poi.ss.usermodel.HorizontalAlignment.FILL
                        else -> org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT
                    }
            }
            with(attribute.vertical) {
                it.verticalAlignment =
                    when (this) {
                        DefaultVerticalAlignment.MIDDLE -> org.apache.poi.ss.usermodel.VerticalAlignment.CENTER
                        DefaultVerticalAlignment.BOTTOM -> org.apache.poi.ss.usermodel.VerticalAlignment.BOTTOM
                        DefaultVerticalAlignment.TOP -> org.apache.poi.ss.usermodel.VerticalAlignment.TOP
                        else -> org.apache.poi.ss.usermodel.VerticalAlignment.TOP
                    }
            }
        }
    }
}

/**
 * Apache POI [CellExcelDataFormatAttribute] renderer.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class CellDataFormatAttributeRenderOperation: CellAttributeRenderOperation<ApachePoiRenderingContext, CellExcelDataFormatAttribute> {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java

    override fun attributeType(): Class<CellExcelDataFormatAttribute> = CellExcelDataFormatAttribute::class.java

    override fun renderAttribute(
        renderingContext: ApachePoiRenderingContext,
        context: CellContext,
        attribute: CellExcelDataFormatAttribute,
    ) {
        renderingContext.provideCellStyle(
            sheetName = context.getTableId(),
            rowIndex = context.getRow(),
            columnIndex = context.getColumn(),
            provideCellStyle = { getCachedStyle(renderingContext, context) }
        ).let {
            it.dataFormat = renderingContext.workbook().createDataFormat().getFormat(attribute.dataFormat)
        }
    }
}

/**
 * Apache POI [ColumnWidthAttribute] renderer.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class ColumnWidthAttributeRenderOperation: ColumnAttributeRenderOperation<ApachePoiRenderingContext, ColumnWidthAttribute> {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java

    override fun attributeType(): Class<ColumnWidthAttribute> = ColumnWidthAttribute::class.java

    override fun renderAttribute(renderingContext: ApachePoiRenderingContext, context: ColumnOpeningContext, attribute: ColumnWidthAttribute) {
        renderingContext.provideSheet(context.getTableId()).let {
            if (attribute.auto == true || attribute.px <= 0) {
                if (!it.isColumnTrackedForAutoSizing(context.getColumn())) {
                    it.trackColumnForAutoSizing(context.getColumn())
                }
                it.autoSizeColumn(context.getColumn())
            } else {
                it.setColumnWidth(context.getColumn(), ApachePoiUtils.widthFromPixels(attribute.px))
            }
        }
    }
}

/**
 * Apache POI [RowHeightAttribute] renderer.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class RowHeightAttributeRenderOperation : RowAttributeRenderOperation<ApachePoiRenderingContext, RowHeightAttribute> {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java
    override fun attributeType(): Class<RowHeightAttribute> = RowHeightAttribute::class.java
    override fun renderAttribute(renderingContext: ApachePoiRenderingContext, context: RowOpeningContext, attribute: RowHeightAttribute) {
        renderingContext.provideRow(context.getTableId(), context.getRow()).height =
            ApachePoiUtils.heightFromPixels(attribute.px)
    }
}

/**
 * Apache POI [FilterAndSortTableAttribute] renderer.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class FilterAndSortTableAttributeRenderOperation: TableAttributeRenderOperation<ApachePoiRenderingContext, FilterAndSortTableAttribute> {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java
    override fun attributeType(): Class<FilterAndSortTableAttribute> = FilterAndSortTableAttribute::class.java
    override fun renderAttribute(renderingContext: ApachePoiRenderingContext, context: TableCreationContext, attribute: FilterAndSortTableAttribute) {
        renderingContext.workbook().creationHelper.createAreaReference(
            CellReference(attribute.rowRange.first, attribute.columnRange.first),
            CellReference(attribute.rowRange.last, attribute.columnRange.last)
        ).let { renderingContext.workbook().xssfWorkbook.getSheet(context.getTableId()).createTable(it) }
            .let {
                attribute.columnRange.forEach { index ->
                    it.ctTable.tableColumns.getTableColumnArray(index).id = (index + 1).toLong()
                }
                it.name = context.getTableId()
                it.displayName = context.getTableId()
                it.ctTable.addNewAutoFilter().ref = it.area.formatAsString()
            }
    }
}

/**
 * Apache POI [TemplateFileAttribute] renderer.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class TemplateFileAttributeRenderOperation: TableAttributeRenderOperation<ApachePoiRenderingContext, TemplateFileAttribute> {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java
    override fun attributeType(): Class<TemplateFileAttribute> = TemplateFileAttribute::class.java
    override fun priority() = -1
    override fun renderAttribute(renderingContext: ApachePoiRenderingContext, context: TableCreationContext, attribute: TemplateFileAttribute) {
        renderingContext.createWorkbook(FileInputStream(attribute.fileName), true).let {
            renderingContext.provideSheet(context.getTableId())
        }
    }
}

