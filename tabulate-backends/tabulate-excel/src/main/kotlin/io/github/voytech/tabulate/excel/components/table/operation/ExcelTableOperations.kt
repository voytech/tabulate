package io.github.voytech.tabulate.excel.components.table.operation

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.model.Height
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.operation.cacheOnAttributeSet
import io.github.voytech.tabulate.core.template.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.template.spi.BuildOperations
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.DocumentFormat.Companion.format
import io.github.voytech.tabulate.core.template.spi.OperationsBundleProvider
import io.github.voytech.tabulate.excel.ApachePoiRenderingContext
import io.github.voytech.tabulate.excel.Utils.toDate
import io.github.voytech.tabulate.excel.components.table.model.ExcelTypeHints
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.streaming.SXSSFCell
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


/**
 * Apache POI based excel export operations provider implementation.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class ExcelTableOperations : OperationsBundleProvider<ApachePoiRenderingContext, Table<Any>> {

    override fun getDocumentFormat(): DocumentFormat<ApachePoiRenderingContext> =
        format("xlsx", "poi")

    override fun getRenderingContextClass(): Class<ApachePoiRenderingContext> = reify()

    override fun provideAttributeOperations(): BuildAttributeOperations<ApachePoiRenderingContext> = {
        operation(TemplateFileAttributeRenderOperation(),-1)
        operation(ColumnWidthAttributeRenderOperation())
        operation(RowHeightAttributeRenderOperation())
        operation(CellTextStylesAttributeRenderOperation())
        operation(CellBordersAttributeRenderOperation())
        operation(CellAlignmentAttributeRenderOperation())
        operation(CellBackgroundAttributeRenderOperation())
        operation(CellDataFormatAttributeRenderOperation())
        operation(CellCommentAttributeRenderOperation())
        operation(FilterAndSortTableAttributeRenderOperation())
        operation(PrintingAttributeRenderOperation())
        operation(RowBordersAttributeRenderOperation<Any>())
    }

    override fun provideExportOperations(): BuildOperations<ApachePoiRenderingContext> = {
        operation(StartTableOperation { renderingContext, context ->
            renderingContext.provideWorkbook()
            renderingContext.provideSheet(context.getSheetName())
        })
        operation(StartColumnOperation { _, _ -> })
        operation(StartRowOperation { renderingContext, context ->
            with(renderingContext) {
                provideSheet(context.getSheetName())
                provideRow(context.getSheetName(), context.getAbsoluteRow(context.getRow()))
            }
        })
        operation(RenderRowCellOperation { renderingContext, context ->
            with(renderingContext) {
                context.getTypeHint()?.let {
                    when (it.type.getCellTypeId()) {
                        ExcelTypeHints.IMAGE_DATA.getCellTypeId() -> renderImageDataCell(context)
                        ExcelTypeHints.IMAGE_URL.getCellTypeId() -> renderImageUrlCell(context)
                        ExcelTypeHints.FORMULA.getCellTypeId() -> renderFormulaCell(context)
                        ExcelTypeHints.NUMERIC.getCellTypeId() -> renderNumericCellValue(context)
                        ExcelTypeHints.DATE.getCellTypeId() -> renderDateCellValue(context)
                        ExcelTypeHints.BOOLEAN.getCellTypeId() -> renderBooleanCellValue(context)
                        ExcelTypeHints.ERROR.getCellTypeId() -> renderErrorCell(context)
                        else -> renderStringCellValue(context)
                    }
                } ?: castAndRenderCellValue(context)
                if (context.hasSpans()) {
                    mergeCells(
                        context.getSheetName(),
                        context.getAbsoluteRow(context.getRow()),
                        context.getAbsoluteColumn(context.getColumn()),
                        context.cellValue.rowSpan,
                        context.cellValue.colSpan
                    )
                }
            }
        })
        operation(EndRowOperation<ApachePoiRenderingContext,Any> { renderingContext, context ->
            with(renderingContext) {
                provideSheet(context.getSheetName()).let { sheet ->
                    val absoluteRowIndex = context.getAbsoluteRow(context.rowIndex)
                    sheet.getRow(absoluteRowIndex).heightInPoints.run {
                        context.setAbsoluteRowHeight(absoluteRowIndex, Height(this, UnitsOfMeasure.PT))
                    }
                }
            }
        })
        operation(EndColumnOperation { renderingContext, context ->
            with(renderingContext) {
                provideSheet(context.getSheetName()).let { sheet ->
                    val absoluteColumnIndex = context.getAbsoluteColumn(context.columnIndex)
                    sheet.getColumnWidthInPixels(absoluteColumnIndex).run {
                        context.setAbsoluteColumnWidth(absoluteColumnIndex, Width(this, UnitsOfMeasure.PX))
                    }
                }
            }
        })
        operation(EndTableOperation { _, _ -> })
    }

    override fun getModelClass(): Class<Table<Any>> = reify()

    private fun CellContext.hasSpans(): Boolean = cellValue.colSpan > 1 || cellValue.rowSpan > 1

    private fun ApachePoiRenderingContext.provideCell(context: CellContext, block: (SXSSFCell.() -> Unit)) {
        provideCell(
            context.getSheetName(),
            context.getAbsoluteRow(context.getRow()),
            context.getAbsoluteColumn(context.getColumn())
        ) { it.apply(block) }
    }

    private fun ApachePoiRenderingContext.renderImageDataCell(context: CellContext) {
        createImageCell(
            context.getSheetName(),
            context.getAbsoluteRow(context.getRow()),
            context.getAbsoluteColumn(context.getColumn()),
            context.cellValue.rowSpan,
            context.cellValue.colSpan,
            context.value as ByteArray
        )
    }

    private fun ApachePoiRenderingContext.renderImageUrlCell(context: CellContext) {
        createImageCell(
            context.getSheetName(),
            context.getAbsoluteRow(context.getRow()),
            context.getAbsoluteColumn(context.getColumn()),
            context.cellValue.rowSpan,
            context.cellValue.colSpan,
            context.value as String
        )
    }

    private fun ApachePoiRenderingContext.renderFormulaCell(context: CellContext) = provideCell(context) {
        cellFormula = context.value as? String
    }

    private fun ApachePoiRenderingContext.renderErrorCell(context: CellContext) = provideCell(context) {
        setCellErrorValue(context.value as Byte)
    }

    private fun ApachePoiRenderingContext.renderStringCellValue(context: CellContext) = provideCell(context) {
        setCellValue(context.value.toString())
    }

    private fun ApachePoiRenderingContext.renderNumericCellValue(context: CellContext) = provideCell(context) {
        setCellValue((context.value as Number).toDouble())
    }

    private fun ApachePoiRenderingContext.renderBooleanCellValue(context: CellContext) = provideCell(context) {
        setCellValue(context.value as Boolean)
    }

    private fun ApachePoiRenderingContext.renderDateCellValue(context: CellContext) = provideCell(context) {
        setCellValue(toDate(context.value))
    }

    private fun ApachePoiRenderingContext.castAndRenderCellValue(context: CellContext) =
        when (context.value) {
            is String -> renderStringCellValue(context)
            is Boolean -> renderBooleanCellValue(context)
            is LocalDate,
            is LocalDateTime,
            is Date,
            -> renderDateCellValue(context)
            is Number -> renderNumericCellValue(context)
            else -> renderStringCellValue(context)
        }

    companion object {

        private const val CELL_STYLE_CACHE_KEY: String = "cellStyle"

        fun getCachedStyle(poi: ApachePoiRenderingContext, context: CellContext): CellStyle {
            return context.cacheOnAttributeSet(
                CELL_STYLE_CACHE_KEY,
                poi.workbook().createCellStyle()
            ) as CellStyle
        }
    }

}
