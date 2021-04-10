package pl.voytech.exporter.impl.template.excel.wrapper

import org.apache.poi.ss.usermodel.*
import org.apache.poi.util.IOUtils
import org.apache.poi.xssf.streaming.SXSSFCell
import org.apache.poi.xssf.streaming.SXSSFRow
import org.apache.poi.xssf.streaming.SXSSFSheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.model.attributes.cell.Color
import pl.voytech.exporter.core.template.context.AttributedCell
import pl.voytech.exporter.core.template.context.CellValue
import pl.voytech.exporter.core.template.context.Coordinates
import pl.voytech.exporter.core.template.operations.impl.putCachedValueIfAbsent
import java.io.FileInputStream
import java.io.InputStream


class ApachePoiExcelFacade {

    private val CELL_STYLE_CACHE_KEY: String = "cellStyle"

    private lateinit var adaptee: SXSSFWorkbook

    fun createWorkbook(templateFile: InputStream? = null): SXSSFWorkbook {
        adaptee = if (templateFile != null) {
            SXSSFWorkbook(WorkbookFactory.create(templateFile) as XSSFWorkbook?, 100)
        } else {
            SXSSFWorkbook()
        }
        return adaptee
    }

    fun workbook(): SXSSFWorkbook = adaptee

    fun tableSheet(tableName: String): SXSSFSheet =
        workbook().getSheet(tableName)

    fun assertTableSheet(tableName: String?): SXSSFSheet =
        workbook().getSheet(tableName) ?: workbook().createSheet(tableName)

    fun assertRow(tableId: String, rowIndex: Int): SXSSFRow =
        row(tableId, rowIndex) ?: createRow(tableId, rowIndex)

    fun xssfCell(coordinates: Coordinates): XSSFCell? =
        adaptee.xssfWorkbook
            .getSheet(coordinates.tableName)
            .getRow(coordinates.rowIndex)
            .getCell(coordinates.columnIndex)

    fun assertCell(
        context: AttributedCell,
        rowIndex: Int,
        columnIndex: Int
    ): SXSSFCell =
        cell(context.getTableId(), rowIndex, columnIndex) ?: createCell(context, rowIndex, columnIndex)

    fun assertCell(context: AttributedCell): SXSSFCell =
        cell(context.getTableId(), context.rowIndex, context.columnIndex) ?: createCell(context)

    fun cellStyle(context: AttributedCell): CellStyle = assertCell(context).cellStyle

    fun createImageCell(context: AttributedCell, imageUrl: String) {
        val cacheKey = "images/${imageUrl}"
        context.additionalAttributes?.get(cacheKey)?.let {
            createImageCell(context, it as Int)
        } ?: FileInputStream(imageUrl).use {
            createImageCell(context, it)
        }
    }

    fun createImageCell(context: AttributedCell, imageDate: InputStream) {
        createImageCell(context, IOUtils.toByteArray(imageDate))
    }

    fun createImageCell(context: AttributedCell, imageData: ByteArray) {
        workbook().addPicture(imageData, Workbook.PICTURE_TYPE_PNG).also {
            createImageCell(context, it)
        }
    }

    fun getImageAsCellValue(context: Coordinates): CellValue? {
        return assertTableSheet(context.tableName).createDrawingPatriarch().find {
            if (it?.drawing?.shapes?.size == 1 && it.drawing?.shapes?.get(0) is Picture) {
                (it.drawing.shapes[0] as Picture).let { picture ->
                    picture.clientAnchor.col1.toInt() == context.columnIndex &&
                            picture.clientAnchor.row1 == context.rowIndex
                }
            } else false
        }?.let { it.drawing?.shapes?.get(0) as Picture  }
         ?.let{
            CellValue(
                value = it.pictureData.data,
                type = CellType.IMAGE_DATA,
                colSpan = it.clientAnchor.col2.toInt() - it.clientAnchor.col1.toInt(),
                rowSpan = it.clientAnchor.row2 - it.clientAnchor.row1
            )
        }
    }

    private fun createImageCell(context: AttributedCell, imageRef: Int): Picture {
        val drawing: Drawing<*> = assertTableSheet(context.getTableId()).createDrawingPatriarch()
        val anchor: ClientAnchor = workbook().creationHelper.createClientAnchor()
        anchor.setCol1(context.columnIndex)
        anchor.row1 = context.rowIndex
        anchor.setCol2(context.columnIndex + context.value.colSpan)
        anchor.row2 = context.rowIndex + context.value.rowSpan
        return drawing.createPicture(anchor, imageRef)
    }

    private fun cell(tableId: String, rowIndex: Int, columnIndex: Int): SXSSFCell? =
        assertRow(tableId, rowIndex).getCell(columnIndex)

    private fun createCell(
        context: AttributedCell,
        alterRowIndex: Int? = null,
        alterColumnIndex: Int? = null
    ): SXSSFCell =
        assertRow(context.getTableId(), alterRowIndex ?: context.rowIndex).let {
            it.createCell(alterColumnIndex ?: context.columnIndex).also { cell ->
                cell.cellStyle =
                    context.putCachedValueIfAbsent(CELL_STYLE_CACHE_KEY, workbook().createCellStyle()) as CellStyle
            }
        }

    private fun createRow(tableId: String, rowIndex: Int): SXSSFRow =
        tableSheet(tableId).createRow(rowIndex)

    private fun row(tableId: String, rowIndex: Int): SXSSFRow? =
        tableSheet(tableId).getRow(rowIndex)

    companion object {
        fun color(color: Color): XSSFColor =
            XSSFColor(byteArrayOf(color.r.toByte(), color.g.toByte(), color.b.toByte()), null)
    }

}
