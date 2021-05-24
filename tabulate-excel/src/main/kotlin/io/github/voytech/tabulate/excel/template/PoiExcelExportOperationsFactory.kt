package io.github.voytech.tabulate.excel.template

import io.github.voytech.tabulate.api.builder.TableBuilder
import io.github.voytech.tabulate.excel.template.Utils.toDate
import io.github.voytech.tabulate.excel.template.wrapper.ApachePoiExcelFacade
import io.github.voytech.tabulate.model.CellType
import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.model.attributes.cell.CellAlignmentAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBackgroundAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.template.ResultHandler
import io.github.voytech.tabulate.template.context.AttributedCell
import io.github.voytech.tabulate.template.context.AttributedRow
import io.github.voytech.tabulate.template.context.RowCellContext
import io.github.voytech.tabulate.template.operations.*
import io.github.voytech.tabulate.template.operations.impl.ensureAttributesCacheEntry
import io.github.voytech.tabulate.template.operations.impl.putCachedValueIfAbsent
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.streaming.SXSSFCell
import org.reactivestreams.Publisher
import java.io.OutputStream

class PoiExcelExportOperationsFactory<T> : ExportOperationsConfiguringFactory<ApachePoiExcelFacade, T, OutputStream>() {

    private lateinit var stream: OutputStream

    override fun getFormat(): String = "xlsx"

    override fun createRenderingContext(): ApachePoiExcelFacade = ApachePoiExcelFacade()

    override fun createTableExportOperation(): TableExportOperations<T, OutputStream> = object: TableExportOperations<T, OutputStream> {

        override fun initialize(source: Publisher<T>, resultHandler: ResultHandler<T, OutputStream>) {
            renderingContext.createWorkbook()
            stream = resultHandler.createResult(source)
        }

        override fun createTable(builder: TableBuilder<T>): Table<T> {
            return builder.build().also {
                renderingContext.assertSheet(it.name!!)
            }
        }

        override fun beginRow(context: AttributedRow<T>) {
            renderingContext.assertRow(context.getTableId(), context.rowIndex)
        }

        override fun renderRowCell(context: AttributedCell) {
            context.ensureAttributesCacheEntry()
            with(context.value) {
                if (type != null) {
                    when (type) {
                        CellType.STRING -> ensureCell(context) {
                            setCellValue(value as? String)
                        }
                        CellType.BOOLEAN -> ensureCell(context) {
                            setCellValue(value as Boolean)
                        }
                        CellType.DATE -> ensureCell(context) {
                            setCellValue(toDate(value))
                        }
                        CellType.NUMERIC -> ensureCell(context) {
                            setCellValue((value as Number).toDouble())
                        }
                        CellType.FUNCTION -> ensureCell(context) {
                            cellFormula = value.toString()
                        }
                        CellType.ERROR -> ensureCell(context) {
                            setCellErrorValue(value as Byte)
                        }
                        CellType.IMAGE_DATA -> (context.value.value as? ByteArray)?.createImageCell(context)
                        CellType.IMAGE_URL -> (context.value.value as? String)?.createImageCell(context)
                    }
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

        private fun String.createImageCell(context: AttributedCell) {
            renderingContext.createImageCell(
                context.getTableId(),
                context.rowIndex,
                context.columnIndex,
                context.value.rowSpan,
                context.value.colSpan,
                this
            )
        }

        private fun ByteArray.createImageCell(context: AttributedCell) {
            renderingContext.createImageCell(
                context.getTableId(),
                context.rowIndex,
                context.columnIndex,
                context.value.rowSpan,
                context.value.colSpan,
                this
            )
        }

        private fun ensureCell(context: AttributedCell, block: (SXSSFCell.() -> Unit)) {
            renderingContext.assertCell(context.getTableId(), context.rowIndex, context.columnIndex) {
                it.apply(block)
            }
        }

        override fun finish() {
            renderingContext.workbook().run {
                write(stream)
                close()
            }
        }

    }

    override fun getAttributeOperationsFactory(renderingContext: ApachePoiExcelFacade): AttributeRenderOperationsFactory<T> =
        StandardAttributeRenderOperationsFactory(renderingContext, object: StandardAttributeRenderOperationsProvider<ApachePoiExcelFacade,T>{
            override fun createTemplateFileRenderer(renderingContext: ApachePoiExcelFacade): TableAttributeRenderOperation<TemplateFileAttribute> =
                TemplateFileAttributeRenderOperation(renderingContext)

            override fun createColumnWidthRenderer(renderingContext: ApachePoiExcelFacade): ColumnAttributeRenderOperation<ColumnWidthAttribute> =
                ColumnWidthAttributeRenderOperation(renderingContext)

            override fun createRowHeightRenderer(renderingContext: ApachePoiExcelFacade): RowAttributeRenderOperation<T, RowHeightAttribute> =
                RowHeightAttributeRenderOperation(renderingContext)

            override fun createCellTextStyleRenderer(renderingContext: ApachePoiExcelFacade): CellAttributeRenderOperation<CellTextStylesAttribute> =
                CellTextStylesAttributeRenderOperation(renderingContext)

            override fun createCellBordersRenderer(renderingContext: ApachePoiExcelFacade): CellAttributeRenderOperation<CellBordersAttribute> =
                CellBordersAttributeRenderOperation(renderingContext)

            override fun createCellAlignmentRenderer(renderingContext: ApachePoiExcelFacade): CellAttributeRenderOperation<CellAlignmentAttribute> =
                CellAlignmentAttributeRenderOperation(renderingContext)

            override fun createCellBackgroundRenderer(renderingContext: ApachePoiExcelFacade): CellAttributeRenderOperation<CellBackgroundAttribute> =
                CellBackgroundAttributeRenderOperation(renderingContext)
        },
            additionalCellAttributeRenderers = setOf(CellDataFormatAttributeRenderOperation(renderingContext)),
            additionalTableAttributeRenderers = setOf(FilterAndSortTableAttributeRenderOperation(renderingContext))
        )

    companion object {

        private const val CELL_STYLE_CACHE_KEY: String = "cellStyle"

        fun getCachedStyle(poi: ApachePoiExcelFacade, context: RowCellContext): CellStyle {
            return context.putCachedValueIfAbsent(
                CELL_STYLE_CACHE_KEY,
                poi.workbook().createCellStyle()
            ) as CellStyle
        }
    }

}
