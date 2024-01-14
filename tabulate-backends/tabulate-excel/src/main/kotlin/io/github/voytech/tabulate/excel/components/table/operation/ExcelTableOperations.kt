package io.github.voytech.tabulate.excel.components.table.operation

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.rendering.*
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.core.model.attributes.WidthAttribute
import io.github.voytech.tabulate.core.operation.Ok
import io.github.voytech.tabulate.core.operation.RenderingResult
import io.github.voytech.tabulate.core.operation.asResult
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.spi.BuildAttributeOperations
import io.github.voytech.tabulate.core.spi.BuildOperations
import io.github.voytech.tabulate.core.spi.DocumentFormat
import io.github.voytech.tabulate.core.spi.DocumentFormat.Companion.format
import io.github.voytech.tabulate.core.spi.OperationsBundleProvider
import io.github.voytech.tabulate.excel.ApachePoiRenderingContext
import io.github.voytech.tabulate.excel.Utils.toDate
import io.github.voytech.tabulate.excel.components.table.model.ExcelTypeHints
import io.github.voytech.tabulate.excel.components.table.operation.ExcelTableOperations.Companion.widthFromPixels
import io.github.voytech.tabulate.excel.measureImage
import io.github.voytech.tabulate.excel.measureText
import org.apache.poi.xssf.streaming.SXSSFCell
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


private object ApachePoiStartTableOperation : StartTableOperation<ApachePoiRenderingContext> {
    override fun invoke(renderingContext: ApachePoiRenderingContext, context: TableStartRenderable) {
        renderingContext.provideWorkbook()
        renderingContext.provideSheet(context.getSheetName())
    }
}

private object ApachePoiStartColumnOperation : StartColumnOperation<ApachePoiRenderingContext> {
    override fun invoke(renderingContext: ApachePoiRenderingContext, context: ColumnStartRenderable) {
        with(renderingContext) {
            if (!context.hasWidthDefined()) {
                val absoluteColumn = context.getAbsoluteColumn()
                provideSheet(context.getSheetName()).trackColumnForAutoSizing(absoluteColumn)
            }
        }
    }

    private fun ColumnStartRenderable.hasWidthDefined(): Boolean =
        boundingBox.width != null || getModelAttribute<WidthAttribute>() != null
}

private object ApachePoiStartRowOperation : StartRowOperation<ApachePoiRenderingContext> {
    override fun invoke(renderingContext: ApachePoiRenderingContext, context: RowStartRenderable) {
        with(renderingContext) {
            provideSheet(context.getSheetName())
            provideRow(context.getSheetName(), context.getAbsoluteRow())
        }
    }
}

private object ApachePoiMeasureRowCellOperation : RenderRowCellOperation<ApachePoiRenderingContext> {
    override fun invoke(renderingContext: ApachePoiRenderingContext, context: CellRenderable): RenderingResult =
        with(renderingContext) {
            context.getTypeHint()?.let {
                when (it.type.getCellTypeId()) {
                    ExcelTypeHints.IMAGE_DATA.getCellTypeId() -> (context.value as ByteArray).measureImage(context.boundingBox)
                    ExcelTypeHints.IMAGE_URI.getCellTypeId() -> (context.value as String).measureImage(context.boundingBox)
                    else -> context.measureText()
                }
            } ?: context.measureText()
            context.synchronizeMeasuredBoundingBox(renderingContext)
            return Ok.asResult()
        }

    // After measuring cell bounding box - try to adjust global row/column measures,
    // if resolved column/row values are globally largest so far
    private fun CellRenderable.synchronizeMeasuredBoundingBox(renderingContext: ApachePoiRenderingContext): Unit =
        with(renderingContext){
        boundingBox.height?.let { computedHeight ->
            trySetAndSyncAbsoluteRowHeight(getAbsoluteRow(), computedHeight, cellValue.rowSpan)
        }
        boundingBox.width?.let { computedWidth ->
            trySetAndSyncAbsoluteColumnWidth(getAbsoluteColumn(), computedWidth, cellValue.colSpan)
        }
    }
}

private object ApachePoiRenderRowCellOperation : RenderRowCellOperation<ApachePoiRenderingContext> {
    override fun invoke(renderingContext: ApachePoiRenderingContext, context: CellRenderable): RenderingResult {
        with(renderingContext) {
            context.getTypeHint()?.let {
                when (it.type.getCellTypeId()) {
                    ExcelTypeHints.IMAGE_DATA.getCellTypeId() -> renderImageDataCell(context)
                    ExcelTypeHints.IMAGE_URI.getCellTypeId() -> renderImageUrlCell(context)
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
                    context.getSheetName(),
                    context.getAbsoluteRow(),
                    context.getAbsoluteColumn(),
                    context.cellValue.rowSpan,
                    context.cellValue.colSpan
                )
            }
        }
        return Ok.asResult()
    }
}

private object ApachePoiEndRowOperation : EndRowOperation<ApachePoiRenderingContext, Any> {
    override fun invoke(renderingContext: ApachePoiRenderingContext, context: RowEndRenderable<Any>) {
        with(renderingContext) {
            provideSheet(context.getSheetName()).let {
                val absoluteRowIndex = context.getAbsoluteRow()
                val row = provideRow(context.getSheetName(), absoluteRowIndex)
                context.boundingBox.height?.let {
                    context.trySetAndSyncAbsoluteRowHeight(absoluteRowIndex,it)?.let { effectiveHeight ->
                        row.heightInPoints = effectiveHeight.switchUnitOfMeasure(UnitsOfMeasure.PT).value
                    }
                }
            }
        }
    }

}

private object ApachePoiEndColumnOperation : EndColumnOperation<ApachePoiRenderingContext> {
    override fun invoke(renderingContext: ApachePoiRenderingContext, context: ColumnEndRenderable) {
        with(renderingContext) {
            provideSheet(context.getSheetName()).let { sheet ->
                val absoluteColumnIndex = context.getAbsoluteColumn()
                if (!context.hasWidthDefined() && sheet.isColumnTrackedForAutoSizing(absoluteColumnIndex)) {
                    sheet.autoSizeColumn(absoluteColumnIndex)
                } else {
                    context.boundingBox.width?.switchUnitOfMeasure(UnitsOfMeasure.PX)?.let {
                        sheet.setColumnWidth(absoluteColumnIndex, widthFromPixels(it))
                    }
                }
                context.trySetAndSyncAbsoluteColumnWidth(
                    absoluteColumnIndex, Width(sheet.getColumnWidthInPixels(absoluteColumnIndex), UnitsOfMeasure.PX)
                )?.also {
                    sheet.setColumnWidth(
                        absoluteColumnIndex, widthFromPixels(it.switchUnitOfMeasure(UnitsOfMeasure.PX))
                    )
                }
            }
        }
    }

    private fun ColumnEndRenderable.hasWidthDefined(): Boolean =
        boundingBox.width != null || getModelAttribute<WidthAttribute>() != null

}

private object ApachePoiEndRowMeasureOperation : EndRowOperation<ApachePoiRenderingContext, Any> {
    override fun invoke(renderingContext: ApachePoiRenderingContext, context: RowEndRenderable<Any>) {
        with(renderingContext) {
            provideSheet(context.getSheetName()).let {
                val absoluteRowIndex = context.getAbsoluteRow()
                context.syncAbsoluteRowHeight(absoluteRowIndex)
            }
        }
    }

}

private object ApachePoiEndColumnMeasureOperation : EndColumnOperation<ApachePoiRenderingContext> {
    override fun invoke(renderingContext: ApachePoiRenderingContext, context: ColumnEndRenderable) {
        with(renderingContext) {
            provideSheet(context.getSheetName()).let {
                val absoluteColumnIndex = context.getAbsoluteColumn()
                context.syncAbsoluteColumnWidth(absoluteColumnIndex)
            }
        }
    }

}

private fun ApachePoiRenderingContext.provideCell(context: CellRenderable, block: (SXSSFCell.() -> Unit)) {
    provideCell(
        context.getSheetName(),
        context.getAbsoluteRow(),
        context.getAbsoluteColumn()
    ) { it.apply(block) }
}

private fun ApachePoiRenderingContext.renderImageDataCell(context: CellRenderable) {
    createImageCell(
        context.getSheetName(),
        context.getAbsoluteRow(),
        context.getAbsoluteColumn(),
        context.cellValue.rowSpan,
        context.cellValue.colSpan,
        context.value as ByteArray
    )
}

private fun ApachePoiRenderingContext.renderImageUrlCell(context: CellRenderable) {
    createImageCell(
        context.getSheetName(),
        context.getAbsoluteRow(),
        context.getAbsoluteColumn(),
        context.cellValue.rowSpan,
        context.cellValue.colSpan,
        context.value as String
    )
}

private fun ApachePoiRenderingContext.renderFormulaCell(context: CellRenderable) = provideCell(context) {
    cellFormula = context.value as? String
}

private fun ApachePoiRenderingContext.renderErrorCell(context: CellRenderable) = provideCell(context) {
    setCellErrorValue(context.value as Byte)
}

private fun ApachePoiRenderingContext.renderStringCellValue(context: CellRenderable) = provideCell(context) {
    setCellValue(context.value.toString())
}

private fun ApachePoiRenderingContext.renderNumericCellValue(context: CellRenderable) = provideCell(context) {
    setCellValue((context.value as Number).toDouble())
}

private fun ApachePoiRenderingContext.renderBooleanCellValue(context: CellRenderable) = provideCell(context) {
    setCellValue(context.value as Boolean)
}

private fun ApachePoiRenderingContext.renderDateCellValue(context: CellRenderable) = provideCell(context) {
    setCellValue(toDate(context.value))
}

private fun ApachePoiRenderingContext.castAndRenderCellValue(context: CellRenderable) =
    when (context.value) {
        is String -> renderStringCellValue(context)
        is Boolean -> renderBooleanCellValue(context)
        is LocalDate,
        is LocalDateTime,
        is Date,
        -> renderDateCellValue(context)

        is Number -> renderNumericCellValue(context)
        else -> renderStringCellValue(context)
    }

private fun CellRenderable.hasSpans(): Boolean = cellValue.colSpan > 1 || cellValue.rowSpan > 1


/**
 * Apache POI based excel export operations provider implementation.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class ExcelTableOperations : OperationsBundleProvider<ApachePoiRenderingContext, Table<Any>> {

    override fun getDocumentFormat(): DocumentFormat<ApachePoiRenderingContext> =
        format("xlsx", "poi")

    override fun getRenderingContextClass(): Class<ApachePoiRenderingContext> = reify()

    override fun provideAttributeOperations(): BuildAttributeOperations<ApachePoiRenderingContext> = {
        operation(TemplateFileAttributeRenderOperation(), -1)
        operation(CellTextStylesAttributeRenderOperation())
        operation(CellBordersAttributeRenderOperation())
        operation(CellAlignmentAttributeRenderOperation())
        operation(CellBackgroundAttributeRenderOperation())
        operation(CellDataFormatAttributeRenderOperation())
        operation(CellCommentAttributeRenderOperation())
        operation(FilterAndSortTableAttributeRenderOperation())
        operation(PrintingAttributeRenderOperation())
        operation(RowBordersAttributeRenderOperation<Any>())
    }

    override fun provideExportOperations(): BuildOperations<ApachePoiRenderingContext> = {
        operation(ApachePoiStartTableOperation)
        operation(ApachePoiStartColumnOperation)
        operation(ApachePoiStartRowOperation)
        operation(ApachePoiRenderRowCellOperation)
        operation(ApachePoiEndRowOperation)
        operation(ApachePoiEndColumnOperation)
        operation(EndTableOperation { _, _ -> })
    }

    override fun provideMeasureOperations(): BuildOperations<ApachePoiRenderingContext> = {
        operation(StartColumnOperation { _, _ ->}) //TODO this should not be required in order to invoke LayoutAware(Render|Measure)Operation logic.
        operation(StartRowOperation { _, _ ->}) //TODO this should not be required in order to invoke LayoutAware(Render|Measure)Operation logic.
        operation(ApachePoiMeasureRowCellOperation)
        operation(ApachePoiEndRowMeasureOperation)
        operation(ApachePoiEndColumnMeasureOperation)
    }

    override fun getModelClass(): Class<Table<Any>> = reify()

    companion object {
        private const val EXCEL_COLUMN_WIDTH_FACTOR: Short = 256
        private const val UNIT_OFFSET_LENGTH = 7
        private val UNIT_OFFSET_MAP = intArrayOf(0, 36, 73, 109, 146, 182, 219)

        fun widthFromPixels(pxs: Width): Int {
            var widthUnits = EXCEL_COLUMN_WIDTH_FACTOR * (pxs.value.toInt() / UNIT_OFFSET_LENGTH)
            widthUnits += UNIT_OFFSET_MAP[pxs.value.toInt() % UNIT_OFFSET_LENGTH]
            return widthUnits
        }
    }

}
