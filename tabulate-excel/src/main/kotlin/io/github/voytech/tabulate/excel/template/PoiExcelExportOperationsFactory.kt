package io.github.voytech.tabulate.excel.template

import io.github.voytech.tabulate.excel.template.Utils.toDate
import io.github.voytech.tabulate.excel.template.poi.ApachePoiOutputStreamResultProvider
import io.github.voytech.tabulate.excel.template.poi.ApachePoiRenderingContext
import io.github.voytech.tabulate.model.CellType
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
import io.github.voytech.tabulate.template.operations.*
import io.github.voytech.tabulate.template.result.ResultProvider
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.streaming.SXSSFCell

class PoiExcelExportOperationsFactory<T> : ExportOperationsConfiguringFactory<T, ApachePoiRenderingContext>() {

    override fun supportsFormat(): TabulationFormat = format("xlsx","poi")

    override fun createRenderingContext(): ApachePoiRenderingContext = ApachePoiRenderingContext()

    override fun createExportOperations(renderingContext: ApachePoiRenderingContext): BasicContextExportOperations<T> = object: BasicContextExportOperations<T> {

        override fun createTable(context: TableContext) {
            renderingContext.createWorkbook()
            renderingContext.provideSheet(context.getTableId())
        }

        override fun beginRow(context: RowContext<T>) {
            renderingContext.provideRow(context.getTableId(), context.rowIndex)
        }

        override fun renderRowCell(context: RowCellContext) {
            with(context.value) {
                if (type != null) {
                    if (type in CellType.BASIC_TYPES) {
                        context.renderBasicTypeCellValue()
                    } else {
                        when (type) {
                            CellType.IMAGE_DATA, CellType.IMAGE_URL -> context.renderImageCell()
                            else -> context.renderDefaultTypeCellValue()
                        }
                    }
                } else {
                    context.renderDefaultTypeCellValue()
                }
            }.also { _ ->
                context.takeIf { it.value.colSpan > 1 || it.value.rowSpan > 1 }?.let {
                    renderingContext.mergeCells(
                        context.getTableId(),
                        context.rowIndex,
                        context.columnIndex,
                        context.value.rowSpan,
                        context.value.colSpan
                    )
                }
            }
        }

        private fun RowCellContext.renderBasicTypeCellValue() {
            provideCell(this) {
                when (value.type) {
                    CellType.STRING -> setCellValue(value.value as? String)
                    CellType.BOOLEAN -> setCellValue(value.value as Boolean)
                    CellType.DATE -> setCellValue(toDate(value.value))
                    CellType.NUMERIC -> setCellValue((value.value as Number).toDouble())
                    CellType.FUNCTION -> cellFormula = value.value.toString()
                    CellType.ERROR -> setCellErrorValue(value.value as Byte)
                    else -> renderDefaultTypeCellValue()
                }
            }
        }

        private fun RowCellContext.renderDefaultTypeCellValue() {
            provideCell(this) {
                setCellValue(value.value as? String)
            }
        }

        private fun RowCellContext.renderImageCell() {
            if (value.type == CellType.IMAGE_DATA) {
                renderingContext.createImageCell(
                    getTableId(), rowIndex, columnIndex, value.rowSpan, value.colSpan, value.value as ByteArray
                )
            } else {
                renderingContext.createImageCell(
                    getTableId(), rowIndex, columnIndex, value.rowSpan, value.colSpan, value.value as String
                )
            }
        }

        private fun provideCell(context: RowCellContext, block: (SXSSFCell.() -> Unit)) {
            renderingContext.provideCell(context.getTableId(), context.rowIndex, context.columnIndex) {
                it.apply(block)
            }
        }

    }

    override fun getAttributeOperationsFactory(renderingContext: ApachePoiRenderingContext): AttributeRenderOperationsFactory<T> =
        StandardAttributeRenderOperationsFactory(renderingContext, object: StandardAttributeRenderOperationsProvider<ApachePoiRenderingContext,T> {
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
