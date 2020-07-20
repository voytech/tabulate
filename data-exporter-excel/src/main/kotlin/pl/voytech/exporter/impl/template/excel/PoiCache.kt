package pl.voytech.exporter.impl.template.excel

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import pl.voytech.exporter.core.model.extension.CellExtension
import pl.voytech.exporter.core.template.Coordinates

object PoiCache {
    private val stylesByCoordinates: MutableMap<Coordinates, CellStyle> = mutableMapOf()
    private val fontsByCoordinates: MutableMap<Coordinates, XSSFFont> = mutableMapOf()
    private val stylesByCellExtension: MutableMap<CellExtension, CellStyle> = mutableMapOf()
    private val fontsByCellExtension: MutableMap<CellExtension, XSSFFont> = mutableMapOf()


    internal fun cachedFont(coordinates: Coordinates, extension: CellExtension, fontSupplier: () -> XSSFFont): XSSFFont {
        val resolved = fontsByCellExtension[extension] ?: fontsByCoordinates[coordinates] ?: fontSupplier.invoke()
        fontsByCoordinates[coordinates] = resolved
        fontsByCellExtension[extension] = resolved
        return resolved
    }

    internal fun cachedStyle(coordinates: Coordinates, extension: CellExtension, styleSupplier: () -> CellStyle): CellStyle {
        val resolved = stylesByCellExtension[extension] ?: stylesByCoordinates[coordinates] ?: styleSupplier.invoke()
        stylesByCoordinates[coordinates] = resolved
        stylesByCellExtension[extension] = resolved
        return resolved
    }

    internal fun getCachedFont(coordinates: Coordinates): XSSFFont? {
        return fontsByCoordinates[coordinates]
    }

    internal fun getCachedStyle(coordinates: Coordinates): CellStyle? {
        return stylesByCoordinates[coordinates]
    }
}