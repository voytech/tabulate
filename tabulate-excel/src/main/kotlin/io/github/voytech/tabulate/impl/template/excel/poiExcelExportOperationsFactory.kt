package io.github.voytech.tabulate.impl.template.excel

import io.github.voytech.tabulate.core.api.builder.TableBuilder
import io.github.voytech.tabulate.core.model.CellType
import io.github.voytech.tabulate.core.model.Table
import io.github.voytech.tabulate.core.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.core.model.attributes.alias.ColumnAttribute
import io.github.voytech.tabulate.core.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.core.model.attributes.alias.TableAttribute
import io.github.voytech.tabulate.core.template.ResultHandler
import io.github.voytech.tabulate.core.template.context.AttributedCell
import io.github.voytech.tabulate.core.template.context.RowCellContext
import io.github.voytech.tabulate.core.template.context.RowContext
import io.github.voytech.tabulate.core.template.context.narrow
import io.github.voytech.tabulate.core.template.operations.*
import io.github.voytech.tabulate.core.template.operations.impl.ensureAttributesCacheEntry
import io.github.voytech.tabulate.core.template.operations.impl.putCachedValueIfAbsent
import io.github.voytech.tabulate.impl.template.excel.Utils.toDate
import io.github.voytech.tabulate.impl.template.excel.wrapper.ApachePoiExcelFacade
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.streaming.SXSSFCell
import org.reactivestreams.Publisher
import java.io.OutputStream

class PoiExcelExportOperationsFactory<T> : ExportOperationsConfiguringFactory<ApachePoiExcelFacade, T, OutputStream>() {

    private lateinit var stream: OutputStream

    override fun getFormat(): String = "xlsx"

    override fun provideFactoryContext(): ApachePoiExcelFacade = ApachePoiExcelFacade()

    inner class CachingTableOperationsOverlay(
        private val overlay: TableRenderOperationsOverlay<T>,
        private val ops: TableRenderOperationsAdapter<T> = TableRenderOperationsAdapter(overlay),
    ) : TableRenderOperations<T> by ops {

        override fun renderRowCell(context: AttributedCell) {
            context.ensureAttributesCacheEntry()
            overlay.renderRowCell(context.narrow())
        }
    }

    override fun getExportOperationsFactory(creationContext: ApachePoiExcelFacade): ExportOperationsFactory<T, OutputStream> =
        object : ExportOperationsFactory<T, OutputStream> {
            override fun createLifecycleOperations(): LifecycleOperations<T, OutputStream> =
                object : LifecycleOperations<T, OutputStream> {

                    override fun initialize(source: Publisher<T>, resultHandler: ResultHandler<T, OutputStream>) {
                        creationContext.createWorkbook()
                        stream = resultHandler.createResult(source)
                    }

                    override fun finish() {
                        creationContext.workbook().run {
                            write(stream)
                            close()
                        }
                    }
                }

            override fun createTableOperation(): TableOperation<T> = object : TableOperation<T> {
                override fun createTable(builder: TableBuilder<T>): Table<T> {
                    return builder.build().also {
                        creationContext.assertSheet(it.name!!)
                    }
                }
            }

            override fun createTableRenderOperations(): TableRenderOperations<T> =
                CachingTableOperationsOverlay(object : TableRenderOperationsOverlay<T> {

                    override fun beginRow(context: RowContext<T>) {
                        creationContext.assertRow(context.getTableId(), context.rowIndex)
                    }

                    override fun renderRowCell(context: RowCellContext) {
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
                                creationContext.mergeCells(
                                    context.getTableId(),
                                    context.rowIndex,
                                    context.columnIndex,
                                    context.value.rowSpan,
                                    context.value.colSpan
                                )
                            }
                        }
                    }

                    private fun String.createImageCell(context: RowCellContext) {
                        creationContext.createImageCell(
                            context.getTableId(),
                            context.rowIndex,
                            context.columnIndex,
                            context.value.rowSpan,
                            context.value.colSpan,
                            this
                        )
                    }

                    private fun ByteArray.createImageCell(context: RowCellContext) {
                        creationContext.createImageCell(
                            context.getTableId(),
                            context.rowIndex,
                            context.columnIndex,
                            context.value.rowSpan,
                            context.value.colSpan,
                            this
                        )
                    }

                    private fun ensureCell(context: RowCellContext, block: (SXSSFCell.() -> Unit)) {
                        creationContext.assertCell(context.getTableId(), context.rowIndex, context.columnIndex) {
                            it.apply(block)
                        }
                    }
                })
        }

    override fun getAttributeOperationsFactory(creationContext: ApachePoiExcelFacade): AttributeRenderOperationsFactory<T> =
        object : AttributeRenderOperationsFactory<T> {
            override fun createTableAttributeRenderOperations(): Set<AdaptingTableAttributeRenderOperation<ApachePoiExcelFacade, out TableAttribute>> =
                tableAttributesOperations(creationContext)

            override fun createRowAttributeRenderOperations(): Set<AdaptingRowAttributeRenderOperation<ApachePoiExcelFacade, T, out RowAttribute>> =
                rowAttributesOperations(creationContext)

            override fun createColumnAttributeRenderOperations(): Set<AdaptingColumnAttributeRenderOperation<ApachePoiExcelFacade, out ColumnAttribute>> =
                columnAttributesOperations(creationContext)

            override fun createCellAttributeRenderOperations(): Set<AdaptingCellAttributeRenderOperation<ApachePoiExcelFacade, out CellAttribute>> =
                cellAttributesOperations(creationContext)
        }

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