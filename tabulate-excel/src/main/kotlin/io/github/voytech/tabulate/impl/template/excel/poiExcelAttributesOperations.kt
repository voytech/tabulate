package io.github.voytech.tabulate.impl.template.excel

import io.github.voytech.tabulate.core.model.Table
import io.github.voytech.tabulate.core.model.attributes.cell.CellAlignmentAttribute
import io.github.voytech.tabulate.core.model.attributes.cell.CellBackgroundAttribute
import io.github.voytech.tabulate.core.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.core.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.core.model.attributes.cell.enums.*
import io.github.voytech.tabulate.core.model.attributes.cell.enums.contract.BorderStyle
import io.github.voytech.tabulate.core.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.core.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.core.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.core.template.context.ColumnContext
import io.github.voytech.tabulate.core.template.context.RowCellContext
import io.github.voytech.tabulate.core.template.context.RowContext
import io.github.voytech.tabulate.core.template.operations.AdaptingCellAttributeRenderOperation
import io.github.voytech.tabulate.core.template.operations.AdaptingColumnAttributeRenderOperation
import io.github.voytech.tabulate.core.template.operations.AdaptingRowAttributeRenderOperation
import io.github.voytech.tabulate.core.template.operations.AdaptingTableAttributeRenderOperation
import io.github.voytech.tabulate.impl.template.excel.PoiExcelExportOperationsFactory.Companion.getCachedStyle
import io.github.voytech.tabulate.impl.template.excel.wrapper.ApachePoiExcelFacade
import io.github.voytech.tabulate.impl.template.excel.wrapper.ApachePoiUtils
import io.github.voytech.tabulate.impl.template.model.attributes.CellExcelDataFormatAttribute
import io.github.voytech.tabulate.impl.template.model.attributes.FilterAndSortTableAttribute
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.FontUnderline
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import java.io.FileInputStream
import org.apache.poi.ss.usermodel.BorderStyle as PoiBorderStyle

class CellTextStylesAttributeRenderOperation(override val adaptee: ApachePoiExcelFacade) :
    AdaptingCellAttributeRenderOperation<ApachePoiExcelFacade, CellTextStylesAttribute>(adaptee) {

    override fun attributeType(): Class<CellTextStylesAttribute> = CellTextStylesAttribute::class.java

    override fun renderAttribute(context: RowCellContext, attribute: CellTextStylesAttribute) {
        adaptee.assertCellStyle(
            sheetName = context.getTableId(),
            rowIndex = context.rowIndex,
            columnIndex = context.columnIndex,
            provideCellStyle = { getCachedStyle(adaptee, context) }
        ).let {
            val font: XSSFFont = adaptee.workbook().createFont() as XSSFFont
            attribute.fontFamily?.run { font.fontName = this }
            attribute.fontColor?.run { font.setColor(ApachePoiExcelFacade.color(this)) }
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

class CellBackgroundAttributeRenderOperation(override val adaptee: ApachePoiExcelFacade) :
    AdaptingCellAttributeRenderOperation<ApachePoiExcelFacade, CellBackgroundAttribute>(adaptee) {
    override fun attributeType(): Class<CellBackgroundAttribute> = CellBackgroundAttribute::class.java

    override fun renderAttribute(
        context: RowCellContext,
        attribute: CellBackgroundAttribute,
    ) {
        adaptee.assertCellStyle(
            sheetName = context.getTableId(),
            rowIndex = context.rowIndex,
            columnIndex = context.columnIndex,
            provideCellStyle = { getCachedStyle(adaptee, context) }
        ).let {
            if (attribute.color != null) {
                (it as XSSFCellStyle).setFillForegroundColor(ApachePoiExcelFacade.color(attribute.color!!))
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

class CellBordersAttributeRenderOperation(override val adaptee: ApachePoiExcelFacade) :
    AdaptingCellAttributeRenderOperation<ApachePoiExcelFacade, CellBordersAttribute>(adaptee) {
    override fun attributeType(): Class<CellBordersAttribute> = CellBordersAttribute::class.java

    override fun renderAttribute(context: RowCellContext, attribute: CellBordersAttribute) {
        adaptee.assertCellStyle(
            sheetName = context.getTableId(),
            rowIndex = context.rowIndex,
            columnIndex = context.columnIndex,
            provideCellStyle = { getCachedStyle(adaptee, context) }
        ).let {
            attribute.leftBorderColor?.run { (it as XSSFCellStyle).setLeftBorderColor(ApachePoiExcelFacade.color(this)) }
            attribute.rightBorderColor?.run { (it as XSSFCellStyle).setRightBorderColor(ApachePoiExcelFacade.color(this)) }
            attribute.topBorderColor?.run { (it as XSSFCellStyle).setTopBorderColor(ApachePoiExcelFacade.color(this)) }
            attribute.bottomBorderColor?.run {
                (it as XSSFCellStyle).setBottomBorderColor(
                    ApachePoiExcelFacade.color(
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

class CellAlignmentAttributeRenderOperation(override val adaptee: ApachePoiExcelFacade) :
    AdaptingCellAttributeRenderOperation<ApachePoiExcelFacade, CellAlignmentAttribute>(adaptee) {

    override fun attributeType(): Class<out CellAlignmentAttribute> = CellAlignmentAttribute::class.java

    override fun renderAttribute(context: RowCellContext, attribute: CellAlignmentAttribute) {
        adaptee.assertCellStyle(
            sheetName = context.getTableId(),
            rowIndex = context.rowIndex,
            columnIndex = context.columnIndex,
            provideCellStyle = { getCachedStyle(adaptee, context) }
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

class CellDataFormatAttributeRenderOperation(override val adaptee: ApachePoiExcelFacade) :
    AdaptingCellAttributeRenderOperation<ApachePoiExcelFacade, CellExcelDataFormatAttribute>(adaptee) {

    override fun attributeType(): Class<out CellExcelDataFormatAttribute> = CellExcelDataFormatAttribute::class.java

    override fun renderAttribute(
        context: RowCellContext,
        attribute: CellExcelDataFormatAttribute,
    ) {
        adaptee.assertCellStyle(
            sheetName = context.getTableId(),
            rowIndex = context.rowIndex,
            columnIndex = context.columnIndex,
            provideCellStyle = { getCachedStyle(adaptee, context) }
        ).let {
            it.dataFormat = adaptee.workbook().createDataFormat().getFormat(attribute.dataFormat)
        }
    }
}

class ColumnWidthAttributeRenderOperation(override val adaptee: ApachePoiExcelFacade) :
    AdaptingColumnAttributeRenderOperation<ApachePoiExcelFacade, ColumnWidthAttribute>(adaptee) {

    override fun attributeType(): Class<out ColumnWidthAttribute> = ColumnWidthAttribute::class.java

    override fun renderAttribute(context: ColumnContext, attribute: ColumnWidthAttribute) {
        adaptee.assertSheet(context.getTableId()).let {
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

class RowHeightAttributeRenderOperation<T>(override val adaptee: ApachePoiExcelFacade) :
    AdaptingRowAttributeRenderOperation<ApachePoiExcelFacade, T, RowHeightAttribute>(adaptee) {
    override fun attributeType(): Class<out RowHeightAttribute> = RowHeightAttribute::class.java
    override fun renderAttribute(context: RowContext<T>, attribute: RowHeightAttribute) {
        adaptee.assertRow(context.getTableId(), context.rowIndex).height =
            ApachePoiUtils.heightFromPixels(attribute.px)
    }
}

class FilterAndSortTableAttributeRenderOperation(override val adaptee: ApachePoiExcelFacade) :
    AdaptingTableAttributeRenderOperation<ApachePoiExcelFacade, FilterAndSortTableAttribute>(adaptee) {
    override fun attributeType(): Class<out FilterAndSortTableAttribute> = FilterAndSortTableAttribute::class.java
    override fun renderAttribute(table: Table<*>, attribute: FilterAndSortTableAttribute) {
        adaptee.workbook().creationHelper.createAreaReference(
            CellReference(attribute.rowRange.first, attribute.columnRange.first),
            CellReference(attribute.rowRange.last, attribute.columnRange.last)
        ).let { adaptee.workbook().xssfWorkbook.getSheet(table.name).createTable(it) }
            .let {
                attribute.columnRange.forEach { index ->
                    it.ctTable.tableColumns.getTableColumnArray(index).id = (index + 1).toLong()
                }
                it.name = table.name
                it.displayName = table.name
                it.ctTable.addNewAutoFilter().ref = it.area.formatAsString()
            }
    }
}

class TemplateFileAttributeRenderOperation(override val adaptee: ApachePoiExcelFacade) :
    AdaptingTableAttributeRenderOperation<ApachePoiExcelFacade, TemplateFileAttribute>(adaptee) {
    override fun attributeType(): Class<out TemplateFileAttribute> = TemplateFileAttribute::class.java
    override fun priority() = -1
    override fun renderAttribute(table: Table<*>, attribute: TemplateFileAttribute) {
        adaptee.createWorkbook(FileInputStream(attribute.fileName), true).let {
            adaptee.assertSheet(table.name!!)
        }
    }
}

internal fun tableAttributesOperations(adaptee: ApachePoiExcelFacade) = setOf(
    FilterAndSortTableAttributeRenderOperation(adaptee),
    TemplateFileAttributeRenderOperation(adaptee)
)

internal fun <T> rowAttributesOperations(adaptee: ApachePoiExcelFacade) = setOf(
    RowHeightAttributeRenderOperation<T>(adaptee)
)

internal fun cellAttributesOperations(adaptee: ApachePoiExcelFacade) = setOf(
    CellTextStylesAttributeRenderOperation(adaptee),
    CellBackgroundAttributeRenderOperation(adaptee),
    CellBordersAttributeRenderOperation(adaptee),
    CellAlignmentAttributeRenderOperation(adaptee),
    CellDataFormatAttributeRenderOperation(adaptee),
)

internal fun columnAttributesOperations(adaptee: ApachePoiExcelFacade) = setOf(
    ColumnWidthAttributeRenderOperation(adaptee)
)
