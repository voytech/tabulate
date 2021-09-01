package io.github.voytech.tabulate.excel.template

import io.github.voytech.tabulate.api.builder.TableBuilder
import io.github.voytech.tabulate.excel.template.Utils.toDate
import io.github.voytech.tabulate.excel.template.poi.ApachePoiRenderingContext
import io.github.voytech.tabulate.model.CellType
import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.model.attributes.cell.CellAlignmentAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBackgroundAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.template.TabulationFormat
import io.github.voytech.tabulate.template.TabulationFormat.Companion.format
import io.github.voytech.tabulate.template.context.AttributedCell
import io.github.voytech.tabulate.template.context.AttributedRow
import io.github.voytech.tabulate.template.context.RowCellContext
import io.github.voytech.tabulate.template.operations.*
import io.github.voytech.tabulate.template.operations.impl.ensureAttributesCacheEntry
import io.github.voytech.tabulate.template.operations.impl.putCachedValueIfAbsent
import io.github.voytech.tabulate.template.result.ResultProvider
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.streaming.SXSSFCell
import java.io.OutputStream

class PoiExcelExportOperationsFactory<T> : ExportOperationsConfiguringFactory<T, ApachePoiRenderingContext>() {

    override fun supportsFormat(): TabulationFormat = format("xlsx","poi")

    override fun createRenderingContext(): ApachePoiRenderingContext = ApachePoiRenderingContext()

    override fun createTableExportOperations(): TableExportOperations<T> = object: TableExportOperations<T> {

        override fun initialize() {
            getRenderingContext().createWorkbook()
        }

        override fun createTable(builder: TableBuilder<T>): Table<T> {
            return builder.build().also {
                getRenderingContext().provideSheet(it.name!!)
            }
        }

        override fun beginRow(context: AttributedRow<T>) {
            getRenderingContext().provideRow(context.getTableId(), context.rowIndex)
        }

        override fun renderRowCell(context: AttributedCell) {
            context.ensureAttributesCacheEntry()
            with(context.value) {
                if (type != null) {
                    when (type) {
                        CellType.STRING -> provideCell(context) {
                            setCellValue(value as? String)
                        }
                        CellType.BOOLEAN -> provideCell(context) {
                            setCellValue(value as Boolean)
                        }
                        CellType.DATE -> provideCell(context) {
                            setCellValue(toDate(value))
                        }
                        CellType.NUMERIC -> provideCell(context) {
                            setCellValue((value as Number).toDouble())
                        }
                        CellType.FUNCTION -> provideCell(context) {
                            cellFormula = value.toString()
                        }
                        CellType.ERROR -> provideCell(context) {
                            setCellErrorValue(value as Byte)
                        }
                        CellType.IMAGE_DATA -> (context.value.value as? ByteArray)?.createImageCell(context)
                        CellType.IMAGE_URL -> (context.value.value as? String)?.createImageCell(context)
                    }
                }
            }.also { _ ->
                context.takeIf { it.value.colSpan > 1 || it.value.rowSpan > 1 }?.let {
                    getRenderingContext().mergeCells(
                        context.getTableId(),
                        context.rowIndex,
                        context.columnIndex,
                        context.value.rowSpan,
                        context.value.colSpan
                    )
                }
            }
        }

        private fun String.createImageCell(context: AttributedCell) {
            getRenderingContext().createImageCell(
                context.getTableId(),
                context.rowIndex,
                context.columnIndex,
                context.value.rowSpan,
                context.value.colSpan,
                this
            )
        }

        private fun ByteArray.createImageCell(context: AttributedCell) {
            getRenderingContext().createImageCell(
                context.getTableId(),
                context.rowIndex,
                context.columnIndex,
                context.value.rowSpan,
                context.value.colSpan,
                this
            )
        }

        private fun provideCell(context: AttributedCell, block: (SXSSFCell.() -> Unit)) {
            getRenderingContext().provideCell(context.getTableId(), context.rowIndex, context.columnIndex) {
                it.apply(block)
            }
        }

    }

    override fun getAttributeOperationsFactory(renderingContext: ApachePoiRenderingContext): AttributeRenderOperationsFactory<T> =
        StandardAttributeRenderOperationsFactory(renderingContext, object: StandardAttributeRenderOperationsProvider<ApachePoiRenderingContext,T>{
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


    override fun createResultProviders(): List<ResultProvider<*>> = listOf(
            object : ResultProvider<OutputStream> {
                override fun outputClass() = OutputStream::class.java

                override fun flush(output: OutputStream) {
                    with(getRenderingContext().workbook()) {
                        write(output)
                        close()
                        dispose()
                    }
                }
            }
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
