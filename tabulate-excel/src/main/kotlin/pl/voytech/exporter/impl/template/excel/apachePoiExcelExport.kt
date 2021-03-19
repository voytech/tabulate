package pl.voytech.exporter.impl.template.excel

import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.streaming.SXSSFCell
import pl.voytech.exporter.core.api.builder.TableBuilder
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.model.Table
import pl.voytech.exporter.core.model.attributes.alias.ColumnAttribute
import pl.voytech.exporter.core.model.attributes.alias.RowAttribute
import pl.voytech.exporter.core.model.attributes.alias.TableAttribute
import pl.voytech.exporter.core.model.attributes.alias.CellAttribute
import pl.voytech.exporter.core.template.context.AttributedCell
import pl.voytech.exporter.core.template.context.AttributedColumn
import pl.voytech.exporter.core.template.context.AttributedRow
import pl.voytech.exporter.core.template.context.CellValue
import pl.voytech.exporter.core.template.operations.*
import pl.voytech.exporter.impl.template.excel.wrapper.ApachePoiExcelFacade
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

fun <T> apachePoiExcelExportFactory(templateFile: InputStream? = null): ExportOperationConfiguringFactory<T> {
    return object : ExportOperationConfiguringFactory<T>() {

        private val adaptee = ApachePoiExcelFacade(templateFile)

        override fun getExportOperationsFactory(): ExportOperationsFactory<T> =
            object : ExportOperationsFactory<T> {
                override fun createLifecycleOperations(): LifecycleOperations =
                    object : AdaptingLifecycleOperations<ApachePoiExcelFacade>(adaptee) {
                        override fun initialize() {
                            println("nothing")
                        }

                        override fun finish(stream: OutputStream) {
                            adaptee.workbook().run {
                                write(stream)
                                close()
                            }
                        }
                    }

                override fun createTableRenderOperations(): TableRenderOperations<T> =
                    object : AdaptingTableRenderOperations<T, ApachePoiExcelFacade>(adaptee) {
                        override fun createTable(builder: TableBuilder<T>): Table<T> {
                            return builder.build().also {
                                adaptee.assertTableSheet(it.name)
                            }
                        }

                        override fun renderColumn(context: AttributedColumn) {
                            println("nothing")
                        }

                        override fun renderRow(context: AttributedRow<T>) {
                            adaptee.assertRow(context.getTableId(), context.rowIndex)
                        }

                        override fun renderRowCell(context: AttributedCell) {
                            adaptee.assertCell(context).also {
                                setCellValue(it, context.value)
                            }.also {
                                mergeCells(context)
                            }
                        }

                        private fun toDateValue(value: Any): Date {
                            return when (value) {
                                is LocalDate -> Date.from(
                                    value.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
                                )
                                is LocalDateTime -> Date.from(value.atZone(ZoneId.systemDefault()).toInstant())
                                is Date -> value
                                is String -> Date.from(Instant.parse(value))
                                else -> throw IllegalStateException("Could not parse Date.")
                            }
                        }

                        private fun setCellValue(cell: SXSSFCell, value: CellValue?) {
                            value?.let { v ->
                                v.type?.let {
                                    when (it) {
                                        CellType.STRING -> cell.setCellValue(v.value as String)
                                        CellType.BOOLEAN -> cell.setCellValue(v.value as Boolean)
                                        CellType.DATE -> cell.setCellValue(toDateValue(v.value))
                                        CellType.NUMERIC -> cell.setCellValue((v.value as Number).toDouble())
                                        CellType.NATIVE_FORMULA -> cell.cellFormula = v.value.toString()
                                        CellType.FORMULA -> cell.cellFormula = v.value.toString()
                                        CellType.ERROR -> throw IllegalStateException("CellType.ERROR not supported.")
                                    }
                                } ?: v.run {
                                    when (this.value) {
                                        is String -> cell.setCellValue(this.value as String)
                                        is Boolean -> cell.setCellValue(this.value as Boolean)
                                        is LocalDate -> cell.setCellValue(toDateValue(this.value))
                                        is LocalDateTime -> cell.setCellValue(toDateValue(this.value))
                                        is Date -> cell.setCellValue(this.value as Date)
                                        is Number -> cell.setCellValue((this.value as Number).toDouble())
                                    }
                                }
                            }
                        }

                        private fun mergeCells(
                            context: AttributedCell
                        ) {
                            context.takeIf { it.value.colSpan > 1 || it.value.rowSpan > 1 }?.also { cell ->
                                (context.rowIndex until context.rowIndex + cell.value.rowSpan).forEach { rowIndex ->
                                    (context.columnIndex until context.columnIndex + cell.value.colSpan).forEach { colIndex ->
                                        adaptee.assertCell(context, rowIndex, colIndex)
                                    }
                                }
                                adaptee.assertTableSheet(context.getTableId()).addMergedRegion(
                                    CellRangeAddress(
                                        context.rowIndex,
                                        context.rowIndex + cell.value.rowSpan - 1,
                                        context.columnIndex,
                                        context.columnIndex + cell.value.colSpan - 1
                                    )
                                )
                            }
                        }
                    }
            }

        override fun getAttributeOperationsFactory(): AttributeRenderOperationsFactory<T>? =
            object : AttributeRenderOperationsFactory<T> {
                override fun createTableAttributeRenderOperations(): Set<AdaptingTableAttributeRenderOperation<ApachePoiExcelFacade, out TableAttribute>> =
                    tableAttributesOperations(adaptee)


                override fun createRowAttributeRenderOperations(): Set<AdaptingRowAttributeRenderOperation<ApachePoiExcelFacade, T, out RowAttribute>> =
                    rowAttributesOperations(adaptee)


                override fun createColumnAttributeRenderOperations(): Set<AdaptingColumnAttributeRenderOperation<ApachePoiExcelFacade, T, out ColumnAttribute>> =
                    columnAttributesOperations(adaptee)


                override fun createCellAttributeRenderOperations(): Set<AdaptingCellAttributeRenderOperation<ApachePoiExcelFacade, T, out CellAttribute>> =
                    cellAttributesOperations(adaptee)

            }
    }
}

fun <T> poiExcelExport(templateFile: InputStream? = null): ExportOperations<T> =
    apachePoiExcelExportFactory<T>(templateFile).let {
        ExportOperations(
            lifecycleOperations = it.createLifecycleOperations(),
            tableRenderOperations =  it.createTableRenderOperations()
        )
    }
