package io.github.voytech.tabulate.excel.components.table.operation

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.model.Height
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.operation.AttributeOperation
import io.github.voytech.tabulate.core.template.operation.OperationsBuilder
import io.github.voytech.tabulate.core.template.operation.cacheOnAttributeSet
import io.github.voytech.tabulate.core.template.operation.factories.AttributeOperationsFactory
import io.github.voytech.tabulate.core.template.operation.factories.ExportOperationsFactory
import io.github.voytech.tabulate.core.template.spi.DocumentFormat
import io.github.voytech.tabulate.core.template.spi.DocumentFormat.Companion.format
import io.github.voytech.tabulate.excel.ApachePoiRenderingContext
import io.github.voytech.tabulate.excel.Utils.toDate
import io.github.voytech.tabulate.excel.components.table.model.ExcelTypeHints
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.streaming.SXSSFCell
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class ExcelTableAttributesOperations : AttributeOperationsFactory<ApachePoiRenderingContext, Table<Any>> {
    override fun createAttributeOperations(): Set<AttributeOperation<ApachePoiRenderingContext, Table<Any>, *, *, *>> =
        setOf(
            TemplateFileAttributeRenderOperation(),
            ColumnWidthAttributeRenderOperation(),
            RowHeightAttributeRenderOperation(),
            CellTextStylesAttributeRenderOperation(),
            CellBordersAttributeRenderOperation(),
            CellAlignmentAttributeRenderOperation(),
            CellBackgroundAttributeRenderOperation(),
            CellDataFormatAttributeRenderOperation(),
            CellCommentAttributeRenderOperation(),
            FilterAndSortTableAttributeRenderOperation(),
            PrintingAttributeRenderOperation(),
            RowBordersAttributeRenderOperation(),
        )

    override fun getRenderingContextClass(): Class<ApachePoiRenderingContext> = reify()

    override fun getModelClass(): Class<Table<Any>> = reify()
}

/**
 * Apache POI based excel export operations provider implementation.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class ExcelTableOperations : ExportOperationsFactory<ApachePoiRenderingContext, Table<Any>>() {

    override fun getDocumentFormat(): DocumentFormat<ApachePoiRenderingContext> =
        format("xlsx", "poi")

    override fun provideExportOperations(): OperationsBuilder<ApachePoiRenderingContext, Table<Any>>.() -> Unit = {
        operation(OpenTableOperation { renderingContext, context ->
            renderingContext.provideWorkbook()
            renderingContext.provideSheet(context.getSheetName())
        })
        operation(OpenColumnOperation { _, _ -> })
        operation(OpenRowOperation { renderingContext, context ->
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
                        context.value.rowSpan,
                        context.value.colSpan
                    )
                }
            }
        })
        operation(CloseRowOperation { renderingContext, context ->
            with(renderingContext) {
                provideSheet(context.getSheetName()).let { sheet ->
                    val absoluteRowIndex = context.getAbsoluteRow(context.rowIndex)
                    sheet.getRow(absoluteRowIndex).heightInPoints.run {
                        context.setAbsoluteRowHeight(absoluteRowIndex, Height(this, UnitsOfMeasure.PT))
                    }
                }
            }
        })
        operation(CloseColumnOperation { renderingContext, context ->
            with(renderingContext) {
                provideSheet(context.getSheetName()).let { sheet ->
                    val absoluteColumnIndex = context.getAbsoluteColumn(context.columnIndex)
                    sheet.getColumnWidthInPixels(absoluteColumnIndex).run {
                        context.setAbsoluteColumnWidth(absoluteColumnIndex, Width(this, UnitsOfMeasure.PX))
                    }
                }
            }
        })
        operation(CloseTableOperation { _, _ -> })
    }

    override fun getAttributeOperationsFactory(): AttributeOperationsFactory<ApachePoiRenderingContext, Table<Any>> =
        ExcelTableAttributesOperations()

    override fun getModelClass(): Class<Table<Any>> = reify()

    private fun CellContext.hasSpans(): Boolean = value.colSpan > 1 || value.rowSpan > 1

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
            context.value.rowSpan,
            context.value.colSpan,
            context.rawValue as ByteArray
        )
    }

    private fun ApachePoiRenderingContext.renderImageUrlCell(context: CellContext) {
        createImageCell(
            context.getSheetName(),
            context.getAbsoluteRow(context.getRow()),
            context.getAbsoluteColumn(context.getColumn()),
            context.value.rowSpan,
            context.value.colSpan,
            context.rawValue as String
        )
    }

    private fun ApachePoiRenderingContext.renderFormulaCell(context: CellContext) = provideCell(context) {
        cellFormula = context.rawValue as? String
    }

    private fun ApachePoiRenderingContext.renderErrorCell(context: CellContext) = provideCell(context) {
        setCellErrorValue(context.rawValue as Byte)
    }

    private fun ApachePoiRenderingContext.renderStringCellValue(context: CellContext) = provideCell(context) {
        setCellValue(context.rawValue.toString())
    }

    private fun ApachePoiRenderingContext.renderNumericCellValue(context: CellContext) = provideCell(context) {
        setCellValue((context.rawValue as Number).toDouble())
    }

    private fun ApachePoiRenderingContext.renderBooleanCellValue(context: CellContext) = provideCell(context) {
        setCellValue(context.rawValue as Boolean)
    }

    private fun ApachePoiRenderingContext.renderDateCellValue(context: CellContext) = provideCell(context) {
        setCellValue(toDate(context.rawValue))
    }

    private fun ApachePoiRenderingContext.castAndRenderCellValue(context: CellContext) =
        when (context.rawValue) {
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
