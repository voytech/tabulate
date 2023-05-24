package io.github.voytech.tabulate.excel.components.table.operation

import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.*
import io.github.voytech.tabulate.components.table.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.model.Height
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.core.model.alignment.DefaultHorizontalAlignment
import io.github.voytech.tabulate.core.model.alignment.DefaultVerticalAlignment
import io.github.voytech.tabulate.core.model.attributes.*
import io.github.voytech.tabulate.core.model.background.DefaultFillType
import io.github.voytech.tabulate.excel.ApachePoiRenderingContext
import io.github.voytech.tabulate.excel.components.table.model.attributes.CellCommentAttribute
import io.github.voytech.tabulate.excel.components.table.model.attributes.CellExcelDataFormatAttribute
import io.github.voytech.tabulate.excel.components.table.model.attributes.FilterAndSortTableAttribute
import io.github.voytech.tabulate.excel.components.table.model.attributes.PrintingAttribute
import io.github.voytech.tabulate.excel.components.table.model.resolveBorderStyle
import io.github.voytech.tabulate.excel.createFontFrom
import io.github.voytech.tabulate.excel.getCachedStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.CellReference
import org.apache.poi.ss.util.RegionUtil
import org.apache.poi.util.Units
import org.apache.poi.xssf.streaming.SXSSFSheet
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFRichTextString
import java.io.FileInputStream


/**
 * Apache POI [CellTextStylesAttribute] renderer.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class CellTextStylesAttributeRenderOperation :
    CellAttributeRenderOperation<ApachePoiRenderingContext, TextStylesAttribute>() {

    override operator fun invoke(
        renderingContext: ApachePoiRenderingContext,
        context: CellRenderable,
        attribute: TextStylesAttribute,
    ) = with(renderingContext) {
        provideCellStyle(
            sheetName = context.getSheetName(),
            rowIndex = context.getAbsoluteRow(context.rowIndex),
            columnIndex = context.getAbsoluteColumn(context.columnIndex),
            provideCellStyle = { getCachedStyle(context) }
        ).let {
            it.setFont(createFontFrom(context, attribute))
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
    CellAttributeRenderOperation<ApachePoiRenderingContext, BackgroundAttribute>() {

    override operator fun invoke(
        renderingContext: ApachePoiRenderingContext,
        context: CellRenderable,
        attribute: BackgroundAttribute,
    ) = with(renderingContext) {
        provideCellStyle(
            sheetName = context.getSheetName(),
            rowIndex = context.getAbsoluteRow(context.rowIndex),
            columnIndex = context.getAbsoluteColumn(context.columnIndex),
            provideCellStyle = { getCachedStyle(context) }
        ).let {
            if (attribute.color != null) {
                (it as XSSFCellStyle).setFillForegroundColor(ApachePoiRenderingContext.color(attribute.color!!))
            }
            when (attribute.fill) {
                DefaultFillType.SOLID -> it.fillPattern = FillPatternType.SOLID_FOREGROUND
                DefaultFillType.BRICKS -> it.fillPattern = FillPatternType.BRICKS
                DefaultFillType.WIDE_DOTS -> it.fillPattern = FillPatternType.ALT_BARS
                DefaultFillType.DIAMONDS -> it.fillPattern = FillPatternType.DIAMONDS
                DefaultFillType.SMALL_DOTS -> it.fillPattern = FillPatternType.FINE_DOTS
                DefaultFillType.SQUARES -> it.fillPattern = FillPatternType.SQUARES
                DefaultFillType.LARGE_SPOTS -> it.fillPattern = FillPatternType.BIG_SPOTS
                else -> it.fillPattern = resolveFillPatternByEnumName(attribute)
            }
        }
    }

    private fun resolveFillPatternByEnumName(background: BackgroundAttribute): FillPatternType {
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
    CellAttributeRenderOperation<ApachePoiRenderingContext, BordersAttribute>() {

    override operator fun invoke(
        renderingContext: ApachePoiRenderingContext,
        context: CellRenderable,
        attribute: BordersAttribute,
    ): Unit = with(renderingContext) {
        renderingContext.provideCellStyle(
            sheetName = context.getSheetName(),
            rowIndex = context.getAbsoluteRow(context.rowIndex),
            columnIndex = context.getAbsoluteColumn(context.columnIndex),
            provideCellStyle = { getCachedStyle(context) }
        ).let {
            attribute.leftBorderColor?.run {
                (it as XSSFCellStyle).setLeftBorderColor(ApachePoiRenderingContext.color(this))
            }
            attribute.rightBorderColor?.run {
                (it as XSSFCellStyle).setRightBorderColor(ApachePoiRenderingContext.color(this))
            }
            attribute.topBorderColor?.run { (it as XSSFCellStyle).setTopBorderColor(ApachePoiRenderingContext.color(this)) }
            attribute.bottomBorderColor?.run {
                (it as XSSFCellStyle).setBottomBorderColor(ApachePoiRenderingContext.color(this))
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
class RowBordersAttributeRenderOperation<T: Any> :
    RowAttributeRenderOperation<ApachePoiRenderingContext, BordersAttribute, RowEndRenderable<T>>() {

    override operator fun invoke(
        renderingContext: ApachePoiRenderingContext,
        context: RowEndRenderable<T>,
        attribute: BordersAttribute,
    ): Unit = with(renderingContext) {
        provideSheet(context.getSheetName()).let { sheet ->
            context.rowCellValues.values.let {
                val absoluteRow = context.getAbsoluteRow(context.rowIndex)
                val absoluteColumnStart = context.getAbsoluteColumn(it.first().columnIndex)
                val absoluteColumnEnd = context.getAbsoluteColumn(it.last().columnIndex)
                val region = CellRangeAddress(absoluteRow, absoluteRow, absoluteColumnStart, absoluteColumnEnd)
                attribute.bottomBorderStyle?.run { RegionUtil.setBorderBottom(resolveBorderStyle(), region, sheet) }
                attribute.bottomBorderColor?.run {
                    RegionUtil.setBottomBorderColor(ApachePoiRenderingContext.color(this).index.toInt(), region, sheet)
                }

                attribute.topBorderStyle?.run { RegionUtil.setBorderTop(resolveBorderStyle(), region, sheet) }
                attribute.topBorderColor?.run {
                    RegionUtil.setTopBorderColor(ApachePoiRenderingContext.color(this).index.toInt(), region, sheet)
                }

                attribute.leftBorderStyle?.run { RegionUtil.setBorderLeft(resolveBorderStyle(), region, sheet) }
                attribute.leftBorderColor?.run {
                    RegionUtil.setLeftBorderColor(ApachePoiRenderingContext.color(this).index.toInt(), region, sheet)
                }

                attribute.rightBorderStyle?.run { RegionUtil.setBorderRight(resolveBorderStyle(), region, sheet) }
                attribute.rightBorderColor?.run {
                    RegionUtil.setRightBorderColor(ApachePoiRenderingContext.color(this).index.toInt(), region, sheet)
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
    CellAttributeRenderOperation<ApachePoiRenderingContext, AlignmentAttribute>() {

    override operator fun invoke(
        renderingContext: ApachePoiRenderingContext,
        context: CellRenderable,
        attribute: AlignmentAttribute,
    ) = with(renderingContext) {
        provideCellStyle(
            sheetName = context.getSheetName(),
            rowIndex = context.getAbsoluteRow(context.rowIndex),
            columnIndex = context.getAbsoluteColumn(context.columnIndex),
            provideCellStyle = { getCachedStyle(context) }
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

    override operator fun invoke(
        renderingContext: ApachePoiRenderingContext,
        context: CellRenderable,
        attribute: CellExcelDataFormatAttribute,
    ) = with(renderingContext) {
        provideCellStyle(
            sheetName = context.getSheetName(),
            rowIndex = context.getAbsoluteRow(context.rowIndex),
            columnIndex = context.getAbsoluteColumn(context.columnIndex),
            provideCellStyle = { getCachedStyle(context) }
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
    ColumnAttributeRenderOperation<ApachePoiRenderingContext, WidthAttribute, ColumnEndRenderable>() {

    override operator fun invoke(
        renderingContext: ApachePoiRenderingContext,
        context: ColumnEndRenderable,
        attribute: WidthAttribute,
    ): Unit = with(renderingContext) {
        provideSheet(context.getSheetName()).let {
            val absoluteColumn = context.getAbsoluteColumn(context.getColumn())
            if (attribute.auto) {
                if (!it.isColumnTrackedForAutoSizing(absoluteColumn)) {
                    it.trackColumnForAutoSizing(absoluteColumn)
                }
                it.autoSizeColumn(absoluteColumn)
            } else if (attribute.value.value > 0) {
                it.setColumnWidth(absoluteColumn, widthFromPixels(attribute.value.switchUnitOfMeasure(UnitsOfMeasure.PX)))
            }
            interceptMeasures(it, context, absoluteColumn)
        }
    }

    private fun ApachePoiRenderingContext.interceptMeasures(
        sheet: SXSSFSheet,
        context: ColumnEndRenderable,
        absoluteColumn: Int,
    ) {
        sheet.getColumnWidthInPixels(absoluteColumn).run {
            context.setAbsoluteColumnWidth(absoluteColumn, Width(this, UnitsOfMeasure.PX))
        }
    }

    companion object {
        private const val EXCEL_COLUMN_WIDTH_FACTOR: Short = 256
        private const val UNIT_OFFSET_LENGTH = 7
        private val UNIT_OFFSET_MAP = intArrayOf(0, 36, 73, 109, 146, 182, 219)

        fun widthFromPixels(pxs: Width): Int {
            var widthUnits = EXCEL_COLUMN_WIDTH_FACTOR * (pxs.value.toInt() / UNIT_OFFSET_LENGTH)
            widthUnits += UNIT_OFFSET_MAP[pxs.value.toInt() % UNIT_OFFSET_LENGTH]
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
    RowAttributeRenderOperation<ApachePoiRenderingContext, HeightAttribute, RowStartRenderable>() {

    override operator fun invoke(
        renderingContext: ApachePoiRenderingContext,
        context: RowStartRenderable,
        attribute: HeightAttribute,
    ): Unit = with(renderingContext) {
        val absoluteRow = context.getAbsoluteColumn(context.getRow())
        renderingContext.provideRow(context.getSheetName(), absoluteRow).heightInPoints =
            Units.pixelToPoints(attribute.value.switchUnitOfMeasure(UnitsOfMeasure.PX).value.toDouble()).toFloat().also {
                context.setAbsoluteRowHeight(absoluteRow, Height(it, UnitsOfMeasure.PT))
            }
    }

}

/**
 * Apache POI [FilterAndSortTableAttribute] renderer.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class FilterAndSortTableAttributeRenderOperation :
    TableAttributeRenderOperation<ApachePoiRenderingContext, FilterAndSortTableAttribute, TableStartRenderable>() {

    override operator fun invoke(
        renderingContext: ApachePoiRenderingContext,
        context: TableStartRenderable,
        attribute: FilterAndSortTableAttribute,
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
    TableAttributeRenderOperation<ApachePoiRenderingContext, TemplateFileAttribute, TableStartRenderable>() {
    override fun invoke(
        renderingContext: ApachePoiRenderingContext,
        context: TableStartRenderable,
        attribute: TemplateFileAttribute,
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
    override operator fun invoke(
        renderingContext: ApachePoiRenderingContext,
        context: CellRenderable,
        attribute: CellCommentAttribute,
    ) = with(renderingContext) {
        provideCell(
            sheetName = context.getSheetName(),
            rowIndex = context.getAbsoluteRow(context.rowIndex),
            columnIndex = context.getAbsoluteColumn(context.columnIndex),
        ).let {
            it.cellComment = renderingContext.ensureDrawingPatriarch(context.getSheetName())
                .createCellComment(renderingContext.createClientAnchor())
                .apply {
                    author = attribute.author
                    string = renderingContext.getCreationHelper().createRichTextString(attribute.comment) as XSSFRichTextString?
                }
        }
    }
}

/**
 * Apache POI [TablePrintAttribute] renderer.
 * @author Wojciech Mąka
 * @since 0.2.0
 */
class PrintingAttributeRenderOperation :
    TableAttributeRenderOperation<ApachePoiRenderingContext, PrintingAttribute, TableStartRenderable>() {

    override operator fun invoke(
        renderingContext: ApachePoiRenderingContext,
        context: TableStartRenderable,
        attribute: PrintingAttribute,
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
