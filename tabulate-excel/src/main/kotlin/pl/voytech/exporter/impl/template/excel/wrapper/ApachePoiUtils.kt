package pl.voytech.exporter.impl.template.excel.wrapper

import kotlin.math.floor

object ApachePoiUtils {
    private const val EXCEL_COLUMN_WIDTH_FACTOR: Short = 256
    private const val EXCEL_ROW_HEIGHT_FACTOR: Short = 20
    private const val UNIT_OFFSET_LENGTH = 7
    private val UNIT_OFFSET_MAP = intArrayOf(0, 36, 73, 109, 146, 182, 219)

    fun widthFromPixels(pxs: Int): Int {
        var widthUnits = (EXCEL_COLUMN_WIDTH_FACTOR * (pxs / UNIT_OFFSET_LENGTH))
        widthUnits += UNIT_OFFSET_MAP[pxs % UNIT_OFFSET_LENGTH]
        return widthUnits
    }

    fun pixelsFromWidth(width: Int): Int {
        var pixels = width / EXCEL_COLUMN_WIDTH_FACTOR * UNIT_OFFSET_LENGTH
        val offsetWidthUnits = width % EXCEL_COLUMN_WIDTH_FACTOR
        pixels += floor(offsetWidthUnits.toFloat() / (EXCEL_COLUMN_WIDTH_FACTOR.toFloat() / UNIT_OFFSET_LENGTH).toDouble())
            .toInt()
        return pixels
    }

    fun pixelsFromHeight(height: Int): Int {
        var pixels = height / EXCEL_ROW_HEIGHT_FACTOR
        val offsetWidthUnits = height % EXCEL_ROW_HEIGHT_FACTOR
        pixels += floor(offsetWidthUnits.toFloat() / (EXCEL_ROW_HEIGHT_FACTOR.toFloat() / UNIT_OFFSET_LENGTH).toDouble())
            .toInt()
        return pixels
    }

    fun heightFromPixels(pxs: Int): Short {
        var heightUnits = (EXCEL_ROW_HEIGHT_FACTOR * (pxs / UNIT_OFFSET_LENGTH))
        heightUnits += UNIT_OFFSET_MAP[pxs % UNIT_OFFSET_LENGTH]
        return heightUnits.toShort()
    }
}
