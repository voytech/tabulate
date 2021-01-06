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
import pl.voytech.exporter.core.model.attributes.CellAttribute
import pl.voytech.exporter.core.model.attributes.functional.FilterAndSortTableAttribute
import pl.voytech.exporter.core.model.attributes.style.*
import pl.voytech.exporter.core.model.attributes.style.enums.BorderStyle
import pl.voytech.exporter.core.model.attributes.style.enums.HorizontalAlignment
import pl.voytech.exporter.core.model.attributes.style.enums.VerticalAlignment
import pl.voytech.exporter.core.model.attributes.style.enums.WeightStyle
import pl.voytech.exporter.core.template.*
import pl.voytech.exporter.core.template.operations.AttributeKeyDrivenCache.Companion.getCellCachedValue
import pl.voytech.exporter.core.template.operations.AttributeKeyDrivenCache.Companion.putCellCachedValue
import pl.voytech.exporter.impl.template.excel.SXSSFWrapper.assertRow
import pl.voytech.exporter.impl.template.excel.SXSSFWrapper.cellStyle
import pl.voytech.exporter.impl.template.excel.SXSSFWrapper.color
import pl.voytech.exporter.impl.template.excel.SXSSFWrapper.tableSheet
import pl.voytech.exporter.impl.template.excel.SXSSFWrapper.workbook
import java.awt.font.FontRenderContext
import java.awt.font.TextAttribute
import java.awt.font.TextLayout
import java.awt.geom.Rectangle2D
import java.text.AttributedString
import kotlin.math.roundToInt


class CellFontAttributeOperation<T> : CellAttributeOperation<T,CellFontAttribute, SXSSFWorkbook> {

    private val cellFontCacheKey = "cellFont"

    override fun attributeType(): Class<CellFontAttribute> = CellFontAttribute::class.java

    override fun renderAttribute(state: SXSSFWorkbook, context: OperationContext<AttributedCell>, attribute: CellFontAttribute) {
        cellStyle(state, context.coordinates, context).let {
            if (getCellCachedValue(context, cellFontCacheKey) == null) {
                val font: XSSFFont = workbook(state).createFont() as XSSFFont
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

class CellBackgroundAttributeOperation<T> : CellAttributeOperation<T,CellBackgroundAttribute, SXSSFWorkbook> {
    override fun attributeType(): Class<CellBackgroundAttribute> = CellBackgroundAttribute::class.java

    override fun renderAttribute(
        state: SXSSFWorkbook,
        context: OperationContext<AttributedCell>,
        attribute: CellBackgroundAttribute
    ) {
        cellStyle(state, context.coordinates, context).let {
            (it as XSSFCellStyle).setFillForegroundColor(color(attribute.color))
            it.fillPattern = FillPatternType.SOLID_FOREGROUND
        }
    }
}

class CellBordersAttributeOperation<T> : CellAttributeOperation<T, CellBordersAttribute, SXSSFWorkbook> {
    override fun attributeType(): Class<CellBordersAttribute> = CellBordersAttribute::class.java

    override fun renderAttribute(state: SXSSFWorkbook, context: OperationContext<AttributedCell>, attribute: CellBordersAttribute) {
        val toPoiStyle = { style: BorderStyle ->
            when (style) {
                BorderStyle.DASHED -> org.apache.poi.ss.usermodel.BorderStyle.DASHED
                BorderStyle.DOTTED -> org.apache.poi.ss.usermodel.BorderStyle.DOTTED
                BorderStyle.SOLID -> org.apache.poi.ss.usermodel.BorderStyle.THIN
                else -> org.apache.poi.ss.usermodel.BorderStyle.NONE
            }
        }
        cellStyle(state, context.coordinates, context).let {
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

class CellAlignmentAttributeOperation<T> : CellAttributeOperation<T, CellAlignmentAttribute, SXSSFWorkbook> {

    override fun attributeType(): Class<out CellAlignmentAttribute> = CellAlignmentAttribute::class.java

    override fun renderAttribute(state: SXSSFWorkbook, context: OperationContext<AttributedCell>, attribute: CellAlignmentAttribute) {
        cellStyle(state, context.coordinates, context).let {
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

class CellDataFormatAttributeOperation<T> : CellAttributeOperation<T, CellExcelDataFormatAttribute, SXSSFWorkbook> {

    private val cellStyleFormatKey = "cellStyleFormatKey"

    override fun attributeType(): Class<out CellExcelDataFormatAttribute> = CellExcelDataFormatAttribute::class.java

    override fun renderAttribute(
        state: SXSSFWorkbook,
        context: OperationContext<AttributedCell>,
        attribute: CellExcelDataFormatAttribute
    ) {
        //if (getCellCachedValue(context, cellStyleFormatKey) == null) {
            cellStyle(state, context.coordinates, context).let {
                attribute.dataFormat.run {
                    it.dataFormat = workbook(state).createDataFormat().getFormat(this)
                    putCellCachedValue(context, cellStyleFormatKey, it.dataFormat)
                }
            }
        //}
    }

}

class ColumnWidthAttributeOperation<T> : ColumnAttributeOperation<T ,ColumnWidthAttribute, SXSSFWorkbook> {
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

    private fun customAutoSize(state: SXSSFWorkbook, context: OperationContext<ColumnOperationTableData>) {
        context.data?.columnValues?.maxBy { v -> v.value.toString().length }?.value.toString().let {
            getStringWidth(
                text = it,
                cellFont = workbook(state).xssfWorkbook.getFontAt(0),
                workbook = workbook(state)
            )
        }
    }

    override fun renderAttribute(state: SXSSFWorkbook, context: OperationContext<ColumnOperationTableData>, attribute: ColumnWidthAttribute) {
        tableSheet(state, context.coordinates.tableName).let {
            if (attribute.auto == true || attribute.width == -1) {
                if (!it.isColumnTrackedForAutoSizing(context.coordinates.columnIndex)) {
                    it.trackColumnForAutoSizing(context.coordinates.columnIndex)
                }
                it.autoSizeColumn(context.coordinates.columnIndex)
            } else {
                PoiUtils.widthFromPixels(attribute.width)
            }
        }
    }
}

class RowHeightAttributeOperation<T> : RowAttributeOperation<T, RowHeightAttribute, SXSSFWorkbook> {
    override fun attributeType(): Class<out RowHeightAttribute> = RowHeightAttribute::class.java
    override fun renderAttribute(state: SXSSFWorkbook, context: OperationContext<AttributedRow<T>>, attribute: RowHeightAttribute) {
        assertRow(state, context.coordinates).height = PoiUtils.heightFromPixels(attribute.height)
    }
}

class FilterAndSortTableAttributeOperation : TableAttributeOperation<FilterAndSortTableAttribute, SXSSFWorkbook> {
    override fun attributeType(): Class<out FilterAndSortTableAttribute> = FilterAndSortTableAttribute::class.java
    override fun renderAttribute(state: SXSSFWorkbook, table: Table<*>, attribute: FilterAndSortTableAttribute) {
        workbook(state).creationHelper.createAreaReference(
            CellReference(attribute.rowRange.first, attribute.columnRange.first),
            CellReference(attribute.rowRange.last, attribute.columnRange.last)
        ).let { workbook(state).xssfWorkbook.getSheet(table.name).createTable(it) }
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

internal val tableAttributesOperations = listOf(
    FilterAndSortTableAttributeOperation()
)

internal fun <T> rowAttributesOperations() = listOf(
    RowHeightAttributeOperation<T>()
)

internal fun <T> cellAttributesOperations(): List<CellAttributeOperation<T, out CellAttribute, SXSSFWorkbook>> = listOf(
    CellFontAttributeOperation(),
    CellBackgroundAttributeOperation(),
    CellBordersAttributeOperation(),
    CellAlignmentAttributeOperation(),
    CellDataFormatAttributeOperation()
)

internal fun <T> columnAttributesOperations() = listOf(
    ColumnWidthAttributeOperation<T>()
)
