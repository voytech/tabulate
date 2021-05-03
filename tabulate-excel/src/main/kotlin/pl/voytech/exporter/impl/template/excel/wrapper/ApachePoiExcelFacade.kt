package pl.voytech.exporter.impl.template.excel.wrapper

import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
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
import pl.voytech.exporter.core.template.context.CellValue
import pl.voytech.exporter.core.template.context.Coordinates
import java.io.FileInputStream
import java.io.InputStream


class ApachePoiExcelFacade {

    private var adaptee: SXSSFWorkbook? = null

    fun createWorkbook(templateFile: InputStream? = null, forceRecreate: Boolean = false): SXSSFWorkbook {
        if (adaptee == null || forceRecreate) {
            adaptee = createWorkbookInternal(templateFile)
        }
        return adaptee!!
    }

    fun workbook(): SXSSFWorkbook = adaptee!!

    fun tableSheet(tableName: String): SXSSFSheet =
        workbook().getSheet(tableName)

    fun assertTableSheet(tableName: String?): SXSSFSheet =
        workbook().getSheet(tableName) ?: workbook().createSheet(tableName)

    fun assertRow(tableId: String, rowIndex: Int): SXSSFRow =
        row(tableId, rowIndex) ?: createRow(tableId, rowIndex)

    fun xssfCell(coordinates: Coordinates): XSSFCell? =
        workbook().xssfWorkbook
            .getSheet(coordinates.tableName)
            .getRow(coordinates.rowIndex)
            .getCell(coordinates.columnIndex)

    fun assertCell(
        sheetName: String,
        rowIndex: Int,
        columnIndex: Int,
        onCreate: ((cell: SXSSFCell) -> Unit)? = null,
    ): SXSSFCell =
        cell(sheetName, rowIndex, columnIndex) ?: createCell(sheetName, rowIndex, columnIndex, onCreate)

    fun cellStyle(
        sheetName: String,
        rowIndex: Int,
        columnIndex: Int,
        onCreate: ((cell: SXSSFCell) -> Unit)? = null,
    ): CellStyle = assertCell(sheetName, rowIndex, columnIndex, onCreate).cellStyle

    fun createImageCell(
        sheetName: String,
        rowIndex: Int,
        columnIndex: Int,
        rowSpan: Int,
        colSpan: Int,
        imageUrl: String,
    ) {
        FileInputStream(imageUrl).use {
            createImageCell(sheetName, rowIndex, columnIndex, rowSpan, colSpan, it)
        }
    }

    fun createImageCell(
        sheetName: String,
        rowIndex: Int,
        columnIndex: Int,
        rowSpan: Int,
        colSpan: Int,
        imageDate: InputStream,
    ) {
        createImageCell(sheetName, rowIndex, columnIndex, rowSpan, colSpan, IOUtils.toByteArray(imageDate))
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
        return assertTableSheet(context.tableName).createDrawingPatriarch().find {
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
                    type = CellType.IMAGE_DATA,
                    colSpan = it.clientAnchor.col2.toInt() - it.clientAnchor.col1.toInt(),
                    rowSpan = it.clientAnchor.row2 - it.clientAnchor.row1
                )
            }
    }

    fun mergeCells(
        sheetName: String,
        rowIndex: Int,
        columnIndex: Int,
        rowSpan: Int,
        colSpan: Int,
        onMerge: ((index: Int) -> Unit)?,
    ) {
        (rowIndex until rowIndex + rowSpan).forEach { rIndex ->
            (columnIndex until columnIndex + colSpan).forEach { cIndex ->
                assertCell(sheetName, rIndex, cIndex)
            }
        }
        assertTableSheet(sheetName).addMergedRegion(
            CellRangeAddress(rowIndex, rowIndex + rowSpan - 1, columnIndex, columnIndex + colSpan - 1)
        ).let {
            if (onMerge != null) {
                onMerge(it)
            }
        }
    }

    private fun createWorkbookInternal(
        templateFile: InputStream? = null,
        rowAccessWindowSize: Int = 100,
    ): SXSSFWorkbook {
        return if (templateFile != null) {
            SXSSFWorkbook(WorkbookFactory.create(templateFile) as XSSFWorkbook?, rowAccessWindowSize)
        } else {
            SXSSFWorkbook()
        }
    }

    private fun createCell(
        sheetName: String,
        alterRowIndex: Int,
        alterColumnIndex: Int,
        onCreate: ((cell: SXSSFCell) -> Unit)? = null,
    ): SXSSFCell =
        assertRow(sheetName, alterRowIndex).let {
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
        val drawing: Drawing<*> = assertTableSheet(sheetName).createDrawingPatriarch()
        val anchor: ClientAnchor = workbook().creationHelper.createClientAnchor()
        anchor.setCol1(columnIndex)
        anchor.row1 = rowIndex
        anchor.setCol2(columnIndex + colSpan)
        anchor.row2 = rowIndex + rowSpan
        return drawing.createPicture(anchor, imageRef)
    }


    private fun createRow(tableId: String, rowIndex: Int): SXSSFRow =
        tableSheet(tableId).createRow(rowIndex)

    private fun row(tableId: String, rowIndex: Int): SXSSFRow? =
        tableSheet(tableId).getRow(rowIndex)

    private fun cell(tableId: String, rowIndex: Int, columnIndex: Int): SXSSFCell? =
        assertRow(tableId, rowIndex).getCell(columnIndex)

    companion object {
        fun color(color: Color): XSSFColor =
            XSSFColor(byteArrayOf(color.r.toByte(), color.g.toByte(), color.b.toByte()), null)
    }

}
