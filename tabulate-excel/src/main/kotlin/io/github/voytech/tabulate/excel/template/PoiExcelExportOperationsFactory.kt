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
import io.github.voytech.tabulate.template.TabulationFormat
import io.github.voytech.tabulate.template.TabulationFormat.Companion.format
import io.github.voytech.tabulate.template.operations.*
import io.github.voytech.tabulate.template.result.OutputBinding
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.streaming.SXSSFCell
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Apache POI based excel export operations provider implementation.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class PoiExcelExportOperationsFactory : ExportOperationsConfiguringFactory<ApachePoiRenderingContext>() {

    override fun supportsFormat(): TabulationFormat = format("xlsx", "poi")

    override fun getContextClass(): Class<ApachePoiRenderingContext> = ApachePoiRenderingContext::class.java

    override fun createRenderingContext(): ApachePoiRenderingContext = ApachePoiRenderingContext()

    override fun provideExportOperations(): TableExportOperations<ApachePoiRenderingContext> =
        object : TableExportOperations<ApachePoiRenderingContext> {

            override fun createTable(renderingContext: ApachePoiRenderingContext, context: TableContext) {
                renderingContext.createWorkbook()
                renderingContext.provideSheet(context.getTableId())
            }

            override fun beginRow(renderingContext: ApachePoiRenderingContext, context: RowContext) {
                renderingContext.provideRow(context.getTableId(), context.getRow())
            }

            override fun renderRowCell(renderingContext: ApachePoiRenderingContext, context: RowCellContext) {
                (context.getTypeHint()?.let {
                    when (it.type.getCellTypeId()) {
                        ExcelTypeHints.IMAGE_DATA.getCellTypeId(),
                        ExcelTypeHints.IMAGE_URL.getCellTypeId() -> context.renderImageCell(renderingContext)
                        ExcelTypeHints.FORMULA.getCellTypeId() -> context.renderFormulaCell(renderingContext)
                        ExcelTypeHints.ERROR.getCellTypeId() -> context.renderErrorCell(renderingContext)
                        else -> context.renderDefaultTypeCellValue(renderingContext)
                    }
                } ?: context.renderBasicTypeCellValue(renderingContext))
                    .also { _ ->
                        context.takeIf { it.value.colSpan > 1 || it.value.rowSpan > 1 }?.let {
                            renderingContext.mergeCells(
                                context.getTableId(),
                                context.getRow(),
                                context.getColumn(),
                                context.value.rowSpan,
                                context.value.colSpan
                            )
                        }
                    }
            }

            private fun RowCellContext.renderBasicTypeCellValue(renderingContext: ApachePoiRenderingContext) {
                provideCell(renderingContext, this) {
                    when (rawValue) {
                        is String -> setCellValue(rawValue as? String)
                        is Boolean -> setCellValue(rawValue as Boolean)
                        is LocalDate,
                        is LocalDateTime,
                        is Date -> setCellValue(toDate(rawValue))
                        is Int,
                        is Long,
                        is Float,
                        is Double,
                        is Byte,
                        is Short,
                        is BigDecimal,
                        is BigInteger -> setCellValue((rawValue as Number).toDouble())
                        else -> renderDefaultTypeCellValue(renderingContext)
                    }
                }
            }

            private fun RowCellContext.renderDefaultTypeCellValue(renderingContext: ApachePoiRenderingContext) {
                provideCell(renderingContext, this) {
                    setCellValue(rawValue as? String)
                }
            }

            private fun RowCellContext.renderFormulaCell(renderingContext: ApachePoiRenderingContext) {
                provideCell(renderingContext, this) {
                    cellFormula = rawValue as? String
                }
            }

            private fun RowCellContext.renderErrorCell(renderingContext: ApachePoiRenderingContext) {
                provideCell(renderingContext, this) {
                    setCellErrorValue(rawValue as Byte)
                }
            }

            private fun RowCellContext.renderImageCell(renderingContext: ApachePoiRenderingContext) {
                if (getTypeHint()?.type?.getCellTypeId() == ExcelTypeHints.IMAGE_DATA.getCellTypeId()) {
                    renderingContext.createImageCell(
                        getTableId(), getRow(), getColumn(), value.rowSpan, value.colSpan, rawValue as ByteArray
                    )
                } else {
                    renderingContext.createImageCell(
                        getTableId(), getRow(), getColumn(), value.rowSpan, value.colSpan, rawValue as String
                    )
                }
            }

            private fun provideCell(
                renderingContext: ApachePoiRenderingContext,
                context: RowCellContext,
                block: (SXSSFCell.() -> Unit)
            ) {
                renderingContext.provideCell(context.getTableId(), context.getRow(), context.getColumn()) {
                    it.apply(block)
                }
            }

        }

    override fun getAttributeOperationsFactory(): AttributeRenderOperationsFactory<ApachePoiRenderingContext> =
        StandardAttributeRenderOperationsFactory(
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

    companion object {

        private const val CELL_STYLE_CACHE_KEY: String = "cellStyle"

        fun getCachedStyle(poi: ApachePoiRenderingContext, context: RowCellContext): CellStyle {
            return context.cacheOnAttributeSet(
                CELL_STYLE_CACHE_KEY,
                poi.workbook().createCellStyle()
            ) as CellStyle
        }
    }

}
