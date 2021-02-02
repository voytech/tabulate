package pl.voytech.exporter.impl.template.excel

import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.FontUnderline
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellReference
import org.apache.poi.ss.util.SheetUtil
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.attributes.functional.FilterAndSortTableAttribute
import pl.voytech.exporter.core.model.attributes.style.*
import pl.voytech.exporter.core.model.attributes.style.enums.BorderStyle
import pl.voytech.exporter.core.model.attributes.style.enums.HorizontalAlignment
import pl.voytech.exporter.core.model.attributes.style.enums.VerticalAlignment
import pl.voytech.exporter.core.model.attributes.style.enums.WeightStyle
import pl.voytech.exporter.core.template.context.*
import pl.voytech.exporter.core.template.operations.*
import pl.voytech.exporter.core.template.operations.impl.AttributeKeyDrivenCache.Companion.getCellCachedValue
import pl.voytech.exporter.core.template.operations.impl.AttributeKeyDrivenCache.Companion.putCellCachedValue
import pl.voytech.exporter.impl.template.excel.wrapper.ApachePoiExcelFacade.assertRow
import pl.voytech.exporter.impl.template.excel.wrapper.ApachePoiExcelFacade.cellStyle
import pl.voytech.exporter.impl.template.excel.wrapper.ApachePoiExcelFacade.color
import pl.voytech.exporter.impl.template.excel.wrapper.ApachePoiExcelFacade.tableSheet
import pl.voytech.exporter.impl.template.excel.wrapper.ApachePoiExcelFacade.workbook
import pl.voytech.exporter.impl.template.excel.wrapper.ApachePoiUtils
import java.awt.font.FontRenderContext
import java.awt.font.TextAttribute
import java.awt.font.TextLayout
import java.awt.geom.Rectangle2D
import java.text.AttributedString
import kotlin.math.roundToInt

class CellFontAttributeRenderOperation<T>(override val adaptee: SXSSFWorkbook) : AdaptingCellAttributeRenderOperation<SXSSFWorkbook,T, CellFontAttribute>(adaptee) {

    private val cellFontCacheKey = "cellFont"

    override fun attributeType(): Class<CellFontAttribute> = CellFontAttribute::class.java

    override fun renderAttribute(context: AttributedCell, attribute: CellFontAttribute) {
        cellStyle(adaptee, context).let {
            if (getCellCachedValue(context, cellFontCacheKey) == null) {
                val font: XSSFFont = workbook(adaptee).createFont() as XSSFFont
                attribute.fontFamily?.run { font.fontName = this }
                attribute.fontColor?.run { font.setColor(color(this)) }
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
                putCellCachedValue(context, cellFontCacheKey, font)
            }
        }
    }
}

class CellBackgroundAttributeRenderOperation<T>(override val adaptee: SXSSFWorkbook) : AdaptingCellAttributeRenderOperation<SXSSFWorkbook,T,CellBackgroundAttribute>(adaptee) {
    override fun attributeType(): Class<CellBackgroundAttribute> = CellBackgroundAttribute::class.java

    override fun renderAttribute(
        context: AttributedCell,
        attribute: CellBackgroundAttribute
    ) {
        cellStyle(adaptee, context).let {
            (it as XSSFCellStyle).setFillForegroundColor(color(attribute.color))
            it.fillPattern = FillPatternType.SOLID_FOREGROUND
        }
    }
}

class CellBordersAttributeRenderOperation<T>(override val adaptee: SXSSFWorkbook) : AdaptingCellAttributeRenderOperation<SXSSFWorkbook,T, CellBordersAttribute>(adaptee) {
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
        cellStyle(adaptee, context).let {
            attribute.leftBorderColor?.run { (it as XSSFCellStyle).setLeftBorderColor(color(this)) }
            attribute.rightBorderColor?.run { (it as XSSFCellStyle).setRightBorderColor(color(this)) }
            attribute.topBorderColor?.run { (it as XSSFCellStyle).setTopBorderColor(color(this)) }
            attribute.bottomBorderColor?.run { (it as XSSFCellStyle).setBottomBorderColor(color(this)) }
            attribute.leftBorderStyle?.run { it.borderLeft = toPoiStyle(this) }
            attribute.rightBorderStyle?.run { it.borderRight = toPoiStyle(this) }
            attribute.topBorderStyle?.run { it.borderTop = toPoiStyle(this) }
            attribute.bottomBorderStyle?.run { it.borderBottom = toPoiStyle(this) }
        }
    }
}

class CellAlignmentAttributeRenderOperation<T>(override val adaptee: SXSSFWorkbook) : AdaptingCellAttributeRenderOperation<SXSSFWorkbook,T, CellAlignmentAttribute>(adaptee) {

    override fun attributeType(): Class<out CellAlignmentAttribute> = CellAlignmentAttribute::class.java

    override fun renderAttribute(context: AttributedCell, attribute: CellAlignmentAttribute) {
        cellStyle(adaptee, context).let {
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

class CellDataFormatAttributeRenderOperation<T>(override val adaptee: SXSSFWorkbook) : AdaptingCellAttributeRenderOperation<SXSSFWorkbook, T, CellExcelDataFormatAttribute>(adaptee){

    private val cellStyleFormatKey = "cellStyleFormatKey"

    override fun attributeType(): Class<out CellExcelDataFormatAttribute> = CellExcelDataFormatAttribute::class.java

    override fun renderAttribute(
        context: AttributedCell,
        attribute: CellExcelDataFormatAttribute
    ) {
        //if (getCellCachedValue(context, cellStyleFormatKey) == null) {
            cellStyle(adaptee, context).let {
                attribute.dataFormat.run {
                    it.dataFormat = workbook(adaptee).createDataFormat().getFormat(this)
                    putCellCachedValue(context, cellStyleFormatKey, it.dataFormat)
                }
            }
        //}
    }

}

class ColumnWidthAttributeRenderOperation<T>(override val adaptee: SXSSFWorkbook) : AdaptingColumnAttributeRenderOperation<SXSSFWorkbook, T, ColumnWidthAttribute>(adaptee) {
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
        tableSheet(adaptee, context.getTableId()).let {
            if (attribute.auto == true || attribute.width == -1) {
                if (!it.isColumnTrackedForAutoSizing(context.columnIndex)) {
                    it.trackColumnForAutoSizing(context.columnIndex)
                }
                it.autoSizeColumn(context.columnIndex)
            } else {
                ApachePoiUtils.widthFromPixels(attribute.width)
            }
        }
    }
}

class RowHeightAttributeRenderOperation<T>(override val adaptee: SXSSFWorkbook) : AdaptingRowAttributeRenderOperation<SXSSFWorkbook,T, RowHeightAttribute>(adaptee) {
    override fun attributeType(): Class<out RowHeightAttribute> = RowHeightAttribute::class.java
    override fun renderAttribute(context: AttributedRow<T>, attribute: RowHeightAttribute) {
        assertRow(adaptee, context.getTableId(), context.rowIndex).height = ApachePoiUtils.heightFromPixels(attribute.height)
    }
}

class FilterAndSortTableAttributeRenderOperation(override val adaptee: SXSSFWorkbook) : AdaptingTableAttributeRenderOperation<SXSSFWorkbook, FilterAndSortTableAttribute>(adaptee) {
    override fun attributeType(): Class<out FilterAndSortTableAttribute> = FilterAndSortTableAttribute::class.java
    override fun renderAttribute(table: Table<*>, attribute: FilterAndSortTableAttribute) {
        workbook(adaptee).creationHelper.createAreaReference(
            CellReference(attribute.rowRange.first, attribute.columnRange.first),
            CellReference(attribute.rowRange.last, attribute.columnRange.last)
        ).let { workbook(adaptee).xssfWorkbook.getSheet(table.name).createTable(it) }
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

internal fun tableAttributesOperations(adaptee: SXSSFWorkbook) = setOf(
    FilterAndSortTableAttributeRenderOperation(adaptee)
)

internal fun <T> rowAttributesOperations(adaptee: SXSSFWorkbook) = setOf(
    RowHeightAttributeRenderOperation<T>(adaptee)
)

internal fun <T> cellAttributesOperations(adaptee: SXSSFWorkbook) = setOf(
    CellFontAttributeRenderOperation<T>(adaptee),
    CellBackgroundAttributeRenderOperation<T>(adaptee),
    CellBordersAttributeRenderOperation<T>(adaptee),
    CellAlignmentAttributeRenderOperation<T>(adaptee),
    CellDataFormatAttributeRenderOperation<T>(adaptee)
)

internal fun <T> columnAttributesOperations(adaptee: SXSSFWorkbook) = setOf(
    ColumnWidthAttributeRenderOperation<T>(adaptee)
)
