package pl.voytech.exporter.impl.template.excel

import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.FontUnderline
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import pl.voytech.exporter.core.model.extension.TableExtension
import pl.voytech.exporter.core.model.extension.style.*
import pl.voytech.exporter.core.model.extension.style.enums.BorderStyle
import pl.voytech.exporter.core.model.extension.style.enums.HorizontalAlignment
import pl.voytech.exporter.core.model.extension.style.enums.VerticalAlignment
import pl.voytech.exporter.core.model.extension.style.enums.WeightStyle
import pl.voytech.exporter.core.template.*
import pl.voytech.exporter.impl.template.excel.PoiWrapper.assertRow
import pl.voytech.exporter.impl.template.excel.PoiWrapper.cellStyle
import pl.voytech.exporter.impl.template.excel.PoiWrapper.color
import pl.voytech.exporter.impl.template.excel.PoiWrapper.tableSheet
import pl.voytech.exporter.impl.template.excel.PoiWrapper.workbook
import kotlin.reflect.KClass

class CellFontExtensionOperation : CellExtensionOperation<CellFontExtension, SXSSFWorkbook> {

    override fun extensionType(): KClass<CellFontExtension> = CellFontExtension::class

    override fun apply(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates, extension: CellFontExtension) {
        cellStyle(state, coordinates).let {
            val font: XSSFFont = workbook(state).createFont() as XSSFFont
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

class CellBackgroundExtensionOperation : CellExtensionOperation<CellBackgroundExtension, SXSSFWorkbook> {
    override fun extensionType(): KClass<CellBackgroundExtension> = CellBackgroundExtension::class

    override fun apply(
        state: DelegateAPI<SXSSFWorkbook>,
        coordinates: Coordinates,
        extension: CellBackgroundExtension
    ) {
        cellStyle(state, coordinates).let {
            (it as XSSFCellStyle).setFillForegroundColor(color(extension.color))
            it.fillPattern = FillPatternType.SOLID_FOREGROUND
        }
    }
}

class CellBordersExtensionOperation : CellExtensionOperation<CellBordersExtension, SXSSFWorkbook> {
    override fun extensionType(): KClass<CellBordersExtension> = CellBordersExtension::class

    override fun apply(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates, extension: CellBordersExtension) {
        val toPoiStyle = { style: BorderStyle ->
            when (style) {
                BorderStyle.DASHED -> org.apache.poi.ss.usermodel.BorderStyle.DASHED
                BorderStyle.DOTTED -> org.apache.poi.ss.usermodel.BorderStyle.DOTTED
                BorderStyle.SOLID -> org.apache.poi.ss.usermodel.BorderStyle.THIN
                else -> org.apache.poi.ss.usermodel.BorderStyle.NONE
            }
        }
        cellStyle(state, coordinates).let {
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

class CellAlignmentExtensionOperation : CellExtensionOperation<CellAlignmentExtension, SXSSFWorkbook> {

    override fun extensionType(): KClass<out CellAlignmentExtension> = CellAlignmentExtension::class

    override fun apply(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates, extension: CellAlignmentExtension) {
        cellStyle(state, coordinates).let {
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

class CellDataFormatExtensionOperation : CellExtensionOperation<CellExcelDataFormatExtension, SXSSFWorkbook> {
    override fun extensionType(): KClass<out CellExcelDataFormatExtension> = CellExcelDataFormatExtension::class

    override fun apply(
        state: DelegateAPI<SXSSFWorkbook>,
        coordinates: Coordinates,
        extension: CellExcelDataFormatExtension
    ) {
        cellStyle(state, coordinates).let {
            extension.dataFormat.run { it.dataFormat = workbook(state).createDataFormat().getFormat(this) }
        }
    }

}

class ColumnWidthExtensionOperation : ColumnExtensionOperation<ColumnWidthExtension, SXSSFWorkbook> {
    override fun extensionType(): KClass<out ColumnWidthExtension> = ColumnWidthExtension::class
    override fun apply(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates, extension: ColumnWidthExtension) =
        tableSheet(state, coordinates.tableName).setColumnWidth(
            coordinates.columnIndex,
            PoiUtils.widthFromPixels(extension.width)
        )
}

class RowHeightExtensionOperation : RowExtensionOperation<RowHeightExtension, SXSSFWorkbook> {
    override fun extensionType(): KClass<out RowHeightExtension> = RowHeightExtension::class
    override fun apply(state: DelegateAPI<SXSSFWorkbook>, coordinates: Coordinates, extension: RowHeightExtension) {
        assertRow(state, coordinates).height = PoiUtils.heightFromPixels(extension.height)
    }
}

val tableExtensionsOperations = emptyList<TableExtensionOperation<TableExtension, SXSSFWorkbook>>()

val rowExtensionsOperations = listOf(
    RowHeightExtensionOperation()
)

val cellExtensionsOperations = listOf(
    CellFontExtensionOperation(),
    CellBackgroundExtensionOperation(),
    CellBordersExtensionOperation(),
    CellAlignmentExtensionOperation(),
    CellDataFormatExtensionOperation()
)

val columnExtensionsOperations = listOf(
    ColumnWidthExtensionOperation()
)