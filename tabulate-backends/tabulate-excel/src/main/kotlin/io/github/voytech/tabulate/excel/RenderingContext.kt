package io.github.voytech.tabulate.excel


import io.github.voytech.tabulate.ImageIndex
import io.github.voytech.tabulate.core.model.color.Color
import io.github.voytech.tabulate.components.table.rendering.CellValue
import io.github.voytech.tabulate.components.table.rendering.Coordinates
import io.github.voytech.tabulate.core.RenderingContextForSpreadsheet
import io.github.voytech.tabulate.core.operation.AttributedEntity
import io.github.voytech.tabulate.core.operation.RenderableEntity
import io.github.voytech.tabulate.core.result.OutputBinding
import io.github.voytech.tabulate.core.result.OutputStreamOutputBinding
import io.github.voytech.tabulate.core.spi.DocumentFormat
import io.github.voytech.tabulate.core.spi.OutputBindingsProvider
import io.github.voytech.tabulate.round3
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.util.IOUtils
import org.apache.poi.util.Units
import org.apache.poi.xssf.streaming.*
import org.apache.poi.xssf.usermodel.*
import java.io.InputStream
import java.io.OutputStream

class PoiExcelOutputBindingsFactory : OutputBindingsProvider<ApachePoiRenderingContext> {
    override fun createOutputBindings(): List<OutputBinding<ApachePoiRenderingContext, *>> = listOf(
        ApachePoiOutputStreamOutputBinding()
    )

    override fun getDocumentFormat(): DocumentFormat<ApachePoiRenderingContext> =
        DocumentFormat.format("xlsx", "poi")

}

class ApachePoiOutputStreamOutputBinding : OutputStreamOutputBinding<ApachePoiRenderingContext>() {
    override fun flush(output: OutputStream) {
        with(renderingContext.workbook()) {
            write(output)
            close()
            output.close()
            dispose()
        }
    }
}

/**
 * Apache POI rendering context.
 * A set of methods wrapping Apache POI calls.
 *
 * @author Wojciech Maka
 * @since 0.1.0
 */
class ApachePoiRenderingContext(private val images: ImageIndex = ImageIndex()) : RenderingContextForSpreadsheet() {

    private var workbook: SXSSFWorkbook? = null

    private val shapesRegistry: MutableMap<AttributedEntity, XSSFShape> = mutableMapOf()

    fun provideWorkbook(templateFile: InputStream? = null, forceRecreate: Boolean = false): SXSSFWorkbook {
        if (workbook == null || forceRecreate) {
            workbook = createWorkbookInternal(templateFile, 100)
        }
        return workbook!!
    }

    fun provideWorkbook(windowSize: Int = 100): SXSSFWorkbook {
        if (workbook == null) {
            workbook = createWorkbookInternal(null, windowSize)
        }
        return workbook!!
    }

    fun workbook(): SXSSFWorkbook = workbook!!

    fun provideSheet(sheetName: String): SXSSFSheet =
        getSheet(sheetName) ?: createSheet(sheetName).also { it.createGrid() }

    private fun createSheet(sheetName: String): SXSSFSheet = workbook().createSheet(sheetName)

    fun createCellStyle(): XSSFCellStyle = workbook().createCellStyle() as XSSFCellStyle

    fun createFont(): XSSFFont = workbook().createFont() as XSSFFont

    fun xssfWorkbook(): XSSFWorkbook = workbook().xssfWorkbook

    fun xssfCell(coordinates: Coordinates): XSSFCell? =
        xssfWorkbook()
            .getSheet(coordinates.tableName)
            .getRow(coordinates.rowIndex)
            .getCell(coordinates.columnIndex)


    private fun Sheet.getInitialColumnWidthInPoints(): Float =
        Units.pixelToPoints(getColumnWidthInPixels(0).toDouble()).toFloat().round3()

    private fun Sheet.getInitialRowHeightInPoints(): Float = defaultRowHeightInPoints.round3()

    private fun Sheet.createGrid() =
        createSpreadsheetGrid(getInitialColumnWidthInPoints(), getInitialRowHeightInPoints())

    fun provideRow(sheetName: String, rowIndex: Int): SXSSFRow =
        row(sheetName, rowIndex) ?: createRow(sheetName, rowIndex)

    fun provideCell(
        sheetName: String,
        rowIndex: Int,
        columnIndex: Int,
        onCreate: ((cell: SXSSFCell) -> Unit)? = null,
    ): SXSSFCell =
        cell(sheetName, rowIndex, columnIndex) ?: createCell(sheetName, rowIndex, columnIndex, onCreate)

    fun provideCellStyle(
        sheetName: String,
        rowIndex: Int,
        columnIndex: Int,
        onCreate: ((cell: SXSSFCell) -> Unit)? = null,
        provideCellStyle: (() -> CellStyle)? = null,
    ): CellStyle {
        provideCell(sheetName, rowIndex, columnIndex, onCreate).let {
            if (provideCellStyle != null) {
                it.cellStyle = provideCellStyle()
            }
            return it.cellStyle
        }
    }

    fun createImageCell(
        sheetName: String,
        rowIndex: Int,
        columnIndex: Int,
        rowSpan: Int,
        colSpan: Int,
        imageUrl: String,
    ) = with(images) {
        createImageCell(sheetName, rowIndex, columnIndex, rowSpan, colSpan, imageUrl.cacheImageAsByteArray())
    }

    fun createImageCell(
        sheetName: String,
        rowIndex: Int,
        columnIndex: Int,
        rowSpan: Int,
        colSpan: Int,
        imageData: InputStream,
    ) {
        createImageCell(sheetName, rowIndex, columnIndex, rowSpan, colSpan, IOUtils.toByteArray(imageData))
    }

    fun createImageCell(
        sheetName: String,
        rowIndex: Int,
        columnIndex: Int,
        rowSpan: Int,
        colSpan: Int,
        imageData: ByteArray,
    ) {
        workbook().addPicture(imageData, Workbook.PICTURE_TYPE_PNG).also {
            createImageCell(sheetName, rowIndex, columnIndex, rowSpan, colSpan, it)
        }
    }

    fun getImageAsCellValue(context: Coordinates): CellValue? {
        return ensureDrawingPatriarch(context.tableName).find {
            if (it?.drawing?.shapes?.size == 1 && it.drawing?.shapes?.get(0) is Picture) {
                (it.drawing.shapes[0] as Picture).let { picture ->
                    picture.clientAnchor.col1.toInt() == context.columnIndex &&
                            picture.clientAnchor.row1 == context.rowIndex
                }
            } else false
        }?.let { it.drawing?.shapes?.get(0) as Picture }
            ?.let {
                CellValue(
                    value = it.pictureData.data,
                    colSpan = it.clientAnchor.col2.toInt() - it.clientAnchor.col1.toInt(),
                    rowSpan = it.clientAnchor.row2 - it.clientAnchor.row1
                )
            }
    }

    fun ensureDrawingPatriarch(sheetName: String): XSSFDrawing = provideSheet(sheetName).let {
        if (it.drawingPatriarch == null) it.createDrawingPatriarch()
        it.drawingPatriarch
    }

    fun getCreationHelper(): CreationHelper = workbook().creationHelper

    fun createClientAnchor(): ClientAnchor = getCreationHelper().createClientAnchor()

    fun <S : XSSFShape, C : AttributedEntity> S.bind(context: C): S = apply { shapesRegistry[context] = this }

    fun <S : XSSFShape> AttributedEntity.shape(clazz: Class<S>): S = shapesRegistry[this] as S

    inline fun <reified S : XSSFShape> AttributedEntity.shape(): S = shape(S::class.java)

    // TODO focus now on getting client anchors right. They need to correctly convert from point/inch/pixels to nominals.
    fun RenderableEntity<*>.createApachePoiSpreadsheetAnchor(): XSSFClientAnchor =
        createSpreadsheetAnchor().let { anchor ->
            (createClientAnchor() as XSSFClientAnchor).apply {
                setCol1(anchor.leftTopColumn)
                row1 = anchor.leftTopRow
                setCol2(anchor.rightBottomColumn)
                row2 = anchor.rightBottomRow
            }
        }

    fun mergeCells(
        sheetName: String,
        rowIndex: Int,
        columnIndex: Int,
        rowSpan: Int,
        colSpan: Int,
        onMerge: ((index: Int) -> Unit)? = null,
    ) {
        (rowIndex until rowIndex + rowSpan).forEach { rIndex ->
            (columnIndex until columnIndex + colSpan).forEach { cIndex ->
                provideCell(sheetName, rIndex, cIndex)
            }
        }
        provideSheet(sheetName).addMergedRegion(
            CellRangeAddress(rowIndex, rowIndex + rowSpan - 1, columnIndex, columnIndex + colSpan - 1)
        ).let {
            if (onMerge != null) {
                onMerge(it)
            }
        }
    }

    private fun getSheet(sheetName: String): SXSSFSheet? = workbook().getSheet(sheetName)

    private fun createWorkbookInternal(
        templateFile: InputStream? = null,
        rowAccessWindowSize: Int = 100,
    ): SXSSFWorkbook {
        return if (templateFile != null) {
            SXSSFWorkbook(WorkbookFactory.create(templateFile) as XSSFWorkbook?, rowAccessWindowSize).also { workbook ->
                workbook.sheetIterator().forEachRemaining { it.createGrid() }
            }
        } else {
            SXSSFWorkbook(rowAccessWindowSize)
        }
    }

    private fun createCell(
        sheetName: String,
        alterRowIndex: Int,
        alterColumnIndex: Int,
        onCreate: ((cell: SXSSFCell) -> Unit)? = null,
    ): SXSSFCell =
        provideRow(sheetName, alterRowIndex).let {
            it.createCell(alterColumnIndex).also { cell ->
                if (onCreate != null) {
                    onCreate(cell)
                }
            }
        }

    private fun createImageCell(
        sheetName: String,
        rowIndex: Int,
        columnIndex: Int,
        rowSpan: Int,
        colSpan: Int,
        imageRef: Int,
    ): Picture {
        val drawing: Drawing<*> = provideSheet(sheetName).createDrawingPatriarch()
        val anchor: ClientAnchor = workbook().creationHelper.createClientAnchor()
        anchor.setCol1(columnIndex)
        anchor.row1 = rowIndex
        anchor.setCol2(columnIndex + colSpan)
        anchor.row2 = rowIndex + rowSpan
        return drawing.createPicture(anchor, imageRef)
    }


    private fun createRow(tableId: String, rowIndex: Int): SXSSFRow =
        provideSheet(tableId).createRow(rowIndex)

    private fun row(tableId: String, rowIndex: Int): SXSSFRow? =
        provideSheet(tableId).getRow(rowIndex)

    private fun cell(tableId: String, rowIndex: Int, columnIndex: Int): SXSSFCell? =
        provideRow(tableId, rowIndex).getCell(columnIndex)

    companion object {
        fun color(color: Color): XSSFColor =
            XSSFColor(byteArrayOf(color.r.toByte(), color.g.toByte(), color.b.toByte()), null)
    }

}
