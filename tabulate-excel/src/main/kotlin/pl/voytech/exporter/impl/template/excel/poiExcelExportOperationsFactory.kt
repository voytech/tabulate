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
import pl.voytech.exporter.core.template.context.AttributedCell
import pl.voytech.exporter.core.template.context.AttributedRow
import pl.voytech.exporter.core.template.context.CellValue
import pl.voytech.exporter.core.template.operations.*
import pl.voytech.exporter.core.template.operations.impl.putCachedValueIfAbsent
import pl.voytech.exporter.impl.template.excel.Utils.toDate
import pl.voytech.exporter.impl.template.excel.wrapper.ApachePoiExcelFacade
import java.io.OutputStream

class PoiExcelExportOperationsFactory<T> : ExportOperationsConfiguringFactory<T, OutputStream>() {

    private val poi = ApachePoiExcelFacade()
    private lateinit var stream: OutputStream

    override fun getExportOperationsFactory(): ExportOperationsFactory<T, OutputStream> =
        object : ExportOperationsFactory<T, OutputStream> {
            override fun createLifecycleOperations(): LifecycleOperations<T, OutputStream> =
                object : AdaptingLifecycleOperations<T, OutputStream, ApachePoiExcelFacade>(poi) {

                    override fun initialize(source: Publisher<T>, resultHandler: ResultHandler<T, OutputStream>) {
                        poi.createWorkbook()
                        stream = resultHandler.createResult(source)
                    }

                    override fun finish() {
                        adaptee.workbook().run {
                            write(stream)
                            close()
                        }
                    }
                }

            override fun createTableOperation(): TableOperation<T> = object : TableOperation<T> {
                override fun createTable(builder: TableBuilder<T>): Table<T> {
                    return builder.build().also {
                        poi.assertTableSheet(it.name)
                    }
                }
            }

            override fun createTableRenderOperations(): TableRenderOperations<T> =
                object : AdaptingTableRenderOperations<T, ApachePoiExcelFacade>(poi) {

                    override fun renderRow(context: AttributedRow<T>) {
                        adaptee.assertRow(context.getTableId(), context.rowIndex)
                    }

                    override fun renderRowCell(context: AttributedCell) {
                        if (context.value.type in CellType.BASIC_TYPES) {
                            adaptee.assertCell(context.getTableId(), context.rowIndex, context.columnIndex) {
                                withCachedStyle(it, context)
                            }.also {
                                setCellValue(it, context.value)
                            }.also { cell ->
                                context.takeIf { it.value.colSpan > 1 || it.value.rowSpan > 1 }?.let {
                                    adaptee.mergeCells(
                                        context.getTableId(),
                                        context.rowIndex,
                                        context.columnIndex,
                                        context.value.rowSpan,
                                        context.value.colSpan
                                    ) {
                                        withCachedStyle(cell, context)
                                    }
                                }
                            }
                        } else when (context.value.type) {
                            CellType.IMAGE_URL -> adaptee.createImageCell(
                                context.getTableId(),
                                context.rowIndex,
                                context.columnIndex,
                                context.value.rowSpan,
                                context.value.colSpan,
                                context.value.value as String
                            )
                            CellType.IMAGE_DATA -> adaptee.createImageCell(
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

                    private fun withCachedStyle(cell: SXSSFCell, context: AttributedCell) {
                        cell.cellStyle = context.putCachedValueIfAbsent(
                            CELL_STYLE_CACHE_KEY,
                            adaptee.workbook().createCellStyle()
                        ) as CellStyle
                    }
                }
        }

    override fun getAttributeOperationsFactory(): AttributeRenderOperationsFactory<T> =
        object : AttributeRenderOperationsFactory<T> {
            override fun createTableAttributeRenderOperations(): Set<AdaptingTableAttributeRenderOperation<ApachePoiExcelFacade, out TableAttribute>> =
                tableAttributesOperations(poi)

            override fun createRowAttributeRenderOperations(): Set<AdaptingRowAttributeRenderOperation<ApachePoiExcelFacade, T, out RowAttribute>> =
                rowAttributesOperations(poi)

            override fun createColumnAttributeRenderOperations(): Set<AdaptingColumnAttributeRenderOperation<ApachePoiExcelFacade, T, out ColumnAttribute>> =
                columnAttributesOperations(poi)

            override fun createCellAttributeRenderOperations(): Set<AdaptingCellAttributeRenderOperation<ApachePoiExcelFacade, T, out CellAttribute>> =
                cellAttributesOperations(poi)
        }

    companion object {
        private const val CELL_STYLE_CACHE_KEY: String = "cellStyle"
    }
}