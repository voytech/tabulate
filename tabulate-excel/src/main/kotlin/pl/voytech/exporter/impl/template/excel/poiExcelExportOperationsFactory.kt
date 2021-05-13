package pl.voytech.exporter.impl.template.excel

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.streaming.SXSSFCell
import org.reactivestreams.Publisher
import pl.voytech.exporter.core.api.builder.TableBuilder
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.attributes.alias.CellAttribute
import pl.voytech.exporter.core.model.attributes.alias.ColumnAttribute
import pl.voytech.exporter.core.model.attributes.alias.RowAttribute
import pl.voytech.exporter.core.model.attributes.alias.TableAttribute
import pl.voytech.exporter.core.template.ResultHandler
import pl.voytech.exporter.core.template.context.*
import pl.voytech.exporter.core.template.operations.*
import pl.voytech.exporter.core.template.operations.impl.ensureAttributesCacheEntry
import pl.voytech.exporter.core.template.operations.impl.putCachedValueIfAbsent
import pl.voytech.exporter.impl.template.excel.Utils.toDate
import pl.voytech.exporter.impl.template.excel.wrapper.ApachePoiExcelFacade
import java.io.OutputStream

class PoiExcelExportOperationsFactory<T> : ExportOperationsConfiguringFactory<ApachePoiExcelFacade,T, OutputStream>() {

    private lateinit var stream: OutputStream

    override fun getFormat(): String = "xlsx"

    override fun provideFactoryContext(): ApachePoiExcelFacade = ApachePoiExcelFacade()

    inner class CachingTableOperationsOverlay(
        private val overlay: TableRenderOperationsOverlay<T>,
        private val ops: TableRenderOperationsAdapter<T> = TableRenderOperationsAdapter(overlay)
    ) : TableRenderOperations<T> by ops {

        override fun renderRowCell(context: AttributedCell) {
            context.ensureAttributesCacheEntry()
            overlay.renderRowCell(context.narrow())
        }
    }

    override fun getExportOperationsFactory(creationContext: ApachePoiExcelFacade): ExportOperationsFactory<T, OutputStream> =
        object : ExportOperationsFactory<T, OutputStream> {
            override fun createLifecycleOperations(): LifecycleOperations<T, OutputStream> =
                object : LifecycleOperations<T, OutputStream>{

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

            override fun createTableRenderOperations(): TableRenderOperations<T> = CachingTableOperationsOverlay(object : TableRenderOperationsOverlay<T> {

                    override fun beginRow(context: RowContext<T>) {
                        creationContext.assertRow(context.getTableId(), context.rowIndex)
                    }

                    override fun renderRowCell(context: RowCellContext) {
                        if (context.value.type in CellType.BASIC_TYPES) {
                            creationContext.assertCell(context.getTableId(), context.rowIndex, context.columnIndex) {
                                setCellValue(it, context.value)
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
                        } else when (context.value.type) {
                            CellType.IMAGE_URL -> creationContext.createImageCell(
                                context.getTableId(),
                                context.rowIndex,
                                context.columnIndex,
                                context.value.rowSpan,
                                context.value.colSpan,
                                context.value.value as String
                            )
                            CellType.IMAGE_DATA -> creationContext.createImageCell(
                                context.getTableId(),
                                context.rowIndex,
                                context.columnIndex,
                                context.value.rowSpan,
                                context.value.colSpan,
                                context.value.value as ByteArray
                            )
                            else -> error("cell type ${context.value.type} does not belong to basic cell types group")
                        }
                    }

                    private fun setCellValue(cell: SXSSFCell, value: CellValue?) {
                        value?.let { v ->
                            v.type?.let {
                                when (it) {
                                    CellType.STRING -> cell.setCellValue(v.value as String)
                                    CellType.BOOLEAN -> cell.setCellValue(v.value as Boolean)
                                    CellType.DATE -> cell.setCellValue(toDate(v.value))
                                    CellType.NUMERIC -> cell.setCellValue((v.value as Number).toDouble())
                                    CellType.FUNCTION -> cell.cellFormula = v.value.toString()
                                    CellType.ERROR -> cell.setCellErrorValue(v.value as Byte)
                                    else -> null
                                }
                            }
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