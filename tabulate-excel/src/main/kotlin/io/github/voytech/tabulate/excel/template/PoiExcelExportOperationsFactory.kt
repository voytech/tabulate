package io.github.voytech.tabulate.excel.template

import io.github.voytech.tabulate.excel.model.ExcelTypeHints
import io.github.voytech.tabulate.excel.template.Utils.toDate
import io.github.voytech.tabulate.excel.template.poi.ApachePoiOutputStreamResultProvider
import io.github.voytech.tabulate.excel.template.poi.ApachePoiRenderingContext
import io.github.voytech.tabulate.model.attributes.cell.CellAlignmentAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBackgroundAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.template.TabulationFormat
import io.github.voytech.tabulate.template.TabulationFormat.Companion.format
import io.github.voytech.tabulate.template.context.RowCellContext
import io.github.voytech.tabulate.template.context.RowContext
import io.github.voytech.tabulate.template.context.TableContext
import io.github.voytech.tabulate.template.context.getTypeHint
import io.github.voytech.tabulate.template.operations.*
import io.github.voytech.tabulate.template.result.ResultProvider
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.streaming.SXSSFCell
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class PoiExcelExportOperationsFactory<T> : ExportOperationsConfiguringFactory<T, ApachePoiRenderingContext>() {

    override fun supportsFormat(): TabulationFormat = format("xlsx", "poi")

    override fun createRenderingContext(): ApachePoiRenderingContext = ApachePoiRenderingContext()

    override fun createExportOperations(renderingContext: ApachePoiRenderingContext): BasicContextExportOperations<T> = object : BasicContextExportOperations<T> {

        override fun createTable(context: TableContext) {
            renderingContext.createWorkbook()
            renderingContext.provideSheet(context.getTableId())
        }

        override fun beginRow(context: RowContext) {
            renderingContext.provideRow(context.getTableId(), context.getRow())
        }

        override fun renderRowCell(context: RowCellContext) {
            (context.getTypeHint()?.let {
                when (it.type.getCellTypeId()) {
                    ExcelTypeHints.IMAGE_DATA.getCellTypeId(),
                    ExcelTypeHints.IMAGE_URL.getCellTypeId() -> context.renderImageCell()
                    ExcelTypeHints.FORMULA.getCellTypeId() -> context.renderFormulaCell()
                    ExcelTypeHints.ERROR.getCellTypeId() -> context.renderErrorCell()
                    else -> context.renderDefaultTypeCellValue()
                }
            } ?: context.renderBasicTypeCellValue())
                    .also { _ ->
                        context.takeIf { it.getValue().colSpan > 1 || it.getValue().rowSpan > 1 }?.let {
                            renderingContext.mergeCells(
                                    context.getTableId(),
                                    context.getRow(),
                                    context.getColumn(),
                                    context.getValue().rowSpan,
                                    context.getValue().colSpan
                            )
                        }
                    }
        }

        private fun RowCellContext.renderBasicTypeCellValue() {
            provideCell(this) {
                when (getRawValue()) {
                    is String -> setCellValue(getRawValue() as? String)
                    is Boolean -> setCellValue(getRawValue() as Boolean)
                    is LocalDate,
                    is LocalDateTime,
                    is Date -> setCellValue(toDate(getRawValue()))
                    is Int,
                    is Long,
                    is Float,
                    is Double,
                    is Byte,
                    is Short,
                    is BigDecimal,
                    is BigInteger -> setCellValue((getRawValue() as Number).toDouble())
                    else -> renderDefaultTypeCellValue()
                }
            }
        }

        private fun RowCellContext.renderDefaultTypeCellValue() {
            provideCell(this) {
                setCellValue(getRawValue() as? String)
            }
        }

        private fun RowCellContext.renderFormulaCell() {
            provideCell(this) {
                cellFormula = getRawValue() as? String
            }
        }

        private fun RowCellContext.renderErrorCell() {
            provideCell(this) {
                setCellErrorValue(getRawValue() as Byte)
            }
        }

        private fun RowCellContext.renderImageCell() {
            if (getTypeHint()?.type?.getCellTypeId() == ExcelTypeHints.IMAGE_DATA.getCellTypeId()) {
                renderingContext.createImageCell(
                        getTableId(), getRow(), getColumn(), getValue().rowSpan, getValue().colSpan, getRawValue() as ByteArray
                )
            } else {
                renderingContext.createImageCell(
                        getTableId(), getRow(), getColumn(), getValue().rowSpan, getValue().colSpan, getRawValue() as String
                )
            }
        }

        private fun provideCell(context: RowCellContext, block: (SXSSFCell.() -> Unit)) {
            renderingContext.provideCell(context.getTableId(), context.getRow(), context.getColumn()) {
                it.apply(block)
            }
        }

    }

    override fun getAttributeOperationsFactory(renderingContext: ApachePoiRenderingContext): AttributeRenderOperationsFactory<T> =
            StandardAttributeRenderOperationsFactory(renderingContext, object : StandardAttributeRenderOperationsProvider<ApachePoiRenderingContext, T> {
                override fun createTemplateFileRenderer(renderingContext: ApachePoiRenderingContext): TableAttributeRenderOperation<TemplateFileAttribute> =
                        TemplateFileAttributeRenderOperation(renderingContext)

                override fun createColumnWidthRenderer(renderingContext: ApachePoiRenderingContext): ColumnAttributeRenderOperation<ColumnWidthAttribute> =
                        ColumnWidthAttributeRenderOperation(renderingContext)

                override fun createRowHeightRenderer(renderingContext: ApachePoiRenderingContext): RowAttributeRenderOperation<T, RowHeightAttribute> =
                        RowHeightAttributeRenderOperation(renderingContext)

                override fun createCellTextStyleRenderer(renderingContext: ApachePoiRenderingContext): CellAttributeRenderOperation<CellTextStylesAttribute> =
                        CellTextStylesAttributeRenderOperation(renderingContext)

                override fun createCellBordersRenderer(renderingContext: ApachePoiRenderingContext): CellAttributeRenderOperation<CellBordersAttribute> =
                        CellBordersAttributeRenderOperation(renderingContext)

                override fun createCellAlignmentRenderer(renderingContext: ApachePoiRenderingContext): CellAttributeRenderOperation<CellAlignmentAttribute> =
                        CellAlignmentAttributeRenderOperation(renderingContext)

                override fun createCellBackgroundRenderer(renderingContext: ApachePoiRenderingContext): CellAttributeRenderOperation<CellBackgroundAttribute> =
                        CellBackgroundAttributeRenderOperation(renderingContext)
            },
                    additionalCellAttributeRenderers = setOf(CellDataFormatAttributeRenderOperation(renderingContext)),
                    additionalTableAttributeRenderers = setOf(FilterAndSortTableAttributeRenderOperation(renderingContext))
            )

    override fun createResultProviders(renderingContext: ApachePoiRenderingContext): List<ResultProvider<*>> = listOf(
            ApachePoiOutputStreamResultProvider(renderingContext)
    )

    companion object {

        private const val CELL_STYLE_CACHE_KEY: String = "cellStyle"

        fun getCachedStyle(poi: ApachePoiRenderingContext, context: RowCellContext): CellStyle {
            return context.putCachedValueIfAbsent(
                    CELL_STYLE_CACHE_KEY,
                    poi.workbook().createCellStyle()
            ) as CellStyle
        }
    }

}
