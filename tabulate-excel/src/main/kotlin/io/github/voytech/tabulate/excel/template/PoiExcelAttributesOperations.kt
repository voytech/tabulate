package io.github.voytech.tabulate.excel.template

import io.github.voytech.tabulate.excel.model.attributes.CellExcelDataFormatAttribute
import io.github.voytech.tabulate.excel.model.attributes.FilterAndSortTableAttribute
import io.github.voytech.tabulate.excel.template.PoiExcelExportOperationsFactory.Companion.getCachedStyle
import io.github.voytech.tabulate.excel.template.poi.ApachePoiRenderingContext
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
import io.github.voytech.tabulate.template.context.ColumnContext
import io.github.voytech.tabulate.template.context.RowCellContext
import io.github.voytech.tabulate.template.context.RowContext
import io.github.voytech.tabulate.template.context.TableContext
import io.github.voytech.tabulate.template.operations.BaseCellAttributeRenderOperation
import io.github.voytech.tabulate.template.operations.BaseColumnAttributeRenderOperation
import io.github.voytech.tabulate.template.operations.BaseRowAttributeRenderOperation
import io.github.voytech.tabulate.template.operations.BaseTableAttributeRenderOperation
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.FontUnderline
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import java.io.FileInputStream
import org.apache.poi.ss.usermodel.BorderStyle as PoiBorderStyle

class CellTextStylesAttributeRenderOperation(override val renderingContext: ApachePoiRenderingContext) :
    BaseCellAttributeRenderOperation<ApachePoiRenderingContext, CellTextStylesAttribute>(renderingContext) {

    override fun attributeType(): Class<CellTextStylesAttribute> = CellTextStylesAttribute::class.java

    override fun renderAttribute(context: RowCellContext, attribute: CellTextStylesAttribute) {
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

class CellBackgroundAttributeRenderOperation(override val renderingContext: ApachePoiRenderingContext) :
    BaseCellAttributeRenderOperation<ApachePoiRenderingContext, CellBackgroundAttribute>(renderingContext) {
    override fun attributeType(): Class<CellBackgroundAttribute> = CellBackgroundAttribute::class.java

    override fun renderAttribute(
        context: RowCellContext,
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
            FillPatternType.valueOf(background.fill?.getCellFillId() ?: "NO_FILL")
        } catch (e: IllegalArgumentException) {
            FillPatternType.NO_FILL
        }
    }
}

class CellBordersAttributeRenderOperation(override val renderingContext: ApachePoiRenderingContext) :
    BaseCellAttributeRenderOperation<ApachePoiRenderingContext, CellBordersAttribute>(renderingContext) {
    override fun attributeType(): Class<CellBordersAttribute> = CellBordersAttribute::class.java

    override fun renderAttribute(context: RowCellContext, attribute: CellBordersAttribute) {
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

class CellAlignmentAttributeRenderOperation(override val renderingContext: ApachePoiRenderingContext) :
    BaseCellAttributeRenderOperation<ApachePoiRenderingContext, CellAlignmentAttribute>(renderingContext) {

    override fun attributeType(): Class<CellAlignmentAttribute> = CellAlignmentAttribute::class.java

    override fun renderAttribute(context: RowCellContext, attribute: CellAlignmentAttribute) {
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

class CellDataFormatAttributeRenderOperation(override val renderingContext: ApachePoiRenderingContext) :
    BaseCellAttributeRenderOperation<ApachePoiRenderingContext, CellExcelDataFormatAttribute>(renderingContext) {

    override fun attributeType(): Class<CellExcelDataFormatAttribute> = CellExcelDataFormatAttribute::class.java

    override fun renderAttribute(
        context: RowCellContext,
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

class ColumnWidthAttributeRenderOperation(override val renderingContext: ApachePoiRenderingContext) :
    BaseColumnAttributeRenderOperation<ApachePoiRenderingContext, ColumnWidthAttribute>(renderingContext) {

    override fun attributeType(): Class<ColumnWidthAttribute> = ColumnWidthAttribute::class.java

    override fun renderAttribute(context: ColumnContext, attribute: ColumnWidthAttribute) {
        renderingContext.provideSheet(context.getTableId()).let {
            if (attribute.auto == true || attribute.px <= 0) {
                if (!it.isColumnTrackedForAutoSizing(context.columnIndex)) {
                    it.trackColumnForAutoSizing(context.columnIndex)
                }
                it.autoSizeColumn(context.columnIndex)
            } else {
                it.setColumnWidth(context.columnIndex, ApachePoiUtils.widthFromPixels(attribute.px))
            }
        }
    }
}

class RowHeightAttributeRenderOperation<T>(override val renderingContext: ApachePoiRenderingContext) :
    BaseRowAttributeRenderOperation<ApachePoiRenderingContext, T, RowHeightAttribute>(renderingContext) {
    override fun attributeType(): Class<RowHeightAttribute> = RowHeightAttribute::class.java
    override fun renderAttribute(context: RowContext<T>, attribute: RowHeightAttribute) {
        renderingContext.provideRow(context.getTableId(), context.rowIndex).height =
            ApachePoiUtils.heightFromPixels(attribute.px)
    }
}

class FilterAndSortTableAttributeRenderOperation(override val renderingContext: ApachePoiRenderingContext) :
    BaseTableAttributeRenderOperation<ApachePoiRenderingContext, FilterAndSortTableAttribute>(renderingContext) {
    override fun attributeType(): Class<FilterAndSortTableAttribute> = FilterAndSortTableAttribute::class.java
    override fun renderAttribute(table: TableContext, attribute: FilterAndSortTableAttribute) {
        renderingContext.workbook().creationHelper.createAreaReference(
            CellReference(attribute.rowRange.first, attribute.columnRange.first),
            CellReference(attribute.rowRange.last, attribute.columnRange.last)
        ).let { renderingContext.workbook().xssfWorkbook.getSheet(table.getTableId()).createTable(it) }
            .let {
                attribute.columnRange.forEach { index ->
                    it.ctTable.tableColumns.getTableColumnArray(index).id = (index + 1).toLong()
                }
                it.name = table.getTableId()
                it.displayName = table.getTableId()
                it.ctTable.addNewAutoFilter().ref = it.area.formatAsString()
            }
    }
}

class TemplateFileAttributeRenderOperation(override val renderingContext: ApachePoiRenderingContext) :
    BaseTableAttributeRenderOperation<ApachePoiRenderingContext, TemplateFileAttribute>(renderingContext) {
    override fun attributeType(): Class<TemplateFileAttribute> = TemplateFileAttribute::class.java
    override fun priority() = -1
    override fun renderAttribute(table: TableContext, attribute: TemplateFileAttribute) {
        renderingContext.createWorkbook(FileInputStream(attribute.fileName), true).let {
            renderingContext.provideSheet(table.getTableId())
        }
    }
}

