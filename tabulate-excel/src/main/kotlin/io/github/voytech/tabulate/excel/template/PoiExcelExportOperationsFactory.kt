package io.github.voytech.tabulate.excel.template

import io.github.voytech.tabulate.excel.model.ExcelTypeHints
import io.github.voytech.tabulate.excel.template.Utils.toDate
import io.github.voytech.tabulate.model.attributes.cell.CellAlignmentAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBackgroundAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.template.operations.*
import io.github.voytech.tabulate.template.result.OutputBinding
import io.github.voytech.tabulate.template.spi.TabulationFormat
import io.github.voytech.tabulate.template.spi.TabulationFormat.Companion.format
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
class PoiExcelExportOperationsFactory : ExportOperationsFactory<ApachePoiRenderingContext>() {

    override fun getTabulationFormat(): TabulationFormat<ApachePoiRenderingContext> =
        format("xlsx", ApachePoiRenderingContext::class.java, "poi")

    override fun provideExportOperations(): OperationsBuilder<ApachePoiRenderingContext>.() -> Unit = {

        openTable = OpenTableOperation { renderingContext, context ->
            renderingContext.createWorkbook()
            renderingContext.provideSheet(context.getTableId())
        }

        openRow = OpenRowOperation { renderingContext, context ->
            renderingContext.provideRow(context.getTableId(), context.getRow())
        }

        renderRowCell = RenderRowCellOperation { renderingContext, context ->
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
                        context.getTableId(),
                        context.getRow(),
                        context.getColumn(),
                        context.value.rowSpan,
                        context.value.colSpan
                    )
                }
            }
        }

    }

    override fun getAttributeOperationsFactory(): AttributeOperationsFactory<ApachePoiRenderingContext> =
        StandardAttributeOperationsFactory(
            object : StandardAttributeRenderOperationsProvider<ApachePoiRenderingContext> {
                override fun createTemplateFileRenderer(): TableAttributeRenderOperation<ApachePoiRenderingContext, TemplateFileAttribute> =
                    TemplateFileAttributeRenderOperation()

                override fun createColumnWidthRenderer(): ColumnAttributeRenderOperation<ApachePoiRenderingContext, ColumnWidthAttribute> =
                    ColumnWidthAttributeRenderOperation()

                override fun createRowHeightRenderer(): RowAttributeRenderOperation<ApachePoiRenderingContext, RowHeightAttribute> =
                    RowHeightAttributeRenderOperation()

                override fun createCellTextStyleRenderer(): CellAttributeRenderOperation<ApachePoiRenderingContext, CellTextStylesAttribute> =
                    CellTextStylesAttributeRenderOperation()

                override fun createCellBordersRenderer(): CellAttributeRenderOperation<ApachePoiRenderingContext, CellBordersAttribute> =
                    CellBordersAttributeRenderOperation()

                override fun createCellAlignmentRenderer(): CellAttributeRenderOperation<ApachePoiRenderingContext, CellAlignmentAttribute> =
                    CellAlignmentAttributeRenderOperation()

                override fun createCellBackgroundRenderer(): CellAttributeRenderOperation<ApachePoiRenderingContext, CellBackgroundAttribute> =
                    CellBackgroundAttributeRenderOperation()
            },
            additionalCellAttributeRenderers = setOf(CellDataFormatAttributeRenderOperation()),
            additionalTableAttributeRenderers = setOf(FilterAndSortTableAttributeRenderOperation())
        )

    override fun createOutputBindings(): List<OutputBinding<ApachePoiRenderingContext, *>> = listOf(
        ApachePoiOutputStreamOutputBinding()
    )

    private fun CellContext.hasSpans(): Boolean = value.colSpan > 1 || value.rowSpan > 1

    private fun ApachePoiRenderingContext.provideCell(context: CellContext, block: (SXSSFCell.() -> Unit)) =
        provideCell(context.getTableId(), context.getRow(), context.getColumn()) { it.apply(block) }

    private fun ApachePoiRenderingContext.renderImageDataCell(context: CellContext) {
        createImageCell(
            context.getTableId(), context.getRow(), context.getColumn(),
            context.value.rowSpan, context.value.colSpan, context.rawValue as ByteArray
        )
    }

    private fun ApachePoiRenderingContext.renderImageUrlCell(context: CellContext) {
        createImageCell(
            context.getTableId(), context.getRow(), context.getColumn(),
            context.value.rowSpan, context.value.colSpan, context.rawValue as String
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
            is Date -> renderDateCellValue(context)
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
