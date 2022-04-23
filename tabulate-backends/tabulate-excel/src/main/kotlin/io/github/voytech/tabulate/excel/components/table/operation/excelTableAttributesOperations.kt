package io.github.voytech.tabulate.excel.components.table.operation

import io.github.voytech.tabulate.components.table.model.attributes.cell.CellAlignmentAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellBackgroundAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.*
import io.github.voytech.tabulate.components.table.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.components.table.model.attributes.row.RowBordersAttribute
import io.github.voytech.tabulate.components.table.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.components.table.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.template.operation.*
import io.github.voytech.tabulate.excel.ApachePoiRenderingContext
import io.github.voytech.tabulate.excel.components.table.model.attributes.CellCommentAttribute
import io.github.voytech.tabulate.excel.components.table.model.attributes.CellExcelDataFormatAttribute
import io.github.voytech.tabulate.excel.components.table.model.attributes.FilterAndSortTableAttribute
import io.github.voytech.tabulate.excel.components.table.model.attributes.PrintingAttribute
import io.github.voytech.tabulate.excel.components.table.model.resolveBorderStyle
import io.github.voytech.tabulate.excel.components.table.operation.ExcelTableOperations.Companion.getCachedStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.FontUnderline
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.CellReference
import org.apache.poi.ss.util.RegionUtil
import org.apache.poi.util.Units
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import java.io.FileInputStream


/**
 * Apache POI [CellTextStylesAttribute] renderer.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class CellTextStylesAttributeRenderOperation :
    CellAttributeRenderOperation<ApachePoiRenderingContext, CellTextStylesAttribute>() {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java

    override fun attributeClass(): Class<CellTextStylesAttribute> = CellTextStylesAttribute::class.java

    override fun renderAttribute(
        renderingContext: ApachePoiRenderingContext,
        context: CellContext,
        attribute: CellTextStylesAttribute
    ) {
        renderingContext.provideCellStyle(
            sheetName = context.getSheetName(),
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
class CellBackgroundAttributeRenderOperation :
    CellAttributeRenderOperation<ApachePoiRenderingContext, CellBackgroundAttribute>() {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java

    override fun attributeClass(): Class<CellBackgroundAttribute> = CellBackgroundAttribute::class.java

    override fun renderAttribute(
        renderingContext: ApachePoiRenderingContext,
        context: CellContext,
        attribute: CellBackgroundAttribute,
    ) {
        renderingContext.provideCellStyle(
            sheetName = context.getSheetName(),
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
class CellBordersAttributeRenderOperation :
    CellAttributeRenderOperation<ApachePoiRenderingContext, CellBordersAttribute>() {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java

    override fun attributeClass(): Class<CellBordersAttribute> = CellBordersAttribute::class.java

    override fun renderAttribute(
        renderingContext: ApachePoiRenderingContext,
        context: CellContext,
        attribute: CellBordersAttribute
    ) {
        renderingContext.provideCellStyle(
            sheetName = context.getSheetName(),
            rowIndex = context.getRow(),
            columnIndex = context.getColumn(),
            provideCellStyle = { getCachedStyle(renderingContext, context) }
        ).let {
            attribute.leftBorderColor?.run {
                (it as XSSFCellStyle).setLeftBorderColor(
                    ApachePoiRenderingContext.color(
                        this
                    )
                )
            }
            attribute.rightBorderColor?.run {
                (it as XSSFCellStyle).setRightBorderColor(
                    ApachePoiRenderingContext.color(
                        this
                    )
                )
            }
            attribute.topBorderColor?.run { (it as XSSFCellStyle).setTopBorderColor(ApachePoiRenderingContext.color(this)) }
            attribute.bottomBorderColor?.run {
                (it as XSSFCellStyle).setBottomBorderColor(
                    ApachePoiRenderingContext.color(
                        this
                    )
                )
            }
            attribute.leftBorderStyle?.run { it.borderLeft = resolveBorderStyle() }
            attribute.rightBorderStyle?.run { it.borderRight = resolveBorderStyle() }
            attribute.topBorderStyle?.run { it.borderTop = resolveBorderStyle() }
            attribute.bottomBorderStyle?.run { it.borderBottom = resolveBorderStyle() }
        }
    }

}


/**
 * Apache POI [RowBordersAttribute] renderer.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
class RowBordersAttributeRenderOperation :
    RowAttributeRenderOperation<ApachePoiRenderingContext, RowBordersAttribute, RowClosingContext<*>>(RowClosingContext::class.java) {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java

    override fun attributeClass(): Class<RowBordersAttribute> = RowBordersAttribute::class.java

    override fun renderAttribute(
        renderingContext: ApachePoiRenderingContext,
        context: RowClosingContext<*>,
        attribute: RowBordersAttribute
    ) {
        renderingContext.provideSheet(context.getSheetName()).let { sheet ->
            context.rowCellValues.values.let {
                val region = CellRangeAddress(context.getRow(), context.getRow(), it.first().columnIndex, it.last().columnIndex)
                attribute.bottomBorderStyle?.run { RegionUtil.setBorderBottom(resolveBorderStyle(),region, sheet) }
                attribute.bottomBorderColor?.run {
                    RegionUtil.setBottomBorderColor(ApachePoiRenderingContext.color(this).index.toInt(),region, sheet)
                }

                attribute.topBorderStyle?.run { RegionUtil.setBorderTop(resolveBorderStyle(),region, sheet) }
                attribute.topBorderColor?.run {
                    RegionUtil.setTopBorderColor(ApachePoiRenderingContext.color(this).index.toInt(),region, sheet)
                }

                attribute.leftBorderStyle?.run { RegionUtil.setBorderLeft(resolveBorderStyle(),region, sheet) }
                attribute.leftBorderColor?.run {
                    RegionUtil.setLeftBorderColor(ApachePoiRenderingContext.color(this).index.toInt(),region, sheet)
                }

                attribute.rightBorderStyle?.run { RegionUtil.setBorderRight(resolveBorderStyle(),region, sheet) }
                attribute.rightBorderColor?.run {
                    RegionUtil.setRightBorderColor(ApachePoiRenderingContext.color(this).index.toInt(),region, sheet)
                }
            }
        }
    }

}


/**
 * Apache POI [CellAlignmentAttribute] renderer.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class CellAlignmentAttributeRenderOperation :
    CellAttributeRenderOperation<ApachePoiRenderingContext, CellAlignmentAttribute>() {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java

    override fun attributeClass(): Class<CellAlignmentAttribute> = CellAlignmentAttribute::class.java

    override fun renderAttribute(
        renderingContext: ApachePoiRenderingContext,
        context: CellContext,
        attribute: CellAlignmentAttribute
    ) {
        renderingContext.provideCellStyle(
            sheetName = context.getSheetName(),
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
class CellDataFormatAttributeRenderOperation :
    CellAttributeRenderOperation<ApachePoiRenderingContext, CellExcelDataFormatAttribute>() {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java

    override fun attributeClass(): Class<CellExcelDataFormatAttribute> = CellExcelDataFormatAttribute::class.java

    override fun renderAttribute(
        renderingContext: ApachePoiRenderingContext,
        context: CellContext,
        attribute: CellExcelDataFormatAttribute,
    ) {
        renderingContext.provideCellStyle(
            sheetName = context.getSheetName(),
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
class ColumnWidthAttributeRenderOperation :
    ColumnAttributeRenderOperation<ApachePoiRenderingContext, ColumnWidthAttribute, ColumnOpeningContext>(
        ColumnOpeningContext::class.java
    ) {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java

    override fun attributeClass(): Class<ColumnWidthAttribute> = ColumnWidthAttribute::class.java

    override fun renderAttribute(
        renderingContext: ApachePoiRenderingContext,
        context: ColumnOpeningContext,
        attribute: ColumnWidthAttribute
    ) {
        renderingContext.provideSheet(context.getSheetName()).let {
            if (attribute.auto) {
                if (!it.isColumnTrackedForAutoSizing(context.getColumn())) {
                    it.trackColumnForAutoSizing(context.getColumn())
                }
                it.autoSizeColumn(context.getColumn())
            } else if (attribute.px > 0){
                it.setColumnWidth(context.getColumn(), widthFromPixels(attribute.px))
            }
        }
    }

    companion object {
        private const val EXCEL_COLUMN_WIDTH_FACTOR: Short = 256
        private const val UNIT_OFFSET_LENGTH = 7
        private val UNIT_OFFSET_MAP = intArrayOf(0, 36, 73, 109, 146, 182, 219)

        fun widthFromPixels(pxs: Int): Int {
            var widthUnits = EXCEL_COLUMN_WIDTH_FACTOR * (pxs / UNIT_OFFSET_LENGTH)
            widthUnits += UNIT_OFFSET_MAP[pxs % UNIT_OFFSET_LENGTH]
            return widthUnits
        }
    }
}

/**
 * Apache POI [RowHeightAttribute] renderer.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class RowHeightAttributeRenderOperation :
    RowAttributeRenderOperation<ApachePoiRenderingContext, RowHeightAttribute, RowOpeningContext>(RowOpeningContext::class.java) {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java
    override fun attributeClass(): Class<RowHeightAttribute> = RowHeightAttribute::class.java
    override fun renderAttribute(
        renderingContext: ApachePoiRenderingContext,
        context: RowOpeningContext,
        attribute: RowHeightAttribute
    ) {
        renderingContext.provideRow(context.getSheetName(), context.getRow()).heightInPoints =
            Units.pixelToPoints(attribute.px.toDouble()).toFloat()
    }
}

/**
 * Apache POI [FilterAndSortTableAttribute] renderer.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class FilterAndSortTableAttributeRenderOperation :
    TableAttributeRenderOperation<ApachePoiRenderingContext, FilterAndSortTableAttribute, TableOpeningContext>(
        TableOpeningContext::class.java
    ) {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java
    override fun attributeClass(): Class<FilterAndSortTableAttribute> = FilterAndSortTableAttribute::class.java
    override fun renderAttribute(
        renderingContext: ApachePoiRenderingContext,
        context: TableOpeningContext,
        attribute: FilterAndSortTableAttribute
    ) {
        renderingContext.workbook().creationHelper.createAreaReference(
            CellReference(attribute.rowRange.first, attribute.columnRange.first),
            CellReference(attribute.rowRange.last, attribute.columnRange.last)
        ).let { renderingContext.workbook().xssfWorkbook.getSheet(context.getSheetName()).createTable(it) }
            .let {
                attribute.columnRange.forEach { index ->
                    it.ctTable.tableColumns.getTableColumnArray(index).id = (index + 1).toLong()
                }
                it.name = context.getSheetName()
                it.displayName = context.getSheetName()
                it.ctTable.addNewAutoFilter().ref = it.area.formatAsString()
            }
    }
}

/**
 * Apache POI [TemplateFileAttribute] renderer.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class TemplateFileAttributeRenderOperation :
    TableAttributeRenderOperation<ApachePoiRenderingContext, TemplateFileAttribute, TableOpeningContext>(
        TableOpeningContext::class.java
    ) {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java
    override fun attributeClass(): Class<TemplateFileAttribute> = TemplateFileAttribute::class.java
    override fun priority() = -1
    override fun renderAttribute(
        renderingContext: ApachePoiRenderingContext,
        context: TableOpeningContext,
        attribute: TemplateFileAttribute
    ) {
        renderingContext.provideWorkbook(FileInputStream(attribute.fileName), true).let {
            renderingContext.provideSheet(context.getSheetName())
        }
    }
}

/**
 * Apache POI [CellCommentAttribute] renderer.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
class CellCommentAttributeRenderOperation :
    CellAttributeRenderOperation<ApachePoiRenderingContext, CellCommentAttribute>() {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java
    override fun attributeClass(): Class<CellCommentAttribute> = CellCommentAttribute::class.java
    override fun renderAttribute(
        renderingContext: ApachePoiRenderingContext,
        context: CellContext,
        attribute: CellCommentAttribute
    ) {
        renderingContext.provideCell(
            sheetName = context.getSheetName(),
            rowIndex = context.getRow(),
            columnIndex = context.getColumn(),
        ).let {
            it.cellComment = renderingContext.getDrawing(context.getSheetName())
                .createCellComment(renderingContext.createClientAnchor())
                .apply {
                    author = attribute.author
                    string = renderingContext.getCreationHelper().createRichTextString(attribute.comment)
                }
        }
    }
}

/**
 * Apache POI [TablePrintAttribute] renderer.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
class PrintingAttributeRenderOperation : TableAttributeRenderOperation<ApachePoiRenderingContext, PrintingAttribute, TableOpeningContext>(TableOpeningContext::class.java) {
    override fun renderingContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java
    override fun attributeClass(): Class<PrintingAttribute> = PrintingAttribute::class.java
    override fun renderAttribute(
        renderingContext: ApachePoiRenderingContext,
        context: TableOpeningContext,
        attribute: PrintingAttribute
    ) {
        renderingContext.workbook().let {
            it.setPrintArea(
                it.getSheetIndex(context.getSheetName()),
                attribute.firstPrintableColumn,
                attribute.lastPrintableColumn,
                attribute.firstPrintableRow,
                attribute.lastPrintableRow
            )
        }
        renderingContext.provideSheet(sheetName = context.getSheetName()).let {
            it.printSetup.apply {
                copies = attribute.numberOfCopies
                fitHeight = attribute.fitHeight
                fitWidth = attribute.fitWidth
                draft = attribute.isDraft
                noColor = attribute.blackAndWhite
                noOrientation = attribute.noOrientation
                leftToRight = attribute.leftToRight
                pageStart = attribute.firstPageNumber
                usePage = attribute.printPageNumber
                landscape = attribute.landscape
                paperSize = attribute.paperSize
                footerMargin = attribute.footerMargin
                headerMargin = attribute.headerMargin
            }
            it.header.left = attribute.headerLeft
            it.header.center = attribute.headerCenter
            it.header.right = attribute.headerRight
            it.footer.left = attribute.footerLeft
            it.footer.center = attribute.footerCenter
            it.footer.right = attribute.footerRight
        }
    }
}
