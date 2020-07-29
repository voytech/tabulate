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
import pl.voytech.exporter.core.model.extension.functional.FilterAndSortTableExtension
import pl.voytech.exporter.core.model.extension.style.*
import pl.voytech.exporter.core.model.extension.style.enums.BorderStyle
import pl.voytech.exporter.core.model.extension.style.enums.HorizontalAlignment
import pl.voytech.exporter.core.model.extension.style.enums.VerticalAlignment
import pl.voytech.exporter.core.model.extension.style.enums.WeightStyle
import pl.voytech.exporter.core.template.*
import pl.voytech.exporter.impl.template.excel.PoiCache.cachedFont
import pl.voytech.exporter.impl.template.excel.PoiCache.getCachedFont
import pl.voytech.exporter.impl.template.excel.PoiWrapper.assertRow
import pl.voytech.exporter.impl.template.excel.PoiWrapper.cellStyle
import pl.voytech.exporter.impl.template.excel.PoiWrapper.color
import pl.voytech.exporter.impl.template.excel.PoiWrapper.tableSheet
import pl.voytech.exporter.impl.template.excel.PoiWrapper.workbook
import java.awt.font.FontRenderContext
import java.awt.font.TextAttribute
import java.awt.font.TextLayout
import java.awt.geom.Rectangle2D
import java.text.AttributedString
import kotlin.math.roundToInt
import kotlin.reflect.KClass


class CellFontExtensionOperation<T> : CellExtensionOperation<T,CellFontExtension, SXSSFWorkbook> {

    override fun extensionType(): KClass<CellFontExtension> = CellFontExtension::class

    override fun apply(state: DelegateAPI<SXSSFWorkbook>, context: OperationContext<T, CellOperationTableData<T>>, extension: CellFontExtension) {
        cellStyle(state, context.coordinates!!).let {
            val font: XSSFFont = cachedFont(context.coordinates!!, extension) { workbook(state).createFont() as XSSFFont }
            extension.fontFamily?.run { font.fontName = this }
            extension.fontColor?.run { font.setColor(color(this)) }
            extension.fontSize?.run { font.fontHeightInPoints = toShort() }
            extension.italic?.run { font.italic = this }
            extension.strikeout?.run { font.strikeout = this }
            extension.underline?.let { underline ->
                if (underline) font.setUnderline(FontUnderline.SINGLE) else font.setUnderline(
                    FontUnderline.NONE
                )
            }
            extension.weight?.run { font.bold = this == WeightStyle.BOLD }
            it.setFont(font)
        }
    }
}

class CellBackgroundExtensionOperation<T> : CellExtensionOperation<T,CellBackgroundExtension, SXSSFWorkbook> {
    override fun extensionType(): KClass<CellBackgroundExtension> = CellBackgroundExtension::class

    override fun apply(
        state: DelegateAPI<SXSSFWorkbook>,
        context: OperationContext<T, CellOperationTableData<T>>,
        extension: CellBackgroundExtension
    ) {
        cellStyle(state, context.coordinates!!).let {
            (it as XSSFCellStyle).setFillForegroundColor(color(extension.color))
            it.fillPattern = FillPatternType.SOLID_FOREGROUND
        }
    }
}

class CellBordersExtensionOperation<T> : CellExtensionOperation<T, CellBordersExtension, SXSSFWorkbook> {
    override fun extensionType(): KClass<CellBordersExtension> = CellBordersExtension::class

    override fun apply(state: DelegateAPI<SXSSFWorkbook>, context: OperationContext<T, CellOperationTableData<T>>, extension: CellBordersExtension) {
        val toPoiStyle = { style: BorderStyle ->
            when (style) {
                BorderStyle.DASHED -> org.apache.poi.ss.usermodel.BorderStyle.DASHED
                BorderStyle.DOTTED -> org.apache.poi.ss.usermodel.BorderStyle.DOTTED
                BorderStyle.SOLID -> org.apache.poi.ss.usermodel.BorderStyle.THIN
                else -> org.apache.poi.ss.usermodel.BorderStyle.NONE
            }
        }
        cellStyle(state, context.coordinates!!).let {
            extension.leftBorderColor?.run { (it as XSSFCellStyle).setLeftBorderColor(color(this)) }
            extension.rightBorderColor?.run { (it as XSSFCellStyle).setRightBorderColor(color(this)) }
            extension.topBorderColor?.run { (it as XSSFCellStyle).setTopBorderColor(color(this)) }
            extension.bottomBorderColor?.run { (it as XSSFCellStyle).setBottomBorderColor(color(this)) }
            extension.leftBorderStyle?.run { it.borderLeft = toPoiStyle(this) }
            extension.rightBorderStyle?.run { it.borderRight = toPoiStyle(this) }
            extension.topBorderStyle?.run { it.borderTop = toPoiStyle(this) }
            extension.bottomBorderStyle?.run { it.borderBottom = toPoiStyle(this) }
        }
    }
}

class CellAlignmentExtensionOperation<T> : CellExtensionOperation<T, CellAlignmentExtension, SXSSFWorkbook> {

    override fun extensionType(): KClass<out CellAlignmentExtension> = CellAlignmentExtension::class

    override fun apply(state: DelegateAPI<SXSSFWorkbook>, context: OperationContext<T, CellOperationTableData<T>>, extension: CellAlignmentExtension) {
        cellStyle(state, context.coordinates!!).let {
            extension.horizontal?.run {
                it.alignment =
                    when (this) {
                        HorizontalAlignment.CENTER -> org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER
                        HorizontalAlignment.LEFT -> org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT
                        HorizontalAlignment.RIGHT -> org.apache.poi.ss.usermodel.HorizontalAlignment.RIGHT
                        HorizontalAlignment.JUSTIFY -> org.apache.poi.ss.usermodel.HorizontalAlignment.FILL
                        else -> org.apache.poi.ss.usermodel.HorizontalAlignment.GENERAL
                    }
            }
            extension.vertical?.run {
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

class CellDataFormatExtensionOperation<T> : CellExtensionOperation<T, CellExcelDataFormatExtension, SXSSFWorkbook> {
    override fun extensionType(): KClass<out CellExcelDataFormatExtension> = CellExcelDataFormatExtension::class

    override fun apply(
        state: DelegateAPI<SXSSFWorkbook>,
        context: OperationContext<T, CellOperationTableData<T>>,
        extension: CellExcelDataFormatExtension
    ) {
        cellStyle(state, context.coordinates!!).let {
            extension.dataFormat.run { it.dataFormat = workbook(state).createDataFormat().getFormat(this) }
        }
    }

}

class ColumnWidthExtensionOperation<T> : ColumnExtensionOperation<T ,ColumnWidthExtension, SXSSFWorkbook> {
    override fun extensionType(): KClass<out ColumnWidthExtension> = ColumnWidthExtension::class

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

    override fun apply(state: DelegateAPI<SXSSFWorkbook>, context: OperationContext<T, ColumnOperationTableData<T>>, extension: ColumnWidthExtension) =
        tableSheet(state, context.coordinates!!.tableName).setColumnWidth(
            context.coordinates!!.columnIndex,
            if (extension.auto == true || extension.width == -1) {
                context.value.columnValues?.maxBy { v -> v.value.toString().length }?.value.toString().let {
                    getStringWidth(
                        text = it,
                        cellFont = getCachedFont(context.coordinates!!)!!,
                        workbook = workbook(state)
                    )
                }
            } else {
                PoiUtils.widthFromPixels(extension.width)
            }
        )
}

class RowHeightExtensionOperation<T> : RowExtensionOperation<T, RowHeightExtension, SXSSFWorkbook> {
    override fun extensionType(): KClass<out RowHeightExtension> = RowHeightExtension::class
    override fun apply(state: DelegateAPI<SXSSFWorkbook>, context: OperationContext<T, RowOperationTableData<T>>, extension: RowHeightExtension) {
        assertRow(state, context.coordinates!!).height = PoiUtils.heightFromPixels(extension.height)
    }
}

class FilterAndSortTableExtensionOperation : TableExtensionOperation<FilterAndSortTableExtension, SXSSFWorkbook> {
    override fun extensionType(): KClass<out FilterAndSortTableExtension> = FilterAndSortTableExtension::class
    override fun apply(state: DelegateAPI<SXSSFWorkbook>, table: Table<*>, extension: FilterAndSortTableExtension) {
        workbook(state).creationHelper.createAreaReference(
            CellReference(extension.rowRange.first, extension.columnRange.first),
            CellReference(extension.rowRange.last, extension.columnRange.last)
        ).let { workbook(state).xssfWorkbook.getSheet(table.name).createTable(it) }
            .let {
                extension.columnRange.forEach { index ->
                    it.ctTable.tableColumns.getTableColumnArray(index).id = (index+1).toLong()
                }
                it.name = table.name
                it.displayName = table.name
                it.ctTable.addNewAutoFilter().ref = it.area.formatAsString()
            }
    }
}

val tableExtensionsOperations = listOf(
    FilterAndSortTableExtensionOperation()
)

fun <T> rowExtensionsOperations() = listOf(
    RowHeightExtensionOperation<T>()
)

fun <T> cellExtensionsOperations() = listOf(
    CellFontExtensionOperation<T>(),
    CellBackgroundExtensionOperation<T>(),
    CellBordersExtensionOperation<T>(),
    CellAlignmentExtensionOperation<T>(),
    CellDataFormatExtensionOperation<T>()
)

fun <T> columnExtensionsOperations() = listOf(
    ColumnWidthExtensionOperation<T>()
)