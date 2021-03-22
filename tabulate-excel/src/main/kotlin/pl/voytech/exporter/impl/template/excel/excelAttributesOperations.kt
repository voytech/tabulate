package pl.voytech.exporter.impl.template.excel

import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.FontUnderline
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellReference
import org.apache.poi.ss.util.SheetUtil
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.attributes.functional.FilterAndSortTableAttribute
import pl.voytech.exporter.core.model.attributes.style.*
import pl.voytech.exporter.core.model.attributes.style.enums.BorderStyle
import pl.voytech.exporter.core.model.attributes.style.enums.HorizontalAlignment
import pl.voytech.exporter.core.model.attributes.style.enums.VerticalAlignment
import pl.voytech.exporter.core.model.attributes.style.enums.WeightStyle
import pl.voytech.exporter.core.template.context.AttributedCell
import pl.voytech.exporter.core.template.context.AttributedColumn
import pl.voytech.exporter.core.template.context.AttributedRow
import pl.voytech.exporter.core.template.operations.AdaptingCellAttributeRenderOperation
import pl.voytech.exporter.core.template.operations.AdaptingColumnAttributeRenderOperation
import pl.voytech.exporter.core.template.operations.AdaptingRowAttributeRenderOperation
import pl.voytech.exporter.core.template.operations.AdaptingTableAttributeRenderOperation
import pl.voytech.exporter.core.template.operations.impl.getCachedValue
import pl.voytech.exporter.core.template.operations.impl.putCachedValue
import pl.voytech.exporter.impl.template.excel.wrapper.ApachePoiExcelFacade
import pl.voytech.exporter.impl.template.excel.wrapper.ApachePoiUtils
import java.awt.font.FontRenderContext
import java.awt.font.TextAttribute
import java.awt.font.TextLayout
import java.awt.geom.Rectangle2D
import java.text.AttributedString
import kotlin.math.roundToInt

class CellFontAttributeRenderOperation<T>(override val adaptee: ApachePoiExcelFacade) : AdaptingCellAttributeRenderOperation<ApachePoiExcelFacade,T, CellFontAttribute>(adaptee) {

    private val cellFontCacheKey = "cellFont"

    override fun attributeType(): Class<CellFontAttribute> = CellFontAttribute::class.java

    override fun renderAttribute(context: AttributedCell, attribute: CellFontAttribute) {
        adaptee.cellStyle(context).let {
            if (context.getCachedValue(cellFontCacheKey) == null) {
                val font: XSSFFont = adaptee.workbook().createFont() as XSSFFont
                attribute.fontFamily?.run { font.fontName = this }
                attribute.fontColor?.run { font.setColor(ApachePoiExcelFacade.color(this)) }
                attribute.fontSize?.run { font.fontHeightInPoints = toShort() }
                attribute.italic?.run { font.italic = this }
                attribute.strikeout?.run { font.strikeout = this }
                attribute.underline?.let { underline ->
                    if (underline) font.setUnderline(FontUnderline.SINGLE) else font.setUnderline(
                        FontUnderline.NONE
                    )
                }
                attribute.weight?.run { font.bold = this == WeightStyle.BOLD }
                it.setFont(font)
                context.putCachedValue(cellFontCacheKey, font)
            }
        }
    }
}

class CellBackgroundAttributeRenderOperation<T>(override val adaptee: ApachePoiExcelFacade) : AdaptingCellAttributeRenderOperation<ApachePoiExcelFacade,T,CellBackgroundAttribute>(adaptee) {
    override fun attributeType(): Class<CellBackgroundAttribute> = CellBackgroundAttribute::class.java

    override fun renderAttribute(
        context: AttributedCell,
        attribute: CellBackgroundAttribute
    ) {
        adaptee.cellStyle(context).let {
            (it as XSSFCellStyle).setFillForegroundColor(ApachePoiExcelFacade.color(attribute.color))
            it.fillPattern = FillPatternType.SOLID_FOREGROUND
        }
    }
}

class CellBordersAttributeRenderOperation<T>(override val adaptee: ApachePoiExcelFacade) : AdaptingCellAttributeRenderOperation<ApachePoiExcelFacade,T, CellBordersAttribute>(adaptee) {
    override fun attributeType(): Class<CellBordersAttribute> = CellBordersAttribute::class.java

    override fun renderAttribute(context: AttributedCell, attribute: CellBordersAttribute) {
        val toPoiStyle = { style: BorderStyle ->
            when (style) {
                BorderStyle.DASHED -> org.apache.poi.ss.usermodel.BorderStyle.DASHED
                BorderStyle.DOTTED -> org.apache.poi.ss.usermodel.BorderStyle.DOTTED
                BorderStyle.SOLID -> org.apache.poi.ss.usermodel.BorderStyle.THIN
                else -> org.apache.poi.ss.usermodel.BorderStyle.NONE
            }
        }
        adaptee.cellStyle(context).let {
            attribute.leftBorderColor?.run { (it as XSSFCellStyle).setLeftBorderColor(ApachePoiExcelFacade.color(this)) }
            attribute.rightBorderColor?.run { (it as XSSFCellStyle).setRightBorderColor(ApachePoiExcelFacade.color(this)) }
            attribute.topBorderColor?.run { (it as XSSFCellStyle).setTopBorderColor(ApachePoiExcelFacade.color(this)) }
            attribute.bottomBorderColor?.run { (it as XSSFCellStyle).setBottomBorderColor(ApachePoiExcelFacade.color(this)) }
            attribute.leftBorderStyle?.run { it.borderLeft = toPoiStyle(this) }
            attribute.rightBorderStyle?.run { it.borderRight = toPoiStyle(this) }
            attribute.topBorderStyle?.run { it.borderTop = toPoiStyle(this) }
            attribute.bottomBorderStyle?.run { it.borderBottom = toPoiStyle(this) }
        }
    }
}

class CellAlignmentAttributeRenderOperation<T>(override val adaptee: ApachePoiExcelFacade) : AdaptingCellAttributeRenderOperation<ApachePoiExcelFacade,T, CellAlignmentAttribute>(adaptee) {

    override fun attributeType(): Class<out CellAlignmentAttribute> = CellAlignmentAttribute::class.java

    override fun renderAttribute(context: AttributedCell, attribute: CellAlignmentAttribute) {
        adaptee.cellStyle(context).let {
            attribute.horizontal?.run {
                it.alignment =
                    when (this) {
                        HorizontalAlignment.CENTER -> org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER
                        HorizontalAlignment.LEFT -> org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT
                        HorizontalAlignment.RIGHT -> org.apache.poi.ss.usermodel.HorizontalAlignment.RIGHT
                        HorizontalAlignment.JUSTIFY -> org.apache.poi.ss.usermodel.HorizontalAlignment.FILL
                        else -> org.apache.poi.ss.usermodel.HorizontalAlignment.GENERAL
                    }
            }
            attribute.vertical?.run {
                it.verticalAlignment =
                    when (this) {
                        VerticalAlignment.MIDDLE -> org.apache.poi.ss.usermodel.VerticalAlignment.CENTER
                        VerticalAlignment.BOTTOM -> org.apache.poi.ss.usermodel.VerticalAlignment.BOTTOM
                        VerticalAlignment.TOP -> org.apache.poi.ss.usermodel.VerticalAlignment.TOP
                        else -> org.apache.poi.ss.usermodel.VerticalAlignment.TOP
                    }
            }
        }
    }

}

class CellDataFormatAttributeRenderOperation<T>(override val adaptee: ApachePoiExcelFacade) : AdaptingCellAttributeRenderOperation<ApachePoiExcelFacade, T, CellExcelDataFormatAttribute>(adaptee){

    private val cellStyleFormatKey = "cellStyleFormatKey"

    override fun attributeType(): Class<out CellExcelDataFormatAttribute> = CellExcelDataFormatAttribute::class.java

    override fun renderAttribute(
        context: AttributedCell,
        attribute: CellExcelDataFormatAttribute
    ) {
        //if (getCellCachedValue(context, cellStyleFormatKey) == null) {
            adaptee.cellStyle(context).let {
                attribute.dataFormat.run {
                    it.dataFormat = adaptee.workbook().createDataFormat().getFormat(this)
                    context.putCachedValue(cellStyleFormatKey, it.dataFormat)
                }
            }
        //}
    }

}

class ColumnWidthAttributeRenderOperation<T>(override val adaptee: ApachePoiExcelFacade) : AdaptingColumnAttributeRenderOperation<ApachePoiExcelFacade, T, ColumnWidthAttribute>(adaptee) {
    override fun attributeType(): Class<out ColumnWidthAttribute> = ColumnWidthAttribute::class.java

    private fun getStringWidth(text: String, cellFont: XSSFFont, workbook: Workbook): Int {
        val attributedString = AttributedString(text)
        attributedString.addAttribute(TextAttribute.FAMILY, cellFont.fontName, 0, text.length)
        attributedString.addAttribute(TextAttribute.SIZE, cellFont.fontHeightInPoints.toFloat())
        if (cellFont.bold) attributedString.addAttribute(
            TextAttribute.WEIGHT,
            TextAttribute.WEIGHT_BOLD,
            0,
            text.length
        )
        if (cellFont.italic) attributedString.addAttribute(
            TextAttribute.POSTURE,
            TextAttribute.POSTURE_OBLIQUE,
            0,
            text.length
        )

        val fontRenderContext = FontRenderContext(null, true, true)
        val layout = TextLayout(attributedString.iterator, fontRenderContext)
        val bounds: Rectangle2D = layout.bounds
        val frameWidth: Double = bounds.x + bounds.width
        val defaultCharWidth = SheetUtil.getDefaultCharWidth(workbook)
        return (frameWidth / defaultCharWidth * 256).roundToInt()
    }

    override fun renderAttribute(context: AttributedColumn, attribute: ColumnWidthAttribute) {
        adaptee.tableSheet(context.getTableId()).let {
            if (attribute.auto == true || attribute.width == -1) {
                if (!it.isColumnTrackedForAutoSizing(context.columnIndex)) {
                    it.trackColumnForAutoSizing(context.columnIndex)
                }
                it.autoSizeColumn(context.columnIndex)
            } else {
                it.setColumnWidth(context.columnIndex, ApachePoiUtils.widthFromPixels(attribute.width))
            }
        }
    }
}

class RowHeightAttributeRenderOperation<T>(override val adaptee: ApachePoiExcelFacade) : AdaptingRowAttributeRenderOperation<ApachePoiExcelFacade,T, RowHeightAttribute>(adaptee) {
    override fun attributeType(): Class<out RowHeightAttribute> = RowHeightAttribute::class.java
    override fun renderAttribute(context: AttributedRow<T>, attribute: RowHeightAttribute) {
        adaptee.assertRow(context.getTableId(), context.rowIndex).height = ApachePoiUtils.heightFromPixels(attribute.height)
    }
}

class FilterAndSortTableAttributeRenderOperation(override val adaptee: ApachePoiExcelFacade) : AdaptingTableAttributeRenderOperation<ApachePoiExcelFacade, FilterAndSortTableAttribute>(adaptee) {
    override fun attributeType(): Class<out FilterAndSortTableAttribute> = FilterAndSortTableAttribute::class.java
    override fun renderAttribute(table: Table<*>, attribute: FilterAndSortTableAttribute) {
        adaptee.workbook().creationHelper.createAreaReference(
            CellReference(attribute.rowRange.first, attribute.columnRange.first),
            CellReference(attribute.rowRange.last, attribute.columnRange.last)
        ).let { adaptee.workbook().xssfWorkbook.getSheet(table.name).createTable(it) }
            .let {
                attribute.columnRange.forEach { index ->
                    it.ctTable.tableColumns.getTableColumnArray(index).id = (index+1).toLong()
                }
                it.name = table.name
                it.displayName = table.name
                it.ctTable.addNewAutoFilter().ref = it.area.formatAsString()
            }
    }
}

internal fun tableAttributesOperations(adaptee: ApachePoiExcelFacade) = setOf(
    FilterAndSortTableAttributeRenderOperation(adaptee)
)

internal fun <T> rowAttributesOperations(adaptee: ApachePoiExcelFacade) = setOf(
    RowHeightAttributeRenderOperation<T>(adaptee)
)

internal fun <T> cellAttributesOperations(adaptee: ApachePoiExcelFacade) = setOf(
    CellFontAttributeRenderOperation<T>(adaptee),
    CellBackgroundAttributeRenderOperation<T>(adaptee),
    CellBordersAttributeRenderOperation<T>(adaptee),
    CellAlignmentAttributeRenderOperation<T>(adaptee),
    CellDataFormatAttributeRenderOperation<T>(adaptee)
)

internal fun <T> columnAttributesOperations(adaptee: ApachePoiExcelFacade) = setOf(
    ColumnWidthAttributeRenderOperation<T>(adaptee)
)
